package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class DragonRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<DragonEntity, R> {
    public static final Map<Integer, Map<String, Vec3>> BONE_POSITIONS = new HashMap<>();
    private static final List<String> BONES = List.of("BreathSource");

    private static final int RENDER_COLOR = ARGB.color(255, 255, 255);
    private static final int TRANSPARENT_RENDER_COLOR = ARGB.colorFromFloat(HunterHandler.MIN_ALPHA, 1, 1, 1);

    /** Factor to multiply the delta yaw and pitch by, needed for scaling for the animations */
    private static final double DELTA_YAW_PITCH_FACTOR = 0.2;

    /** Factor to multiply the delta movement by, needed for scaling for the animations */
    private static final double DELTA_MOVEMENT_FACTOR = 10;

    public Identifier glowTexture;
    public boolean isRenderingLayer;
    public boolean shouldRenderLayers = true;

    // Data tickets
    public static DataTicket<DragonEntity> DRAGON_ENTITY = DataTicket.create("dragonEntity", DragonEntity.class);

    public DragonRenderer(final EntityRendererProvider.Context context, final GeoModel<DragonEntity> model) {
        super(context, model);

        // FIXME
        /*getRenderLayers().add(new DragonGlowLayerRenderer(this));
        getRenderLayers().add(new DragonArmorRenderLayer(this));
        getRenderLayers().add(new DragonItemRenderLayer(this, (bone, animatable) -> {
            if (bone.getName().equals(ClientDragonRenderer.renderItemsInMouth ? "RightItem_jaw" : "RightItem")) {
                return animatable.getMainHandItem();
            } else if (bone.getName().equals(ClientDragonRenderer.renderItemsInMouth ? "LeftItem_jaw" : "LeftItem")) {
                return animatable.getOffhandItem();
            }
            return null;
        }, (bone, animatable) -> null));*/

        // FIXME :: COMPAT
        /*if (ModCheck.isModLoaded(ModCheck.SOPHISTICATED_BACKPACKS)) {
            getRenderLayers().add(new DragonBackpackRenderLayer(this));
        }*/
    }

    @Override
    public void setMolangQueryValues(DragonEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        super.setMolangQueryValues(animatable, relatedObject, renderState, partialTick);

        DragonEntity dragon = animatable;
        Player player = dragon.getPlayer();

        if (player == null) {
            return;
        }

        MovementData movement = MovementData.getData(player);
        float deltaTick = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
        float partialDeltaTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        if (dragon.neckLocked) {
            MathParser.setVariable("query.head_yaw", state -> 0);
            MathParser.setVariable("query.head_pitch", state -> 0);
        } else {
            MathParser.setVariable("query.head_yaw", state -> movement.headYaw);
            MathParser.setVariable("query.head_pitch", state -> movement.headPitch);
        }

        double gravity = player.getAttributeValue(Attributes.GRAVITY);
        MathParser.setVariable("query.gravity", state -> gravity);

        double bodyYawAvg;
        double headYawAvg;
        double headPitchAvg;
        double verticalVelocityAvg;

        if (!dragon.isInInventory) {
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

            while (true) {
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

        // Clear out any NaNs that may have been caused by the average calculation (I think this happens if we try to load data before the game logic has actually begun?
        bodyYawAvg = Double.isNaN(bodyYawAvg) ? 0 : bodyYawAvg;
        headYawAvg = Double.isNaN(headYawAvg) ? 0 : headYawAvg;
        headPitchAvg = Double.isNaN(headPitchAvg) ? 0 : headPitchAvg;
        verticalVelocityAvg = Double.isNaN(verticalVelocityAvg) ? 0 : verticalVelocityAvg;

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
            MathParser.setVariable("query.tail_motion_up", state -> 0);
            MathParser.setVariable("query.body_yaw_change", state -> 0);
        } else {
            MathParser.setVariable("query.body_yaw_change", state -> dragon.currentBodyYawChange);
            MathParser.setVariable("query.tail_motion_up", state -> dragon.currentTailMotionUp);
        }

        MathParser.setVariable("query.head_yaw_change", state -> dragon.currentHeadYawChange);
        MathParser.setVariable("query.head_pitch_change", state -> dragon.currentHeadPitchChange);
    }

    @Override
    public void preRenderPass(@NotNull RenderPassInfo<@NotNull R> renderPassInfo, @NotNull SubmitNodeCollector renderTasks) {
        DragonEntity animatable = renderPassInfo.getGeckolibData(DRAGON_ENTITY);
        Profiler.get().push("player_dragon");
        Player player = animatable.getPlayer();

        final RenderPassInfo.BoneUpdater<R> neckVisibilitySetter = (renderPassInfoForBones, snapshots) -> {
            snapshots.get("Neck").map(bone -> {
                bone.skipRender(!(animatable.isInInventory || Compat.displayNeck()) && RenderingUtils.isFirstPerson(player));
                return null;
            });
        };

        renderPassInfo.addBoneUpdater(neckVisibilitySetter);

        Function<String, RenderPassInfo.BonePositionListener> bonePositionListenerCreator = boneName -> (worldPos, modelPos, localPos) -> {
            if (worldPos == null) return;
            Vec3 position = new Vec3(worldPos.x(), worldPos.y(), worldPos.z()).subtract(getModelOffset(animatable, 1));
            BONE_POSITIONS.computeIfAbsent(animatable.getId(), key -> new HashMap<>()).put(boneName, position);
        };

        final RenderPassInfo.BoneUpdater<R> addBreathBoneListener = (renderPassInfoForBones, snapshots) -> {
            // Need to store the positions per entity ourselves
            // Since the model is a singleton, and it stores the bones
            BONES.forEach(name -> snapshots.get(name).ifPresent(bone -> {
                renderPassInfo.addBonePositionListener(bone.getBone(), bonePositionListenerCreator.apply(bone.getBone().name()));
            }));
        };

        renderPassInfo.addBoneUpdater(addBreathBoneListener);

        DragonStateHandler handler = DragonStateProvider.getData(player);
        boolean hasWings = !handler.body().value().canHideWings() || handler.getCurrentStageCustomization().wings;
        final RenderPassInfo.BoneUpdater<R> wingBoneHider = (renderPassInfoForBones, snapshots) -> {
            for (String boneName : handler.body().value().bonesToHideForToggle()) {
                snapshots.get(boneName).ifPresent(bone -> bone.skipRender(!hasWings));
            }
        };

        renderPassInfo.addBoneUpdater(wingBoneHider);

        super.preRenderPass(renderPassInfo, renderTasks);
    }

    @Override
    public @Nullable RenderType getRenderType(R renderState, @NotNull Identifier texture) {
        DragonEntity animatable = renderState.getGeckolibData(DRAGON_ENTITY);
        Player player = animatable.getPlayer();

        if (player != null && HunterData.hasTransparency(player)) {
            return RenderTypes.itemEntityTranslucentCull(texture);
        }

        return RenderTypes.entityCutout(texture);
    }

    // Also used by the layers
    public int getRenderColor(final DragonEntity animatable) {
        boolean isInvisible = animatable.isInvisible();
        int color;

        //noinspection DataFlowIssue -> player is not null
        if (isInvisible && !animatable.isInvisibleTo(Minecraft.getInstance().player)) {
            color = TRANSPARENT_RENDER_COLOR;
        } else {
            color = RENDER_COLOR;
        }

        return HunterHandler.modifyAlpha(animatable.getPlayer(), color);
    }

    @Override
    public void addRenderData(DragonEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(DRAGON_ENTITY, animatable);
        renderState.addGeckolibData(DataTickets.RENDER_COLOR, getRenderColor(animatable));
    }

    /**
     * Note: Position does not work in first person <br>
     * - GeckoLib cannot update the bone positions if ClientDragonRenderer#renderInFirstPerson is not enabled <br>
     * - Even if it is enabled the position won't be correct - unsure as to why
     */
    public static Vec3 getBonePosition(final Player player, final String name) {
        DragonEntity dragon = ClientDragonRenderer.getDragon(player);

        if (dragon == null) {
            return Vec3.ZERO;
        }

        Map<String, Vec3> positions = BONE_POSITIONS.get(dragon.getId());

        if (positions == null) {
            return Vec3.ZERO;
        }

        return positions.getOrDefault(name, Vec3.ZERO);
    }

    @Override
    public void postRenderPass(@NotNull RenderPassInfo<@NotNull R> renderPassInfo, @NotNull SubmitNodeCollector renderTasks) {
        super.postRenderPass(renderPassInfo, renderTasks);

        DragonEntity animatable = renderPassInfo.getGeckolibData(DRAGON_ENTITY);
        Player player = animatable.getPlayer();
        DragonStateHandler handler = DragonStateProvider.getData(player);

        // If a body refresh was requested, all the animations will have been reset once we are post-render
        handler.refreshBody = false;

        Profiler.get().pop();
    }

    private Vec3 getModelOffset(final DragonEntity dragon, float partialTicks) {
        Player player = dragon.getPlayer();

        if (player == null) {
            return Vec3.ZERO;
        }

        float angle = -(float) MovementData.getData(player).bodyYaw * ((float) Math.PI / 180);
        float x = Mth.sin(angle);
        float z = Mth.cos(angle);

        DragonStateHandler handler = DragonStateProvider.getData(player);
        float scale = (float) handler.getVisualScale(player, partialTicks) * (float) handler.body().value().scalingProportions().scaleMultiplier();

        return new Vec3(x * scale, 0, z * scale);
    }

    public @NotNull Vec3 getRenderOffset(@NotNull final DragonEntity dragon, final float partialTicks) {
        return getModelOffset(dragon, partialTicks);
    }

    private void setupRender(final DragonEntity dragon, final Player player, final PoseStack pose, final float partialTick) {
        MovementData movement = MovementData.getData(player);

        // This is normally used in 'EntityRenderDispatcher#render', but that isn't triggered for 'DragonEntity'
        Vec3 offset = getRenderOffset(dragon, partialTick);
        pose.translate(-offset.x(), -offset.y(), -offset.z());

        pose.mulPose(Axis.YN.rotationDegrees((float) movement.bodyYaw));

        if (ServerFlightHandler.isGliding(player) || (player.isPassenger() && DragonStateProvider.isDragon(player.getVehicle()) && ServerFlightHandler.isGliding((Player) player.getVehicle()))) {
            // Responsible for the pitch (rotating entity downward / upward)
            pose.mulPose(Axis.XN.rotationDegrees(dragon.prevXRot));
            // Responsible for the roll (rotating entity to the side)
            pose.mulPose(Axis.ZP.rotation(dragon.prevZRot));
        }
    }

    @Override
    public void performRenderPass(R renderState, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector renderTasks, @NotNull CameraRenderState cameraState) {
        DragonEntity animatable = renderState.getGeckolibData(DRAGON_ENTITY);
        Player player = animatable.getPlayer();

        //noinspection DataFlowIssue -> player is present
        if (player == null || player.isSpectator() || player.isInvisibleTo(Minecraft.getInstance().player)) {
            return;
        }

        poseStack.pushPose();
        setupRender(animatable, player, poseStack, renderState.partialTick);
        super.performRenderPass(renderState, poseStack, renderTasks, cameraState);
        poseStack.popPose();
    }
}