package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DamageModification;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
            double duration = Functions.ticksToSeconds((int) damageModification.duration().calculate(ability.level()));
            MutableComponent name = Component.empty();

            float amount = damageModification.multiplier().calculate(ability.level());
            String difference = NumberFormat.getPercentInstance().format(Math.abs(amount - 1));
            // TODO :: skip tooltip if amount equals 1?

            if (amount == 0) {
                name = name.append(Component.translatable(LangKey.ABILITY_IMMUNITY));
            } else if (amount < 1) {
                name = name.append(Component.translatable(LangKey.ABILITY_DAMAGE_REDUCTION, DSColors.blue(difference)));
            } else {
                name = name.append(Component.translatable(LangKey.ABILITY_DAMAGE_INCREASE, DSColors.blue(difference)));
            }

            int numTypes = damageModification.damageTypes().size();
            int count = 0;

            for (Holder<DamageType> damageType : damageModification.damageTypes()) {
                //noinspection DataFlowIssue -> key is present
                name = name.append(Component.translatable(Translation.Type.DAMAGE_TYPE.wrap(damageType.getKey().location())).withColor(DSColors.BLUE));
                count++;

                if (count == numTypes - 1) {
                    name = name.append(" and ");
                } else if (count < numTypes - 1) {
                    name = name.append(", ");
                }
            }

            if (duration > 0) {
                name = name.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.blue(duration)));
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
