package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncClimbFlag;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncClimbableInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Climbable extends DurationInstanceBase<ClimbableData, Climbable.Instance> {
    public static final Codec<Climbable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            LevelBasedBlockPredicate.CODEC.fieldOf("blocks").forGetter(Climbable::blocks),
            LevelBasedBoolean.CODEC.optionalFieldOf("can_stick_to_walls", LevelBasedBoolean.constant(false)).forGetter(Climbable::canStickToWall),
            LevelBasedBoolean.CODEC.optionalFieldOf("can_climb_ceilings", LevelBasedBoolean.constant(false)).forGetter(Climbable::canClimbCeilings)
    ).apply(instance, Climbable::new));

    private final LevelBasedBlockPredicate blocks;
    private final LevelBasedBoolean canStickToWall;
    private final LevelBasedBoolean canClimbCeilings;

    public Climbable(final DurationInstanceBase<?, ?> base, final LevelBasedBlockPredicate blocks, final LevelBasedBoolean canStickToWall, final LevelBasedBoolean canClimbCeilings) {
        super(base);
        this.blocks = blocks;
        this.canStickToWall = canStickToWall;
        this.canClimbCeilings = canClimbCeilings;
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

    public LevelBasedBoolean canClimbCeilings() {
        return canClimbCeilings;
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

        public boolean canStickToWalls(final WorldGenLevel level, final BlockPos climbPosition) {
            return baseData().blocks.matches(appliedAbilityLevel(), level, climbPosition);
        }

        public boolean canClimbCeilings() {
            return baseData().canClimbCeilings.calculate(appliedAbilityLevel());
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

            if (storageHolder instanceof LivingEntity entity && !entity.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntity(entity, new SyncClimbFlag(entity.getId(), SyncClimbFlag.ClimbingType.NONE));
            }
        }

        public void save(@NotNull final ValueOutput valueOutput, final String key) {
            valueOutput.store(key, CODEC, this);
        }

        public static @Nullable Instance load(@NotNull final ValueInput valueInput, final String key) {
            return valueInput.read(key, CODEC).orElse(null);
        }
    }
}
