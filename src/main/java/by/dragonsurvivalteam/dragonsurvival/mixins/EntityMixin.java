package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.compat.attachments.AttachmentType;
import by.dragonsurvivalteam.dragonsurvival.common.compat.event.EntityTickEvent;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EntityScale;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentStorage;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonRidingHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(Entity.class)
public abstract class EntityMixin implements AttachmentStorage {
    @Shadow private EntityDimensions dimensions;
    @Unique private final Map<AttachmentType<?>, Object> dragonSurvival$attachments = new IdentityHashMap<>();

    @Override
    public Map<AttachmentType<?>, Object> dragonSurvival$getAttachments() {
        return dragonSurvival$attachments;
    }

    @Inject(method = "saveWithoutId", at = @At("RETURN"))
    private void dragonSurvival$saveAttachments(final CompoundTag tag, final CallbackInfoReturnable<CompoundTag> callback) {
        AttachmentManager.writeEntityAttachments((Entity) (Object) this, tag);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void dragonSurvival$loadAttachments(final CompoundTag tag, final CallbackInfo callback) {
        AttachmentManager.readEntityAttachments((Entity) (Object) this, tag);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void dragonSurvival$postEntityTick(final CallbackInfo callback) {
        MinecraftForge.EVENT_BUS.post(new EntityTickEvent.Post((Entity) (Object) this));
    }

    /** Make sure to consider the actual dragon hitbox when doing checks like these */
    @ModifyReturnValue(method = "canEnterPose", at = @At("RETURN"))
    private boolean dragonSurvival$checkDragonHitbox(boolean canEnterPose, final Pose pose) {
        //noinspection ConstantValue -> statement is not always true
        if (!((Object) this instanceof Player player)) {
            return canEnterPose;
        }

        if (DragonStateProvider.isDragon(player) && !Compat.hasModelSwapOrDoesNotUseModel(player)) {
            return DragonSizeHandler.canPoseFit(player, pose);
        } else {
            return canEnterPose;
        }
    }

    /** Correctly position the passenger when riding a dragon */
    @WrapOperation(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity$MoveFunction;accept(Lnet/minecraft/world/entity/Entity;DDD)V"))
    public void dragonSurvival$positionRider(final Entity.MoveFunction instance, final Entity entity, final double x, final double y, final double z, final Operation<Void> original) {
        Entity mount = (Entity) (Object) this;

        if (mount instanceof Player player && DragonStateProvider.isDragon(player)) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            MovementData movement = MovementData.getData(player);

            // Pose changes shrink the collision box, but the seat must remain anchored to the dragon model.
            EntityDimensions dragonDims = DragonSizeHandler.calculateDimensions(handler, player,  DragonSizeHandler.getOverridePose(player));
            // TODO :: This is a bad approximation to get the right mounting height, not really sure what is best to do here without having to tweak the 1.21.1 values for mounting offsets
            Vec3 pos = new Vec3(0, dragonDims.height * 0.85D, 0);
            Vec3 mountingOffset = Vec3.ZERO;

            if (handler.body().value().mountingOffsets().isPresent()) {
                DragonBody.MountingOffsets mountingOffsets = handler.body().value().mountingOffsets().get();
                mountingOffset = DragonStateProvider.isDragon(entity) ? mountingOffsets.dragonOffset() : mountingOffsets.humanOffset();
                Vec3 offsetPerScaleAboveOne = mountingOffsets.scale();
                float scale = EntityScale.get(player);
                mountingOffset = mountingOffset.add(offsetPerScaleAboveOne.scale(scale - 1));

                pos = pos.add(mountingOffset);
            }

            pos = pos.xRot((float) Math.toRadians(movement.prevXRot * 1.5)).zRot(-(float) Math.toRadians(movement.prevZRot * 90));
            pos = pos.multiply(1, Math.signum(pos.y), 1);
            // The mounting offset brackets the pitch/roll transform so the rider stays aligned with the model origin.
            pos = pos.add(mountingOffset).yRot(-(float) Math.toRadians(movement.bodyYawLastFrame));

            original.call(instance, entity, mount.getX() + pos.x, mount.getY() + pos.y, mount.getZ() + pos.z);
        } else if (DragonStateProvider.isDragon(entity) && !DragonStateProvider.isDragon(mount)) {
            // Handle dragon riding normal mounts (e.g. boats)
            // The vanilla player hitbox actually clips through most mounts, but the dragon player does not.
            // So we need to push it up such that it meets the point at which the vanilla player's actual model starts
            Vec3 offset = DragonRidingHandler.getMountingOffsetForEntity(mount);
            original.call(instance, entity, x + offset.x, y + offset.y, z + offset.z);
        } else {
            original.call(instance, entity, x, y, z);
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
        if (DragonStateProvider.isDragon(passenger)) {
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

    /**
     * Prevent dragons from riding certain vehicles
     */
    @ModifyReturnValue(method = "canRide", at = @At(value = "RETURN"))
    private boolean dragonSurvival$canRide(boolean canRide, final Entity mount) {
        if (!canRide) {
            return false;
        }

        //noinspection ConstantValue -> the check is valid
        if (ServerConfig.limitedRiding && DragonStateProvider.isDragon((Entity) (Object) this) && /* Still allow riding dragons */ !DragonStateProvider.isDragon(mount)) {
            return mount.getType().is(DSEntityTypeTags.VEHICLE_WHITELIST);
        }

        return canRide;
    }

    /** To just skip rendering entirely instead of rendering with a 0 alpha value */
    @ModifyReturnValue(method = "isInvisible", at = @At("RETURN"))
    private boolean dragonSurvival$enableHunterStacksInvisibility(boolean isInvisible) {
        if (isInvisible) {
            return true;
        }

        //noinspection ConstantValue -> check is valid
        if ((Object) this instanceof LivingEntity entity && HunterData.hasMaxHunterStacks(entity)) {
            // With max. stacks the visibility value is set to 0 anyway so this shouldn't affect actual gameplay features
            return HunterHandler.calculateAlpha(entity) == 0;
        }

        return false;
    }

    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean dragonSurvival$caveDragonFireImmunity(boolean isFireImmune) {
        if (isFireImmune) {
            return true;
        }

        Entity self = (Entity) (Object) this;
        return AttachmentManager.getExistingData(self, DSDataAttachments.DAMAGE_MODIFICATIONS)
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

    // TODO :: maybe handle this in movement goals or somewhere else so they can still be pushed by game mechanics?
    @ModifyReturnValue(method = "getDeltaMovement", at = @At("RETURN"))
    private Vec3 dragonSurvival$handleSummonStay(final Vec3 deltaMovement) {
        Entity self = (Entity) (Object) this;

        if (AttachmentManager.getExistingData(self, DSDataAttachments.SUMMON).map(data -> data.movementBehaviour == SummonedEntities.MovementBehaviour.STAY).orElse(false)) {
            return Vec3.ZERO;
        }

        return deltaMovement;
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
    private void dragonSurvival$fudgePositionAfterDragonSizeChange(final CallbackInfo callback, @Local(ordinal = 0) final EntityDimensions currentDimension) {
        if ((Object) this instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon() || Compat.hasModelSwapOrDoesNotUseModel(player)) {
                return;
            }

            if (handler.shouldFudgePosition) {
                DragonSizeHandler.fudgePositionAfterSizeChange(player, currentDimension, dimensions);
            }

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

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract boolean hasPassenger(Entity entity);
}
