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

public record OnAttackEffect(PotionData potionData) implements AbilityEntityEffect {
    public static final MapCodec<OnAttackEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PotionData.CODEC.fieldOf("potion").forGetter(OnAttackEffect::potionData)
    ).apply(instance, OnAttackEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        OnAttackEffects data = OnAttackEffects.getData(entity);
        data.addEffect(ability.id(), new OnAttackEffectInstance(potionData.effects(), (int) potionData.amplifier().calculate(ability.level()), (int) potionData.duration().calculate(ability.level()), potionData.probability().calculate(ability.level())));
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        OnAttackEffects.getData(entity).removeEffect(ability.id());
    }

    @Override
    public boolean shouldAppendSelfTargetingToDescription() {
        return false;
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> description = potionData.getDescription(ability.level());
        description.add(Component.translatable(LangKey.ABILITY_ON_HIT));
        return description;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
