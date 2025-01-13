package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
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

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record PotionData(HolderSet<MobEffect> effects, LevelBasedValue amplifier, LevelBasedValue duration, LevelBasedValue probability, boolean effectParticles) {
    public static final LevelBasedValue DEFAULT_PROBABILITY = LevelBasedValue.constant(1);

    public static final MapCodec<PotionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(BuiltInRegistries.MOB_EFFECT.key()).fieldOf("effects").forGetter(PotionData::effects),
            LevelBasedValue.CODEC.fieldOf("amplifier").forGetter(PotionData::amplifier),
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(PotionData::duration),
            LevelBasedValue.CODEC.optionalFieldOf("probability", DEFAULT_PROBABILITY).forGetter(PotionData::probability),
            Codec.BOOL.optionalFieldOf("effect_particles", false).forGetter(PotionData::effectParticles)
    ).apply(instance, PotionData::new));

    public record Calculated(int amplifier, int duration, float probability) {
        public static Calculated from(final PotionData data, final int level) {
            return new Calculated((int) data.amplifier().calculate(level), (int) data.duration().calculate(level), data.probability().calculate(level));
        }
    }

    public void apply(@Nullable final ServerPlayer dragon, final int level, final Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            effects().forEach(effect -> {
                MobEffectInstance instance = livingEntity.getEffect(effect);
                Calculated calculated = Calculated.from(this, level);

                if (instance != null && (instance.getAmplifier() >= calculated.amplifier() && instance.getDuration() >= calculated.duration())) {
                    // Don't do anything if the current effect is at least equally strong and has at least the same duration
                    // For all other cases this new effect will either override the current instance or be added as hidden effect
                    // (Whose duration etc. will be applied once the stronger (and shorter) effect runs out)
                    return;
                }

                if (livingEntity.getRandom().nextDouble() < calculated.probability()) {
                    livingEntity.addEffect(new MobEffectInstance(effect, calculated.duration(), calculated.amplifier(), false, effectParticles()), dragon);
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
    public static PotionData of(final LevelBasedValue amplifier, final LevelBasedValue duration,  final boolean effectParticles, final Holder<MobEffect>... effects) {
        return of(amplifier, duration, DEFAULT_PROBABILITY, effectParticles, effects);
    }

    @SafeVarargs
    public static PotionData of(final LevelBasedValue amplifier, final LevelBasedValue duration, final LevelBasedValue probability, final boolean effectParticles, final Holder<MobEffect>... effects) {
        return new PotionData(HolderSet.direct(effects), amplifier, duration, probability, effectParticles);
    }
}
