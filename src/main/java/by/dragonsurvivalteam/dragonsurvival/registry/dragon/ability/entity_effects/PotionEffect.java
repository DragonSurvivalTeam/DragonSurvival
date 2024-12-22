package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public record PotionEffect(HolderSet<MobEffect> effects, LevelBasedValue amplifier, LevelBasedValue duration, LevelBasedValue probability) implements AbilityEntityEffect {
    public static final MapCodec<PotionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(BuiltInRegistries.MOB_EFFECT.key()).fieldOf("effects").forGetter(PotionEffect::effects),
            LevelBasedValue.CODEC.fieldOf("amplifier").forGetter(PotionEffect::amplifier),
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(PotionEffect::duration),
            LevelBasedValue.CODEC.optionalFieldOf("probability", LevelBasedValue.constant(1)).forGetter(PotionEffect::probability)
    ).apply(instance, PotionEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            int abilityLevel = ability.level();

            effects.forEach(effect -> {
                MobEffectInstance currentInstance = livingEntity.getEffect(effect);

                int duration = (int) duration().calculate(abilityLevel);
                int amplifier = (int) amplifier().calculate(abilityLevel);

                if (currentInstance != null && (currentInstance.getAmplifier() >= amplifier && currentInstance.getDuration() >= duration)) {
                    // Don't do anything if the current effect is at least equally strong and has at least the same duration
                    // For all other cases this new effect will either override the current instance or be added as hidden effect
                    // (Whose duration etc. will be applied once the stronger (and shorter) effect runs out)
                    return;
                }

                if (livingEntity.getRandom().nextDouble() < probability.calculate(abilityLevel)) {
                    livingEntity.addEffect(new MobEffectInstance(effect, duration, amplifier), dragon);
                }
            });
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();
        double duration = Functions.ticksToSeconds((int) duration().calculate(ability.level()));

        for (Holder<MobEffect> effect : effects) {
            MutableComponent name = Component.literal("§6■ ").append(Component.translatable(LangKey.ABILITY_APPLIES).append(Component.translatable(effect.value().getDescriptionId())).withColor(DSColors.ORANGE));
            int amplifier = (int) amplifier().calculate(ability.level());

            if (amplifier > 0) {
                name.append(Component.literal(Integer.toString(amplifier)).withColor(DSColors.ORANGE));
            }

            name.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.blue(duration)));
            float probability = probability().calculate(ability.level());

            if (probability < 1) {
                name.append(Component.translatable(LangKey.ABILITY_EFFECT_CHANCE, DSColors.blue(NumberFormat.getPercentInstance().format(probability))));
            }

            components.add(name);
        }

        return components;
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            // FIXME :: need to flag the instance to know that we applied it?
            //  would also need to check (and remove) the hidden effect if the base instance is not ours?
            effects.forEach(livingEntity::removeEffect);
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
