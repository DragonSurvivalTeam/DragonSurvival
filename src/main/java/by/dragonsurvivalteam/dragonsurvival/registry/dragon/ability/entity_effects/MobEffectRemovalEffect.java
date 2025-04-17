package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public record MobEffectRemovalEffect(
        Optional<List<MobEffectCategory>> categories,
        Optional<HolderSet<MobEffect>> validEffects,
        Optional<LevelBasedValue> maxAmount,
        Optional<LevelBasedValue> maximumEffectLevel
) implements AbilityEntityEffect {
    public static final MapCodec<MobEffectRemovalEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.list(MiscCodecs.enumCodec(MobEffectCategory.class)).optionalFieldOf("categories").forGetter(MobEffectRemovalEffect::categories),
            RegistryCodecs.homogeneousList(BuiltInRegistries.MOB_EFFECT.key()).optionalFieldOf("valid_effects").forGetter(MobEffectRemovalEffect::validEffects),
            LevelBasedValue.CODEC.optionalFieldOf("max_amount").forGetter(MobEffectRemovalEffect::maxAmount),
            LevelBasedValue.CODEC.optionalFieldOf("maximum_effect_level").forGetter(MobEffectRemovalEffect::maximumEffectLevel)
    ).apply(instance, MobEffectRemovalEffect::new));

    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            int removed = 0;
            for (MobEffectInstance instance : livingEntity.getActiveEffects()) {
                if (maxAmount.isPresent()) {
                    if (maxAmount.get().calculate(ability.level()) < removed) {
                        return;
                    }
                }
                if (categories.isEmpty() || categories.get().contains(instance.getEffect().value().getCategory())) {
                    if (validEffects.isEmpty() || validEffects.get().contains(instance.getEffect())) {
                        if (maximumEffectLevel.isEmpty() || instance.getAmplifier() <= maximumEffectLevel.get().calculate(ability.level())) {
                            livingEntity.removeEffect(instance.getEffect());
                            removed++;
                        }
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
