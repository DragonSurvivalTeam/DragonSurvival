package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EntityScale;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow @Final private Vector3f up;
    @Shadow private Vec3 position;
    @Shadow private Entity entity;

    @ModifyArg(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(D)D"), index = 0)
    private double dragonSurvival$modifyDetachedCameraDistance(double distance, @Local(argsOnly = true) float partialTick) {
        return ClientFlightHandler.modifyDetachedCameraDistance((Camera) (Object) this, distance, partialTick);
    }

    @Inject(method = "getMaxZoom", at = @At(value = "HEAD"))
    private void dragonSurvival$moveCameraPositionUpWhenInVisualBlockWithNoCollision(double maxZoom, final CallbackInfoReturnable<Double> callback) {
        Player player = Minecraft.getInstance().player;

        if (player == null || this.entity != player) {
            return;
        }

        //noinspection DataFlowIssue -> player is present
        if (((LocalPlayerAccessor) player).dragonSurvival$suffocatesAt(BlockPos.containing(player.position()))) {
            return;
        }

        // Check in 5 places: 1 in the center, 4 around the player
        for (int i = 0; i < 5; i++) {
            float scale = Math.min(1, EntityScale.get(player));
            float cameraOffset = 0.1f * scale;
            float xOffset = i == 0 ? 0 : i == 1 ? cameraOffset : i == 2 ? -cameraOffset : 0;
            float zOffset = i == 0 ? 0 : i == 3 ? cameraOffset : i == 4 ? -cameraOffset : 0;

            Vec3 offsetStart = this.position.subtract(xOffset, cameraOffset, zOffset);
            Vec3 up = offsetStart.add(new Vec3(this.up).scale(maxZoom));

            // Start from the lowest point that the raycast check around the player does in the next step of getMaxZoom
            // This is to check if the camera angle checks in the next step would get tripped by a visual block
            BlockHitResult visualHit = Minecraft.getInstance().level.clip(new ClipContext(offsetStart, up, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.entity));
            BlockHitResult collisionHit = Minecraft.getInstance().level.clip(new ClipContext(offsetStart, up, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.entity));

            // This means the player is inside a visual block. In this case, set the camera y-position to be slightly above whatever the visual height is here.
            if (visualHit.getLocation() != collisionHit.getLocation() && visualHit.getType() == HitResult.Type.BLOCK && visualHit.isInside()) {
                // Use the highest point of the shape
                VoxelShape shape = Minecraft.getInstance().level.getBlockState(visualHit.getBlockPos()).getVisualShape(Minecraft.getInstance().level, visualHit.getBlockPos(), CollisionContext.empty());
                double highestY = shape.max(Direction.Axis.Y);
                this.position = new Vec3(this.position.x, visualHit.getBlockPos().getY() + highestY + cameraOffset, this.position.z);
                return;
            }
        }
    }

    /** Reduce x-ray through the blocks to the side of the player (when moving the third person camera) */
    @ModifyExpressionValue(method = "getMaxZoom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;distanceTo(Lnet/minecraft/world/phys/Vec3;)D"))
    private double dragonSurvival$modifyDistance(double distance) {
        Player player = Minecraft.getInstance().player;

        if (player != null && this.entity == player) {
            float scale = EntityScale.get(player);
            if (scale < 1) {
                return distance * scale;
            }
        }

        return distance;
    }

    // We need to adjust the distance at which blocks are checked for collision to prevent the camera from thinking it is blocked
    @ModifyArg(method = "getMaxZoom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"), index = 0)
    private double dragonSurvival$adjustCameraPositionX(final double original) {
        return dragonSurvival$scaleCameraOffset(original);
    }

    @ModifyArg(method = "getMaxZoom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"), index = 1)
    private double dragonSurvival$adjustCameraPositionY(final double original) {
        return dragonSurvival$scaleCameraOffset(original);
    }

    @ModifyArg(method = "getMaxZoom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"), index = 2)
    private double dragonSurvival$adjustCameraPositionZ(final double original) {
        return dragonSurvival$scaleCameraOffset(original);
    }

    private double dragonSurvival$scaleCameraOffset(final double original) {
        Player player = Minecraft.getInstance().player;

        if (player != null && this.entity == player) {
            float scale = Math.min(1, EntityScale.get(player));
            return original * scale;
        }

        return original;
    }

    /**
     * Small scale values have camera / x-ray issues if the near plane is too far away.
     * Keep the main camera projection and culling projection in sync.
     */
    @ModifyConstant(method = "getNearPlane", constant = @Constant(floatValue = 0.05F))
    private float dragonSurvival$adjustNearPlane(final float original) {
        return RenderingUtils.getNearPlane(original);
    }
}
