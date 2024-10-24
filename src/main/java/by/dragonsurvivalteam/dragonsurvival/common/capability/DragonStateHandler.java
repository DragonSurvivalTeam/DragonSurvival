package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.commands.DragonCommand;
import by.dragonsurvivalteam.dragonsurvival.common.capability.objects.DragonMovementData;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.*;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.UnknownNullability;

public class DragonStateHandler extends EntityStateHandler {

	public final Supplier<SubCap>[] caps = new Supplier[]{this::getSkinData, this::getMagicData, this::getEmoteData, this::getClawToolData};

    /** Used in {@link by.dragonsurvivalteam.dragonsurvival.mixins.MixinPlayerStart} and {@link by.dragonsurvivalteam.dragonsurvival.mixins.MixinPlayerEnd} */
    public ItemStack storedMainHandWeapon = ItemStack.EMPTY;
	public boolean switchedWeapon;

	public ItemStack storedMainHandTool = ItemStack.EMPTY;
	public boolean switchedTool;
	public int switchedToolSlot = -1;
	/**
	 * Since {@link Player#hasCorrectToolForDrops(BlockState)} has its own swap<br>
	 * Which would close the swap of {@link net.minecraft.server.level.ServerPlayerGameMode#destroyBlock(BlockPos)}
	 */
	public int toolSwapLayer = 0;

	public boolean hasFlown;
	public boolean growing = true;

	public boolean treasureResting;
	public int treasureRestTimer;
	public int treasureSleepTimer;

	public int altarCooldown;
	public boolean hasUsedAltar;
	public boolean isInAltar = false;
	public boolean refreshBody;

    /** Last timestamp the server synchronized the player */
    public int lastSync = 0;


	private final DragonMovementData movementData = new DragonMovementData();
	private final ClawInventory clawToolData = new ClawInventory(this);
	private final EmoteCap emoteData = new EmoteCap(this);
	private final MagicCap magicData = new MagicCap(this);
	private final SkinCap skinData = new SkinCap(this);
	private final Map<String, Double> savedDragonSize = new ConcurrentHashMap<>();

	private AbstractDragonType dragonType;
	private AbstractDragonBody dragonBody;

	private int passengerId = -1;
	private boolean isHiding;
	private boolean hasFlight;
	private boolean areWingsSpread;
	private double size;
	private boolean destructionEnabled;

	/** Sets the size, health and base damage */
	public void setSize(double size, Player player) {
		setSize(size);
		DSModifiers.updateSizeModifiers(player);
	}

	private void checkAndRemoveModifier(@Nullable final AttributeInstance attribute, @Nullable final ResourceLocation modifier) {
		if (attribute != null && modifier != null && attribute.hasModifier(modifier)) {
			attribute.removeModifier(modifier);
		}
	}

	public void setFreeLook(boolean isFreeLook) {
		movementData.wasFreeLook = movementData.isFreeLook;
		movementData.isFreeLook = isFreeLook;
	}

	public void setFirstPerson(boolean isFirstPerson) {
		movementData.isFirstPerson = isFirstPerson;
	}

	public void setBite(boolean bite) {
		movementData.bite = bite;
	}

    public void setDesiredMoveVec(Vec2 desiredMoveVec) {
        movementData.desiredMoveVec = desiredMoveVec;
    }

    public void setMovementData(double bodyYaw, double headYaw, double headPitch, Vec3 deltaMovement, double realTimeDeltaTick) {
        movementData.headYawLastFrame = movementData.headYaw;
        movementData.bodyYawLastFrame = movementData.bodyYaw;
        movementData.headPitchLastFrame = movementData.headPitch;
        movementData.deltaMovementLastFrame = movementData.deltaMovement;

		movementData.bodyYaw = bodyYaw;
		movementData.headYaw = headYaw;
		movementData.headPitch = headPitch;
		movementData.deltaMovement = deltaMovement;
		movementData.realtimeDeltaTick = realTimeDeltaTick;
	}

	// Only call this version of setSize if we are doing something purely for rendering. Otherwise, call the setSize that accepts a Player object so that the player's attributes are updated.
	public void setSize(double size) {
		if (size != this.size) {
			DragonLevel oldLevel = getLevel();
			this.size = size;

			if (oldLevel != getLevel()) {
				if (FMLEnvironment.dist == Dist.CLIENT) {
					ClientProxy.sendClientData();
				}
			}

			if (dragonType != null) {
				setSavedDragonSize(dragonType.getTypeName(), size);
			}
		}
	}

	public double getSavedDragonSize(final String type) {
		Double value = savedDragonSize.get(type);
		value = value == null ? 0 : value;

		return value;
	}

	public void setSavedDragonSize(final String type, double size) {
		Double value = savedDragonSize.get(type);

		if (size == 0 || (value != null && value == size)) {
			return;
		}

		savedDragonSize.put(type, size);
	}

	public AbstractDragonType getType(){
		return dragonType;
	}

	public AbstractDragonBody getBody() {
		return dragonBody;
	}

	public String getTypeName() {
		if (dragonType == null) {
			return "human";
		}

		return dragonType.getTypeName();
	}

	public String getTypeNameLowerCase() {
		if (dragonType == null) {
			return "human";
		}

		return dragonType.getTypeNameLowerCase();
	}

	public String getSubtypeName() {
		if (dragonType == null) {
			return "human";
		}

		return dragonType.getSubtypeName();
	}

	public void setType(final AbstractDragonType type, Player player) {
		AbstractDragonType oldType = dragonType;
		setType(type);
		if (oldType != dragonType) {
			DSModifiers.updateTypeModifiers(player);
		}
	}

	// Only call this version of setType if we are doing something purely for rendering. Otherwise, call the setSize that accepts a Player object so that the player's attributes are updated.
	public void setType(final AbstractDragonType type) {
		if (type != null && !Objects.equals(dragonType, type)) {
			growing = true;
			getMagicData().initAbilities(type);
		}

		if (type != null) {
			if (Objects.equals(dragonType, type)) {
				return;
			}

			dragonType = DragonTypes.newDragonTypeInstance(type.getSubtypeName());
		} else {
			dragonType = null;
		}
	}

	public void setBody(final AbstractDragonBody body, Player player) {
		AbstractDragonBody oldBody = dragonBody;
		setBody(body);
		if (oldBody != dragonBody) {
			DSModifiers.updateBodyModifiers(player);
		}
	}

	// Only call this version of setBody if we are doing something purely for rendering. Otherwise, call the setSize that accepts a Player object so that the player's attributes are updated.
	public void setBody(final AbstractDragonBody body) {
		if (body != null) {
			if (dragonBody == null || !body.getBodyName().equals(dragonBody.getBodyName())) {
				dragonBody = DragonBodies.newDragonBodyInstance(body.getBodyName());
				refreshBody = true;
			}
		} else {
			dragonBody = null;
		}
	}

	public static DragonLevel getLevel(double size) {
		if (size < 20F) {
			return DragonLevel.NEWBORN;
		} else if (size < 30F) {
			return DragonLevel.YOUNG;
		} else {
			return DragonLevel.ADULT;
		}
	}

	public DragonLevel getLevel() {
		return getLevel(size);
	}

	/** Determines if the current dragon type can harvest the supplied block (with or without tools) (configured harvest bonuses are taken into account) */
	public boolean canHarvestWithPaw(final BlockState state) {
		if (!isDragon()) {
			return false;
		}

		for (int i = 0; i < 4; i++) {
			ItemStack stack = getClawToolData().getClawsInventory().getItem(i);

			if (stack.isCorrectToolForDrops(state)) {
				return true;
			}
		}

		return canHarvestWithPawNoTools(state);
	}

	/** Determines if the current dragon type can harvest the supplied block without a tool (configured harvest bonuses are taken into account) */
	public boolean canHarvestWithPawNoTools(final BlockState blockState) {
		if (!isDragon()) {
			return false;
		}

		boolean initialCheck = getFakeTool(blockState).isCorrectToolForDrops(blockState);

		if (initialCheck) {
			return true;
		}

		int harvestLevel = blockState.is(BlockTags.NEEDS_DIAMOND_TOOL) ? 3 : blockState.is(BlockTags.NEEDS_IRON_TOOL) ? 2 : blockState.is(BlockTags.NEEDS_STONE_TOOL) ? 1 : 0;

		if (harvestLevel <= ServerConfig.baseHarvestLevel) {
			return true;
		}

		for (TagKey<Block> tagKey : getType().mineableBlocks()) {
			if (blockState.is(tagKey)) {
				return harvestLevel <= getDragonHarvestLevel(getType().slotForBonus);
			}
		}

		return false;
	}

	/**
	 * Returns an effective tool for the supplied block state<br>
	 * The tier of the tool is based on the current harvest level of the dragon
	 * @param blockState The block for which the tool is required for
	 * @return The appropriate harvest tool for the supplied block<br>
	 * The tier depends on the dragon and configured harvest bonuses
	 */
	public ItemStack getFakeTool(final BlockState blockState) {
		if (getType() == null) {
			return ItemStack.EMPTY;
		}

		int harvestLevel = 0;

		for (TagKey<Block> tagKey : getType().mineableBlocks()) {
			if (blockState.is(tagKey)) {
				harvestLevel = getDragonHarvestLevel(blockState);

				break;
			}
		}

		if (harvestLevel < 0) {
			return ItemStack.EMPTY;
		} else {
			return getToolOfType(getDragonHarvestTier(blockState), blockState);
		}
	}

	/**
	 * @return Harvest tool of which the dragon type is effective for (tier is based on the current harvest level of the dragon)
	 */
	public ItemStack getInnateFakeTool() {
		if (getType() == null) {
			return ItemStack.EMPTY;
		}

		int harvestLevel = getDragonHarvestLevel(getType().slotForBonus);

		if (harvestLevel < 0) {
			return ItemStack.EMPTY;
		} else {
			return getToolOfType(DragonUtils.levelToVanillaTier(harvestLevel), getType().slotForBonus);
		}
	}

	/** Calls {@link DragonStateHandler#getToolOfType(Tier, int)} with the result of {@link DragonStateHandler#getRelevantToolSlot(BlockState)} */
	public ItemStack getToolOfType(final Tier tier, final BlockState blockState) {
		return getToolOfType(tier, getRelevantToolSlot(blockState));
	}

	/**
	 * Returns a tiered item for the supplied slot - useful to fake checks regarding block breaking
	 * @param tier The tier which the returned item is supposed to have
	 * @param toolSlot To determine the type of harvest tool (e.g. `1` for Pickaxe)
	 * @return A default instance of the tool (or {@link ItemStack#EMPTY} if nothing matches / some problem occurs)
	 */
	public ItemStack getToolOfType(final Tier tier, int toolSlot) {
		if (!(tier instanceof Tiers tiers)) {
			// TODO :: Do something with ForgeTier to support custom tools (benefit = ?)
			return ItemStack.EMPTY;
		}

		String tierPath = tiers.name().toLowerCase(Locale.ENGLISH) + "_";
		tierPath = tierPath.replace("wood", "wooden");

		Item item = switch (toolSlot) {
			case 1 -> BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(tierPath + "pickaxe"));
			case 2 -> BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace( tierPath + "axe"));
			case 3 -> BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(tierPath + "shovel"));
			default -> ItemStack.EMPTY.getItem();
		};

		if (item != null) {
			return item.getDefaultInstance();
		}

		return ItemStack.EMPTY;
	}

	/** Calls {@link DragonStateHandler#getDragonHarvestLevel(int)} with the result of {@link DragonStateHandler#getRelevantToolSlot(BlockState)} */
	public int getDragonHarvestLevel(final BlockState blockState) {
		return getDragonHarvestLevel(getRelevantToolSlot(blockState));
	}

	/**
	 * Don't call this when the player is not a dragon, if you do you get a -1
	 * @param slot The dragon tool slot of the harvest tool which needs to be checked
	 * @return The harvest level of the current dragon type for the provided slot
	 */
	public int getDragonHarvestLevel(int slot) {
		if (getType() == null) {
			return -1;
		}

		int harvestLevel = ServerConfig.baseHarvestLevel;
		int bonusLevel = 0;

		if (getLevel() == DragonLevel.NEWBORN && ServerConfig.bonusUnlockedAt == DragonLevel.NEWBORN) {
			bonusLevel = ServerConfig.bonusHarvestLevel;
		} else if (getLevel() == DragonLevel.YOUNG && ServerConfig.bonusUnlockedAt != DragonLevel.ADULT) {
			bonusLevel = ServerConfig.bonusHarvestLevel;
		} else if (getLevel() == DragonLevel.ADULT) {
			bonusLevel = ServerConfig.bonusHarvestLevel;
		}

		if (slot == getType().slotForBonus) {
			return harvestLevel + bonusLevel;
		}

		return harvestLevel;
	}

	/** Calls {@link DragonStateHandler#getDragonHarvestTier(int)} with the result of {@link DragonStateHandler#getRelevantToolSlot(BlockState)} */
	public @Nullable Tier getDragonHarvestTier(final BlockState blockState) {
		return getDragonHarvestTier(getRelevantToolSlot(blockState));
	}

	/**
	 *
	 * @param slot The (dragon) tool slot the item would belong to (e.g. `1` for Pickaxe)
	 * @return the tier of the current dragon harvest level<br>
	 * (Which is a combination of {@link ServerConfig#baseHarvestLevel} and {@link ServerConfig#bonusHarvestLevel} (if enabled))<br>
	 * Will return null if the config somehow set a negative harvest level
	 */
	public @Nullable Tier getDragonHarvestTier(int slot) {
		int harvestLevel = getDragonHarvestLevel(slot);

		if (harvestLevel < 0) {
			return null;
		}

		return switch(harvestLevel) {
			case 0 -> Tiers.WOOD;
			case 1 -> Tiers.STONE;
			case 2 -> Tiers.IRON;
			case 3 -> Tiers.DIAMOND;
			default -> Tiers.NETHERITE;
		};
	}

	/**
	 * @param blockState The block to test against
	 * @return The dragon tool slot which is effective for the supplied block (e.g. `1` (Pickaxe) for the block `Stone`)
	 */
	public int getRelevantToolSlot(final BlockState blockState) {
		if (blockState.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
			return 1;
		} else if (blockState.is(BlockTags.MINEABLE_WITH_AXE)) {
			return 2;
		} else if (blockState.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
			return 3;
		}

		return 0;
	}

	public void setPassengerId(int passengerId) {
		this.passengerId = passengerId;
	}

	public void setWingsSpread(boolean areWingsSpread) {
		this.areWingsSpread = areWingsSpread;
	}

	public void setDestructionEnabled(boolean destructionEnabled) {
		this.destructionEnabled = destructionEnabled;
	}

	public void setHasFlight(boolean hasFlight) {
		if (hasFlight != this.hasFlight) { // TODO :: Why this check?
			this.hasFlight = hasFlight;
		}
	}

	public void setIsHiding(boolean isHiding) {
		this.isHiding = isHiding;
	}

	public MagicCap getMagicData(){
		return magicData;
	}

	public DragonMovementData getMovementData() {
		return movementData;
	}

	public double getSize(){
		return size;
	}

	public boolean getDestructionEnabled() {
		return destructionEnabled;
	}

	public boolean isDragon() {
		return dragonType != null;
	}

	public int getPassengerId() {
		return passengerId;
	}

	public EmoteCap getEmoteData() {
		return emoteData;
	}

	public SkinCap getSkinData() {
		return skinData;
	}

	public boolean hasFlight() {
		return hasFlight;
	}

	public boolean isWingsSpread() {
		return hasFlight && areWingsSpread;
	}

	public boolean isHiding(){
		return isHiding;
	}

	public ClawInventory getClawToolData()  {
		return clawToolData;
	}

	public CompoundTag serializeNBT(HolderLookup.Provider provider, boolean isSavingForSoul) {
		CompoundTag tag = new CompoundTag();
		tag.putString("type", dragonType != null ? dragonType.getTypeName() : "none");
		tag.putString("subtype", dragonType != null ? dragonType.getSubtypeName(): "none");
		tag.putString("dragonBody", dragonBody != null ? dragonBody.getBodyName() : "none");

		if (isDragon()) {
			tag.put("typeData", dragonType.writeNBT());
			if (dragonBody != null) {
				tag.put("bodyData", dragonBody.writeNBT());
			}

			//Rendering
			DragonMovementData movementData = getMovementData();
			tag.putBoolean("bite", movementData.bite);
			tag.putBoolean("dig", movementData.dig);

			tag.putBoolean("isHiding", isHiding());

			//Spin attack
			tag.putInt("spinCooldown", movementData.spinCooldown);
			tag.putInt("spinAttack", movementData.spinAttack);

			tag.putDouble("size", getSize());
			tag.putBoolean("destructionEnabled", getDestructionEnabled());
			tag.putBoolean("growing", growing);

			tag.putBoolean("isFlying", isWingsSpread());

			tag.putBoolean("resting", treasureResting);
			tag.putInt("restingTimer", treasureRestTimer);
		}

		if (isDragon() || ServerConfig.saveAllAbilities) {
			tag.putBoolean("spinLearned", getMovementData().spinLearned);
			tag.putBoolean("hasWings", hasFlight());
		}

		// Only store the size of the dragon the player is currently in if we are saving for the soul
		if(isSavingForSoul) {
			switch(getTypeName()) {
				case "sea" -> tag.putDouble("seaSize", getSavedDragonSize(DragonTypes.SEA.getTypeName()));
				case "cave" -> tag.putDouble("caveSize", getSavedDragonSize(DragonTypes.CAVE.getTypeName()));
				case "forest" -> tag.putDouble("forestSize", getSavedDragonSize(DragonTypes.FOREST.getTypeName()));
			}
		} else {
			tag.putDouble("seaSize", getSavedDragonSize(DragonTypes.SEA.getTypeName()));
			tag.putDouble("caveSize", getSavedDragonSize(DragonTypes.CAVE.getTypeName()));
			tag.putDouble("forestSize", getSavedDragonSize(DragonTypes.FOREST.getTypeName()));
		}

		for (int i = 0; i < caps.length; i++) {
			if (isSavingForSoul) {
				if (/* Emote Data */ i == 2 || /* Claw Tool Data */ i == 3) {
					continue;
				}
			}

			tag.put("cap_" + i, caps[i].get().serializeNBT(provider));
		}

		tag.putInt("altarCooldown", altarCooldown);
		tag.putBoolean("usedAltar", hasUsedAltar);

		if (lastPos != null) {
			tag.put("lastPos", Functions.newDoubleList(lastPos.x, lastPos.y, lastPos.z));
		}

		tag.putInt("lastAfflicted", lastAfflicted);

		return tag;
	}

	@Override
	public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
		return serializeNBT(provider, false);
	}

	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag, boolean isLoadingForSoul) {
		if (tag.getAllKeys().contains("subtype"))
			dragonType = DragonTypes.newDragonTypeInstance(tag.getString("subtype"));
		else
			dragonType = DragonTypes.newDragonTypeInstance(tag.getString("type"));

		if (dragonType != null) {
			if (tag.contains("typeData")) {
				dragonType.readNBT(tag.getCompound("typeData"));
			}
		}

		dragonBody = DragonBodies.newDragonBodyInstance(tag.getString("dragonBody"));
		if (dragonBody != null) {
			if (tag.contains("bodyData")) {
				dragonBody.readNBT(tag.getCompound("bodyData"));
			}
		}

		if (isDragon()) {
			setBite(tag.getBoolean("bite"));
			getMovementData().headYawLastFrame = getMovementData().headYaw;
			getMovementData().bodyYawLastFrame = getMovementData().bodyYaw;
			getMovementData().headPitchLastFrame = getMovementData().headPitch;
			setIsHiding(tag.getBoolean("isHiding"));
			getMovementData().dig = tag.getBoolean("dig");

			setWingsSpread(tag.getBoolean("isFlying"));

			getMovementData().spinCooldown = tag.getInt("spinCooldown");
			getMovementData().spinAttack = tag.getInt("spinAttack");

			setSize(tag.getDouble("size"));
			setDestructionEnabled(tag.getBoolean("destructionEnabled"));
			growing = !tag.contains("growing") || tag.getBoolean("growing");

			treasureResting = tag.getBoolean("resting");
			treasureRestTimer = tag.getInt("restingTimer");

			if(getSize() == 0){
				setSize(DragonLevel.NEWBORN.size);
			}
		}

		if (isDragon() || ServerConfig.saveAllAbilities) {
			getMovementData().spinLearned = tag.getBoolean("spinLearned");
			setHasFlight(tag.getBoolean("hasWings"));
		}

		// Only load the size of the dragon the player is currently in if we are loading for the soul
		if(isLoadingForSoul) {
			switch(getTypeName()) {
				case "sea" -> setSavedDragonSize(DragonTypes.SEA.getTypeName(), tag.getDouble("seaSize"));
				case "cave" -> setSavedDragonSize(DragonTypes.CAVE.getTypeName(), tag.getDouble("caveSize"));
				case "forest" -> setSavedDragonSize(DragonTypes.FOREST.getTypeName(), tag.getDouble("forestSize"));
			}
		} else {
			setSavedDragonSize(DragonTypes.SEA.getTypeName(), tag.getDouble("seaSize"));
			setSavedDragonSize(DragonTypes.CAVE.getTypeName(), tag.getDouble("caveSize"));
			setSavedDragonSize(DragonTypes.FOREST.getTypeName(), tag.getDouble("forestSize"));
		}

		for (int i = 0; i < caps.length; i++) {
			if(isLoadingForSoul) {
				if(i == 2) {
					continue;
				}
			}

			if(isLoadingForSoul) {
				if(i == 3) {
					continue;
				}
			}

			if (tag.contains("cap_" + i)) {
				caps[i].get().deserializeNBT(provider, (CompoundTag) tag.get("cap_" + i));
			}
		}

		altarCooldown = tag.getInt("altarCooldown");
		hasUsedAltar = tag.getBoolean("usedAltar");

		if (tag.contains("lastPos")) {
			ListTag listnbt = tag.getList("lastPos", 6);
			lastPos = new Vec3(listnbt.getDouble(0), listnbt.getDouble(1), listnbt.getDouble(2));
		}

		lastAfflicted = tag.getInt("lastAfflicted");
		refreshBody = true;

		getSkinData().compileSkin();
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		deserializeNBT(provider, tag, false);
	}

	public void revertToHumanForm(Player player, boolean isRevertingFromSoul) {
		// Don't set the saved dragon size if we are reverting from a soul, as we already are storing the size of the dragon in the soul
		if (ServerConfig.saveGrowthStage && !isRevertingFromSoul) {
			this.setSavedDragonSize(this.getTypeName(), this.getSize());
		}

		// Drop everything in your claw slots
		DragonCommand.reInsertClawTools(player, this);

		this.setType(null);
		this.setBody(null, player);
		this.setSize(20F, player);
		this.setIsHiding(false);

		if (!ServerConfig.saveAllAbilities) {
			this.getMovementData().spinLearned = false;
			this.setHasFlight(false);
		}

		this.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
		this.hasUsedAltar = true;
	}
}