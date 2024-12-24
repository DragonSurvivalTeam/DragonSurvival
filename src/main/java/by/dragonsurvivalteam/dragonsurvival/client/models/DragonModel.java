package by.dragonsurvivalteam.dragonsurvival.client.models;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonStageCustomization;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.model.GeoModel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonModel extends GeoModel<DragonEntity> {
    private final ResourceLocation defaultTexture = ResourceLocation.fromNamespaceAndPath(MODID, "textures/dragon/cave_newborn.png");
    private ResourceLocation overrideTexture;
    private CompletableFuture<Void> textureRegisterFuture = CompletableFuture.completedFuture(null);

    /** Factor to multiply the delta yaw and pitch by, needed for scaling for the animations */
    private static final double DELTA_YAW_PITCH_FACTOR = 0.2;

    /** Factor to multiply the delta movement by, needed for scaling for the animations */
    private static final double DELTA_MOVEMENT_FACTOR = 10;

    /**
     * TODO Body Types Update
     * Required:
     * - tips for body types like for magic abilities
     * <p>
     * Extras:
     * - customization.json - Ability to disallow some details in the editor for some Body Types (for example, wing details are not required for wingless).
     * - emotes.json - Ability to disallow some emotions for certain Body Types.
     */

    @Override
    public void applyMolangQueries(final AnimationState<DragonEntity> animationState, double currentTick) {
        super.applyMolangQueries(animationState, currentTick);
        DragonEntity dragon = animationState.getAnimatable();
        Player player = dragon.getPlayer();

        if (player == null) {
            return;
        }

        float deltaTick = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
        float partialDeltaTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        MovementData movement = MovementData.getData(player);

        MathParser.setVariable("query.head_yaw", () -> movement.headYaw);
        MathParser.setVariable("query.head_pitch", () -> movement.headPitch);

        double gravity = player.getAttributeValue(Attributes.GRAVITY);
        MathParser.setVariable("query.gravity", () -> gravity);


        double bodyYawAvg;
        double headYawAvg;
        double headPitchAvg;
        double verticalVelocityAvg;
        if (!ClientDragonRenderer.isOverridingMovementData) {
            double bodyYawChange = Functions.angleDifference(movement.bodyYaw, movement.bodyYawLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;
            double headYawChange = Functions.angleDifference(movement.headYaw, movement.headYawLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;
            double headPitchChange = Functions.angleDifference(movement.headPitch, movement.headPitchLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;

            double verticalVelocity = Mth.lerp(partialDeltaTick, movement.deltaMovementLastFrame.y, movement.deltaMovement.y) * DELTA_MOVEMENT_FACTOR;
            // Factor in the vertical angle of the dragon so that the vertical velocity is scaled down when the dragon is looking up or down
            // Ideally, we would just use more precise data (factor in the full rotation of the player in our animations)
            // but this works pretty well in most situations the player will encounter
            verticalVelocity *= 1 - Mth.abs(Mth.clampedMap(movement.prevXRot, -90, 90, -1, 1));

            float deltaTickFor60FPS = AnimationUtils.getDeltaTickFor60FPS();
            // Accumulate them in the history
            while(dragon.bodyYawHistory.size() > 10 / deltaTickFor60FPS ) {
                dragon.bodyYawHistory.removeFirst();
            }
            dragon.bodyYawHistory.add(bodyYawChange);

            while(dragon.headYawHistory.size() > 10 / deltaTickFor60FPS ) {
                dragon.headYawHistory.removeFirst();
            }
            dragon.headYawHistory.add(headYawChange);

            while(dragon.headPitchHistory.size() > 10 / deltaTickFor60FPS ) {
                dragon.headPitchHistory.removeFirst();
            }
            dragon.headPitchHistory.add(headPitchChange);

            // Handle the clear case (see DragonEntity.java)
            if(dragon.clearVerticalVelocity) {
                dragon.verticalVelocityHistory.clear();
                while(dragon.verticalVelocityHistory.size() < 10 / deltaTickFor60FPS) {
                    dragon.verticalVelocityHistory.add(0.);
                }
            }

            while(dragon.verticalVelocityHistory.size() > 10 / deltaTickFor60FPS ) {
                dragon.verticalVelocityHistory.removeFirst();
            }
            dragon.verticalVelocityHistory.add(verticalVelocity);

            bodyYawAvg = dragon.bodyYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            headYawAvg = dragon.headYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            headPitchAvg = dragon.headPitchHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            verticalVelocityAvg = dragon.verticalVelocityHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        } else {
            bodyYawAvg = 0;
            headYawAvg = 0;
            headPitchAvg = 0;
            verticalVelocityAvg = 0;
        }

        double lerpRate = Math.min(1., deltaTick);
        dragon.currentBodyYawChange = Mth.lerp(lerpRate, dragon.currentBodyYawChange, bodyYawAvg);
        dragon.currentHeadYawChange = Mth.lerp(lerpRate, dragon.currentHeadYawChange, headYawAvg);
        dragon.currentHeadPitchChange = Mth.lerp(lerpRate, dragon.currentHeadPitchChange, headPitchAvg);
        if(dragon.clearVerticalVelocity) {
            dragon.currentTailMotionUp = 0;
            dragon.clearVerticalVelocity = false;
        } else {
            dragon.currentTailMotionUp = Mth.lerp(lerpRate, dragon.currentTailMotionUp, -verticalVelocityAvg);
        }
        MathParser.setVariable("query.body_yaw_change", () -> dragon.currentBodyYawChange);
        MathParser.setVariable("query.head_yaw_change", () -> dragon.currentHeadYawChange);
        MathParser.setVariable("query.head_pitch_change", () -> dragon.currentHeadPitchChange);
        MathParser.setVariable("query.tail_motion_up", () -> dragon.currentTailMotionUp);
    }

    @Override
    public ResourceLocation getModelResource(final DragonEntity dragon) {
        return DragonStateProvider.getData(dragon.getPlayer()).getBody().value().customModel();
    }

    @Override
    public ResourceLocation getTextureResource(final DragonEntity dragon) {
        if (overrideTexture != null) {
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
        DragonStageCustomization customization = handler.getSkinData().get(handler.getStage().getKey()).get();

        if (handler.getSkinData().blankSkin) {
            return ResourceLocation.fromNamespaceAndPath(MODID, "textures/dragon/blank_skin_" + handler.getTypeNameLowerCase() + ".png");
        }

        ResourceKey<DragonStage> stageKey = handler.getStage().getKey();

        if (handler.getSkinData().recompileSkin.getOrDefault(stageKey, true)) {
            if (ClientConfig.forceCPUSkinGeneration) {
                if (textureRegisterFuture.isDone()) {
                    CompletableFuture<List<Pair<NativeImage, ResourceLocation>>> imageGenerationFuture = DragonEditorHandler.generateSkinTextures(dragon);
                    textureRegisterFuture = imageGenerationFuture.thenRunAsync(() -> {
                        handler.getSkinData().isCompiled.put(stageKey, true);
                        handler.getSkinData().recompileSkin.put(stageKey, false);
                        for (Pair<NativeImage, ResourceLocation> pair : imageGenerationFuture.join()) {
                            RenderingUtils.uploadTexture(pair.getFirst(), pair.getSecond());
                        }
                    }, Minecraft.getInstance());
                }
            } else {
                DragonEditorHandler.generateSkinTexturesGPU(dragon);
                handler.getSkinData().isCompiled.put(handler.getStage().getKey(), true);
                handler.getSkinData().recompileSkin.put(handler.getStage().getKey(), false);
            }
        }

        // Show the default skin while we are compiling if we haven't already compiled the skin
        if (customization.defaultSkin || !handler.getSkinData().isCompiled.getOrDefault(stageKey, false)) {
            return ResourceLocation.fromNamespaceAndPath(MODID, "textures/dragon/" + handler.getTypeNameLowerCase() + "_" + Objects.requireNonNull(stageKey).location().getPath() + ".png");
        }

        String uuid = player.getStringUUID();
        return ResourceLocation.fromNamespaceAndPath(MODID, "dynamic_normal_" + uuid + "_" + Objects.requireNonNull(stageKey).location().getPath());
    }

    @Override
    public ResourceLocation getAnimationResource(final DragonEntity dragon) {
        Player player = dragon.getPlayer();
        return getAnimationResource(player);
    }

    @Override // GeoEntityRenderer#getRenderType handles invisible and glowing
    public RenderType getRenderType(final DragonEntity animatable, final ResourceLocation texture) {
        Player player = animatable.getPlayer();

        if (player != null) {
            HunterData data = player.getData(DSDataAttachments.HUNTER);

            if (!data.isBeingRenderedInInventory && data.hasHunterStacks()) {
                // Transparent rendering in inventory causes the entity to be invisible
                return RenderType.itemEntityTranslucentCull(texture);
            }
        }

        return RenderType.entityCutout(texture);
    }

    public void setOverrideTexture(final ResourceLocation overrideTexture) {
        this.overrideTexture = overrideTexture;
    }

    public static ResourceLocation getAnimationResource(final Player player) {
        if (player != null) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            Holder<DragonBody> body = handler.getBody();

            if (body != null) {
                ResourceLocation location = Objects.requireNonNull(body.getKey()).location();
                return ResourceLocation.fromNamespaceAndPath(location.getNamespace(), String.format("animations/dragon_%s.json", location.getPath()));
            }
        }

        return ResourceLocation.fromNamespaceAndPath(MODID, "animations/dragon_center.json");
    }
}