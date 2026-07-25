package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectsMaintainedThroughDeath;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber
public class EffectHandler {
    @SubscribeEvent
    public static void handleEffectApplication(final MobEffectEvent.Added event) {
        ((AdditionalEffectData) event.getEffectInstance()).dragonSurvival$setApplier(event.getEffectSource());

        if (event.getEffectInstance().getEffect() == DSEffects.EMPOWERED_SOUL.get() && event.getEntity().hasEffect(DSEffects.EXHAUSTED_SOUL.get())) {
            event.getEntity().removeEffect(DSEffects.EXHAUSTED_SOUL.get());
        }

        if (event.getEntity() instanceof Player player && event.getEffectInstance().getEffect() == DSEffects.EXHAUSTED_SOUL.get()) {
            // Only for the visuals - we return 'false' for the cooldown check
            player.getCooldowns().addCooldown(DSItems.DRAGON_SOUL.get(), event.getEffectInstance().getDuration());
        }
    }

    @SubscribeEvent
    public static void checkIfEffectIsApplicable(final MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() == DSEffects.EXHAUSTED_SOUL.get() && event.getEntity().hasEffect(DSEffects.EMPOWERED_SOUL.get())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void preserveEffects(final LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        EffectsMaintainedThroughDeath effects = EffectsMaintainedThroughDeath.getData(player);

        if (player.hasEffect(DSEffects.HUNTER_OMEN.get())) {
            effects.addEffect(player.getEffect(DSEffects.HUNTER_OMEN.get()));
        }

        if (player.hasEffect(DSEffects.EXHAUSTED_SOUL.get())) {
            effects.addEffect(player.getEffect(DSEffects.EXHAUSTED_SOUL.get()));
        }
    }

    @SubscribeEvent // 'Expired' should be handled by the cooldown naturally ticking down
    public static void handleEffectRemoval(final MobEffectEvent.Remove event) {
        if (event.getEntity() instanceof Player player) {
            MobEffectInstance instance = event.getEffectInstance();

            if (instance != null && instance.getEffect() == DSEffects.EXHAUSTED_SOUL.get()) {
                player.getCooldowns().removeCooldown(DSItems.DRAGON_SOUL.get());
            }
        }
    }

    @SubscribeEvent
    public static void experienceDrop(final LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();

        if (player != null) {
            int droppedExperience = event.getDroppedExperience();
            event.setDroppedExperience((int) (droppedExperience * player.getAttributeValue(DSAttributes.EXPERIENCE.get())));
        }
    }

    public static void renderEffectParticle(final LivingEntity entity, final ParticleOptions particle) {
        double d0 = (double) entity.getRandom().nextFloat() * entity.getBbWidth();
        double d1 = (double) entity.getRandom().nextFloat() * entity.getBbHeight();
        double d2 = (double) entity.getRandom().nextFloat() * entity.getBbWidth();
        double x = entity.getX() + d0 - entity.getBbWidth() / 2;
        double y = entity.getY() + d1;
        double z = entity.getZ() + d2 - entity.getBbWidth() / 2;

        entity.level().addParticle(particle, x, y, z, 0, 0, 0);
    }
}
