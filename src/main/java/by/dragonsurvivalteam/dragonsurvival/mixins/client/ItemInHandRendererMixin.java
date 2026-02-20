package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    /**
     * In case the item cannot be naturally eaten, replace the animation with the eating animation if we consider it as food
     */
    @ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseAnimation()Lnet/minecraft/world/item/ItemUseAnimation;"))
    private ItemUseAnimation dragonSurvival$dragonRenderArmWithItem(ItemUseAnimation original, @Local(argsOnly = true) AbstractClientPlayer player, @Local(argsOnly = true) ItemStack item) {
        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!DragonFoodHandler.dragonFoodHandlingIsDisabled() && data.isDragon()) {
            return (DragonFoodHandler.isEdible(player, item) && original != ItemUseAnimation.DRINK) ? ItemUseAnimation.EAT : original;
        }

        return original;
    }

    /** If we made an item edible we need to supply the duration it takes to eat the item */
    @ModifyExpressionValue(method = "applyEatTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getUseDuration(Lnet/minecraft/world/entity/LivingEntity;)I"))
    private int dragonSurvival$dragonUseDuration(int original, @Local(argsOnly = true) ItemStack stack) {
        if (!DragonFoodHandler.dragonFoodHandlingIsDisabled() && DragonStateProvider.isDragon(minecraft.player)) {
            return DragonFoodHandler.getUseDuration(stack, minecraft.player, original);
        }

        return original;
    }

    /** Don't render the actual hand ('RenderHandEvent' cannot be used since it would also hide the item) */
    @Inject(at = @At(value = "HEAD"), method = "renderPlayerArm", cancellable = true)
    private void dragonSurvival$skipRender(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, float equippedProgress, float swingProgress, HumanoidArm arm, CallbackInfo ci) {
        if (DragonStateProvider.isDragon(minecraft.player)) {
            ci.cancel();
        }
    }

    /** Don't render the actual hand ('RenderHandEvent' cannot be used since it would also hide the item) */
    @ModifyExpressionValue(method = "renderTwoHandedMap", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isInvisible()Z"))
    private boolean dragonSurvival$skipRenderWhenHoldingMaps(boolean original) {
        return original || DragonStateProvider.isDragon(minecraft.player);
    }
}