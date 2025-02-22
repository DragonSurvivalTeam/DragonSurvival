package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record ActionContainer(AbilityTargeting effect, LevelBasedValue triggerRate) {
    public static final Codec<ActionContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AbilityTargeting.CODEC.fieldOf("target_selection").forGetter(ActionContainer::effect),
            LevelBasedValue.CODEC.fieldOf("trigger_rate").forGetter(ActionContainer::triggerRate)
    ).apply(instance, ActionContainer::new));

    public void tick(final ServerPlayer dragon, final DragonAbilityInstance instance, int currentTick) {
        int actualTick = currentTick - instance.value().activation().getCastTime(instance.level());
        float rate = triggerRate.calculate(instance.level());

        if (rate > 0 && actualTick % rate != 0) {
            return;
        }

        effect.apply(dragon, instance);
    }

    public void remove(final ServerPlayer dragon, final DragonAbilityInstance instance) {
        effect.remove(dragon, instance);
    }
}
