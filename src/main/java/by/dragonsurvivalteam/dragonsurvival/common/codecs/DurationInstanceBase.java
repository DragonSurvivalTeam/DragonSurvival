package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record DurationInstanceBase(ResourceLocation id, LevelBasedValue duration, Optional<LootItemCondition> earlyRemovalCondition, Optional<ResourceLocation> customIcon, boolean isHidden) {
    public static final Codec<DurationInstanceBase> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(DurationInstanceBase::id),
            LevelBasedValue.CODEC.optionalFieldOf("duration", DragonAbilities.INFINITE_DURATION).forGetter(DurationInstanceBase::duration),
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("early_removal_condition").forGetter(DurationInstanceBase::earlyRemovalCondition),
            ResourceLocation.CODEC.optionalFieldOf("custom_icon").forGetter(DurationInstanceBase::customIcon),
            Codec.BOOL.optionalFieldOf("is_hidden", false).forGetter(DurationInstanceBase::isHidden)
    ).apply(instance, DurationInstanceBase::new));

    public static Builder create(final ResourceLocation id) {
        return new Builder(id);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public static class Builder {
        private final ResourceLocation id;
        private LevelBasedValue duration;
        private Optional<LootItemCondition> earlyRemovalCondition = Optional.empty();
        private Optional<ResourceLocation> customIcon = Optional.empty();
        private boolean isHidden = false;

        public Builder(final ResourceLocation id) {
            this.id = id;
        }

        public Builder infinite() {
            this.duration = DragonAbilities.INFINITE_DURATION;
            return this;
        }

        public Builder duration(final LevelBasedValue duration) {
            this.duration = duration;
            return this;
        }

        public Builder earlyRemoval(final LootItemCondition condition) {
            this.earlyRemovalCondition = Optional.ofNullable(condition);
            return this;
        }

        public Builder customIcon(final ResourceLocation icon) {
            this.customIcon = Optional.ofNullable(icon);
            return this;
        }
        public Builder hidden() {
            this.isHidden = true;
            return this;
        }

        public DurationInstanceBase build() {
            return new DurationInstanceBase(id, duration, earlyRemovalCondition, customIcon, isHidden);
        }
    }
}
