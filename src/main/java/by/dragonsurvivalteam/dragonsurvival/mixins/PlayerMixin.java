package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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
    public void dragonSurvival$disableSuffocationDamage(DamageSource source, CallbackInfoReturnable<Boolean> callback) {
        if (ServerConfig.disableDragonSuffocation && source == damageSources().inWall() && DragonStateProvider.isDragon(this)) {
            callback.setReturnValue(true);
        }
    }

    /** Disables the mining speed penalty for not being on the ground (if the dragon can swim in the fluid) */
    @WrapOperation(method = "getDigSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean dragonSurvival$disablePenalty(final Player instance, final Operation<Boolean> original) {
        if (SwimData.getData(instance).canSwimIn(instance.getMaxHeightFluidType())) {
            return true;
        }

        return original.call(instance);
    }

    /** Applies any attributes that are from the item being dug with for the getDigSpeed calculations if it is from a tool swap */
    @Inject(method = "getDigSpeed", at = @At(value = "HEAD"))
    private void dragonSurvival$applyAttributesFromToolSwap(final CallbackInfoReturnable<Float> callback, @Local(argsOnly = true) BlockState state) {
        Player self = (Player) (Object) this;
        Pair<ItemStack, Integer> data = ClawToolHandler.getDragonHarvestToolAndSlot(self, state);
        ItemStack dragonHarvestTool = data.getFirst();
        int toolSlot = data.getSecond();

        // Copied from collectEquipmentChanges() in LivingEntity.java
        if (toolSlot != -1) {
                dragonHarvestTool.forEachModifier(EquipmentSlot.MAINHAND, (attributeHolder, attributeModifier) -> {
                    AttributeInstance attributeinstance = self.getAttributes().getInstance(attributeHolder);
                    if (attributeinstance != null) {
                        attributeinstance.removeModifier(attributeModifier.id());
                        attributeinstance.addTransientModifier(attributeModifier);
                    }

                    if (this.level() instanceof ServerLevel serverlevel) {
                        EnchantmentHelper.runLocationChangedEffects(serverlevel, dragonHarvestTool, this, EquipmentSlot.MAINHAND);
                    }
                });
        }
    }

    /** Removes any attributes that are from the item being dug with for the getDigSpeed calculations if it is from a tool swap */
    @Inject(method = "getDigSpeed", at = @At(value = "TAIL"))
    private void dragonSurvival$removeAttributesFromToolSwap(final CallbackInfoReturnable<Float> callback, @Local(argsOnly = true) BlockState state) {
        Player self = (Player) (Object) this;
        Pair<ItemStack, Integer> data = ClawToolHandler.getDragonHarvestToolAndSlot(self, state);
        ItemStack dragonHarvestTool = data.getFirst();
        int toolSlot = data.getSecond();

        // Copied from collectEquipmentChanges() in LivingEntity.java
        if (toolSlot != -1) {
            dragonHarvestTool.forEachModifier(EquipmentSlot.MAINHAND, (attributeHolder, attributeModifier) -> {
                    AttributeInstance attributeinstance = self.getAttributes().getInstance(attributeHolder);
                    if (attributeinstance != null) {
                        attributeinstance.removeModifier(attributeModifier);
                    }

                    EnchantmentHelper.stopLocationBasedEffects(dragonHarvestTool, this, EquipmentSlot.MAINHAND);
                });
        }
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

        if (DragonStateProvider.isDragon(self) && !Compat.hasModelSwap(self)) {
            return DragonSizeHandler.canPoseFit(self, pose);
        } else {
            return returnValue;
        }
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isSwimming()Z"))
    private boolean dragonSurvival$consideredSwimmingEvenWhenGroundedInWater(boolean isSwimming) {
        if (isSwimming) {
            return true;
        }

        Player self = (Player) (Object) this;
        return DragonStateProvider.isDragon(self) && DragonEntity.isConsideredSwimmingForAnimation(self);
    }
}