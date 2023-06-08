package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.common.capability.objects.DragonMovementData;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.*;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonModifiers;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.DistExecutor;

import java.util.Objects;
import java.util.function.Supplier;


public class DragonStateHandler implements NBTInterface{

	//TODO Remove / cleanup the following
	private final DragonMovementData movementData = new DragonMovementData(0, 0, 0, false);

	private final ClawInventory clawToolData = new ClawInventory(this);
	private final EmoteCap emoteData = new EmoteCap(this);
	private final MagicCap magicData = new MagicCap(this);
	private final SkinCap skinData = new SkinCap(this);
	private final VillageRelationShips villageRelationShips = new VillageRelationShips(this);


	public final Supplier<SubCap>[] caps = new Supplier[]{this::getSkinData, this::getMagicData, this::getEmoteData, this::getClawToolData, this::getVillageRelationShips};
	public boolean hasFlown;
	public boolean growing = true;

	public boolean treasureResting;
	public int treasureRestTimer;
	public int treasureSleepTimer;

	public int altarCooldown;
	public boolean hasUsedAltar;

	public Vec3 lastPos;
	private boolean isHiding;

	//Last entity this entity recieved a debuff from
	public int lastAfflicted = -1;

	//Amount of times the last chain attack has chained
	public int chainCount = 0;

	private AbstractDragonType dragonType;

	private boolean hasWings;
	private boolean spreadWings;
	private double size;
	private int passengerId;

	/**
	 * Sets the size, health and base damage
	 */
	public void setSize(double size, Player player){
		setSize(size);
		updateModifiers(size, player);
	}

	private void updateModifiers(double size, Player player){
		if(isDragon()){
			AttributeModifier healthMod = DragonModifiers.buildHealthMod(size);
			DragonModifiers.updateHealthModifier(player, healthMod);
			AttributeModifier damageMod = DragonModifiers.buildDamageMod(this, isDragon());
			DragonModifiers.updateDamageModifier(player, damageMod);
			AttributeModifier swimSpeedMod = DragonModifiers.buildSwimSpeedMod(getType());
			DragonModifiers.updateSwimSpeedModifier(player, swimSpeedMod);
			AttributeModifier reachMod = DragonModifiers.buildReachMod(size);
			DragonModifiers.updateReachModifier(player, reachMod);
		}else{
			AttributeModifier oldMod = DragonModifiers.getHealthModifier(player);
			if(oldMod != null){
				AttributeInstance max = Objects.requireNonNull(player.getAttribute(Attributes.MAX_HEALTH));
				max.removeModifier(oldMod);
			}

			oldMod = DragonModifiers.getDamageModifier(player);
			if(oldMod != null){
				AttributeInstance max = Objects.requireNonNull(player.getAttribute(Attributes.ATTACK_DAMAGE));
				max.removeModifier(oldMod);
			}

			oldMod = DragonModifiers.getSwimSpeedModifier(player);
			if(oldMod != null){
				AttributeInstance max = Objects.requireNonNull(player.getAttribute(ForgeMod.SWIM_SPEED.get()));
				max.removeModifier(oldMod);
			}

			oldMod = DragonModifiers.getReachModifier(player);
			if(oldMod != null){
				AttributeInstance max = Objects.requireNonNull(player.getAttribute(ForgeMod.REACH_DISTANCE.get()));
				max.removeModifier(oldMod);
			}
		}
	}

	public boolean isDragon(){
		return dragonType != null;
	}

	public int getPassengerId(){
		return passengerId;
	}

	public void setPassengerId(int passengerId){
		this.passengerId = passengerId;
	}

	public EmoteCap getEmoteData(){
		return emoteData;
	}

	public SkinCap getSkinData(){
		return skinData;
	}

	public int lastSync = 0;//Last timestamp the server synced this player

	@Override
	public CompoundTag writeNBT(){
		CompoundTag tag = new CompoundTag();
		//tag.putString("type", getType().name());
		tag.putString("type", dragonType != null ? dragonType.getTypeName() : "none");

		if(isDragon()){
			tag.put("typeData", dragonType.writeNBT());

			//Rendering
			DragonMovementData movementData = getMovementData();
			tag.putDouble("bodyYaw", movementData.bodyYaw);
			tag.putDouble("headYaw", movementData.headYaw);
			tag.putDouble("headPitch", movementData.headPitch);

			tag.putBoolean("bite", movementData.bite);
			tag.putBoolean("dig", movementData.dig);
			tag.putBoolean("isHiding", isHiding());

			tag.putBoolean("resting", treasureResting);
			tag.putInt("restingTimer", treasureRestTimer);
		}
		//Spin attack
		tag.putInt("spinCooldown", movementData.spinCooldown);
		tag.putInt("spinAttack", movementData.spinAttack);
		tag.putBoolean("spinLearned", movementData.spinLearned);

		tag.putDouble("size", getSize());
		tag.putBoolean("growing", growing);

		tag.putBoolean("hasWings", hasWings());
		tag.putBoolean("isFlying", isWingsSpread());

		for(int i = 0; i < caps.length; i++){
			tag.put("cap_" + i, caps[i].get().writeNBT());
		}

		tag.putInt("altarCooldown", altarCooldown);
		tag.putBoolean("usedAltar", hasUsedAltar);

		if(lastPos != null){
			tag.put("lastPos", Functions.newDoubleList(lastPos.x, lastPos.y, lastPos.z));
		}

		tag.putInt("lastAfflicted", lastAfflicted);

		return tag;
	}

	@Override
	public void readNBT(CompoundTag tag){
		//setType(DragonType.valueOf(tag.getString("type").toUpperCase(Locale.ROOT)));
		dragonType = DragonTypes.newDragonTypeInstance(tag.getString("type"));

		if(dragonType != null){
			if(tag.contains("typeData")){
				dragonType.readNBT(tag.getCompound("typeData"));
			}
		}

		if(isDragon()){
			setMovementData(tag.getDouble("bodyYaw"), tag.getDouble("headYaw"), tag.getDouble("headPitch"), tag.getBoolean("bite"));
			getMovementData().headYawLastTick = getMovementData().headYaw;
			getMovementData().bodyYawLastTick = getMovementData().bodyYaw;
			getMovementData().headPitchLastTick = getMovementData().headPitch;
			setIsHiding(tag.getBoolean("isHiding"));
			getMovementData().dig = tag.getBoolean("dig");

			treasureResting = tag.getBoolean("resting");
			treasureRestTimer = tag.getInt("restingTimer");

		}
		getMovementData().spinCooldown = tag.getInt("spinCooldown");
		getMovementData().spinAttack = tag.getInt("spinAttack");
		getMovementData().spinLearned = tag.getBoolean("spinLearned");

		setHasWings(tag.getBoolean("hasWings"));
		setWingsSpread(tag.getBoolean("isFlying"));

		setSize(tag.getDouble("size"));
		if(getSize() == 0){
			setSize(DragonLevel.NEWBORN.size);
		}
		growing = !tag.contains("growing") || tag.getBoolean("growing");

		for(int i = 0; i < caps.length; i++){
			if(tag.contains("cap_" + i)){
				caps[i].get().readNBT((CompoundTag)tag.get("cap_" + i));
			}
		}
		altarCooldown = tag.getInt("altarCooldown");
		hasUsedAltar = tag.getBoolean("usedAltar");

		if(tag.contains("lastPos")){
			ListTag listnbt = tag.getList("lastPos", 6);
			lastPos = new Vec3(listnbt.getDouble(0), listnbt.getDouble(1), listnbt.getDouble(2));
		}

		lastAfflicted = tag.getInt("lastAfflicted");

		getSkinData().compileSkin();
	}

	public void setHasWings(boolean hasWings){
		if(hasWings != this.hasWings){
			this.hasWings = hasWings;
		}
	}

	public void setIsHiding(boolean hiding){
		isHiding = hiding;
	}

	public void setMovementData(double bodyYaw, double headYaw, double headPitch, boolean bite){
		movementData.headYawLastTick = movementData.headYaw;
		movementData.bodyYawLastTick = movementData.bodyYaw;
		movementData.headPitchLastTick = movementData.headPitch;

		movementData.bodyYaw = bodyYaw;
		movementData.headYaw = headYaw;
		movementData.headPitch = headPitch;
		movementData.bite = bite;
	}

	public double getSize(){
		return size;
	}

	public void setSize(double size){
		if(size != this.size){
			DragonLevel oldLevel = getLevel();
			this.size = size;

			if(oldLevel != getLevel())
				onGrow();
		}
	}

	public void onGrow(){
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> (DistExecutor.SafeRunnable)this::requestSkinUpdate);
	}

	@OnlyIn( Dist.CLIENT )
	public void requestSkinUpdate(){
		if(this == DragonUtils.getHandler(Minecraft.getInstance().player))
			ClientEvents.sendClientData(new RequestClientData(getType(), getLevel()));
	}

	public AbstractDragonType getType(){
		return dragonType;
	}

	public void setType(AbstractDragonType type){
		if(type != null && !Objects.equals(dragonType, type)){
			growing = true;
			getMagicData().initAbilities(type);
		}

		if(type != null){
			if(Objects.equals(dragonType, type)) return;
			dragonType = DragonTypes.newDragonTypeInstance(type.getTypeName());
		}else{
			dragonType = null;
		}

		//TODO Reimplement
//		if(ServerConfig.saveGrowthStage)
//			switch(type){
//				case SEA -> {
//					size = seaSize;
//					hasWings = seaWings;
//				}
//				case CAVE -> {
//					size = caveSize;
//					hasWings = caveWings;
//				}
//				case FOREST -> {
//					size = forestSize;
//					hasWings = forestWings;
//				}
//			}
	}

	public MagicCap getMagicData(){
		return magicData;
	}

	public DragonLevel getLevel(){
		if(size < 20F)
			return DragonLevel.NEWBORN;
		else if(size < 30F)
			return DragonLevel.YOUNG;
		else
			return DragonLevel.ADULT;
	}

	public DragonMovementData getMovementData(){
		return movementData;
	}

	public boolean hasWings(){
		return hasWings;
	}

	public boolean isWingsSpread(){
		return hasWings && spreadWings;
	}

	public void setWingsSpread(boolean flying){
		spreadWings = flying;
	}

	public boolean isHiding(){
		return isHiding;
	}

	public boolean canHarvestWithPaw(Player player, BlockState state){
		int harvestLevel = state.is(BlockTags.NEEDS_DIAMOND_TOOL) ? 3 : state.is(BlockTags.NEEDS_IRON_TOOL) ? 2 : state.is(BlockTags.NEEDS_STONE_TOOL) ? 1 : 0;
		int baseHarvestLevel = 0;

		for(int i = 1; i < 4; i++){
			ItemStack stack = getClawToolData().getClawsInventory().getItem(i);
			if(stack.isCorrectToolForDrops(state))
				return true;
		}

		switch(getLevel()){
			case NEWBORN:
				if(ServerConfig.bonusUnlockedAt != DragonLevel.NEWBORN){
					if(harvestLevel <= ServerConfig.baseHarvestLevel + baseHarvestLevel)
						return true;
					break;
				}
			case YOUNG:
				if(ServerConfig.bonusUnlockedAt == DragonLevel.ADULT && getLevel() != DragonLevel.NEWBORN){
					if(harvestLevel <= ServerConfig.baseHarvestLevel + baseHarvestLevel)
						return true;
					break;
				}
			case ADULT:
				if(harvestLevel <= ServerConfig.bonusHarvestLevel + baseHarvestLevel){
					for(TagKey<Block> blockTagKey : getType().mineableBlocks(player)){
						if(state.is(blockTagKey)){
							return true;
						}
					}
				}
				if(harvestLevel <= ServerConfig.baseHarvestLevel + baseHarvestLevel)
					return true;
		}
		return false;
	}

	public ClawInventory getClawToolData(){
		return clawToolData;
	}

	public VillageRelationShips getVillageRelationShips(){
		return villageRelationShips;
	}
}