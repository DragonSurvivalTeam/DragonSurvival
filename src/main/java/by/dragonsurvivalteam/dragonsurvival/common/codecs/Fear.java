package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FearData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
public class Fear extends DurationInstanceBase<FearData, Fear.Instance> {
    public static final float DEFAULT_WALK_SPEED = 1;
    public static final float DEFAULT_SPRINT_SPEED = 1.3f;

    public static final Codec<Fear> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("entity_condition").forGetter(Fear::entityCondition),
            LevelBasedValue.CODEC.fieldOf("distance").forGetter(Fear::distance),
            LevelBasedValue.CODEC.optionalFieldOf("walk_speed", LevelBasedValue.constant(DEFAULT_WALK_SPEED)).forGetter(Fear::walkSpeed),
            LevelBasedValue.CODEC.optionalFieldOf("sprint_speed", LevelBasedValue.constant(DEFAULT_SPRINT_SPEED)).forGetter(Fear::sprintSpeed)
    ).apply(instance, Fear::new));

    private final Optional<LootItemCondition> entityCondition;
    private final LevelBasedValue distance;
    private final LevelBasedValue walkSpeed;
    private final LevelBasedValue sprintSpeed;

    public Fear(final DurationInstanceBase<?, ?> base, final Optional<LootItemCondition> entityCondition, final LevelBasedValue distance, final LevelBasedValue walkSpeed, final LevelBasedValue sprintSpeed) {
        super(base);
        this.entityCondition = entityCondition;
        this.distance = distance;
        this.walkSpeed = walkSpeed;
        this.sprintSpeed = sprintSpeed;
    }

    public Optional<LootItemCondition> entityCondition() {
        return entityCondition;
    }

    public LevelBasedValue distance() {
        return distance;
    }

    public LevelBasedValue walkSpeed() {
        return walkSpeed;
    }

    public LevelBasedValue sprintSpeed() {
        return sprintSpeed;
    }

    public static class Instance extends DurationInstance<Fear> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(instance, () -> Fear.CODEC)
                .and(Codec.INT.fieldOf("distance").forGetter(Instance::distance))
                .and(Codec.FLOAT.fieldOf("walk_speed").forGetter(Instance::walkSpeed))
                .and(Codec.FLOAT.fieldOf("sprint_speed").forGetter(Instance::sprintSpeed))
                .apply(instance, Instance::new));

        private final int distance;
        private final float walkSpeed;
        private final float sprintSpeed;
        
        public Instance(final Fear baseData, final CommonData commonData, final int currentDuration, final int distance, final float walkSpeed, final float sprintSpeed) {
            super(baseData, commonData, currentDuration);
            this.distance = distance;
            this.walkSpeed = walkSpeed;
            this.sprintSpeed = sprintSpeed;
        }

        @Override
        public Component getDescription() {
            return Component.empty();
        }

        public int distance() {
            return distance;
        }

        public float walkSpeed() {
            return walkSpeed;
        }

        public float sprintSpeed() {
            return sprintSpeed;
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }
}