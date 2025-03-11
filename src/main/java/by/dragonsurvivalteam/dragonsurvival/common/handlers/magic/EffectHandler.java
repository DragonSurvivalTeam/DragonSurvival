package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;


@EventBusSubscriber
public class EffectHandler {
    @SubscribeEvent
    public static void markLastAfflictedOnApplyEffect(final MobEffectEvent.Added event) {
        ((AdditionalEffectData) event.getEffectInstance()).dragonSurvival$setApplier(event.getEffectSource());
    }

    @SubscribeEvent
    public static void experienceDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();

        if (player != null) {
            int droppedExperience = event.getDroppedExperience();
            event.setDroppedExperience((int) (droppedExperience * player.getAttributeValue(DSAttributes.EXPERIENCE)));
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