package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.GeoBone;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow @Final private RenderBuffers renderBuffers;

    @ModifyVariable(method = "renderLevel", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"))
    private int dragonSurvival$getTypeColor(final int teamColor, @Local final Entity entity) {
        if (teamColor != /* ChatFormatting#WHITE */ 16777215) {
            // For compatibility use the already modified color (if present)
            return teamColor;
        }

        int color = entity.getExistingData(DSDataAttachments.GLOW).map(GlowData::getColor).orElse(teamColor);

        if (color == GlowData.NO_COLOR) {
            return teamColor;
        }

        return color;
    }

    /** Render the dragon body (except the head) in first person */
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0, shift = At.Shift.BEFORE))
    public void render(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer renderer, LightTexture light, Matrix4f frustum, Matrix4f projection, CallbackInfo callback, @Local PoseStack poseStack) {
        if (camera.isDetached() || !ClientDragonRenderer.renderInFirstPerson || !DragonStateProvider.isDragon(camera.getEntity())) {
            return;
        }

        final GeoBone neckAndHead = ClientDragonRenderer.dragonModel.getAnimationProcessor().getBone("Neck");

        if (neckAndHead != null) {
            neckAndHead.setHidden(true);
        }

        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        boolean renderHitboxes = manager.shouldRenderHitBoxes();

        Vec3 cameraPosition = camera.getPosition();
        double x = cameraPosition.x();
        double y = cameraPosition.y();
        double z = cameraPosition.z();

        MultiBufferSource immediate = renderBuffers.bufferSource();
        manager.setRenderHitBoxes(false);
        renderEntity(camera.getEntity(), x, y, z, deltaTracker.getGameTimeDeltaPartialTick(false), poseStack, immediate);
        manager.setRenderHitBoxes(renderHitboxes);

        if (neckAndHead != null) {
            neckAndHead.setHidden(false);
        }
    }

    @Shadow protected abstract void renderEntity(Entity pEntity, double pCamX, double pCamY, double pCamZ, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource);
}