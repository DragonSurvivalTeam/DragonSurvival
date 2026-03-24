package by.dragonsurvivalteam.dragonsurvival.client.models;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class DragonModel extends GeoModel<DragonEntity> {
    private static final Identifier DEFAULT_ANIMATION = DragonSurvival.res("dragon_center.animation");

    // FIXME 'dragon_dragon'?
    private final Identifier defaultTexture = DragonSurvival.res("textures/dragon_dragon/newborn.png");

    private Identifier overrideTexture;

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
        if (overrideTexture != null && RenderingUtils.hasTexture(overrideTexture)) {
            return overrideTexture;
        }

        Player player = renderData == null ? null : renderData.texturePlayer();

        if (player == null) {
            return defaultTexture;
        }

        DragonStateHandler handler = renderData != null ? renderData.handler() : null;

        if (handler == null) {
            return defaultTexture;
        }
        DragonStageCustomization customization = handler.getCurrentStageCustomization();

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

        ResourceKey<DragonStage> stageKey = handler.stageKey();
        if (handler.needsSkinRecompilation()) {
            DragonEditorHandler.generateSkinTextures(player, handler);
            handler.getSkinData().isCompiled.put(handler.stageKey(), true);
            handler.getSkinData().recompileSkin.put(handler.stageKey(), false);
        }

        Identifier texture = dynamicTexture(player, handler, false);

        // Show the default skin while we are compiling if we haven't already compiled the skin
        if (customization.defaultSkin || !handler.getSkinData().isCompiled.getOrDefault(stageKey, false) || !RenderingUtils.hasTexture(texture)) {
            return StageResources.getDefaultSkin(handler.species(), handler.stageKey(), false);
        }

        return Identifier.withDefaultNamespace("");
    }

    public static Identifier dynamicTexture(final Player player, final DragonStateHandler handler, boolean isGlowLayer) {
        String prefix = isGlowLayer ? "dynamic_glow_" : "dynamic_normal_";
        return DragonSurvival.res(prefix + player.getStringUUID() + "_" + handler.speciesId().getPath() + "_" + handler.stageKey().identifier().getPath());
    }

    @Override
    public @NotNull Identifier getAnimationResource(final DragonEntity dragon) {
        return getAnimationResource(dragon.getPlayer());
    }

    public void setOverrideTexture(final Identifier overrideTexture) {
        this.overrideTexture = overrideTexture;
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
