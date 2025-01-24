package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class
EntityMixin {
    /** Correctly position the passenger when riding a player dragon */
    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At(value = "HEAD"), cancellable = true)
    private void dragonSurvival$positionRider(Entity entity, Entity.MoveFunction move, CallbackInfo callback) {
        if (!((Entity) (Object) this instanceof Player player) || !(entity instanceof Player passenger) || !hasPassenger(passenger)) {
            return;
        }

        if (DragonStateProvider.isDragon(player)) {
            MovementData movement = MovementData.getData(player);
            DragonStateHandler handler = DragonStateProvider.getData(player);
            Vec3 originalPassPos = player.getPassengerRidingPosition(player);
            Vec3 offset = handler.body().value().mountingOffsets().offset();
            Vec3 scale = handler.body().value().mountingOffsets().scale();
            Vec3 offsetFromBb = offset.add(scale.multiply(player.getBoundingBox().getXsize(), player.getBoundingBox().getYsize(), player.getBoundingBox().getZsize()));

            Vec3 offsetFromCenter = originalPassPos.subtract(player.position());
            offsetFromCenter = offsetFromCenter.xRot((float) Math.toRadians(movement.prevXRot * 1.5)).zRot(-(float) Math.toRadians(movement.prevZRot * 90));
            Vec3 totalOffset = offsetFromCenter.add(offsetFromBb).yRot(-(float) Math.toRadians(movement.bodyYawLastFrame));
            Vec3 passPos = player.position().add(totalOffset);

            move.accept(passenger, passPos.x(), passPos.y(), passPos.z());
            player.onPassengerTurned(passenger);

            callback.cancel();
        }
    }

    /** Correctly rotate the passenger when riding a dragon */
    @SuppressWarnings("ConstantValue") // the if statement checks are valid
    @Inject(method = "onPassengerTurned(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void dragonSurvival$onPassengerTurned(Entity entity, CallbackInfo callback) {
        if (!(entity instanceof Player passenger) || !((Entity) (Object) this instanceof Player vehicle) || !passenger.level().isClientSide()) {
            return;
        }

        if (!DragonStateProvider.isDragon(vehicle)) {
            return;
        }

        MovementData vehicleMovement = MovementData.getData(vehicle);
        if(DragonStateProvider.isDragon(passenger)) {
            MovementData passengerMovement = MovementData.getData(passenger);
            float facing = (float) Mth.wrapDegrees(passenger.getYRot() - vehicleMovement.bodyYawLastFrame);
            float facingClamped = Mth.clamp(facing, -150.0F, 150.0F);
            passenger.yRotO += facingClamped - facing + vehicle.yRotO;
            passengerMovement.bodyYaw = vehicleMovement.bodyYawLastFrame;
            passengerMovement.headYaw = -facing;
            passenger.setYRot((float) (passenger.getYRot() + facingClamped - facing + (vehicleMovement.bodyYawLastFrame - vehicleMovement.bodyYaw)));
        } else {
            float facing = (float) Mth.wrapDegrees(passenger.getYRot() - vehicleMovement.bodyYawLastFrame);
            float facingClamped = Mth.clamp(facing, -120.0F, 120.0F);
            passenger.yRotO += facingClamped - facing + vehicle.yRotO;
            passenger.setYBodyRot((float) (passenger.getYRot() + facingClamped - facing + (vehicleMovement.bodyYawLastFrame - vehicleMovement.bodyYaw)));
            passenger.setYRot((float) (passenger.getYRot() + facingClamped - facing + (vehicleMovement.bodyYawLastFrame - vehicleMovement.bodyYaw)));
            passenger.setYHeadRot(passenger.getYRot());
        }
    }

    /** Don't show fire animation (when burning) when being a cave dragon when rendered in the inventory */
    @ModifyReturnValue(method = "displayFireAnimation()Z", at = @At("RETURN"))
    private boolean dragonSurvival$hideCaveDragonFireAnimation(boolean displayAnimation) {
        if (!displayAnimation) {
            return false;
        }

        Entity entity = (Entity) (Object) this;
        return !entity.fireImmune();
    }

    @Inject(method = "isVisuallyCrawling()Z", at = @At(value = "HEAD"), cancellable = true)
    public void dragonSurvival$isDragonVisuallyCrawling(CallbackInfoReturnable<Boolean> callback) {
        if (DragonStateProvider.isDragon((Entity) (Object) this)) {
            callback.setReturnValue(false);
        }
    }

    /** Prevent dragons from riding certain vehicles */
    @SuppressWarnings("ConstantValue") // the if statement checks are valid
    @ModifyReturnValue(method = "canRide", at = @At(value = "RETURN"))
    public boolean dragonSurvival$canRide(boolean original, @Local(argsOnly = true, ordinal = 0) Entity entity) {
        if (ServerConfig.limitedRiding && DragonStateProvider.isDragon((Entity) (Object) this) && /* Still allow riding dragons */ !DragonStateProvider.isDragon(entity)) {
            return entity.getType().is(DSEntityTypeTags.VEHICLE_WHITELIST);
        }

        return original;
    }

    /** To just skip rendering entirely instead of rendering with a 0 alpha value */
    @ModifyReturnValue(method = "isInvisible", at = @At("RETURN"))
    private boolean dragonSurvival$enableHunterStacksInvisibility(boolean isInvisible) {
        if (isInvisible) {
            return true;
        }

        Entity self = (Entity) (Object) this;
        HunterData data = self.getData(DSDataAttachments.HUNTER);

        if (data.hasMaxHunterStacks()) {
            // With max. stacks the visibility value is set to 0 anyway so this shouldn't affect actual gameplay features
            return HunterHandler.calculateAlpha(self) == 0;
        }

        return false;
    }

    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean dragonSurvival$caveDragonFireImmunity(boolean isFireImmune) {
        if (isFireImmune) {
            return true;
        }

        Entity self = (Entity) (Object) this;
        return self.getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS)
                .map(DamageModifications::isFireImmune)
                .orElse(false);
    }

    // Using 'ModifyReturnValue' seems to not work - the mixin cannot find the method
    @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"), cancellable = true)
    private void dragonSurvival$checkSummonRelationship(final Entity target, final CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue()) {
            return;
        }

        Entity self = (Entity) (Object) this;

        if (SummonedEntities.hasSummonRelationship(self, target)) {
            callback.setReturnValue(true);
        }
    }

    @ModifyReturnValue(method = "getMaxAirSupply", at = @At("RETURN"))
    private int dragonSurvival$modifyMaxAirSupply(int maxAirSupply) {
        Entity self = (Entity) (Object) this;

        if (self instanceof Player player) {
            SwimData swimData = SwimData.getData(player);
            int newMaxAirSupply = swimData.getMaxOxygen(player, self.getEyeInFluidType());

            if (newMaxAirSupply == SwimData.UNLIMITED_OXYGEN) {
                // Unlimited oxygen is handled in the 'ILivingEntityExtensionMixin'
                return maxAirSupply;
            }

            return newMaxAirSupply;
        }

        return maxAirSupply;
    }

    // After a size refresh, vanilla normally prevents fudgePosition from being called. So we force it to be called, then *only* override the pose after all position fudging has completed
    // to prevent a pose change from triggering based on an incorrect position (which would cause stuttering otherwise).
    @Inject(method = "refreshDimensions", at = @At("TAIL"))
    private void dragonSurvival$fudgePositionAfterDragonSizeChange(CallbackInfo callback, @Local(ordinal = 0) EntityDimensions entitydimensions) {
        if ((Object) this instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return;
            }

            DragonSizeHandler.fudgePositionAfterSizeChange(player, entitydimensions);
            DragonSizeHandler.overridePose(player);
        }
    }

    @ModifyExpressionValue(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity$MovementEmission;emitsSounds()Z"))
    private boolean dragonSurvival$modifyWalkSoundsWhenWalkingUnderwater(boolean original) {
        Entity self = (Entity) (Object) this;

        if (DragonStateProvider.isDragon(self) && self instanceof Player player) {
            return original && !DragonEntity.isConsideredSwimmingForAnimation(player);
        } else {
            return original;
        }
    }

    @Shadow public abstract double getX();
    @Shadow public abstract double getY();
    @Shadow public abstract double getZ();
    @Shadow public abstract boolean hasPassenger(Entity entity);
}