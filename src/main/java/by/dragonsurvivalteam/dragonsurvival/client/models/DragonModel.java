package by.dragonsurvivalteam.dragonsurvival.client.models;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.DragonAnimationState;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonRidingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.core.molang.MolangQueries;
import software.bernie.geckolib.model.GeoModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DragonModel extends GeoModel<DragonEntity> {
    private static final List<String> REQUIRED_MOLANG_QUERIES = List.of(
            MolangQueries.MOON_PHASE,
            "query.body_yaw_change",
            "query.gravity",
            "query.head_pitch",
            "query.head_pitch_change",
            "query.head_yaw",
            "query.head_yaw_change",
            "query.look_angle_x",
            "query.look_angle_y",
            "query.player_level",
            "query.tail_motion_up",
            "query.x_rotation",
            "query.y_rotation",
            "query.z_rotation"
    );

    // FIXME 'dragon_dragon'?
    private final ResourceLocation defaultTexture = DragonSurvival.res("textures/dragon_dragon/newborn.png");

    private ResourceLocation overrideTexture;
    private final Set<ResourceLocation> validatedMountingBoneModels = new HashSet<>();

    public DragonModel() {
        REQUIRED_MOLANG_QUERIES.forEach(MolangParser.INSTANCE::getVariable);
    }

    @Override
    public void applyMolangQueries(final DragonEntity dragon, double currentTick) {
        super.applyMolangQueries(dragon, currentTick);

        Player player = dragon.getPlayer();

        if (player == null) {
            return;
        }

        MovementData movement = MovementData.getData(player);
        DragonAnimationState.Pose pose = dragon.isInInventory ? null : dragon.animationState.sample(Minecraft.getInstance().getFrameTime());
        double headYaw = pose != null ? pose.headYaw() : movement.headYaw;
        double headPitch = pose != null ? pose.headPitch() : movement.headPitch;
        MolangParser.INSTANCE.setValue("query.head_yaw", () -> dragon.neckLocked ? 0 : headYaw);
        MolangParser.INSTANCE.setValue("query.head_pitch", () -> dragon.neckLocked ? 0 : headPitch);

        double gravity = player.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
        MolangParser.INSTANCE.setValue("query.gravity", () -> gravity);

        double growth = DragonStateProvider.getData(player).getGrowth();
        MolangParser.INSTANCE.setValue("query.ds_growth", () -> growth);

        MolangParser.INSTANCE.setValue("query.body_yaw_change", () -> dragon.tailLocked || pose == null ? 0 : pose.bodyYawChange());
        MolangParser.INSTANCE.setValue("query.tail_motion_up", () -> dragon.tailLocked || dragon.clearVerticalVelocity || pose == null ? 0 : pose.tailMotionUp());
        MolangParser.INSTANCE.setValue("query.head_yaw_change", () -> pose == null ? 0 : pose.headYawChange());
        MolangParser.INSTANCE.setValue("query.head_pitch_change", () -> pose == null ? 0 : pose.headPitchChange());
    }

    @Override
    public ResourceLocation getModelResource(final DragonEntity dragon) {
        Player player = dragon.getPlayer();
        ResourceLocation model;

        if (player == null) {
            model = DragonBody.DEFAULT_MODEL;
        } else {
            model = DragonStateProvider.getData(player).getModel();
        }

        model = model.withPrefix("geo/").withSuffix(".geo.json");

        try {
            BakedGeoModel bakedModel = getBakedModel(model);

            if (player != null) {
                validateMountingBone(DragonStateProvider.getData(player).body(), model, bakedModel);
            }
        } catch (Exception e) {
            DragonSurvival.LOGGER.error("Model not found for dragon species: {}", Translation.Type.DRAGON_SPECIES.wrap(DragonStateProvider.getData(player).speciesKey().location()));
            return DragonBody.DEFAULT_MODEL;
        }

        return model;
    }

    private void validateMountingBone(final Holder<DragonBody> body, final ResourceLocation modelResource, final BakedGeoModel bakedModel) {
        if (!body.value().rideable() || !validatedMountingBoneModels.add(modelResource)) {
            return;
        }

        if (bakedModel.getBone(DragonRidingHandler.MOUNTING_BONE).isEmpty()) {
            String bodyId = body.unwrapKey().map(key -> key.location().toString()).orElse("<direct>");
            DragonSurvival.LOGGER.error("Dragon body [{}] is marked as rideable, but model [{}] does not contain the required mounting bone [{}].", bodyId, modelResource, DragonRidingHandler.MOUNTING_BONE);
        }
    }

    @Override
    public ResourceLocation getTextureResource(final DragonEntity dragon) {
        if (overrideTexture != null && RenderingUtils.hasTexture(overrideTexture)) {
            return overrideTexture;
        }

        Player player;

        if (dragon.overrideUUIDWithLocalPlayerForTextureFetch) {
            player = Minecraft.getInstance().player;
        } else {
            player = dragon.getPlayer();
        }

        if (player == null) {
            return defaultTexture;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        DragonStageCustomization customization = handler.getCurrentStageCustomization();

        // Don't try to fetch skins if it is a fake client player; the only case where we need custom skins for a fake client player
        // is in the dragon skins screen, and we already have special logic for that outside of this getTextureResource method
        if (handler.getModel().equals(DragonBody.DEFAULT_MODEL) && !(player instanceof FakeClientPlayer)) {
            ResourceLocation skin = DragonSkins.getPlayerSkin(player, handler.stageKey());

            if (RenderingUtils.hasTexture(skin)) {
                return skin;
            }
        }

        if (handler.getSkinData().blankSkin) {
            return DragonSurvival.res("textures/dragon/" + handler.speciesId().getPath() + "/blank_skin.png");
        }

        ResourceKey<DragonStage> stageKey = handler.stageKey();
        ResourceLocation texture = dynamicTexture(player, handler, false);

        // Show the default skin while we are compiling if we haven't already compiled the skin
        if (customization.defaultSkin || !handler.getSkinData().isCompiled.getOrDefault(stageKey, false)) {
            return StageResources.getDefaultSkin(handler.species(), handler.stageKey(), false);
        }

        if (!DragonEditorHandler.hasGeneratedSkinTexture(texture)) {
            DragonEditorHandler.generateSkinTextures(player, handler);
        }

        DragonEditorHandler.markSkinTextureUsed(texture);
        return texture;
    }

    public static ResourceLocation dynamicTexture(final Player player, final DragonStateHandler handler, boolean isGlowLayer) {
        String prefix = isGlowLayer ? "dynamic_glow_" : "dynamic_normal_";
        return DragonSurvival.res(prefix + player.getStringUUID() + "_" + handler.speciesId().getPath() + "_" + handler.stageKey().location().getPath());
    }

    @Override
    public ResourceLocation getAnimationResource(final DragonEntity dragon) {
        Player player = dragon.getPlayer();
        return getAnimationResource(player);
    }

    @Override // GeoEntityRenderer#getRenderType handles invisible and glowing
    public RenderType getRenderType(final DragonEntity animatable, final ResourceLocation texture) {
        Player player = animatable.getPlayer();

        if (player != null && HunterData.hasTransparency(player)) {
            return RenderType.itemEntityTranslucentCull(texture);
        }

        return RenderType.entityCutout(texture);
    }

    public void setOverrideTexture(final ResourceLocation overrideTexture) {
        this.overrideTexture = overrideTexture;
    }

    public static ResourceLocation getAnimationResource(final Player player) {
        if (player != null) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            Holder<DragonBody> body = handler.body();

            if (body != null) {
                return body.value().animation().withPrefix("animations/").withSuffix(".json");
            }
        }

        return DragonSurvival.res("animations/dragon_center.json");
    }
}
