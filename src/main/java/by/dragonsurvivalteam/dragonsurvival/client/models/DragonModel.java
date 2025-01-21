package by.dragonsurvivalteam.dragonsurvival.client.models;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
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
import java.util.concurrent.CompletableFuture;

public class DragonModel extends GeoModel<DragonEntity> {
    /** Factor to multiply the delta yaw and pitch by, needed for scaling for the animations */
    private static final double DELTA_YAW_PITCH_FACTOR = 0.2;

    /** Factor to multiply the delta movement by, needed for scaling for the animations */
    private static final double DELTA_MOVEMENT_FACTOR = 10;

    private final ResourceLocation defaultTexture = DragonSurvival.res("textures/dragon_dragon/newborn.png");

    private ResourceLocation overrideTexture;
    private CompletableFuture<Void> textureRegisterFuture = CompletableFuture.completedFuture(null);

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

        if (dragon.neckLocked) {
            MathParser.setVariable("query.head_yaw", () -> 0);
            MathParser.setVariable("query.head_pitch", () -> 0);
        } else {
            MathParser.setVariable("query.head_yaw", () -> movement.headYaw);
            MathParser.setVariable("query.head_pitch", () -> movement.headPitch);
        }

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
            int removeSize = (int) (10 / deltaTickFor60FPS);

            // Handle the clear case (see DragonEntity.java)
            if (dragon.clearVerticalVelocity) {
                dragon.verticalVelocityHistory.clear();

                while (dragon.verticalVelocityHistory.size() < removeSize) {
                    dragon.verticalVelocityHistory.add(0d);
                }
            }

            while(true) {
                boolean removedElement = false;

                if (dragon.bodyYawHistory.size() > removeSize) {
                    dragon.bodyYawHistory.removeFirst();
                    removedElement = true;
                }

                if (dragon.headYawHistory.size() > removeSize) {
                    dragon.headYawHistory.removeFirst();
                    removedElement = true;
                }

                if (dragon.headPitchHistory.size() > removeSize) {
                    dragon.headPitchHistory.removeFirst();
                    removedElement = true;
                }

                if (dragon.verticalVelocityHistory.size() > removeSize) {
                    dragon.verticalVelocityHistory.removeFirst();
                    removedElement = true;
                }

                if (!removedElement) {
                    break;
                }
            }

            dragon.bodyYawHistory.add(bodyYawChange);
            dragon.headYawHistory.add(headYawChange);
            dragon.headPitchHistory.add(headPitchChange);
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

        double lerpRate = Math.min(1, deltaTick);
        dragon.currentBodyYawChange = Mth.lerp(lerpRate, dragon.currentBodyYawChange, bodyYawAvg);
        dragon.currentHeadYawChange = Mth.lerp(lerpRate, dragon.currentHeadYawChange, headYawAvg);
        dragon.currentHeadPitchChange = Mth.lerp(lerpRate, dragon.currentHeadPitchChange, headPitchAvg);

        if (dragon.clearVerticalVelocity) {
            dragon.currentTailMotionUp = 0;
            dragon.clearVerticalVelocity = false;
        } else {
            dragon.currentTailMotionUp = Mth.lerp(lerpRate, dragon.currentTailMotionUp, -verticalVelocityAvg);
        }

        if (dragon.tailLocked) {
            MathParser.setVariable("query.tail_motion_up", () -> 0);
            MathParser.setVariable("query.body_yaw_change", () -> 0);
        } else {
            MathParser.setVariable("query.body_yaw_change", () -> dragon.currentBodyYawChange);
            MathParser.setVariable("query.tail_motion_up", () -> dragon.currentTailMotionUp);
        }

        MathParser.setVariable("query.head_yaw_change", () -> dragon.currentHeadYawChange);
        MathParser.setVariable("query.head_pitch_change", () -> dragon.currentHeadPitchChange);
    }

    @Override
    public ResourceLocation getModelResource(final DragonEntity dragon) {
        ResourceLocation model;

        if (dragon.getPlayer() == null) {
            model = DragonBody.DEFAULT_MODEL;
        } else {
            model = DragonStateProvider.getData(dragon.getPlayer()).getModel();
        }

        return model.withPrefix("geo/").withSuffix(".geo.json");
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
        DragonStageCustomization customization = handler.getCurrentStageCustomization();

        if (handler.getSkinData().blankSkin) {
            return DragonSurvival.res("textures/dragon/" + handler.speciesId().getPath() + "/blank_skin.png");
        }

        ResourceKey<DragonStage> stageKey = handler.stageKey();

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
                handler.getSkinData().isCompiled.put(handler.stageKey(), true);
                handler.getSkinData().recompileSkin.put(handler.stageKey(), false);
            }
        }

        // Show the default skin while we are compiling if we haven't already compiled the skin
        if (customization.defaultSkin || !handler.getSkinData().isCompiled.getOrDefault(stageKey, false)) {
            // TODO :: support custom
            return DragonSurvival.res("textures/dragon/" + handler.speciesId().getPath() + "/" + stageKey.location().getPath() + ".png");
        }

        return dynamicTexture(player, handler, false);
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
            Holder<DragonBody> body = handler.body();

            if (body != null) {
                return body.value().animation().withPrefix("animations/").withSuffix(".json");
            }
        }

        return DragonSurvival.res("animations/dragon_center.json");
    }
}