package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public record OnDeath() implements ActivationTrigger {
    public static final OnDeath INSTANCE = new OnDeath();
    public static final MapCodec<OnDeath> CODEC = MapCodec.unit(INSTANCE);

    public static void trigger(final LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return;
            }

            float previousHealth = player.getHealth();
            MagicData.getData(player).filterPassiveByTrigger(trigger -> trigger.type() == TriggerType.ON_DEATH).forEach(ability -> ability.tick(player));

            if (previousHealth <= 0 && player.getHealth() > 0) {
                // An effect healed the player, so we assume that the death should not occur
                event.setCanceled(true);
            }
        }
    }

    @Override
    public TriggerType type() {
        return TriggerType.ON_DEATH;
    }

    @Override
    public MapCodec<? extends ActivationTrigger> codec() {
        return CODEC;
    }
}
