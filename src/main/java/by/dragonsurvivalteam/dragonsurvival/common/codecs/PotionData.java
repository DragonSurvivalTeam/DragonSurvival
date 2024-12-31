package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

public record PotionData(HolderSet<MobEffect> effects, LevelBasedValue amplifier, LevelBasedValue duration, LevelBasedValue probability) {
    public static final LevelBasedValue DEFAULT_PROBABILITY = LevelBasedValue.constant(1);

    public static final MapCodec<PotionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(BuiltInRegistries.MOB_EFFECT.key()).fieldOf("effects").forGetter(PotionData::effects),
            LevelBasedValue.CODEC.fieldOf("amplifier").forGetter(PotionData::amplifier),
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(PotionData::duration),
            LevelBasedValue.CODEC.optionalFieldOf("probability", DEFAULT_PROBABILITY).forGetter(PotionData::probability)
    ).apply(instance, PotionData::new));

    public void apply(@Nullable final ServerPlayer dragon, final int level, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            effects().forEach(effect -> {
                MobEffectInstance instance = livingEntity.getEffect(effect);

                int duration = (int) duration().calculate(level);
                int amplifier = (int) amplifier().calculate(level);

                if (instance != null && (instance.getAmplifier() >= amplifier && instance.getDuration() >= duration)) {
                    // Don't do anything if the current effect is at least equally strong and has at least the same duration
                    // For all other cases this new effect will either override the current instance or be added as hidden effect
                    // (Whose duration etc. will be applied once the stronger (and shorter) effect runs out)
                    return;
                }

                if (livingEntity.getRandom().nextDouble() < probability.calculate(level)) {
                    livingEntity.addEffect(new MobEffectInstance(effect, duration, amplifier), dragon);
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
        double duration = Functions.ticksToSeconds((int) duration().calculate(level));

        for (Holder<MobEffect> effect : effects) {
            MutableComponent name = Component.literal("§6■ ").append(Component.translatable(LangKey.ABILITY_APPLIES).append(Component.translatable(effect.value().getDescriptionId())).withColor(DSColors.GOLD));
            int amplifier = (int) amplifier().calculate(level);

            if (amplifier > 0) {
                name.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (amplifier + 1))).withColor(DSColors.GOLD);
            }

            if (duration > 0) {
                name.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.dynamicValue(duration)));
            }

            float probability = probability().calculate(level);

            if (probability < 1) {
                name.append(Component.translatable(LangKey.ABILITY_EFFECT_CHANCE, DSColors.dynamicValue(NumberFormat.getPercentInstance().format(probability))));
            }

            components.add(name);
        }

        return components;
    }

    public PotionContents toPotionContents(final ServerPlayer player, final int level) {
        List<MobEffectInstance> instances = new ArrayList<>();

        for (Holder<MobEffect> effect : effects) {
            if (player.getRandom().nextDouble() >= probability.calculate(level)) {
                continue;
            }

            int duration = (int) this.duration.calculate(level);
            int amplifier = (int) this.amplifier.calculate(level);
            instances.add(new MobEffectInstance(effect, duration, amplifier));
        }

        return new PotionContents(Optional.empty(), Optional.empty(), instances);
    }

    @SafeVarargs
    public static PotionData of(final LevelBasedValue amplifier, final LevelBasedValue duration, final Holder<MobEffect>... effects) {
        return of(amplifier, duration, DEFAULT_PROBABILITY, effects);
    }

    @SafeVarargs
    public static PotionData of(final LevelBasedValue amplifier, final LevelBasedValue duration, final LevelBasedValue probability, final Holder<MobEffect>... effects) {
        return new PotionData(HolderSet.direct(effects), amplifier, duration, probability);
    }
}
