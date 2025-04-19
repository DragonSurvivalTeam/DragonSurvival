package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
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
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public record MobEffectRemovalEffect(
        Optional<List<MobEffectCategory>> categories,
        Optional<HolderSet<MobEffect>> validEffects,
        Optional<LevelBasedValue> maxAmount,
        Optional<LevelBasedValue> maximumEffectLevel
) implements AbilityEntityEffect {
    @Translation(comments = "Removes %s effect")
    public static final String REMOVE_ONE = Translation.Type.GUI.wrap("mobeffect_removal_effect.remove_one");

    @Translation(comments = "Removes %s status effects")
    public static final String REMOVE_MULTIPLE = Translation.Type.GUI.wrap("mobeffect_removal_effect.remove_multiple");

    @Translation(comments = "Removes %s status effects")
    public static final String REMOVE_ALL = Translation.Type.GUI.wrap("mobeffect_removal_effect.remove_all");

    @Translation(comments = " from:")
    public static final String CATEGORY_FILTER = Translation.Type.GUI.wrap("mobeffect_removal_effect.category_filter");

    @Translation(comments = " limited to:")
    public static final String LIMITED_TO = Translation.Type.GUI.wrap("mobeffect_removal_effect.limited_to");

    @Translation(comments = " with a level at most %s")
    public static final String MAXIMUM_LEVEL = Translation.Type.GUI.wrap("mobeffect_removal_effect.maximum_level");

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
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> description = List.of(Component.empty());
        if (maxAmount.isPresent() && maxAmount.get().calculate(ability.level()) == 1) {
            description.getFirst().append(Component.translatable(REMOVE_ONE, DSColors.dynamicValue("1")));
        } else if (maxAmount.isPresent()) {
            description.getFirst().append(Component.translatable(REMOVE_MULTIPLE, DSColors.dynamicValue(maxAmount.get().calculate(ability.level()))));
        } else {
            description.getFirst().append(Component.translatable(REMOVE_ALL));
        }

        if (categories.isPresent()) {
            description.getFirst().append(Component.translatable(CATEGORY_FILTER));
            for (MobEffectCategory category : categories.get()) {
                description.getFirst().append(DSColors.withColor(Component.literal(" " + category.name()), DSColors.GOLD));
            }
        }

        if (validEffects.isPresent()) {
            description.getFirst().append(Component.translatable(LIMITED_TO));
            for (Holder<MobEffect> effect : validEffects.get()) {
                description.getFirst().append(DSColors.withColor(Component.translatable(" " + effect.value().getDisplayName()), DSColors.GREEN));
            }
        }

        if (maximumEffectLevel.isPresent()) {
            description.getFirst().append(Component.translatable(MAXIMUM_LEVEL, DSColors.dynamicValue(maximumEffectLevel.get())));
        }
        description.getFirst().append(".");

        return description;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
