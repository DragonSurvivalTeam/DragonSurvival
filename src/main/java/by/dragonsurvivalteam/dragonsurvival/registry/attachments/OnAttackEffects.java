package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.OnAttackEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class OnAttackEffects {
    private final Map<String, OnAttackEffectInstance> effectsToApply = new HashMap<>();

    public static OnAttackEffects getData(final Entity entity) {
        return entity.getData(DSDataAttachments.ON_ATTACK_EFFECTS);
    }

    public void addEffect(final String id, final OnAttackEffectInstance effect) {
        effectsToApply.put(id, effect);
    }

    public void removeEffect(final String id) {
        effectsToApply.remove(id);
    }

    @SubscribeEvent
    public static void applyOnAttackEffects(final LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        OnAttackEffects onAttackEffects = getData(attacker);

        for (OnAttackEffectInstance effect : onAttackEffects.effectsToApply.values()) {
            effect.apply(event.getEntity());
        }
    }
}
