package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.UUID;

@EventBusSubscriber
public class ProjectileHandler {
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

    /** Prevent projectiles in the same batch from colliding against each other */
    @SubscribeEvent
    public static void handleBatchImpact(final ProjectileImpactEvent event) {
        UUID batchID = AttachmentManager.getExistingData(event.getProjectile(), DSDataAttachments.ENTITY_HANDLER).map(data -> data.projectileBatchID).orElse(null);

        if (batchID == null) {
            return;
        }

        if (event.getRayTraceResult() instanceof EntityHitResult result) {
            if (batchID.equals(AttachmentManager.getExistingData(result.getEntity(), DSDataAttachments.ENTITY_HANDLER).map(data -> data.projectileBatchID).orElse(null))) {
                event.setCanceled(true);
            }
        }
    }
}
