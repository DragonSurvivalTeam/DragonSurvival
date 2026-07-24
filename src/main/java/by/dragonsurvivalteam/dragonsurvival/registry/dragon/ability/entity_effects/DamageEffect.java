package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Expression;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.math.BigDecimal;
import java.util.List;

public record DamageEffect(Holder<DamageType> damageType, LevelBasedValue amount, Holder<Attribute> scale, Expression expression, boolean useClaw) implements AbilityEntityEffect {
    public static final Expression DEFAULT_EXPRESSION = new Expression("amount * scale");

    public static final MapCodec<DamageEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageType.CODEC.fieldOf("damage_type").forGetter(DamageEffect::damageType),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(DamageEffect::amount),
            Attribute.CODEC.optionalFieldOf("scale", DSAttributes.DRAGON_ABILITY_DAMAGE).forGetter(DamageEffect::scale),
            MiscCodecs.expressionCodec("amount", "scale").optionalFieldOf("expression", DEFAULT_EXPRESSION).forGetter(DamageEffect::expression),
            Codec.BOOL.optionalFieldOf("use_claw", false).forGetter(DamageEffect::useClaw)
    ).apply(instance, DamageEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        boolean swap = swap(dragon);
        target.hurt(new DamageSource(damageType, dragon), calculate(dragon, ability.level()));

        // Used by 'OwnerHurtTargetGoal'
        dragon.setLastHurtMob(target);

        if (swap) {
            ClawInventoryData.getData(dragon).swapFinish(dragon);
        }
    }

    private float calculate(final Player player, final int abilityLevel) {
        // Sadly, we cannot re-use GeckoLib expressions since it calls 'MolangQueries' which loads client classes
        expression.setVariable("amount", new BigDecimal(amount.calculate(abilityLevel)));
        expression.setVariable("scale", new BigDecimal(player.getAttributeValue(this.scale)));
        return expression.eval().floatValue();
    }

    private boolean swap(final Player player) {
        boolean swap = this.useClaw;

        if (swap) {
            ItemStack sword = ClawToolHandler.getDragonSword(player);

            if (!sword.isEmpty()) {
                ClawInventoryData.getData(player).swapStart(player, sword, ClawInventoryData.Slot.SWORD.ordinal());
            } else {
                swap = false;
            }
        }

        return swap;
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        boolean swap = swap(dragon);

        //noinspection DataFlowIssue -> key is present
        MutableComponent damageType = Component.translatable(Translation.Type.DAMAGE_TYPE.wrap(this.damageType.getKey().location()));
        MutableComponent abilityDamage = Component.translatable(LangKey.ABILITY_DAMAGE, DSColors.dynamicValue(calculate(dragon, ability.level())), DSColors.dynamicValue(damageType));

        if (swap) {
            ClawInventoryData.getData(dragon).swapFinish(dragon);
        }

        return List.of(abilityDamage);
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
