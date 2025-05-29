package by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonTraitHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.WaterAbility;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonTypeData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.Precipitation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class SeaDragonType extends AbstractDragonType {
	public double timeWithoutWater;

	public SeaDragonType() {
		slotForBonus = 3;
	}

	@Override
	public CompoundTag writeNBT(){
		CompoundTag tag = new CompoundTag();
		tag.putDouble("timeWithoutWater", timeWithoutWater);
		return tag;
	}

	@Override
	public void readNBT(CompoundTag base){
		timeWithoutWater = base.getDouble("timeWithoutWater");
	}

	@Override
	public String getTypeName(){
		return "sea";
	}

	@Override
	public void onPlayerUpdate(Player player, DragonStateHandler dragonStateHandler){
		Level world = player.level();
		BlockState feetBlock = player.getFeetBlockState();
		BlockState blockUnder = world.getBlockState(player.blockPosition().below());
		Block block = blockUnder.getBlock();
		Biome biome = world.getBiome(player.blockPosition()).value();

		boolean isInCauldron = DragonTraitHandler.isInCauldron(feetBlock, blockUnder);
		boolean isInSeaBlock = DragonConfigHandler.SEA_DRAGON_HYDRATION_BLOCKS != null && (DragonConfigHandler.SEA_DRAGON_HYDRATION_BLOCKS.contains(block) || DragonConfigHandler.SEA_DRAGON_HYDRATION_BLOCKS.contains(feetBlock.getBlock()) || isInCauldron);

		int maxTicksOutofWater = ServerConfig.seaTicksWithoutWater;
		WaterAbility waterAbility = DragonAbilities.getSelfAbility(player, WaterAbility.class);

		if(waterAbility != null){
			maxTicksOutofWater += Functions.secondsToTicks(waterAbility.getDuration());
		}
		
		double oldWaterTime = timeWithoutWater;
		
		if(!world.isClientSide()) {
			if ((player.hasEffect(DragonEffects.PEACE) || player.isInWater() && player.getAirSupply() < player.getMaxAirSupply())) {
				player.setAirSupply(player.getMaxAirSupply());
			}
		}
		
		if(ServerConfig.penalties && maxTicksOutofWater > 0 && !player.isCreative() && !player.isSpectator()) {
			if (!world.isClientSide()) {
				if (player.hasEffect(DragonEffects.PEACE)) {
					timeWithoutWater = 0;
				} else {
					if (!player.isInWaterRainOrBubble() && !isInSeaBlock) {
						boolean hotBiome = biome.getPrecipitationAt(player.blockPosition()) == Precipitation.NONE && biome.getBaseTemperature() > 1.0;
						double timeIncrement = (world.isNight() ? 0.5F : 1.0) * (hotBiome ? biome.getBaseTemperature() : 1F);
						timeWithoutWater += ServerConfig.seaTicksBasedOnTemperature ? timeIncrement : 1;
					}
					
					if (player.isInWaterRainOrBubble() || isInSeaBlock) {
						timeWithoutWater = Math.max(timeWithoutWater - (int)Math.ceil(maxTicksOutofWater * 0.005F), 0);
					}
					
					timeWithoutWater = Math.min(timeWithoutWater, maxTicksOutofWater * 2);
					
					
					if (!player.level().isClientSide()) {
						float hydrationDamage = ServerConfig.seaDehydrationDamage.floatValue();
						
						if (timeWithoutWater > maxTicksOutofWater && timeWithoutWater < maxTicksOutofWater * 2) {
							if (player.tickCount % 40 == 0) {
								player.hurt(DSDamageTypes.damageSource(player.level(), DSDamageTypes.DEHYDRATION), hydrationDamage);
							}
							
						} else if (timeWithoutWater >= maxTicksOutofWater * 2) {
							if (player.tickCount % 20 == 0) {
								player.hurt(DSDamageTypes.damageSource(player.level(), DSDamageTypes.DEHYDRATION), hydrationDamage);
							}
						}
					}
				}
				
				if(oldWaterTime != timeWithoutWater){
					NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SyncDragonTypeData(player.getId(), dragonStateHandler.getType()));
				}
			}
			
			if(world.isClientSide() && !player.isCreative() && !player.isSpectator()){
				if(!player.hasEffect(DragonEffects.PEACE) && timeWithoutWater >= maxTicksOutofWater){
					world.addParticle(ParticleTypes.WHITE_ASH, player.getX() + world.random.nextDouble() * (world.random.nextBoolean() ? 1 : -1), player.getY() + 0.5F, player.getZ() + world.random.nextDouble() * (world.random.nextBoolean() ? 1 : -1), 0, 0, 0);
				}
			}
		}
	}


	@Override
	public boolean isInManaCondition(final Player player, final DragonStateHandler cap) {
		BlockState blockBelow = player.level().getBlockState(player.blockPosition().below());
		BlockState blockAtFeet = player.getFeetBlockState();

		if (player.isInWaterRainOrBubble() || player.hasEffect(DragonEffects.CHARGED) || player.hasEffect(DragonEffects.PEACE)) {
			return true;
		}

		if (DragonConfigHandler.DRAGON_MANA_BLOCKS != null && DragonConfigHandler.DRAGON_MANA_BLOCKS.containsKey(DragonTypes.SEA.getTypeName())) {
			boolean containsBlockAtFeet = DragonConfigHandler.DRAGON_MANA_BLOCKS.get(DragonTypes.SEA.getTypeName()).contains(blockAtFeet.getBlock());
			boolean containsBlockBelow = DragonConfigHandler.DRAGON_MANA_BLOCKS.get(DragonTypes.SEA.getTypeName()).contains(blockBelow.getBlock());

			if ((containsBlockAtFeet || containsBlockBelow) && DragonTraitHandler.isInCauldron(blockAtFeet, blockBelow)) {
				if (blockBelow.hasProperty(LayeredCauldronBlock.LEVEL)) {
					int level = blockBelow.getValue(LayeredCauldronBlock.LEVEL);

					if (level > 0) {
						return true;
					}
				}

				if (blockAtFeet.hasProperty(LayeredCauldronBlock.LEVEL)) {
					int level = blockAtFeet.getValue(LayeredCauldronBlock.LEVEL);

					return level > 0;
				}
			}
		}

		return false;
	}

	@Override
	public void onPlayerDeath(){
		timeWithoutWater = 0;
	}

	@Override
	public List<Pair<ItemStack, FoodData>> validFoods(Player player, DragonStateHandler handler){
		return null;
	}

	@Override
	public List<TagKey<Block>> mineableBlocks(){
		return List.of(BlockTags.MINEABLE_WITH_SHOVEL);
	}
}