package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import by.dragonsurvivalteam.dragonsurvival.util.FluidTypeUtil;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(final EntityType<? extends LivingEntity> type, final Level level) {
        super(type, level);
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$disableSuffocationDamage(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (ServerConfig.disableDragonSuffocation && source.is(DamageTypes.IN_WALL) && DragonStateProvider.isDragon(this)) {
            cir.setReturnValue(true);
        }
    }

    /** Disables the mining speed penalty for not being on the ground (if the dragon can swim in the current fluid) */
    @WrapOperation(method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean dragonSurvival$disablePenalty(final Player instance, final Operation<Boolean> original) {
        if (SwimData.getData(instance).canSwimIn(FluidTypeUtil.getEyeFluidType(instance))) {
            return true;
        }

        return original.call(instance);
    }

    /** Prevent the player from moving when casting certain abilities or using emotes */
    @Inject(method = "isImmobile", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$preventMovement(CallbackInfoReturnable<Boolean> callback) {
        if (!isDeadOrDying() && !isSleeping()) {
            MagicData data = MagicData.getData((Player) (Object) this);

            if (data.getCurrentlyCasting() != null && !data.getCurrentlyCasting().value().activation().canMoveWhileCasting()) {
                callback.setReturnValue(true);
            }
        }
    }

    /** Allow treasure blocks to trigger sleep logic */
    @ModifyReturnValue(method = "isSleepingLongEnough", at = @At("RETURN"))
    public boolean dragonSurvival$isSleepingLongEnough(final boolean isSleepingLongEnough) {
        if (isSleepingLongEnough) {
            return true;
        }

        Player player = (Player) (Object) this;
        return DragonStateProvider.isDragon(player) && TreasureRestData.getData(player).canSleep();
    }

    /** Make sure to consider the actual dragon hitbox when doing checks like these */
    @ModifyReturnValue(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At("RETURN"))
    private boolean dragonSurvival$checkDragonHitbox(boolean returnValue, @Local(argsOnly = true) Pose pose) {
        Player self = (Player) (Object) this;

        if (DragonStateProvider.isDragon(self) && !Compat.hasModelSwapOrDoesNotUseModel(self)) {
            return DragonSizeHandler.canPoseFit(self, pose);
        } else {
            return returnValue;
        }
    }
}
