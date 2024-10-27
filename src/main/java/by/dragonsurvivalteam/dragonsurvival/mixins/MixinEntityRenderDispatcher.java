package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin( EntityRenderDispatcher.class )
public class MixinEntityRenderDispatcher{
	@Inject(method = "renderShadow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;"))
	private static void renderShadowOffset(PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight, float partialTicks, LevelReader level, float size, CallbackInfo ci){
		if(DragonStateProvider.isDragon(entity)) {
			Vector3f offset = Functions.getDragonCameraOffset(entity).negate();
			poseStack.pushPose();
			poseStack.translate(offset.x(), offset.y(), offset.z());
		}
	}

	// FIXME :: not exactly safe (other mixins could cause this to not run and cause an hard-to-debug pose stack is empty crash)
	@Inject(method = "renderShadow", at = @At(value = "TAIL"))
	private static void renderShadowOffsetEnd(PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight, float partialTicks, LevelReader level, float size, CallbackInfo ci){
		if(DragonStateProvider.isDragon(entity)) {
			poseStack.popPose();
		}
	}

	// TODO: Dragon shadows disappear if you are too big (since you are too far from the ground). To fix this we would need to mixin to the weight calculation in renderShadow.
}