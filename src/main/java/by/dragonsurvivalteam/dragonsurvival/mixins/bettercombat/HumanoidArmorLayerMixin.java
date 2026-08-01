package by.dragonsurvivalteam.dragonsurvival.mixins.bettercombat;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.compat.bettercombat.BetterCombat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>> {
    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$hideArmor(final PoseStack poseStack, final MultiBufferSource buffer, final int light, final T entity, final float limbSwing, final float limbSwingAmount, final float partialTick, final float ageInTicks, final float netHeadYaw, final float headPitch, final CallbackInfo callback) {
        if (entity instanceof Player player && BetterCombat.isAttacking(player) && DragonStateProvider.isDragon(player)) {
            callback.cancel();
        }
    }
}
