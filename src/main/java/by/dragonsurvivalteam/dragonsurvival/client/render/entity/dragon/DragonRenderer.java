package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;


public class DragonRenderer extends GeoEntityRenderer<DragonEntity> {
    public ResourceLocation glowTexture = null;

    public boolean shouldRenderLayers = true;
    public boolean isRenderLayers = false;

    private static final Color RENDER_COLOR = Color.ofRGB(255, 255, 255);
    private static final Color TRANSPARENT_RENDER_COLOR = Color.ofRGBA(1, 1, 1, HunterHandler.MIN_ALPHA);

    public DragonRenderer(final EntityRendererProvider.Context context, final GeoModel<DragonEntity> model) {
        super(context, model);

        getRenderLayers().add(new DragonGlowLayerRenderer(this));
        getRenderLayers().add(new ClawsAndTeethRenderLayer(this));
        getRenderLayers().add(new DragonArmorRenderLayer(this));
        getRenderLayers().add(new DragonItemRenderLayer(this, (bone, animatable) -> {
            if (bone.getName().equals(ClientDragonRenderer.renderItemsInMouth ? "RightItem_jaw" : "RightItem")) {
                return animatable.getMainHandItem();
            } else if (bone.getName().equals(ClientDragonRenderer.renderItemsInMouth ? "LeftItem_jaw" : "LeftItem")) {
                return animatable.getOffhandItem();
            }
            return null;
        }, (bone, animatable) -> null));
    }

    @Override
    public void preRender(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel model, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        Minecraft.getInstance().getProfiler().push("player_dragon");
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }

    @Override
    public void postRender(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel model, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
    public void actuallyRender(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel model, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        Player player = animatable.getPlayer();

        //noinspection DataFlowIssue -> player is present
        if (player == null || player.isSpectator() || player.isInvisibleTo(Minecraft.getInstance().player)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        boolean hasWings = !handler.body().value().canHideWings() || handler.getCurrentStageCustomization().wings;

        if (handler.body() != null) {
            for (String boneName : handler.body().value().bonesToHideForToggle()) {
                GeoBone bone = ClientDragonRenderer.dragonModel.getAnimationProcessor().getBone(boneName);

                if (bone != null) {
                    bone.setHidden(!hasWings);
                }
            }
        }

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }

    @Override // Also used by the layers
    public Color getRenderColor(final DragonEntity animatable, float partialTick, int packedLight) {
        boolean isInvisible = animatable.isInvisible();
        Color color;

        //noinspection DataFlowIssue -> player is not null
        if (isInvisible && !animatable.isInvisibleTo(Minecraft.getInstance().player)) {
            color = TRANSPARENT_RENDER_COLOR;
        } else {
            color = RENDER_COLOR;
        }

        return HunterHandler.modifyAlpha(animatable.getPlayer(), color);
    }
}