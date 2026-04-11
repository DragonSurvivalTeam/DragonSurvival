package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EnchantmentEffectHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnTargetKilled;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.util.Mth;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow protected boolean jumping;
    @Shadow protected ItemStack useItem;

    public LivingEntityMixin(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    /** Happens here so that the trigger can occur after the loot has been dropped */
    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V", shift = At.Shift.AFTER))
    private void dragonSurvival$triggerOnTargetKilled(final DamageSource source, final CallbackInfo callback) {
        OnTargetKilled.trigger((LivingEntity) (Object) this, source);
    }

    /** Slightly apply lava swim speed to other entities as well (doesn't include up or down movement) */
    @ModifyArg(method = "travelInLava", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"))
    private float dragonSurvival$modifyLavaSwimSpeed(float original) {
        return (float) (original * getAttributeValue(DSAttributes.LAVA_SWIM_SPEED));
    }

    @Inject(method = "travelInLava", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$handleLavaSwimming(final Vec3 travelVector, final double baseGravity, final boolean isFalling, final double oldY, final CallbackInfo callback) {
        if (!((Object) this instanceof Player player)) {
            return;
        }

        if (!player.isAffectedByFluids() || !SwimData.getData(player).canSwimIn(NeoForgeMod.LAVA_TYPE.value())) {
            return;
        }

        dragonSurvival$applyVerticalFluidSwimMovement(player, travelVector);

        float slowDown = isSprinting() ? 0.9F : getWaterSlowDown();
        float swimSpeed = 0.05F;
        float swimSpeedModifier = onGround() ? 1.0F : 0.5F;

        if (swimSpeedModifier > 0.0F) {
            slowDown += (0.54600006F - slowDown) * swimSpeedModifier;
            swimSpeed += (player.getSpeed() - swimSpeed) * swimSpeedModifier;
        }

        swimSpeed *= (float) getAttributeValue(DSAttributes.LAVA_SWIM_SPEED);
        moveRelative(swimSpeed, travelVector);
        move(MoverType.SELF, getDeltaMovement());

        Vec3 adjustedMovement = getDeltaMovement();
        if (horizontalCollision && player.onClimbable()) {
            adjustedMovement = new Vec3(adjustedMovement.x, 0.2, adjustedMovement.z);
        }

        adjustedMovement = adjustedMovement.multiply(slowDown, 0.8F, slowDown);
        adjustedMovement = player.getFluidFallingAdjustedMovement(baseGravity, isFalling, adjustedMovement);
        setDeltaMovement(adjustedMovement);

        if (horizontalCollision && isFree(adjustedMovement.x, adjustedMovement.y + 0.6 - getY() + oldY, adjustedMovement.z)) {
            setDeltaMovement(adjustedMovement.x, 0.3, adjustedMovement.z);
        }

        player.calculateEntityAnimation(false);
        callback.cancel();
    }

    private void dragonSurvival$applyVerticalFluidSwimMovement(final Player player, final Vec3 travelVector) {
        boolean isCrouching = player.isCrouching();

        if (!jumping && !isCrouching && travelVector.horizontalDistance() <= 0.05D) {
            return;
        }

        double lookY = player.getLookAngle().y;
        if (!jumping && !isCrouching && Math.abs(lookY) <= 0.1D) {
            return;
        }

        float minSpeed = 0.04F;
        float maxSpeed = 0.12F;
        float verticalSpeed = Mth.lerp((float) Math.abs(Mth.clamp(lookY, -1.0D, 1.0D)), minSpeed, maxSpeed);

        if (isSprinting()) {
            verticalSpeed *= 1.2F;
        }

        if ((jumping && lookY < 0.0D) || (isCrouching && lookY > 0.0D)) {
            lookY *= -1.0D;
            verticalSpeed = minSpeed;
        }

        Vec3 deltaMovement = getDeltaMovement();
        setDeltaMovement(deltaMovement.add(0.0D, (lookY - deltaMovement.y) * Math.abs(verticalSpeed), 0.0D));
    }

    @ModifyExpressionValue(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getEffect(Lnet/minecraft/core/Holder;)Lnet/minecraft/world/effect/MobEffectInstance;"))
    private MobEffectInstance dragonSurvival$disableLevitationWhenTrapped(MobEffectInstance original) {
        if (hasEffect(DSEffects.TRAPPED)) {
            return null;
        }

        return original;
    }

    @ModifyReturnValue(method = "canBeSeenByAnyone", at = @At("RETURN"))
    private boolean dragonSurvival$hasMaxHunterStacks(boolean canBeSeen) {
        if (!canBeSeen) {
            return false;
        }

        return !HunterData.hasMaxHunterStacks((LivingEntity) (Object) this);
    }

    @ModifyExpressionValue(method = "getPassengerRidingPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
    public EntityDimensions dragonSurvival$useCorrectDimensionsForPassengerRidingCalculation(EntityDimensions original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (DragonStateProvider.isDragon(self) && self instanceof Player player) {
            return DragonSizeHandler.calculateDimensions(DragonStateProvider.getData(player), player, DragonSizeHandler.getOverridePose(player));
        } else {
            return original;
        }
    }

    /** There is no event to actually modify the effect when it's being applied */
    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), argsOnly = true)
    private MobEffectInstance dragonSurvival$modifyEffect(final MobEffectInstance instance, final @Local(argsOnly = true) Entity applier) {
        LivingEntity self = (LivingEntity) (Object) this;
        MobEffectInstance newInstance = instance;

        if (self instanceof Player affected) {
            newInstance = EnchantmentEffectHandler.modifyEffect(affected, instance, applier);
        }

        EffectModifications data = self.getExistingData(DSDataAttachments.EFFECT_MODIFICATIONS).orElse(null);

        if (data != null) {
            newInstance = data.modifyEffect(newInstance);
        }

        return newInstance;
    }

    @ModifyReturnValue(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("RETURN"))
    private boolean dragonSurvival$checkSummonRelationship(boolean canAttack, @Local(argsOnly = true, ordinal = 0) final LivingEntity target) {
        if (!canAttack) {
            return false;
        }

        return !SummonedEntities.hasSummonRelationship(this, target);
    }

    @Shadow
    public abstract double getAttributeValue(Holder<Attribute> attribute);

    @Shadow
    public abstract boolean hasEffect(final Holder<MobEffect> effect);

    @Shadow
    public abstract @Nullable MobEffectInstance getEffect(Holder<MobEffect> effect);

    @Shadow
    protected abstract float getWaterSlowDown();
}
