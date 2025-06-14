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
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
    @Shadow public abstract boolean onClimbable();

    @Shadow protected abstract Vec3 handleOnClimbable(Vec3 deltaMovement);

    @Shadow protected abstract float getFrictionInfluencedSpeed(float friction);

    @Shadow public abstract void calculateEntityAnimation(boolean includeHeight);

    @Shadow public abstract boolean shouldDiscardFriction();

    @Shadow @Nullable public abstract MobEffectInstance getEffect(Holder<MobEffect> effect);

    @Shadow public abstract Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 deltaMovement, float friction);

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
    @Inject(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getBlockPosBelowThatAffectsMyMovement()Lnet/minecraft/core/BlockPos;"), cancellable = true)
    private void dragonSurvival$fixGravityBeingAppliedTooLate(final Vec3 travelVector, final CallbackInfo callback, @Local double gravity)
    {
        //noinspection ConstantValue -> it's not always true
        if (!((Object) this instanceof Player player)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return;
        }

        callback.cancel();

        BlockPos blockpos = this.getBlockPosBelowThatAffectsMyMovement();
        float blockFriction = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getFriction(level(), this.getBlockPosBelowThatAffectsMyMovement(), this);
        float velocityDecay = this.onGround() ? blockFriction * 0.91F : 0.91F;

        // This is where we deviate from vanilla logic. What we are doing here is essentially calling handleRelativeFrictionAndCalculateMovement()
        // but after handling the relative movement and the climbing logic, we apply the gravity to the y-velocity, then call move()
        //
        // Vanilla here would instead apply the gravity to the y-velocity after the move() call, which causes issues with the isOnGround() logic
        this.moveRelative(this.getFrictionInfluencedSpeed(blockFriction), travelVector);
        this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
        double yVel = getDeltaMovement().y;
        if (this.hasEffect(MobEffects.LEVITATION)) {
            yVel += (0.05 * (double)(this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - yVel) * 0.2;
        } else if (!this.level().isClientSide || this.level().hasChunkAt(blockpos)) {
            yVel -= gravity;
        } else if (this.getY() > (double)this.level().getMinBuildHeight()) {
            yVel = -0.1;
        } else {
            yVel = 0.0;
        }

        Vec3 postYModifierMovement = new Vec3(this.getDeltaMovement().x, yVel, this.getDeltaMovement().z);
        this.setDeltaMovement(postYModifierMovement);

        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 postMoveCallDeltaMovement = this.getDeltaMovement();
        if ((this.horizontalCollision || this.jumping)
            && (this.onClimbable() || this.getInBlockState().is(Blocks.POWDER_SNOW) && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
            postMoveCallDeltaMovement = new Vec3(postMoveCallDeltaMovement.x, 0.2, postMoveCallDeltaMovement.z);
        }

        if (this.shouldDiscardFriction()) {
            this.setDeltaMovement(postYModifierMovement);
        } else {
            this.setDeltaMovement(
                postMoveCallDeltaMovement.x * (double)velocityDecay,
                this instanceof FlyingAnimal ? postMoveCallDeltaMovement.y * (double)velocityDecay : postMoveCallDeltaMovement.y * 0.98F,
                postMoveCallDeltaMovement.z * (double)velocityDecay);
        }

        this.calculateEntityAnimation(this instanceof FlyingAnimal);
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