package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber
public class HunterProjectileImpactHandler {
    @SubscribeEvent
    public static void onHunterProjectileImpact(final ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof AbstractArrow arrow) {
            Entity owner = arrow.getOwner();

            if (owner == null || !owner.getType().is(DSEntityTypeTags.HUNTER_FACTION)) {
                return;
            }

            if (event.getRayTraceResult() instanceof EntityHitResult result) {
                if (result.getEntity().getType().is(DSEntityTypeTags.HUNTER_FACTION)) {
                    event.setCanceled(true);
                }
            }
        }
    }
}
