package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.item.HunterItemLayerAccess;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public class ItemStackRenderStateLayerRenderStateMixin implements HunterItemLayerAccess {
    @Unique private float dragonSurvival$hunterItemAlpha = HunterHandler.UNMODIFIED;

    @Redirect(
        method = "submit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"
        )
    )
    private void dragonSurvival$submitHunterTranslucentItem(
        final SubmitNodeCollector submitNodeCollector,
        final PoseStack poseStack,
        final ItemDisplayContext displayContext,
        final int lightCoords,
        final int overlayCoords,
        final int outlineColor,
        final int[] tintLayers,
        final List<BakedQuad> quads,
        final ItemStackRenderState.FoilType foilType
    ) {
        if (dragonSurvival$hunterItemAlpha == HunterHandler.UNMODIFIED || dragonSurvival$hunterItemAlpha == HunterHandler.NON_TRANSPARENT || quads.isEmpty()) {
            submitNodeCollector.submitItem(poseStack, displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads, foilType);
            return;
        }

        int[] translucentTints = Arrays.copyOf(tintLayers, tintLayers.length);

        for (int i = 0; i < translucentTints.length; i++) {
            translucentTints[i] = HunterHandler.applyAlpha(dragonSurvival$hunterItemAlpha, translucentTints[i]);
        }

        int defaultTintIndex = -1;
        List<BakedQuad> translucentQuads = new ArrayList<>(quads.size());

        for (BakedQuad quad : quads) {
            BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
            int tintIndex = materialInfo.tintIndex();

            if (tintIndex < 0 || tintIndex >= translucentTints.length) {
                if (defaultTintIndex == -1) {
                    defaultTintIndex = translucentTints.length;
                    translucentTints = Arrays.copyOf(translucentTints, defaultTintIndex + 1);
                    translucentTints[defaultTintIndex] = HunterHandler.applyAlpha(dragonSurvival$hunterItemAlpha, -1);
                }

                tintIndex = defaultTintIndex;
            }

            RenderType renderType = dragonSurvival$getTranslucentRenderType(materialInfo);
            translucentQuads.add(dragonSurvival$copyQuad(quad, materialInfo, renderType, tintIndex));
        }

        submitNodeCollector.submitItem(poseStack, displayContext, lightCoords, overlayCoords, outlineColor, translucentTints, translucentQuads, foilType);
    }

    @Unique private static BakedQuad dragonSurvival$copyQuad(
        final BakedQuad quad,
        final BakedQuad.MaterialInfo materialInfo,
        final RenderType renderType,
        final int tintIndex
    ) {
        return new BakedQuad(
            quad.position0(),
            quad.position1(),
            quad.position2(),
            quad.position3(),
            quad.packedUV0(),
            quad.packedUV1(),
            quad.packedUV2(),
            quad.packedUV3(),
            quad.direction(),
            new BakedQuad.MaterialInfo(
                materialInfo.sprite(),
                dragonSurvival$getLayer(renderType, materialInfo.layer()),
                renderType,
                tintIndex,
                materialInfo.shade(),
                materialInfo.lightEmission(),
                materialInfo.ambientOcclusion()
            ),
            quad.bakedNormals(),
            quad.bakedColors()
        );
    }

    @Unique private static ChunkSectionLayer dragonSurvival$getLayer(final RenderType renderType, final ChunkSectionLayer currentLayer) {
        return renderType.hasBlending() ? ChunkSectionLayer.TRANSLUCENT : currentLayer;
    }

    @Unique private static RenderType dragonSurvival$getTranslucentRenderType(final BakedQuad.MaterialInfo materialInfo) {
        RenderType renderType = materialInfo.itemRenderType();

        if (renderType.hasBlending()) {
            return renderType;
        }

        boolean blockAtlas = materialInfo.sprite().atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS);

        if (renderType.pipeline() == RenderPipelines.ITEM_CUTOUT) {
            return blockAtlas ? Sheets.translucentBlockItemSheet() : Sheets.translucentItemSheet();
        }

        return blockAtlas ? Sheets.translucentBlockSheet() : Sheets.translucentItemSheet();
    }

    @Override
    public float dragonSurvival$getHunterItemAlpha() {
        return dragonSurvival$hunterItemAlpha;
    }

    @Override
    public void dragonSurvival$setHunterItemAlpha(final float alpha) {
        dragonSurvival$hunterItemAlpha = alpha;
    }
}
