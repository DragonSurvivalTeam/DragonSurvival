package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonBeacon;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTileEntities;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
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

import java.util.List;
import java.util.Random;

// TODO :: new data attachment that stores effects valid dragon items
//  said attachment is stored in the item stack data and transferred to the placed block entity
public class DragonBeaconBlockEntity extends BaseBlockBlockEntity {
    public Type type = Type.NONE;
    public float tick;

    public final float bobOffs;

    public enum Type {
        PEACE,
        MAGIC,
        FIRE,
        NONE
    }

    public DragonBeaconBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(DSTileEntities.DRAGON_BEACON.get(), pWorldPosition, pBlockState);
        setType(this, pBlockState.getBlock());
        bobOffs = new Random().nextFloat() * (float) Math.PI * 2.0F;
    }

    public static void serverTick(Level level, BlockPos position, BlockState state, DragonBeaconBlockEntity beacon) {
        BlockState below = level.getBlockState(position.below());

        if (below.getBlock() == DSBlocks.DRAGON_MEMORY_BLOCK.get() && beacon.type != Type.NONE) {
            if (!state.getValue(DragonBeacon.LIT)) {
                level.setBlockAndUpdate(position, state.cycle(DragonBeacon.LIT));
                level.playSound(null, position, DSSounds.ACTIVATE_BEACON.get(), SoundSource.BLOCKS, 1, 1);
            }

            List<Player> dragons = level.getEntitiesOfClass(Player.class, new AABB(position).inflate(50).expandTowards(0, level.getMaxBuildHeight(), 0), DragonStateProvider::isDragon);

            switch (beacon.type) {
                case PEACE ->
                        dragons.forEach(player -> ConfigHandler.getResourceElements(MobEffect.class, ServerConfig.forestDragonBeaconEffects).forEach(effect -> {
                            if (effect != null) {
                                player.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(effect)).get(), Functions.secondsToTicks(ServerConfig.secondsOfBeaconEffect) + 5, 0, true, true));
                            }
                        }));
                case MAGIC ->
                        dragons.forEach(player -> ConfigHandler.getResourceElements(MobEffect.class, ServerConfig.seaDragonBeaconEffects).forEach(effect -> {
                            if (effect != null) {
                                player.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(effect)).get(), Functions.secondsToTicks(ServerConfig.secondsOfBeaconEffect) + 5, 0, true, true));
                            }
                        }));
                case FIRE ->
                        dragons.forEach(player -> ConfigHandler.getResourceElements(MobEffect.class, ServerConfig.caveDragonBeaconEffects).forEach(effect -> {
                            if (effect != null) {
                                player.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(BuiltInRegistries.MOB_EFFECT.getId(effect)).get(), Functions.secondsToTicks(ServerConfig.secondsOfBeaconEffect) + 5, 0, true, true));
                            }
                        }));
            }
        } else {
            if (state.getValue(DragonBeacon.LIT)) {
                level.setBlockAndUpdate(position, state.cycle(DragonBeacon.LIT));
                level.playSound(null, position, DSSounds.DEACTIVATE_BEACON.get(), SoundSource.BLOCKS, 1, 1);
            }
        }
    }

    private static void setType(final DragonBeaconBlockEntity beaconTileEntity, final Block beacon) {
        if (beaconTileEntity.type == Type.NONE) {
            if (beacon == DSBlocks.SEA_DRAGON_BEACON.get()) {
                beaconTileEntity.type = Type.PEACE;
            } else if (beacon == DSBlocks.FOREST_DRAGON_BEACON.get()) {
                beaconTileEntity.type = Type.MAGIC;
            } else if (beacon == DSBlocks.CAVE_DRAGON_BEACON.get()) {
                beaconTileEntity.type = Type.FIRE;
            }
        }
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        type = Type.valueOf(pTag.getString("Type"));
    }

    @Override
    public void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.putString("Type", type.name());
    }
}