package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
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
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 1))
    private float dragonSurvival$modifyLavaSwimSpeed(float original) {
        return (float) (original * getAttributeValue(DSAttributes.LAVA_SWIM_SPEED));
    }

    @ModifyExpressionValue(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/core/Holder;)Z", ordinal = 2))
    private boolean dragonSurvival$disableLevitationWhenTrapped(final boolean hasLevitation) {
        if (hasEffect(DSEffects.TRAPPED)) {
            return false;
        }

        return hasLevitation;
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

    @Unique private int dragonSurvival$getHumanOrDragonUseDuration(int original) {
        if ((Object) this instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler != null && handler.isDragon()) {
                return DragonFoodHandler.getUseDuration(useItem, player, original);
            }
        }

        return original;
    }

    @ModifyExpressionValue(method = "shouldTriggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int replaceUseDurationInShouldTriggerItemUseEffects(int original) {
        return dragonSurvival$getHumanOrDragonUseDuration(original);
    }

    @ModifyExpressionValue(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int replaceUseDurationInSyncedDataUpdated(int original) {
        return dragonSurvival$getHumanOrDragonUseDuration(original);
    }

    @ModifyExpressionValue(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
    private UseAnim dragonSurvival$replaceEatAndDrinkAnimation(UseAnim original, ItemStack stack, int amount) {
        if ((Object) this instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                return (DragonFoodHandler.isEdible(player, stack) && original != UseAnim.DRINK) ? UseAnim.EAT : original;
            }
        }

        return original;
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

    /** Enable cave dragons to properly swim in lava and also enables properly swimming up or down (for water and lava) */
    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;", shift = At.Shift.BY, by = 2), cancellable = true)
    private void dragonSurvival$handleDragonSwimming(final Vec3 travelVector, final CallbackInfo callback, @Local double gravity, @Local final FluidState fluidState) {
        //noinspection ConstantValue -> it's not always true
        if (!((Object) this instanceof Player player)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return;
        }

        SwimData swimData = SwimData.getData(player);
        boolean isLavaSwimming = swimData.canSwimIn(NeoForgeMod.LAVA_TYPE.getKey()) && isInLava();

        if (!isLavaSwimming && !isInWater() || !player.isAffectedByFluids() || player.canStandOnFluid(fluidState)) {
            return;
        }

        boolean isCrouching = player.isCrouching();
        boolean isFalling = getDeltaMovement().y <= 0;

        // Don't move the player up or down if they're not currently moving
        if (jumping || isCrouching || travelVector.horizontalDistance() > 0.05) {
            float lookY = (float) getLookAngle().y;

            float minSpeed = 0.04f;
            float maxSpeed = 0.12f;

            // Speed increase depending on how much the player looks up or down
            float yModifier = minSpeed + (maxSpeed - minSpeed) * Mth.abs(Math.clamp(lookY, -1, 1));

            if (isSprinting()) {
                yModifier *= 1.2f;
            }

            if (jumping || isCrouching || Math.abs(lookY) > 0.1) {
                // Jumping should always result in going up and crouching should always result in going down
                if (jumping && lookY < 0 || isCrouching && lookY > 0) {
                    lookY *= -1; // Reverse direction of movement
                    yModifier = minSpeed; // Since we are moving in the opposite direction we're looking, use the minimum speed bonus
                }

                // Move the player up or down, depending on where they look
                Vec3 deltaMovement = getDeltaMovement();
                setDeltaMovement(deltaMovement.add(0, (lookY - deltaMovement.y) * Mth.abs(yModifier), 0));
            }
        }

        if (isLavaSwimming) {
            double oldY = getY();
            float speedModifier = isSprinting() ? 0.9f : getWaterSlowDown();
            float swimSpeed = 0.05f; // Vanilla swim speed for water is 0.02
            float swimSpeedModifier = 1; // Max. value of 'WATER_MOVEMENT_EFFICIENCY' attribute

            // The rest is mostly a copy of 'LivingEntity#travel' water swim logic
            if (!onGround()) {
                swimSpeedModifier *= 0.5f;
            }

            if (swimSpeedModifier > 0) {
                speedModifier += (0.54600006f - speedModifier) * swimSpeedModifier;
                swimSpeed += (player.getSpeed() - swimSpeed) * swimSpeedModifier;
            }

            if (player.hasEffect(MobEffects.DOLPHINS_GRACE)) {
                speedModifier = 0.96f;
            }

            swimSpeed *= (float) player.getAttributeValue(DSAttributes.LAVA_SWIM_SPEED);
            moveRelative(swimSpeed, travelVector);
            move(MoverType.SELF, getDeltaMovement());
            Vec3 newMovement = getDeltaMovement();

            if (horizontalCollision && player.onClimbable()) {
                newMovement = new Vec3(newMovement.x, 0.2, newMovement.z);
            }

            setDeltaMovement(newMovement.multiply(speedModifier, 0.8, speedModifier));
            Vec3 adjustedMovement = player.getFluidFallingAdjustedMovement(gravity, isFalling, getDeltaMovement());
            setDeltaMovement(adjustedMovement);

            if (horizontalCollision && isFree(adjustedMovement.x, adjustedMovement.y + 0.6 - getY() + oldY, adjustedMovement.z)) {
                setDeltaMovement(adjustedMovement.x, 0.3, adjustedMovement.z);
            }

            player.calculateEntityAnimation(false);
            callback.cancel();
        }
    }

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);

    @Shadow
    public abstract double getAttributeValue(Holder<Attribute> attribute);

    @Shadow
    public abstract boolean hasEffect(final Holder<MobEffect> effect);

    @Shadow
    protected abstract float getWaterSlowDown();
}