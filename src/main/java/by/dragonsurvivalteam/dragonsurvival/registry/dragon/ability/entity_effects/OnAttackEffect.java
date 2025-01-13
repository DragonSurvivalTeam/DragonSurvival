package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.OnAttackEffectInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.OnAttackEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

// TODO :: shouldn't this have the option to add a duration (to have the ability to apply said effect)
//  Add option to stack the effect?
public record OnAttackEffect(PotionData potionData) implements AbilityEntityEffect {
    public static final MapCodec<OnAttackEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PotionData.CODEC.fieldOf("potion").forGetter(OnAttackEffect::potionData)
    ).apply(instance, OnAttackEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        OnAttackEffects data = OnAttackEffects.getData(entity);
        PotionData.Calculated calculated = PotionData.Calculated.from(potionData, ability.level());
        data.addEffect(ability.id(), new OnAttackEffectInstance(potionData.effects(), calculated.amplifier(), calculated.duration(), calculated.probability()));
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        OnAttackEffects.getData(entity).removeEffect(ability.id());
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> description = potionData.getDescription(ability.level());

        for (MutableComponent component : description) {
            // Append the info per applied effect
            component.append(Component.translatable(LangKey.ABILITY_ON_HIT));
        }

        return description;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
