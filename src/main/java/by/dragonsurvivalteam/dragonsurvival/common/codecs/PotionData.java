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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public record PotionData(HolderSet<MobEffect> effects, LevelBasedValue amplifier, LevelBasedValue duration, LevelBasedValue probability) {
    public static final MapCodec<PotionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(BuiltInRegistries.MOB_EFFECT.key()).fieldOf("effects").forGetter(PotionData::effects),
            LevelBasedValue.CODEC.fieldOf("amplifier").forGetter(PotionData::amplifier),
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(PotionData::duration),
            LevelBasedValue.CODEC.optionalFieldOf("probability", LevelBasedValue.constant(1)).forGetter(PotionData::probability)
    ).apply(instance, PotionData::new));

    public void apply(@Nullable final ServerPlayer dragon, final int level, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            effects().forEach(effect -> {
                MobEffectInstance currentInstance = livingEntity.getEffect(effect);

                int duration = (int) duration().calculate(level);
                int amplifier = (int) amplifier().calculate(level);

                if (currentInstance != null && (currentInstance.getAmplifier() >= amplifier && currentInstance.getDuration() >= duration)) {
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
            effects().forEach(effect -> {
                MobEffectInstance currentInstance = livingEntity.getEffect(effect);
                if(dragon == null || ((AdditionalEffectData)currentInstance).dragonSurvival$getApplier((ServerLevel)dragon.level()).is(dragon)) {
                    livingEntity.removeEffect(effect);
                }
            });
        }
    }

    public List<MutableComponent> getDescription(final int level) {
        List<MutableComponent> components = new ArrayList<>();
        double duration = Functions.ticksToSeconds((int) duration().calculate(level));

        for (Holder<MobEffect> effect : effects) {
            MutableComponent name = Component.literal("§6■ ").append(Component.translatable(LangKey.ABILITY_APPLIES).append(Component.translatable(effect.value().getDescriptionId())).withColor(DSColors.ORANGE));
            int amplifier = (int) amplifier().calculate(level);

            if (amplifier > 0) {
                name.append(Component.literal(Integer.toString(amplifier)).withColor(DSColors.ORANGE));
            }

            name.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.blue(duration)));
            float probability = probability().calculate(level);

            if (probability < 1) {
                name.append(Component.translatable(LangKey.ABILITY_EFFECT_CHANCE, DSColors.blue(NumberFormat.getPercentInstance().format(probability))));
            }

            components.add(name);
        }

        return components;
    }
}
