package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncPhasingInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PhasingData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Phasing extends DurationInstanceBase<PhasingData, Phasing.Instance> {
    public static final Codec<Phasing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            BlockPredicate.CODEC.fieldOf("valid_blocks").forGetter(Phasing::validBlocks)
    ).apply(instance, Phasing::new));

    private final BlockPredicate validBlocks;
    
    public Phasing(final DurationInstanceBase<?, ?> base, BlockPredicate validBlocks) {
        super(base);
        this.validBlocks = validBlocks;
    }

    public static Phasing create(final ResourceLocation id, final BlockPredicate validBlocks) {
        return new Phasing(DurationInstanceBase.create(id).infinite().removeAutomatically().hidden().build(), validBlocks);
    }

    public static Phasing create(final ResourceLocation id, final LevelBasedValue duration, final BlockPredicate validBlocks) {
        return new Phasing(DurationInstanceBase.create(id).duration(duration).hidden().build(), validBlocks);
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration);
    }

    @Override
    public AttachmentType<PhasingData> type() {
        return DSDataAttachments.PHASING.value();
    }

    public BlockPredicate validBlocks() {
        return validBlocks;
    }

    public static class Instance extends DurationInstance<Phasing> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(
                instance, () -> Phasing.CODEC).apply(instance, Instance::new)
        );

        public Instance(final Phasing baseData, final CommonData commonData, int currentDuration) {
            super(baseData, commonData, currentDuration);
        }

        public boolean testValidBlocks(WorldGenLevel t, BlockPos u, Vec3 v, Vec3 w, float x) {
            // Test for if block position is above the plane angle in addition to predicate
            // Looking 45 degrees up or down should result in 'stairs' of collision when phasing
            // Looking within 10 degrees of 'down' should result in no collision at all
            // Maybe instead of this possibly expensive calculation, get angle from vector between BlockPos and Player pos
            // Then compare that angle to the amount the player is looking down
            double d = v.dot(u.getCenter().subtract(w));
            return baseData().validBlocks().test(t, u) && ( d > 0 || x > 80 );
        }

        @Override
        public Component getDescription() {
            return Component.empty();
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (!storageHolder.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(storageHolder, new SyncPhasingInstance(storageHolder.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (!storageHolder.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(storageHolder, new SyncPhasingInstance(storageHolder.getId(), this, true));
            }
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Phasing.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }
}
