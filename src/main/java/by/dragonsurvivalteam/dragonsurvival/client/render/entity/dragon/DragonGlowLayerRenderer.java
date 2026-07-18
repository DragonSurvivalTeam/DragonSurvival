package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class DragonGlowLayerRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoRenderLayer<DragonEntity, Void, R> {
    public DragonGlowLayerRenderer(final DragonRenderer<R> renderer) {
        super(renderer);
    }

    @Override
    public void submitRenderTask(final RenderPassInfo<R> renderPassInfo, final SubmitNodeCollector renderTasks) {
        if (!renderPassInfo.willRender() || !(renderer instanceof DragonRenderer<R> dragonRenderer)) {
            return;
        }

        DragonRenderer.DragonRenderData renderData = renderPassInfo.renderState().getGeckolibData(DragonRenderer.DRAGON_RENDER_DATA);

        if (renderData == null || renderData.handler() == null || renderData.player() == null) {
            return;
        }

        Player player = renderData.texturePlayer() != null ? renderData.texturePlayer() : renderData.player();
        DragonStateHandler handler = renderData.handler();
        DragonStageCustomization customization = handler.getCurrentStageCustomization();
        Identifier glowTexture = null;

        if (handler.getModel().equals(DragonBody.DEFAULT_MODEL)) {
            glowTexture = DragonSkins.getGlowTexture(player, handler.stageKey());
        }

        Identifier glowTextureOverride = renderData.glowTextureOverride();

        if (glowTextureOverride != null && (glowTexture == null || glowTexture.getPath().contains("/" + handler.speciesId().getPath() + "_"))) {
            glowTexture = glowTextureOverride;
        }

        if (glowTexture == null && customization.defaultSkin) {
            Identifier defaultGlowSkin = StageResources.getDefaultSkin(handler.species(), handler.stageKey(), true);

            if (Minecraft.getInstance().getResourceManager().getResource(defaultGlowSkin).isPresent()) {
                glowTexture = defaultGlowSkin;
            }
        }

        if (glowTexture == null && customization.layerSettings.values().stream().anyMatch(layerSettings -> layerSettings.get().isGlowing)) {
            glowTexture = DragonModel.dynamicTexture(player, handler, true);
        }

        boolean hasGlowTexture = glowTexture != null
            && (DragonEditorHandler.hasGeneratedSkinTexture(glowTexture)
                || (!DragonEditorHandler.isDynamicSkinTexture(glowTexture) && RenderingUtils.hasTexture(glowTexture)));

        if (!hasGlowTexture) {
            return;
        }

        DragonEditorHandler.markSkinTextureUsed(glowTexture);
        dragonRenderer.submitRenderTasks(renderPassInfo, renderTasks.order(1), RenderTypes.entityTranslucentEmissive(glowTexture, false));
    }
}
