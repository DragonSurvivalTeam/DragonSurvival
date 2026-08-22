package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncClimbableInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Climbable extends DurationInstanceBase<ClimbableData, Climbable.Instance> {
    public static final Codec<Climbable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            LevelBasedBlockPredicate.CODEC.fieldOf("blocks").forGetter(Climbable::blocks),
            LevelBasedBoolean.CODEC.fieldOf("ca_stick_to_wall").forGetter(Climbable::canStickToWall)
    ).apply(instance, Climbable::new));

    private final LevelBasedBlockPredicate blocks;
    private final LevelBasedBoolean canStickToWall;

    public Climbable(final DurationInstanceBase<?, ?> base, final LevelBasedBlockPredicate blocks, final LevelBasedBoolean canStickToWall) {
        super(base);
        this.blocks = blocks;
        this.canStickToWall = canStickToWall;
    }

    public MutableComponent getDescription(final int abilityLevel) {
        // FIXME :: add prefix sentence "Allows climbing on ..." + add stick to wall flag
        return Functions.translateBlockPredicate(blocks.get(abilityLevel));
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration);
    }

    @Override
    public AttachmentType<ClimbableData> type() {
        return DSDataAttachments.CLIMBABLE_DATA.value();
    }

    public LevelBasedBlockPredicate blocks() {
        return blocks;
    }

    public LevelBasedBoolean canStickToWall() {
        return canStickToWall;
    }

    public static class Instance extends DurationInstance<Climbable> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(
                instance, () -> Climbable.CODEC).apply(instance, Instance::new)
        );

        public Instance(final Climbable baseData, final CommonData commonData, int currentDuration) {
            super(baseData, commonData, currentDuration);
        }

        public boolean canClimb(final WorldGenLevel level, final BlockPos position) {
            return baseData().blocks.matches(appliedAbilityLevel(), level, position);
        }

        public boolean canStickToWall(final WorldGenLevel level, final BlockPos climbPosition) {
            // TODO :: Cache the position (but also update it if anything happens on that position)
            return baseData().blocks.matches(appliedAbilityLevel(), level, climbPosition);
        }

        @Override
        public Component getDescription() {
            return baseData().getDescription(appliedAbilityLevel());
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncClimbableInstance(player.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncClimbableInstance(player.getId(), this, true));
            }
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }
}
