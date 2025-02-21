package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin( ItemInHandRenderer.class )
public class MixinItemInHandRenderer{

	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject( at = @At( value = "HEAD" ), method = "applyEatTransform", cancellable = true, expect = 1 )
	public void applyDragonEatTransform(PoseStack p_228398_1_, float p_228398_2_, HumanoidArm p_228398_3_, ItemStack p_228398_4_, CallbackInfo ci){
		if (DragonFoodHandler.disableDragonFoodHandling) {
			return;
		}

		DragonStateProvider.getCap(minecraft.player).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				float f = (float)minecraft.player.getUseItemRemainingTicks() - p_228398_2_ + 1.0F;
				float f1 = f / (float)DragonFoodHandler.getUseDuration(p_228398_4_, dragonStateHandler.getType());
				if(f1 < 0.8F){
					float f2 = Mth.abs(Mth.cos(f / 4.0F * (float)Math.PI) * 0.1F);
					p_228398_1_.translate(0.0D, f2, 0.0D);
				}

				float f3 = 1.0F - (float)Math.pow(f1, 27.0D);
				int i = p_228398_3_ == HumanoidArm.RIGHT ? 1 : -1;
				p_228398_1_.translate(f3 * 0.6F * (float)i, f3 * -0.5F, f3 * 0.0F);
				p_228398_1_.mulPose(Vector3f.YP.rotationDegrees((float)i * f3 * 90.0F));
				p_228398_1_.mulPose(Vector3f.XP.rotationDegrees(f3 * 10.0F));
				p_228398_1_.mulPose(Vector3f.ZP.rotationDegrees((float)i * f3 * 30.0F));

				ci.cancel();
			}
		});
	}

	@Inject( at = @At( value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V", ordinal = 2 ), method = "renderArmWithItem", cancellable = true )
	public void renderDragonArmWithItem(AbstractClientPlayer p_228405_1_, float p_228405_2_, float p_228405_3_, InteractionHand p_228405_4_, float p_228405_5_, ItemStack p_228405_6_, float p_228405_7_, PoseStack p_228405_8_, MultiBufferSource pBuffer, int pCombinedLight, CallbackInfo ci){
		if (DragonFoodHandler.disableDragonFoodHandling) {
			return;
		}

		DragonStateProvider.getCap(minecraft.player).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon() && DragonFoodHandler.isDragonEdible(p_228405_6_.getItem(), dragonStateHandler.getType())){
				applyEatTransform(p_228405_8_, p_228405_2_, p_228405_4_ == InteractionHand.MAIN_HAND ? p_228405_1_.getMainArm() : p_228405_1_.getMainArm().getOpposite(), p_228405_6_);
			}
		});
	}

	@Shadow
	private void applyEatTransform(PoseStack p_228405_8_, float p_228405_2_, HumanoidArm handSide, ItemStack p_228405_6_){
		throw new IllegalStateException("Mixin failed to shadow applyEatTransform()");
	}

	@Inject( at = @At( value = "HEAD" ), method = "renderPlayerArm", cancellable = true )
	private void renderDragonArm(PoseStack p_228401_1_, MultiBufferSource p_228401_2_, int p_228401_3_, float p_228401_4_, float p_228401_5_, HumanoidArm p_228401_6_, CallbackInfo ci){
		if(DragonUtils.isDragon(minecraft.player)){
			ci.cancel();
		}
	}

	@Redirect( at = @At( value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderMapHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/HumanoidArm;)V" ), method = "renderTwoHandedMap" )
	private void removeTwoHandsMapForDragon(ItemInHandRenderer firstPersonRenderer, PoseStack p_228403_1_, MultiBufferSource p_228403_2_, int p_228403_3_, HumanoidArm p_228403_4_){
		if(!DragonUtils.isDragon(minecraft.player)){
			renderMapHand(p_228403_1_, p_228403_2_, p_228403_3_, p_228403_4_);
		}
	}

	@Shadow
	private void renderMapHand(PoseStack p_228403_1_, MultiBufferSource p_228403_2_, int p_228403_3_, HumanoidArm p_228403_4_){
		throw new IllegalStateException("Mixin failed to shadow renderMapHand()");
	}
}