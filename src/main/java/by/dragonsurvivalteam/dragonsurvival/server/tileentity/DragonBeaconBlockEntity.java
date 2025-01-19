package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonBeaconData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DragonBeaconBlockEntity extends BlockEntity {
    private static final String DRAGON_BEACON_DATA = "dragon_beacon_data";
    private static final RandomSource RANDOM = RandomSource.create();

    /** Random offset so that all beacons don't sync-up in their movement */
    public final float bobOffset;
    public float tick;

    private @Nullable DragonBeaconData data;

    public DragonBeaconBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.DRAGON_BEACON.get(), position, state);
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

            player.addEffect(new MobEffectInstance(effect.effect(), duration, amplifier, !paidExperience, true));
        });

        return true;
    }

    public void setData(@Nullable final DragonBeaconData data) {
        this.data = data;
    }

    public static void serverTick(final Level level, final BlockPos position, final BlockState state, final DragonBeaconBlockEntity beacon) {
        if (!state.getValue(BlockStateProperties.LIT)) {
            return;
        }

        if (beacon.data == null || level.getGameTime() % 20 != 0) {
            return;
        }

        if (level.getBlockState(position.below()).getBlock() == DSBlocks.DRAGON_MEMORY_BLOCK.get()) {
            level.getEntitiesOfClass(Player.class, new AABB(position).inflate(50).expandTowards(0, level.getMaxBuildHeight(), 0)).forEach(player -> beacon.applyEffects(player, false));
        }
    }

    @Override
    public void loadAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        if (tag.contains(DRAGON_BEACON_DATA)) {
            DragonBeaconData.CODEC.decode(provider.createSerializationContext(NbtOps.INSTANCE), tag.getCompound(DRAGON_BEACON_DATA)).ifSuccess(data -> {
                this.data = data.getFirst();
            });
        }
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        if (data != null) {
            DragonBeaconData.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), data).result().ifPresent(compound -> tag.put(DRAGON_BEACON_DATA, compound));
        }
    }
}