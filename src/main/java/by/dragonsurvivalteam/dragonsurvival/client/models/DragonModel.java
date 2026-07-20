package by.dragonsurvivalteam.dragonsurvival.client.models;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class DragonModel extends GeoModel<DragonEntity> {
    private static final Identifier DEFAULT_ANIMATION = DragonSurvival.res("dragon_center.animation");

    // TODO :: 'dragon_dragon'?
    private final Identifier defaultTexture = DragonSurvival.res("textures/dragon_dragon/newborn.png");

    @Override
    public @NotNull Identifier getModelResource(@NotNull GeoRenderState renderState) {
        DragonRenderer.DragonRenderData renderData = renderState.getGeckolibData(DragonRenderer.DRAGON_RENDER_DATA);
        Identifier model = renderData == null ? DragonBody.DEFAULT_MODEL : renderData.modelResource();

        try {
            getBakedModel(model);
        } catch (Exception e) {
            if (renderData != null && renderData.handler() != null) {
                DragonSurvival.LOGGER.error("Model not found for dragon species: {}", Translation.Type.DRAGON_SPECIES.wrap(renderData.handler().speciesKey().identifier()));
            }
            return DragonBody.DEFAULT_MODEL;
        }

        return model;
    }

    @Override
    public @NotNull Identifier getTextureResource(final GeoRenderState renderState) {
        DragonRenderer.DragonRenderData renderData = renderState.getGeckolibData(DragonRenderer.DRAGON_RENDER_DATA);
        Identifier textureOverride = renderData == null ? null : renderData.textureOverride();

        if (RenderingUtils.hasTexture(textureOverride)) {
            return textureOverride;
        }

        Player player = renderData == null ? null : renderData.texturePlayer();

        if (player == null) {
            return defaultTexture;
        }

        DragonStateHandler handler = renderData.handler();

        if (handler == null) {
            return defaultTexture;
        }

        // Don't try to fetch skins if it is a fake client player; the only case where we need custom skins for a fake client player
        // is in the dragon skins screen, and we already have special logic for that outside of this getTextureResource method
        if (handler.getModel().equals(DragonBody.DEFAULT_MODEL) && !(player instanceof FakeClientPlayer)) {
            Identifier skin = DragonSkins.getPlayerSkin(player, handler.stageKey());

            if (RenderingUtils.hasTexture(skin)) {
                return skin;
            }
        }

        if (handler.getSkinData().blankSkin) {
            return DragonSurvival.res("textures/dragon/" + handler.speciesId().getPath() + "/blank_skin.png");
        }

        Identifier texture = dynamicTexture(player, handler, false);

        if (handler.getCurrentStageCustomization().defaultSkin) {
            return StageResources.getDefaultSkin(handler.species(), handler.stageKey(), false);
        }

        if (!DragonEditorHandler.hasGeneratedSkinTexture(texture)) {
            DragonEditorHandler.generateSkinTextures(player, handler);
        }

        DragonEditorHandler.markSkinTextureUsed(texture);
        return texture;
    }

    public static Identifier dynamicTexture(final Player player, final DragonStateHandler handler, boolean isGlowLayer) {
        String prefix = isGlowLayer ? "dynamic_glow_" : "dynamic_normal_";
        return DragonSurvival.res(prefix + player.getStringUUID() + "_" + handler.speciesId().getPath() + "_" + handler.stageKey().identifier().getPath());
    }

    @Override
    public @NotNull Identifier getAnimationResource(final DragonEntity dragon) {
        return getAnimationResource(dragon.getPlayer());
    }

    public static Identifier getAnimationResource(final Player player) {
        if (player != null) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            Holder<DragonBody> body = handler.body();

            if (body != null) {
                return body.value().animation();
            }
        }

        return DEFAULT_ANIMATION;
    }
}
