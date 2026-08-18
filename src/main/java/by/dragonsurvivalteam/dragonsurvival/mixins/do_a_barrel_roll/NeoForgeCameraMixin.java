package by.dragonsurvivalteam.dragonsurvival.mixins.do_a_barrel_roll;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import nl.enjarai.doabarrelroll.api.RollCamera;
import nl.enjarai.doabarrelroll.api.RollEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Replaces Do a Barrel Roll's Fabric camera hook with its NeoForge three-axis equivalent. */
@Mixin(Camera.class)
public abstract class NeoForgeCameraMixin implements RollCamera {
    @Shadow
    private Entity entity;

    @Shadow
    private float roll;

    @Unique private boolean dragonSurvival$isRolling;

    @Unique private float dragonSurvival$lastRollBack;

    @Unique private float dragonSurvival$rollBack;

    @Inject(
        method = "tick",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Camera;eyeHeight:F",
            ordinal = 0
        )
    )
    private void dragonSurvival$interpolateRoll(final CallbackInfo callbackInfo) {
        if (!((RollEntity) entity).doABarrelRoll$isRolling()) {
            dragonSurvival$lastRollBack = dragonSurvival$rollBack;
            dragonSurvival$rollBack *= 0.5F;
        }
    }

    @Inject(method = "alignWithEntity", at = @At("HEAD"))
    private void dragonSurvival$capturePartialTick(
        final float partialTick,
        final CallbackInfo callbackInfo,
        @Share("partialTick") final LocalFloatRef partialTickRef
    ) {
        partialTickRef.set(partialTick);
        dragonSurvival$isRolling = ((RollEntity) entity).doABarrelRoll$isRolling();
    }

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void dragonSurvival$updateRollBack(final float partialTick, final CallbackInfo callbackInfo) {
        if (dragonSurvival$isRolling) {
            dragonSurvival$rollBack = roll;
            dragonSurvival$lastRollBack = roll;
        }
    }

    @ModifyArg(
        method = "alignWithEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setRotation(FFF)V",
            ordinal = 0
        ),
        index = 2
    )
    private float dragonSurvival$applyRoll(
        final float original,
        @Share("partialTick") final LocalFloatRef partialTickRef
    ) {
        if (dragonSurvival$isRolling) {
            return original + ((RollEntity) entity).doABarrelRoll$getRoll(partialTickRef.get());
        }

        return original + Mth.lerp(partialTickRef.get(), dragonSurvival$lastRollBack, dragonSurvival$rollBack);
    }

    @ModifyArg(
        method = "alignWithEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;setRotation(FFF)V",
            ordinal = 1
        ),
        index = 2
    )
    private float dragonSurvival$applyMirroredRoll(
        final float original,
        @Share("partialTick") final LocalFloatRef partialTickRef
    ) {
        if (dragonSurvival$isRolling) {
            return original - ((RollEntity) entity).doABarrelRoll$getRoll(partialTickRef.get());
        }

        return original - Mth.lerp(partialTickRef.get(), dragonSurvival$lastRollBack, dragonSurvival$rollBack);
    }

    @Override
    public float doABarrelRoll$getRoll() {
        return roll;
    }
}
