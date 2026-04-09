package by.dragonsurvivalteam.dragonsurvival.client.render.blocks;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.HelmetBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.HelmetBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class HelmetEntityRenderer<T extends HelmetBlockEntity> implements BlockEntityRenderer<T, HelmetEntityRenderer.State> {
    private static final Map<HelmetBlock, Identifier> TEXTURE_BY_TYPE = Map.of(
        DSBlocks.GRAY_KNIGHT_HELMET.value(), DragonSurvival.res("textures/block/gray_knight_helmet.png"),
        DSBlocks.GOLDEN_KNIGHT_HELMET.value(), DragonSurvival.res("textures/block/golden_knight_helmet.png"),
        DSBlocks.BLACK_KNIGHT_HELMET.value(), DragonSurvival.res("textures/block/black_knight_helmet.png")
    );

    private final SkullModel humanoidHeadModel;

    public HelmetEntityRenderer(final BlockEntityRendererProvider.Context context) {
        humanoidHeadModel = new SkullModel(context.bakeLayer(ModelLayers.PLAYER_HEAD));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
        final T blockEntity,
        final State state,
        final float partialTicks,
        final Vec3 cameraPosition,
        final ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.yRot = -22.5F * blockEntity.getBlockState().getValue(HelmetBlock.ROTATION);
        state.renderType = RenderTypes.entityCutoutZOffset(
            TEXTURE_BY_TYPE.getOrDefault(blockEntity.getBlockState().getBlock(), DragonSurvival.MISSING_TEXTURE)
        );
    }

    @Override
    public void submit(final State state, final PoseStack poseStack, final SubmitNodeCollector nodeCollector, final CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        SkullBlockRenderer.submitSkull(0.0F, poseStack, nodeCollector, state.lightCoords, humanoidHeadModel, state.renderType, 0, state.breakProgress);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState {
        public float yRot;
        public RenderType renderType = RenderTypes.entityCutoutZOffset(DragonSurvival.MISSING_TEXTURE);
    }
}
