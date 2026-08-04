package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.compat.damage.DamageContainer;
import by.dragonsurvivalteam.dragonsurvival.common.compat.event.LivingIncomingDamageEvent;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EnchantmentEffectHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnTargetKilled;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Unique private static final ThreadLocal<DamageContainer> dragonSurvival$currentDamage = new ThreadLocal<>();

    @Shadow protected boolean jumping;
    @Shadow protected ItemStack useItem;

    public LivingEntityMixin(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    @Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"), cancellable = true)
    private void dragonSurvival$fireIncomingDamage(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> callback,
                                                   @Share("dragonSurvival$damageContainer") final LocalRef<DamageContainer> containerRef,
                                                   @Local(argsOnly = true) final LocalFloatRef amountRef) {
        DamageContainer container = new DamageContainer(source, amount);
        LivingIncomingDamageEvent event = new LivingIncomingDamageEvent((LivingEntity) (Object) this, container);

        if (MinecraftForge.EVENT_BUS.post(event)) {
            callback.setReturnValue(false);
            return;
        }

        containerRef.set(container);
        amountRef.set(container.getNewDamage());
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void dragonSurvival$trackDamageContainer(final LivingEntity entity, final DamageSource source, final float amount,
                                                     final Operation<Void> original,
                                                     @Share("dragonSurvival$damageContainer") final LocalRef<DamageContainer> containerRef) {
        DamageContainer previous = dragonSurvival$currentDamage.get();
        DamageContainer container = containerRef.get();

        if (container != null) {
            container.setNewDamage(amount);
            dragonSurvival$currentDamage.set(container);
        }

        try {
            original.call(entity, source, amount);
        } finally {
            if (previous == null) {
                dragonSurvival$currentDamage.remove();
            } else {
                dragonSurvival$currentDamage.set(previous);
            }
        }
    }

    @WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float dragonSurvival$modifyArmorReduction(final LivingEntity entity, final DamageSource source, final float amount,
                                                      final Operation<Float> original) {
        float reducedDamage = original.call(entity, source, amount);
        DamageContainer container = dragonSurvival$currentDamage.get();

        if (container == null) {
            return reducedDamage;
        }

        container.setNewDamage(amount);
        container.setReduction(DamageContainer.Reduction.ARMOR, amount - reducedDamage);
        return container.getNewDamage();
    }

    @ModifyConstant(method = "hurt", constant = @Constant(intValue = 20))
    private int dragonSurvival$modifyPostAttackInvulnerability(final int original,
                                                               @Share("dragonSurvival$damageContainer") final LocalRef<DamageContainer> containerRef) {
        DamageContainer container = containerRef.get();
        return container == null ? original : container.getPostAttackInvulnerabilityTicks();
    }

    /** Happens here so that the trigger can occur after the loot has been dropped */
    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;dropAllDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;)V", shift = At.Shift.AFTER))
    private void dragonSurvival$triggerOnTargetKilled(final DamageSource source, final CallbackInfo callback) {
        OnTargetKilled.trigger((LivingEntity) (Object) this, source);
    }

    /** Slightly apply lava swim speed to other entities as well (doesn't include up or down movement) */
    @ModifyArg(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V", ordinal = 1))
    private float dragonSurvival$modifyLavaSwimSpeed(float original) {
        return (float) (original * getAttributeValue(DSAttributes.LAVA_SWIM_SPEED.get()));
    }

    @ModifyConstant(method = "getJumpPower", constant = @Constant(floatValue = 0.42F))
    private float dragonSurvival$useJumpStrengthAttribute(final float original) {
        return (float) ((LivingEntity) (Object) this).getAttributeValue(DSAttributes.JUMP_STRENGTH.get());
    }

    @ModifyExpressionValue(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z", ordinal = 2))
    private boolean dragonSurvival$disableLevitationWhenTrapped(final boolean hasLevitation) {
        if (hasEffect(DSEffects.TRAPPED.get())) {
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

    // FIXME :: 1.21.1 backport issue? -> method does not exist
//    @ModifyExpressionValue(method = "getPassengerRidingPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;"))
//    public EntityDimensions dragonSurvival$useCorrectDimensionsForPassengerRidingCalculation(EntityDimensions original) {
//        LivingEntity self = (LivingEntity) (Object) this;
//        if (DragonStateProvider.isDragon(self) && self instanceof Player player) {
//            return DragonSizeHandler.calculateDimensions(DragonStateProvider.getData(player), player, DragonSizeHandler.getOverridePose(player));
//        } else {
//            return original;
//        }
//    }

    @Unique private int dragonSurvival$getHumanOrDragonUseDuration(final ItemStack stack, int original) {
        if (!DragonFoodHandler.dragonFoodHandlingIsDisabled() && (Object) this instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler != null && handler.isDragon()) {
                return DragonFoodHandler.getUseDuration(stack, player, original);
            }
        }

        return original;
    }

    @ModifyExpressionValue(method = "startUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int replaceUseDurationInStartUsingItem(int original, final InteractionHand hand) {
        return dragonSurvival$getHumanOrDragonUseDuration(((LivingEntity) (Object) this).getItemInHand(hand), original);
    }

    @ModifyExpressionValue(method = "shouldTriggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int replaceUseDurationInShouldTriggerItemUseEffects(int original) {
        return dragonSurvival$getHumanOrDragonUseDuration(useItem, original);
    }

    @ModifyExpressionValue(method = "onSyncedDataUpdated", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration()I"))
    private int replaceUseDurationInSyncedDataUpdated(int original) {
        return dragonSurvival$getHumanOrDragonUseDuration(useItem, original);
    }

    @ModifyExpressionValue(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEdible()Z"))
    private boolean dragonSurvival$eatDragonFood(final boolean original, final Level level, final ItemStack stack) {
        return original || (Object) this instanceof Player player && DragonFoodHandler.isEdible(player, stack);
    }

    @ModifyExpressionValue(method = "addEatEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEdible()Z"))
    private boolean dragonSurvival$applyDragonFoodEffects(final boolean original, final ItemStack stack, final Level level, final LivingEntity entity) {
        return original || entity instanceof Player player && DragonFoodHandler.isEdible(player, stack);
    }

    @ModifyExpressionValue(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/UseAnim;"))
    private UseAnim dragonSurvival$replaceEatAndDrinkAnimation(UseAnim original, ItemStack stack, int amount) {
        if (!DragonFoodHandler.dragonFoodHandlingIsDisabled() && (Object) this instanceof Player player) {
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

        EffectModifications data = AttachmentManager.getExistingData(self, DSDataAttachments.EFFECT_MODIFICATIONS).orElse(null);

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

    // FIXME :: Currently this seems to handle gravity differently (lower jump height / falling in creative mode)
    //          Unsure why - the difference here seems to be that the content of 'LivingEntity#handleRelativeFrictionAndCalculateMovement' happens after applying gravity instead of before (vanilla)
    /** Fixes a bug with vanilla where effects that modify the player's y-velocity were called too late, causing some problems with things like slime blocks.
     * The issue isn't noticeable in vanilla, since vanilla doesn't rely on isOnGround() or not for logic that modifies the player's animations and hitbox.
     * <p>
     * For some more context, the bug would be the following:
     * - The player is on a slime block and crouches
     * - The change in hitbox causes a collision with the slime block, which causes the player to be pushed up
     * - The next tick, the player uses their new upward velocity in the move() function (before gravity is applied), which causes isOnGround() to get set to false since the collision detection uses the upward velocity
     * - The tick after that, the player now has gravity applied, so they fall down, but since we marked isOnGround() to false, we now trigger the slime block again
     * - This causes the player to be pushed up again, which causes the player to be stuck in a loop of being pushed up and down
     * <p>
     * This also potentially fixes issues involving the dragon clipping through ceilings or floors with the levitation effect, as that effect was also applied post move() call in vanilla
     * <p>
     * This is a pretty disruptive mixin, but I'm not sure about the best way here to fix the order of operations here without messing up the vanilla logic
     * */
//    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getBlockPosBelowThatAffectsMyMovement()Lnet/minecraft/core/BlockPos;"), cancellable = true)
//    private void dragonSurvival$fixGravityBeingAppliedTooLateAndClampPosToWorldBorder(final Vec3 travelVector, final CallbackInfo callback, @Local double gravity)
//    {
//        //noinspection ConstantValue -> it's not always true
//        if (!((Object) this instanceof Player player)) {
//            return;
//        }
//
//        DragonStateHandler data = DragonStateProvider.getData(player);
//
//        if (!data.isDragon()) {
//            return;
//        }
//
//        callback.cancel();
//
//        BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
//        float blockFriction = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getFriction(level(), this.getBlockPosBelowThatAffectsMyMovement(), this);
//        float velocityDecay = this.onGround() ? blockFriction * 0.91F : 0.91F;
//
//        // This is where we deviate from vanilla logic. What we are doing here is essentially calling handleRelativeFrictionAndCalculateMovement()
//        // but after handling the relative movement and the climbing logic, we apply the gravity to the y-velocity, then call move()
//        //
//        // Vanilla here would instead apply the gravity to the y-velocity after the move() call, which causes issues with the isOnGround() logic
//        this.moveRelative(this.getFrictionInfluencedSpeed(blockFriction), travelVector);
//        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
//        double yVel = getDeltaMovement().y;
//        if (this.hasEffect(MobEffects.LEVITATION)) {
//            yVel += (0.05 * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - yVel) * 0.2;
//        } else if (!this.level().isClientSide || this.level().hasChunkAt(blockpos)) {
//            yVel -= gravity;
//        } else if (this.getY() > (double)this.level().getMinBuildHeight()) {
//            yVel = -0.1;
//        } else {
//            yVel = 0.0;
//        }
//
//        Vec3 postYModifierMovement = new Vec3(this.getDeltaMovement().x, yVel, this.getDeltaMovement().z);
//        this.setDeltaMovement(postYModifierMovement);
//
//        this.move(MoverType.SELF, this.getDeltaMovement());
//        Vec3 postMoveCallDeltaMovement = this.getDeltaMovement();
//        if ((this.horizontalCollision || this.jumping)
//            && (this.onClimbable() || this.getInBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
//            postMoveCallDeltaMovement = new Vec3(postMoveCallDeltaMovement.x, 0.2, postMoveCallDeltaMovement.z);
//        }
//
//        if (this.shouldDiscardFriction()) {
//            this.setDeltaMovement(postYModifierMovement);
//        } else {
//            this.setDeltaMovement(
//                postMoveCallDeltaMovement.x * (double)velocityDecay,
//                this instanceof FlyingAnimal ? postMoveCallDeltaMovement.y * (double)velocityDecay : postMoveCallDeltaMovement.y * 0.98F,
//                postMoveCallDeltaMovement.z * (double)velocityDecay);
//        }
//
//        // Clamp position to within world border
//        // This is because the player will just clip through the world border due to growth because of how
//        // fudgePositionAfterSizeChange works, so we need to clamp the position here
//        if (!this.level().getWorldBorder().isWithinBounds(this.getBoundingBox()))
//        {
//            double clampedX = Mth.clamp(this.getX(), this.level().getWorldBorder().getMinX(), this.level().getWorldBorder().getMaxX());
//            double clampedZ = Mth.clamp(this.getZ(), this.level().getWorldBorder().getMinZ(), this.level().getWorldBorder().getMaxZ());
//            this.setPos(clampedX, this.getY(), clampedZ);
//        }
//
//        this.calculateEntityAnimation(this instanceof FlyingAnimal);
//    }

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
        boolean isLavaSwimming = swimData.canSwimIn(ForgeMod.LAVA_TYPE.getKey()) && isInLava();

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
        float yModifier = minSpeed + (maxSpeed - minSpeed) * Mth.abs(Mth.clamp(lookY, -1, 1));

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

            swimSpeed *= (float) player.getAttributeValue(DSAttributes.LAVA_SWIM_SPEED.get());
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
    public abstract double getAttributeValue(Attribute attribute);

    @Shadow
    public abstract boolean hasEffect(final MobEffect effect);

    @Shadow
    protected abstract float getWaterSlowDown();

    @Shadow
    public abstract boolean onClimbable();

    @Shadow
    protected abstract Vec3 handleOnClimbable(Vec3 deltaMovement);

    @Shadow
    protected abstract float getFrictionInfluencedSpeed(float friction);

    @Shadow
    public abstract void calculateEntityAnimation(boolean includeHeight);

    @Shadow
    public abstract boolean shouldDiscardFriction();

}
