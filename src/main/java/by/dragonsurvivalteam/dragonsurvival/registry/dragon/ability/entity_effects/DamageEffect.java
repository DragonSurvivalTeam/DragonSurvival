package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public record DamageEffect(Holder<DamageType> damageType, LevelBasedValue amount) implements AbilityEntityEffect {
    public static final MapCodec<DamageEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageType.CODEC.fieldOf("damage_type").forGetter(DamageEffect::damageType),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(DamageEffect::amount)
    ).apply(instance, DamageEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        float damageAmount = amount().calculate(ability.level());
        damageAmount *= (float) dragon.getAttributeValue(DSAttributes.DRAGON_ABILITY_DAMAGE);
        target.hurt(new DamageSource(damageType, dragon), damageAmount);

        // Used by 'OwnerHurtTargetGoal'
        dragon.setLastHurtMob(target);
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        //noinspection DataFlowIssue -> key is present
        MutableComponent damageType = Component.translatable(Translation.Type.DAMAGE_TYPE.wrap(this.damageType.getKey().location()));
        float damage = amount.calculate(ability.level());
        MutableComponent abilityDamage = Component.translatable(LangKey.ABILITY_DAMAGE, DSColors.dynamicValue(damage), DSColors.dynamicValue(damageType));

        float additionalDamage = damage * (float) dragon.getAttributeValue(DSAttributes.DRAGON_ABILITY_DAMAGE) - damage;
        if (additionalDamage != 0) {
            abilityDamage.append(Component.translatable(LangKey.ABILITY_ADDITIONAL_DAMAGE, DSColors.dynamicValue(additionalDamage)));
        }

        return List.of(abilityDamage);
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
