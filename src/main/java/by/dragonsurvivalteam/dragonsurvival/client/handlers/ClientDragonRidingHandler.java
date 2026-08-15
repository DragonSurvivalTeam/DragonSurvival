package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class ClientDragonRidingHandler {

    // Run this early, since we are overwriting the position
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void positionDetachedRiderCamera(final CalculateDetachedCameraDistanceEvent event) {
        positionRiderCamera(event.getCamera(), event.getCamera().getCameraEntityPartialTicks(Minecraft.getInstance().getDeltaTracker()));
    }

    // Run this early, since we are overwriting the position
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void positionFirstPersonRiderCamera(final ViewportEvent.ComputeFov event) {
        if (event.getCamera().isDetached() || !event.usedConfiguredFov()) {
            return;
        }

        positionRiderCamera(event.getCamera(), (float) event.getPartialTick());
    }

    private static void positionRiderCamera(final Camera camera, float partialTick) {
        if (!(camera.entity() instanceof Player rider)) {
            return;
        }

        Vec3 positionCorrection = DragonRenderer.getMountingBonePositionCorrection(rider, partialTick);
        if (positionCorrection == null) {
            return;
        }

        float zoom = (float) positionCorrection.dot(new Vec3(camera.forwardVector()));
        float vertical = (float) positionCorrection.dot(new Vec3(camera.upVector()));
        float horizontal = -(float) positionCorrection.dot(new Vec3(camera.leftVector()));
        camera.move(zoom, vertical, horizontal);
    }
}
