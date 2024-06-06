package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonBeacon;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTileEntities;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class DragonBeaconTileEntity extends BaseBlockTileEntity{
	public Type type = Type.NONE;
	public float tick;

	public final float bobOffs;

	public enum Type{
		PEACE,
		MAGIC,
		FIRE,
		NONE
	}

	public DragonBeaconTileEntity(BlockPos pWorldPosition, BlockState pBlockState){
		super(DSTileEntities.DRAGON_BEACON.get(), pWorldPosition, pBlockState);
		setType(this, pBlockState.getBlock());
		bobOffs = new Random().nextFloat() * (float)Math.PI * 2.0F;
	}

	public static void serverTick(Level pLevel, BlockPos pPos, BlockState pState, DragonBeaconTileEntity pBlockEntity){
		BlockState below = pLevel.getBlockState(pPos.below());

		if(below.getBlock() == DSBlocks.DRAGON_MEMORY_BLOCK.get() && pBlockEntity.type != Type.NONE){
			if(!pState.getValue(DragonBeacon.LIT)){
				pLevel.setBlockAndUpdate(pPos, pState.cycle(DragonBeacon.LIT));
				pLevel.playSound(null, pPos, DSSounds.ACTIVATE_BEACON.get(), SoundSource.BLOCKS, 1, 1);
			}
			if(!pLevel.isClientSide()){
				List<Player> dragons = pLevel.getEntitiesOfClass(Player.class, new AABB(pPos).inflate(50).expandTowards(0, pLevel.getMaxBuildHeight(), 0), DragonStateProvider::isDragon);
				switch(pBlockEntity.type){
					case PEACE -> dragons.forEach(playerEntity -> {
						ConfigHandler.getResourceElements(MobEffect.class, ServerConfig.peaceBeaconEffects).forEach(effect -> {
							if(effect != null){
								playerEntity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(effect)).get(), Functions.minutesToTicks(ServerConfig.minutesOfDragonEffect) + 5, 0, true, true));
							}
						});
					});
					case MAGIC -> dragons.forEach(playerEntity -> {
						ConfigHandler.getResourceElements(MobEffect.class, ServerConfig.magicBeaconEffects).forEach(effect -> {
							if(effect != null){
								playerEntity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(effect)).get(), Functions.minutesToTicks(ServerConfig.minutesOfDragonEffect) + 5, 0, true, true));
							}
						});
					});
					case FIRE -> dragons.forEach(playerEntity -> {
						ConfigHandler.getResourceElements(MobEffect.class, ServerConfig.fireBeaconEffects).forEach(effect -> {
							if(effect != null){
								playerEntity.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(effect)).get(), Functions.minutesToTicks(ServerConfig.minutesOfDragonEffect) + 5, 0, true, true));
							}
						});
					});
				}
			}
		}else{
			if(pState.getValue(DragonBeacon.LIT)){
				pLevel.setBlockAndUpdate(pPos, pState.cycle(DragonBeacon.LIT));
				pLevel.playSound(null, pPos, DSSounds.DEACTIVATE_BEACON.get(), SoundSource.BLOCKS, 1, 1);
			}
		}
	}

	private static void setType(final DragonBeaconTileEntity beaconTileEntity, final Block beacon) {
		if(beaconTileEntity.type == Type.NONE){
			if(beacon == DSBlocks.MAGIC_DRAGON_BEACON.get()){
				beaconTileEntity.type = Type.MAGIC;
			}else if(beacon == DSBlocks.PEACE_DRAGON_BEACON.get()){
				beaconTileEntity.type = Type.PEACE;
			}else if(beacon == DSBlocks.FIRE_DRAGON_BEACON.get()){
				beaconTileEntity.type = Type.FIRE;
			}
		}
	}

	@Override
	public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries){
		type = Type.valueOf(pTag.getString("Type"));
	}

	@Override
	public void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries){
		pTag.putString("Type", type.name());
	}
}