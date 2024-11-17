package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.objects.DragonMovementData;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin( Entity.class )
public abstract class MixinEntity extends net.minecraftforge.common.capabilities.CapabilityProvider<Entity>{
	protected MixinEntity(Class<Entity> baseClass){
		super(baseClass);
	}

	@Inject( at = @At( value = "HEAD" ), method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", cancellable = true )
	private void positionRider(Entity entity, Entity.MoveFunction move, CallbackInfo callbackInfo){
		Object self = this;

		if(DragonUtils.isDragon((Entity) self)){
			if(hasPassenger(entity)) {
				if ((Object)this instanceof Player player && entity instanceof Player passenger) {
					DragonMovementData md = DragonUtils.getHandler((Entity) self).getMovementData();
					double heightOffset = -0.2 * this.getPassengersRidingOffset();
					Vec3 offsetFromBb = new Vec3(0, heightOffset, -1.5 * player.getBbWidth());
					Vec3 offsetFromCenter = new Vec3(0, this.getPassengersRidingOffset() - heightOffset, 0);
					offsetFromCenter = offsetFromCenter.xRot((float) Math.toRadians(md.prevXRot * 1.5)).zRot(-(float) Math.toRadians(md.prevZRot * 90));
					offsetFromCenter = offsetFromCenter.multiply(1, Math.signum(offsetFromCenter.y), 1);
					Vec3 totalOffset = offsetFromCenter.add(offsetFromBb).yRot(-(float) Math.toRadians(md.bodyYawLastFrame));
					Vec3 passPos = player.position().add(totalOffset);
					move.accept(passenger, passPos.x(), passPos.y(), passPos.z());

					((Entity)(Object)this).onPassengerTurned(passenger);
					callbackInfo.cancel();
				}
			}
		}
	}
	
	@Inject(method = "onPassengerTurned(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
	private void onPassengerTurned(Entity passenger, CallbackInfo callbackInfo) {
		if (passenger instanceof Player player && player.getVehicle() != null && DragonUtils.getHandler(player.getVehicle()).isDragon() && player.level().isClientSide()) {
			this.clampRotation(passenger);
		}
	}
	
	@Unique
	private void clampRotation(Entity passenger) {
		Entity self = (Entity)(Object) this;
		DragonStateHandler selfHandler = DragonUtils.getHandler(self);
		DragonMovementData selfmd = selfHandler.getMovementData();
		if (DragonUtils.isDragon(passenger)) {
			DragonStateHandler handler = DragonUtils.getHandler(passenger);
			DragonMovementData md = handler.getMovementData();
			float facing = (float) Mth.wrapDegrees(passenger.getYRot() - selfmd.bodyYawLastFrame);
			float facingClamped = Mth.clamp(facing, -150.0F, 150.0F);
			passenger.yRotO += facingClamped - facing + self.yRotO;
			md.bodyYaw = selfmd.bodyYawLastFrame;
			md.headYaw = -facing;
			passenger.setYRot((float) (passenger.getYRot() + facingClamped - facing + (ClientDragonRender.rotateCameraWithDragon ? (selfmd.bodyYawLastFrame - selfmd.bodyYaw) : 0)));
			if (passenger instanceof DragonEntity de) {
				de.prevZRot = ((DragonEntity) self).prevZRot;
			}
		}
		else {
			float facing = (float) Mth.wrapDegrees(passenger.getYRot() - selfmd.bodyYawLastFrame);
			float facingClamped = Mth.clamp(facing, -120.0F, 120.0F);
			passenger.yRotO += facingClamped - facing + self.yRotO;
			passenger.setYBodyRot((float) (passenger.getYRot() + facingClamped - facing + (ClientDragonRender.rotateCameraWithDragon ? (selfmd.bodyYawLastFrame - selfmd.bodyYaw) : 0)));
			passenger.setYRot((float) (passenger.getYRot() + facingClamped - facing + (ClientDragonRender.rotateCameraWithDragon ? (selfmd.bodyYawLastFrame - selfmd.bodyYaw) : 0)));
			passenger.setYHeadRot(passenger.getYRot());
		}
	}

	@Shadow
	public boolean hasPassenger(Entity p_184196_1_){
		throw new IllegalStateException("Mixin failed to shadow hasPassenger()");
	}

	@Shadow
	public double getPassengersRidingOffset(){
		throw new IllegalStateException("Mixin failed to shadow getPassengersRidingOffset()");
	}

	@Shadow
	public double getX(){
		throw new IllegalStateException("Mixin failed to shadow getX()");
	}

	@Shadow
	public double getY(){
		throw new IllegalStateException("Mixin failed to shadow getY()");
	}

	@Shadow
	public double getZ(){
		throw new IllegalStateException("Mixin failed to shadow getZ()");
	}

	@Inject( at = @At( value = "HEAD" ), method = "Lnet/minecraft/world/entity/Entity;displayFireAnimation()Z", cancellable = true )
	private void hideCaveDragonFireAnimation(CallbackInfoReturnable<Boolean> ci){
		DragonStateProvider.getCap((Entity)(Object)this).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon() && Objects.equals(dragonStateHandler.getType(), DragonTypes.CAVE)){
				ci.setReturnValue(false);
			}
		});
	}

	@Inject( at = @At( value = "HEAD" ), method = "Lnet/minecraft/world/entity/Entity;getPassengersRidingOffset()D", cancellable = true )
	public void getDragonPassengersRidingOffset(CallbackInfoReturnable<Double> ci){
		if(DragonUtils.isDragon((Entity)(Object)this)){
			if (!DragonUtils.isDragon(((Entity)(Object)this).getPassengers().get(0))) { // Human
				double height = DragonSizeHandler.getDragonHeight((Player)(Object)this);
				switch(((Entity)(Object)this).getPose()){
					case FALL_FLYING, SWIMMING, SPIN_ATTACK -> ci.setReturnValue(height * 0.6D);
					case CROUCHING -> ci.setReturnValue(height * 0.45D);
					default -> ci.setReturnValue(height * 0.5D);
				}
			} else { // Dragon
				double height = DragonSizeHandler.getDragonHeight((Player)(Object)this);
				switch(((Entity)(Object)this).getPose()){
					case FALL_FLYING, SWIMMING, SPIN_ATTACK -> ci.setReturnValue(height * 0.66D);
					case CROUCHING -> ci.setReturnValue(height * 0.61D);
					default -> ci.setReturnValue(height * 0.66D);
				}
			}
		}
	}

	@Inject( at = @At( value = "HEAD" ), method = "Lnet/minecraft/world/entity/Entity;isVisuallyCrawling()Z", cancellable = true )
	public void isDragonVisuallyCrawling(CallbackInfoReturnable<Boolean> ci){
		if(DragonUtils.isDragon((Entity)(Object)this)){
			ci.setReturnValue(false);
		}
	}

	@Inject( at = @At( value = "RETURN" ), method = "canRide", cancellable = true )
	public void canRide(Entity entity, CallbackInfoReturnable<Boolean> ci){
		if(ci.getReturnValue() && DragonUtils.isDragon((Entity)(Object)this) && !DragonUtils.isDragon(entity)){
			if(ServerConfig.ridingBlacklist){
				ci.setReturnValue(ServerConfig.allowedVehicles.contains(ResourceHelper.getKey(entity).toString()));
			}
		}
	}

	@Redirect( method = "canEnterPose(Lnet/minecraft/world/entity/Pose;)Z", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getBoundingBoxForPose(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/phys/AABB;" ) )
	public AABB dragonPoseBB(Entity entity, Pose pose){
		if(DragonUtils.isDragon(entity) && ServerConfig.sizeChangesHitbox){
			boolean squish = DragonUtils.getDragonBody(entity) != null ? DragonUtils.getDragonBody(entity).isSquish() : false;
			double heightMult = 1.0;
			if (DragonUtils.getDragonBody(entity) != null) {
				squish = DragonUtils.getDragonBody(entity).isSquish();
				heightMult = DragonUtils.getDragonBody(entity).getHeightMult();
			}
			double size = DragonUtils.getHandler(entity).getSize();
			double height = DragonSizeHandler.calculateModifiedHeight(DragonSizeHandler.calculateDragonHeight(size, ServerConfig.hitboxGrowsPastHuman), pose, ServerConfig.sizeChangesHitbox, squish) * heightMult;
			double width = DragonSizeHandler.calculateDragonWidth(size, ServerConfig.hitboxGrowsPastHuman) / 2.0D;
			return DragonSizeHandler.calculateDimensions(width, height).makeBoundingBox(entity.position());
		}else
			return getBoundingBoxForPose(pose);
	}

	@Shadow
	protected AABB getBoundingBoxForPose(Pose pose){
		throw new IllegalStateException("Mixin failed to shadow getBoundingBoxForPose()");
	}
}