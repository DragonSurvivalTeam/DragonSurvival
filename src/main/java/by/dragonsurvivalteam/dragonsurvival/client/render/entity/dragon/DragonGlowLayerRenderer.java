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
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class DragonGlowLayerRenderer<R extends LivingEntityRenderState & GeoRenderState> extends AutoGlowingGeoLayer<DragonEntity, Void, R> {
    public DragonGlowLayerRenderer(final DragonRenderer<R> renderer) {
        super(renderer);
    }

    @Override
    public void submitRenderTask(final RenderPassInfo<R> renderPassInfo, final @NonNull SubmitNodeCollector renderTasks) {
        if (!renderPassInfo.willRender() || getGlowTexture(renderPassInfo.renderState()) == null) {
            return;
        }

        super.submitRenderTask(renderPassInfo, renderTasks);
    }

    @Override
    protected @NonNull Identifier getTextureResource(final @NonNull R renderState) {
        Identifier glowTexture = getGlowTexture(renderState);

        if (glowTexture == null) {
            throw new IllegalStateException("Tried to render a dragon glow layer without a glow texture");
        }

        return glowTexture;
    }

    private @Nullable Identifier getGlowTexture(final R renderState) {
        DragonRenderer.DragonRenderData renderData = renderState.getGeckolibData(DragonRenderer.DRAGON_RENDER_DATA);

        if (renderData == null || renderData.handler() == null || renderData.player() == null) {
            return null;
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
            return null;
        }

        DragonEditorHandler.markSkinTextureUsed(glowTexture);
        return glowTexture;
    }
}
