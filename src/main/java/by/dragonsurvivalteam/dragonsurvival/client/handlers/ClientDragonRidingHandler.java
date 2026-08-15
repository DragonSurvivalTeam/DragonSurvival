package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientDragonRidingHandler {

    // Run this early, since we are overwriting the position
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void positionRiderCamera(final ViewportEvent.ComputeCameraAngles event) {
        positionRiderCamera(event.getCamera(), (float) event.getPartialTick());
    }

    private static void positionRiderCamera(final Camera camera, float partialTick) {
        if (!(camera.getEntity() instanceof Player rider)) {
            return;
        }

        Vec3 positionCorrection = DragonRenderer.getMountingBonePositionCorrection(rider, partialTick);
        if (positionCorrection == null) {
            return;
        }

        float zoom = (float) positionCorrection.dot(new Vec3(camera.getLookVector()));
        float vertical = (float) positionCorrection.dot(new Vec3(camera.getUpVector()));
        float horizontal = -(float) positionCorrection.dot(new Vec3(camera.getLeftVector()));
        camera.move(zoom, vertical, horizontal);
    }
}
