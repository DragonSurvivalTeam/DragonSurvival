package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record PotionData(HolderSet<MobEffect> effects, LevelBasedValue amplifier, LevelBasedValue duration, LevelBasedValue probability, boolean effectParticles,
                         boolean showIcon) {
    public static final LevelBasedValue DEFAULT_PROBABILITY = LevelBasedValue.constant(1);

    public static final MapCodec<PotionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(BuiltInRegistries.MOB_EFFECT.key()).fieldOf("effects").forGetter(PotionData::effects),
            LevelBasedValue.CODEC.fieldOf("amplifier").forGetter(PotionData::amplifier),
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(PotionData::duration),
            LevelBasedValue.CODEC.optionalFieldOf("probability", DEFAULT_PROBABILITY).forGetter(PotionData::probability),
            Codec.BOOL.optionalFieldOf("effect_particles", false).forGetter(PotionData::effectParticles),
            Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(PotionData::showIcon)
    ).apply(instance, PotionData::new));

    public record Calculated(int amplifier, int duration, float probability) {
        public static Calculated from(final PotionData data, final int level) {
            return new Calculated((int) data.amplifier().calculate(level), (int) data.duration().calculate(level), data.probability().calculate(level));
        }
    }

    public void apply(@Nullable final ServerPlayer dragon, final int level, final Entity target) {
        if (target instanceof LivingEntity livingTarget) {
            effects().forEach(effect -> {
                MobEffectInstance instance = livingTarget.getEffect(effect);
                Calculated calculated = Calculated.from(this, level);

                if (instance != null && (instance.getAmplifier() >= calculated.amplifier() && instance.getDuration() >= calculated.duration())) {
                    // Don't do anything if the current effect is at least equally strong and has at least the same duration
                    // For all other cases this new effect will either override the current instance or be added as hidden effect
                    // (Whose duration etc. will be applied once the stronger (and shorter) effect runs out)
                    return;
                }

                if (livingTarget.getRandom().nextDouble() > calculated.probability()) {
                    return;
                }

                if (effect.value().isInstantenous()) {
                    effect.value().applyInstantenousEffect(dragon, null, livingTarget, calculated.amplifier(), /* Seems to be the effect strength */ 1);
                } else {
                    livingTarget.addEffect(new MobEffectInstance(effect, calculated.duration(), calculated.amplifier(), false, effectParticles, showIcon), dragon);
                }
            });
        }
    }

    public void remove(@Nullable final ServerPlayer dragon, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            effects.forEach(effect -> {
                MobEffectInstance instance = livingEntity.getEffect(effect);

                if (instance == null) {
                    return;
                }

                if (dragon == null || ((AdditionalEffectData) instance).dragonSurvival$getApplier(dragon.serverLevel()) == dragon) {
                    livingEntity.removeEffect(effect);
                }
            });
        }
    }

    public List<MutableComponent> getDescription(final int level) {
        List<MutableComponent> components = new ArrayList<>();
        Calculated calculated = Calculated.from(this, level);

        for (Holder<MobEffect> effect : effects) {
            MutableComponent name = Component.translatable(effect.value().getDescriptionId());

            if (calculated.amplifier() > 0) {
                name.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (calculated.amplifier() + 1)));
            }

            MutableComponent applies = Component.translatable(LangKey.ABILITY_APPLIES, DSColors.dynamicValue(name));

            if (calculated.duration() != DurationInstance.INFINITE_DURATION) {
                applies.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.dynamicValue(Functions.ticksToSeconds(calculated.duration()))));
            }

            if (calculated.probability() < 1) {
                applies.append(Component.translatable(LangKey.ABILITY_EFFECT_CHANCE, DSColors.dynamicValue(NumberFormat.getPercentInstance().format(calculated.probability()))));
            }

            components.add(applies);
        }

        return components;
    }

    public PotionContents toPotionContents(final ServerPlayer player, final int level) {
        List<MobEffectInstance> instances = new ArrayList<>();
        Calculated calculated = Calculated.from(this, level);

        for (Holder<MobEffect> effect : effects) {
            if (player.getRandom().nextDouble() >= calculated.probability()) {
                continue;
            }

            instances.add(new MobEffectInstance(effect, calculated.duration(), calculated.amplifier(), false, effectParticles()));
        }

        return new PotionContents(Optional.empty(), Optional.empty(), instances);
    }

    @SafeVarargs
    public static Builder create(final Holder<MobEffect>... effects) {
        return new Builder(effects);
    }

    public static Builder create(final HolderSet<MobEffect> effects) {
        return new Builder(effects);
    }

    public static class Builder {
        private final HolderSet<MobEffect> effects;
        private LevelBasedValue amplifier = LevelBasedValue.constant(0);
        private LevelBasedValue duration = DragonAbilities.INFINITE_DURATION;
        private LevelBasedValue probability = DEFAULT_PROBABILITY;
        private boolean effectParticles;
        private boolean showIcon = true;

        @SafeVarargs
        public Builder(final Holder<MobEffect>... effects) {
            this.effects = HolderSet.direct(effects);
        }

        public Builder(final HolderSet<MobEffect> effects) {
            this.effects = effects;
        }

        public Builder amplifier(final int amplifier) {
            this.amplifier = LevelBasedValue.constant(amplifier);
            return this;
        }

        public Builder amplifierPer(final float amplifier) {
            this.amplifier = LevelBasedValue.perLevel(amplifier);
            return this;
        }

        /** Takes the value in seconds */
        public Builder duration(final int duration) {
            this.duration = LevelBasedValue.constant(Functions.secondsToTicks(duration));
            return this;
        }

        /** Takes the value in seconds */
        public Builder durationPer(final int duration) {
            this.duration = LevelBasedValue.perLevel(Functions.secondsToTicks(duration));
            return this;
        }

        public Builder probability(final float probability) {
            this.probability = LevelBasedValue.constant(probability);
            return this;
        }

        public Builder probabilityPer(final float probability) {
            this.probability = LevelBasedValue.perLevel(probability);
            return this;
        }

        public Builder showParticles() {
            this.effectParticles = true;
            return this;
        }

        public Builder hideIcon() {
            this.showIcon = false;
            return this;
        }

        public PotionData build() {
            return new PotionData(effects, amplifier, duration, probability, effectParticles, showIcon);
        }
    }
}
