package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DamageModification;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.Tags;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public record DamageModificationEffect(List<DamageModification> modifications) implements AbilityEntityEffect {
    public static final MapCodec<DamageModificationEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageModification.CODEC.listOf().fieldOf("modifications").forGetter(DamageModificationEffect::modifications)
    ).apply(instance, DamageModificationEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        modifications.forEach(modification -> modification.apply(dragon, entity, ability));
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            modifications.forEach(modification -> modification.remove(livingEntity));
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        for (DamageModification damageModification : modifications) {
            float amount = damageModification.multiplier().calculate(ability.level());
            String difference = NumberFormat.getPercentInstance().format(Math.abs(amount - 1));

            MutableComponent name;

            if (amount == 0) {
                name = Component.translatable(LangKey.ABILITY_IMMUNITY);
            } else if (amount < 1) {
                name = Component.translatable(LangKey.ABILITY_DAMAGE_REDUCTION, DSColors.dynamicValue(difference));
            } else {
                name = Component.translatable(LangKey.ABILITY_DAMAGE_INCREASE, DSColors.dynamicValue(difference));
            }

            if (damageModification.damageTypes() instanceof HolderSet.Named<DamageType> named) {
                name.append(DSColors.dynamicValue(Component.translatable(Tags.getTagTranslationKey(named.key()))));
            } else {
                int count = 0;

                for (Holder<DamageType> damageType : damageModification.damageTypes()) {
                    //noinspection DataFlowIssue -> key is present
                    name.append(DSColors.dynamicValue(Component.translatable(Translation.Type.DAMAGE_TYPE.wrap(damageType.getKey().location()))));

                    if (count < damageModification.damageTypes().size() - 1) {
                        name.append(", ");
                    }

                    count++;
                }
            }

            float duration = damageModification.duration().calculate(ability.level());

            if (duration != DurationInstance.INFINITE_DURATION) {
                name.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.dynamicValue(Functions.ticksToSeconds((int) duration))));
            }

            components.add(name);
        }

        return components;
    }

    @Override
    public boolean shouldAppendSelfTargetingToDescription() {
        return false;
    }

    public static List<AbilityEntityEffect> single(final DamageModification modification) {
        return List.of(new DamageModificationEffect(List.of(modification)));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
