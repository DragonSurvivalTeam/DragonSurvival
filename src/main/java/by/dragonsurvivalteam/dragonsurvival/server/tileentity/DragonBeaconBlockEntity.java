package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonBeaconData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DragonBeaconBlockEntity extends BaseBlockBlockEntity {
    private static final String DRAGON_BEACON_DATA = "dragon_beacon_data";
    private static final String TYPE = "type";

    private static final RandomSource RANDOM = RandomSource.create();

    public enum Type {PEACE, MAGIC, FIRE, NONE}

    public Type type = Type.NONE;
    public float tick;

    /** Random offset so that all beacons don't sync-up in their movement */
    public final float bobOffset;

    private @Nullable DragonBeaconData data;

    public DragonBeaconBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.DRAGON_BEACON.get(), position, state);
        setType(this, state.getBlock());
        bobOffset = (float) (RANDOM.nextFloat() * Math.PI * 2);
    }

    public int getExperienceCost() {
        if (data == null) {
            return 0;
        }

        return data.paymentData().experienceCost();
    }

    public boolean applyEffects(final Player player, boolean paidExperience) {
        if (data == null) {
            return false;
        }

        if (data.effects().isEmpty()) {
            return false;
        }

        data.effects().forEach(effect -> {
            int duration = effect.duration();
            int amplifier = effect.amplifier();

            if (paidExperience) {
                duration *= data.paymentData().durationMultiplier();
                amplifier += data.paymentData().amplifierModification();
            }

            player.addEffect(new MobEffectInstance(effect.effect(), duration, amplifier));
        });

        return true;
    }

    public boolean isRelevant(final Player player) {
        if (this.data == null) {
            return false;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return false;
        }

        if (this.data.applicableSpecies().isEmpty()) {
            return true;
        }

        return this.data.applicableSpecies().contains(data.speciesKey());
    }

    public void setData(@Nullable final DragonBeaconData data) {
        this.data = data;
    }

    public static void serverTick(final Level level, final BlockPos position, final BlockState state, final DragonBeaconBlockEntity beacon) {
        BlockState below = level.getBlockState(position.below());

        if (below.getBlock() == DSBlocks.DRAGON_MEMORY_BLOCK.get() && beacon.type != Type.NONE) {
            if (!state.getValue(BlockStateProperties.LIT)) {
                level.setBlockAndUpdate(position, state.cycle(BlockStateProperties.LIT));
                level.playSound(null, position, DSSounds.ACTIVATE_BEACON.get(), SoundSource.BLOCKS, 1, 1);
            }

            if (beacon.data == null || level.getGameTime() % 20 != 0) {
                return;
            }

            level.getEntitiesOfClass(Player.class, new AABB(position).inflate(50).expandTowards(0, level.getMaxBuildHeight(), 0), beacon::isRelevant).forEach(player -> beacon.applyEffects(player, false));
        } else if (state.getValue(BlockStateProperties.LIT)) {
            level.setBlockAndUpdate(position, state.cycle(BlockStateProperties.LIT));
            level.playSound(null, position, DSSounds.DEACTIVATE_BEACON.get(), SoundSource.BLOCKS, 1, 1);
        }
    }

    private static void setType(final DragonBeaconBlockEntity beacon, final Block block) {
        if (beacon.type != Type.NONE) {
            return;
        }

        if (block == DSBlocks.SEA_DRAGON_BEACON.get()) {
            beacon.type = Type.PEACE;
        } else if (block == DSBlocks.FOREST_DRAGON_BEACON.get()) {
            beacon.type = Type.MAGIC;
        } else if (block == DSBlocks.CAVE_DRAGON_BEACON.get()) {
            beacon.type = Type.FIRE;
        }
    }

    @Override
    public void loadAdditional(final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        type = Type.valueOf(tag.getString(TYPE));

        if (tag.contains(DRAGON_BEACON_DATA)) {
            DragonBeaconData.CODEC.decode(provider.createSerializationContext(NbtOps.INSTANCE), tag.getCompound(DRAGON_BEACON_DATA)).ifSuccess(data -> {
                this.data = data.getFirst();
            });
        }
    }

    @Override
    public void saveAdditional(final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        tag.putString(TYPE, type.name());

        if (data != null) {
            DragonBeaconData.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), data).result().ifPresent(compound -> tag.put(DRAGON_BEACON_DATA, compound));
        }
    }
}