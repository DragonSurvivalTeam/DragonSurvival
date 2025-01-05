package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.EffectHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.*;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
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
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow protected boolean jumping;
    @Shadow protected ItemStack useItem;

    public LivingEntityMixin(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    /** Slightly apply lava swim speed to other entities as well (doesn't include up or down movement) */
    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 1))
    private float dragonSurvival$modifyLavaSwimSpeed(float original) {
        return (float) (original * getAttributeValue(DSAttributes.LAVA_SWIM_SPEED));
    }

    @SuppressWarnings("ConstantValue") // both checks in the if statement are valid
    @Redirect(method = "collectEquipmentChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack dragonSurvival$grantDragonSwordAttributes(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND && (Object) this instanceof Player player) {
            if (DragonStateProvider.isDragon(entity) && ToolUtils.shouldUseDragonTools(player.getMainHandItem())) {
                // Without this the item in the dragon slot for the sword would not grant any of its attributes
                ItemStack sword = ClawInventoryData.getData(player).getContainer().getItem(ClawInventoryData.Slot.SWORD.ordinal());

                if (!sword.isEmpty()) {
                    return sword;
                }
            }
        }

        return getItemBySlot(slot);
    }

    @ModifyReturnValue(at = @At(value = "RETURN"), method = "getPassengerRidingPosition")
    public Vec3 dragonSurvival$getDragonPassengersRidingOffset(Vec3 original) {
        if ((Object) this instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                double height = DragonSizeHandler.calculateDragonHeight(handler, player);

                if (!DragonStateProvider.isDragon(getPassengers().getFirst())) {
                    // Human passenger
                    switch (getPose()) {
                        case FALL_FLYING, SWIMMING, SPIN_ATTACK -> {
                            return original.add(new Vec3(0, (height * 0.65) - 1D, 0));
                        }
                        case CROUCHING -> {
                            return original.add(new Vec3(0, (height * 0.73D) - 2D, 0));
                        }
                        default -> {
                            return original.add(new Vec3(0, (height * 0.66D) - 1.9D, 0));
                        }
                    }
                } else {
                    // Dragon passenger
                    switch (getPose()) {
                        case FALL_FLYING, SWIMMING, SPIN_ATTACK -> {
                            return original.add(new Vec3(0, (height * 0.66) - 0.4D, 0));
                        }
                        case CROUCHING -> {
                            return original.add(new Vec3(0, (height * 0.79D) - 1.7D, 0));
                        }
                        default -> {
                            return original.add(new Vec3(0, (height * 0.72D) - 1.9D, 0));
                        }
                    }
                }
            }
        }

        return original;
    }

    @Unique private int dragonSurvival$getHumanOrDragonUseDuration(int original) {
        if ((Object) this instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler != null && handler.isDragon()) {
                return DragonFoodHandler.getUseDuration(useItem, player);
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
                return (DragonFoodHandler.isEdible(stack, handler.species()) && original != UseAnim.DRINK) ? UseAnim.EAT : original;
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
            newInstance = EffectHandler.modifyEffect(affected, instance, applier);
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

    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot pSlot);
    @Shadow public abstract double getAttributeValue(Holder<Attribute> attribute);
    @Shadow protected abstract float getWaterSlowDown();
}