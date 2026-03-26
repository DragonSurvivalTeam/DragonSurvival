package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
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
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.math.MathParser;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@EventBusSubscriber(Dist.CLIENT)
public class DragonRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<DragonEntity, R> {
    public static final Map<Integer, Map<String, Vec3>> BONE_POSITIONS = new HashMap<>();
    private static final List<String> BONES = List.of("BreathSource");
    private static final Map<Integer, DragonAnimationState> ANIMATION_STATES = new HashMap<>();
    private static final ThreadLocal<InventoryRenderOverrides> INVENTORY_RENDER_OVERRIDES = new ThreadLocal<>();

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
    public static final DataTicket<DragonRenderData> DRAGON_RENDER_DATA = DataTicket.create("dragonRenderData", DragonRenderData.class);

    public record DragonRenderData(
        int dragonId,
        @Nullable Player player,
        @Nullable Player texturePlayer,
        @Nullable DragonStateHandler handler,
        Identifier modelResource,
        boolean inInventory,
        boolean neckLocked,
        boolean tailLocked,
        boolean overrideUUIDWithLocalPlayerForTextureFetch,
        boolean spectator,
        boolean invisible,
        boolean invisibleToLocalPlayer,
        boolean hunterTransparent,
        boolean hasWings,
        List<String> bonesToHideForToggle,
        float prevXRot,
        float prevZRot,
        double bodyYaw,
        double headYaw,
        double headPitch,
        double gravity,
        double currentBodyYawChange,
        double currentHeadYawChange,
        double currentHeadPitchChange,
        double currentTailMotionUp,
        double visualScale,
        float bodyScaleMultiplier
    ) { }

    private static final class DragonAnimationState {
        private final List<Double> headYawHistory = new java.util.ArrayList<>();
        private double currentHeadYawChange;
        private final List<Double> bodyYawHistory = new java.util.ArrayList<>();
        private double currentBodyYawChange;
        private final List<Double> headPitchHistory = new java.util.ArrayList<>();
        private double currentHeadPitchChange;
        private final List<Double> verticalVelocityHistory = new java.util.ArrayList<>();
        private double currentTailMotionUp;
    }

    private record InventoryRenderOverrides(double bodyYaw, double headYaw, double headPitch) { }

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

    @SubscribeEvent
    public static void updateAnimationStatesOncePerFrame(final RenderFrameEvent.Pre event) {
        ClientDragonRenderer.process(DragonRenderer::updateAnimationState);
        FakeClientPlayerUtils.processDragons(DragonRenderer::updateAnimationState);
    }

    private DragonRenderData captureRenderData(final DragonEntity dragon, final float partialTick) {
        Player player = dragon.getPlayer();

        if (player == null) {
            return new DragonRenderData(
                dragon.getId(),
                null,
                null,
                null,
                DragonBody.DEFAULT_MODEL,
                dragon.isInInventory,
                dragon.neckLocked,
                dragon.tailLocked,
                dragon.overrideUUIDWithLocalPlayerForTextureFetch,
                false,
                false,
                false,
                false,
                true,
                List.of(),
                dragon.prevXRot,
                dragon.prevZRot,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                1,
                1
            );
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        DragonAnimationState animationState = ANIMATION_STATES.computeIfAbsent(dragon.getId(), key -> new DragonAnimationState());

        Identifier modelResource = handler.getModel();
        float bodyScaleMultiplier = handler.body() == null ? 1.0F : (float) handler.body().value().scalingProportions().scaleMultiplier();
        boolean hasWings = handler.body() == null || !handler.body().value().canHideWings() || handler.getCurrentStageCustomization().wings;
        List<String> bonesToHideForToggle = handler.body() == null ? List.of() : List.copyOf(handler.body().value().bonesToHideForToggle());

        DragonRenderData renderData = new DragonRenderData(
            dragon.getId(),
            player,
            dragon.overrideUUIDWithLocalPlayerForTextureFetch ? Minecraft.getInstance().player : player,
            handler,
            modelResource,
            dragon.isInInventory,
            dragon.neckLocked,
            dragon.tailLocked,
            dragon.overrideUUIDWithLocalPlayerForTextureFetch,
            player.isSpectator(),
            dragon.isInvisible(),
            player.isInvisibleTo(Minecraft.getInstance().player),
            HunterData.hasTransparency(player),
            hasWings,
            bonesToHideForToggle,
            dragon.prevXRot,
            dragon.prevZRot,
            MovementData.getData(player).bodyYaw,
            MovementData.getData(player).headYaw,
            MovementData.getData(player).headPitch,
            player.getAttributeValue(Attributes.GRAVITY),
            animationState.currentBodyYawChange,
            animationState.currentHeadYawChange,
            animationState.currentHeadPitchChange,
            animationState.currentTailMotionUp,
            handler.getVisualScale(player, partialTick),
            bodyScaleMultiplier
        );

        InventoryRenderOverrides inventoryRenderOverrides = INVENTORY_RENDER_OVERRIDES.get();

        if (inventoryRenderOverrides != null) {
            return createInventoryRenderData(renderData, inventoryRenderOverrides.bodyYaw(), inventoryRenderOverrides.headYaw(), inventoryRenderOverrides.headPitch());
        }

        return renderData;
    }

    public static void clearAnimationState(final int dragonId) {
        ANIMATION_STATES.remove(dragonId);
    }

    public static void clearAnimationStates() {
        ANIMATION_STATES.clear();
    }

    public static DragonRenderData createInventoryRenderData(final DragonRenderData renderData, final double bodyYaw, final double headYaw, final double headPitch) {
        return new DragonRenderData(
            renderData.dragonId(),
            renderData.player(),
            renderData.texturePlayer(),
            renderData.handler(),
            renderData.modelResource(),
            true,
            renderData.neckLocked(),
            renderData.tailLocked(),
            renderData.overrideUUIDWithLocalPlayerForTextureFetch(),
            renderData.spectator(),
            renderData.invisible(),
            renderData.invisibleToLocalPlayer(),
            false,
            renderData.hasWings(),
            renderData.bonesToHideForToggle(),
            renderData.prevXRot(),
            renderData.prevZRot(),
            bodyYaw,
            headYaw,
            headPitch,
            renderData.gravity(),
            0,
            0,
            0,
            0,
            renderData.visualScale(),
            renderData.bodyScaleMultiplier()
        );
    }

    public static void pushInventoryRenderOverrides(final double bodyYaw, final double headYaw, final double headPitch) {
        INVENTORY_RENDER_OVERRIDES.set(new InventoryRenderOverrides(bodyYaw, headYaw, headPitch));
    }

    public static void clearInventoryRenderOverrides() {
        INVENTORY_RENDER_OVERRIDES.remove();
    }

    public static boolean useInventoryAnimationCache() {
        return INVENTORY_RENDER_OVERRIDES.get() != null;
    }

    private static void updateAnimationState(final DragonEntity dragon) {
        Player player = dragon.getPlayer();

        if (player == null) {
            return;
        }

        DragonAnimationState animationState = ANIMATION_STATES.computeIfAbsent(dragon.getId(), key -> new DragonAnimationState());
        MovementData movement = MovementData.getData(player);
        float deltaTick = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
        float partialDeltaTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

        double bodyYawAvg;
        double headYawAvg;
        double headPitchAvg;
        double verticalVelocityAvg;

        if (!dragon.isInInventory) {
            double bodyYawChange = Functions.angleDifference(movement.bodyYaw, movement.bodyYawLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;
            double headYawChange = Functions.angleDifference(movement.headYaw, movement.headYawLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;
            double headPitchChange = Functions.angleDifference(movement.headPitch, movement.headPitchLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;

            double verticalVelocity = Mth.lerp(partialDeltaTick, movement.deltaMovementLastFrame.y, movement.deltaMovement.y) * DELTA_MOVEMENT_FACTOR;
            verticalVelocity *= 1 - Mth.abs(Mth.clampedMap(movement.prevXRot, -90, 90, -1, 1));

            float deltaTickFor60FPS = AnimationUtils.getDeltaTickFor60FPS();
            int removeSize = (int) (10 / deltaTickFor60FPS);

            if (dragon.clearVerticalVelocity) {
                animationState.verticalVelocityHistory.clear();

                while (animationState.verticalVelocityHistory.size() < removeSize) {
                    animationState.verticalVelocityHistory.add(0d);
                }
            }

            trimHistory(animationState.bodyYawHistory, removeSize);
            trimHistory(animationState.headYawHistory, removeSize);
            trimHistory(animationState.headPitchHistory, removeSize);
            trimHistory(animationState.verticalVelocityHistory, removeSize);

            animationState.bodyYawHistory.add(bodyYawChange);
            animationState.headYawHistory.add(headYawChange);
            animationState.headPitchHistory.add(headPitchChange);
            animationState.verticalVelocityHistory.add(verticalVelocity);

            bodyYawAvg = animationState.bodyYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            headYawAvg = animationState.headYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            headPitchAvg = animationState.headPitchHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            verticalVelocityAvg = animationState.verticalVelocityHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        } else {
            bodyYawAvg = 0;
            headYawAvg = 0;
            headPitchAvg = 0;
            verticalVelocityAvg = 0;
        }

        bodyYawAvg = Double.isNaN(bodyYawAvg) ? 0 : bodyYawAvg;
        headYawAvg = Double.isNaN(headYawAvg) ? 0 : headYawAvg;
        headPitchAvg = Double.isNaN(headPitchAvg) ? 0 : headPitchAvg;
        verticalVelocityAvg = Double.isNaN(verticalVelocityAvg) ? 0 : verticalVelocityAvg;

        double lerpRate = Math.min(1, deltaTick);
        animationState.currentBodyYawChange = Mth.lerp(lerpRate, animationState.currentBodyYawChange, bodyYawAvg);
        animationState.currentHeadYawChange = Mth.lerp(lerpRate, animationState.currentHeadYawChange, headYawAvg);
        animationState.currentHeadPitchChange = Mth.lerp(lerpRate, animationState.currentHeadPitchChange, headPitchAvg);

        if (dragon.clearVerticalVelocity) {
            animationState.currentTailMotionUp = 0;
            dragon.clearVerticalVelocity = false;
        } else {
            animationState.currentTailMotionUp = Mth.lerp(lerpRate, animationState.currentTailMotionUp, -verticalVelocityAvg);
        }
    }

    private static void trimHistory(final List<Double> history, final int removeSize) {
        while (history.size() > removeSize) {
            history.removeFirst();
        }
    }

    @Override
    public void setMolangQueryValues(DragonEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        super.setMolangQueryValues(animatable, relatedObject, renderState, partialTick);

        DragonRenderData renderData = renderState.getGeckolibData(DRAGON_RENDER_DATA);

        if (renderData == null || renderData.player() == null) {
            return;
        }

        if (renderData.neckLocked()) {
            MathParser.setVariable("query.head_yaw", state -> 0);
            MathParser.setVariable("query.head_pitch", state -> 0);
        } else {
            MathParser.setVariable("query.head_yaw", state -> renderData.headYaw());
            MathParser.setVariable("query.head_pitch", state -> renderData.headPitch());
        }

        MathParser.setVariable("query.gravity", state -> renderData.gravity());

        if (renderData.tailLocked()) {
            MathParser.setVariable("query.tail_motion_up", state -> 0);
            MathParser.setVariable("query.body_yaw_change", state -> 0);
        } else {
            MathParser.setVariable("query.body_yaw_change", state -> renderData.currentBodyYawChange());
            MathParser.setVariable("query.tail_motion_up", state -> renderData.currentTailMotionUp());
        }

        MathParser.setVariable("query.head_yaw_change", state -> renderData.currentHeadYawChange());
        MathParser.setVariable("query.head_pitch_change", state -> renderData.currentHeadPitchChange());
    }

    @Override
    public void preRenderPass(@NotNull RenderPassInfo<@NotNull R> renderPassInfo, @NotNull SubmitNodeCollector renderTasks) {
        DragonRenderData renderData = renderPassInfo.getGeckolibData(DRAGON_RENDER_DATA);

        if (renderData == null || renderData.player() == null) {
            super.preRenderPass(renderPassInfo, renderTasks);
            return;
        }

        Profiler.get().push("player_dragon");
        Player player = renderData.player();

        final RenderPassInfo.BoneUpdater<R> neckVisibilitySetter = (renderPassInfoForBones, snapshots) -> {
            snapshots.get("Neck").map(bone -> {
                bone.skipRender(!(renderData.inInventory() || Compat.displayNeck()) && RenderingUtils.isFirstPerson(player));
                return null;
            });
        };

        renderPassInfo.addBoneUpdater(neckVisibilitySetter);

        Function<String, RenderPassInfo.BonePositionListener> bonePositionListenerCreator = boneName -> (worldPos, modelPos, localPos) -> {
            if (worldPos == null) return;
            Vec3 position = new Vec3(worldPos.x(), worldPos.y(), worldPos.z()).subtract(getModelOffset(renderData));
            BONE_POSITIONS.computeIfAbsent(renderData.dragonId(), key -> new HashMap<>()).put(boneName, position);
        };

        final RenderPassInfo.BoneUpdater<R> addBreathBoneListener = (renderPassInfoForBones, snapshots) -> {
            // Need to store the positions per entity ourselves
            // Since the model is a singleton, and it stores the bones
            BONES.forEach(name -> snapshots.get(name).ifPresent(bone -> {
                renderPassInfo.addBonePositionListener(bone.getBone(), bonePositionListenerCreator.apply(bone.getBone().name()));
            }));
        };

        renderPassInfo.addBoneUpdater(addBreathBoneListener);

        final RenderPassInfo.BoneUpdater<R> wingBoneHider = (renderPassInfoForBones, snapshots) -> {
            for (String boneName : renderData.bonesToHideForToggle()) {
                snapshots.get(boneName).ifPresent(bone -> bone.skipRender(!renderData.hasWings()));
            }
        };

        renderPassInfo.addBoneUpdater(wingBoneHider);

        super.preRenderPass(renderPassInfo, renderTasks);
    }

    @Override
    public @Nullable RenderType getRenderType(R renderState, @NotNull Identifier texture) {
        DragonRenderData renderData = renderState.getGeckolibData(DRAGON_RENDER_DATA);

        if (renderData != null && renderData.hunterTransparent()) {
            return RenderTypes.itemTranslucent(texture);
        }

        return RenderTypes.entityCutout(texture);
    }

    // Also used by the layers
    public static int getRenderColor(final DragonRenderData renderData) {
        int color;

        if (renderData.invisible() && !renderData.invisibleToLocalPlayer()) {
            color = TRANSPARENT_RENDER_COLOR;
        } else {
            color = RENDER_COLOR;
        }

        return HunterHandler.modifyAlpha(renderData.player(), color);
    }

    @Override
    public void addRenderData(DragonEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        DragonRenderData renderData = captureRenderData(animatable, partialTick);
        renderState.addGeckolibData(DRAGON_RENDER_DATA, renderData);
        renderState.addGeckolibData(DataTickets.RENDER_COLOR, getRenderColor(renderData));
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

        DragonRenderData renderData = renderPassInfo.getGeckolibData(DRAGON_RENDER_DATA);

        if (renderData == null || renderData.handler() == null) {
            return;
        }

        // If a body refresh was requested, all the animations will have been reset once we are post-render
        renderData.handler().refreshBody = false;

        Profiler.get().pop();
    }

    private Vec3 getModelOffset(final DragonRenderData renderData) {
        if (renderData.player() == null) {
            return Vec3.ZERO;
        }

        MovementData movement = MovementData.getData(renderData.player());
        float angle = -(float)movement.bodyYaw * ((float)Math.PI / 180.0F);
        float x = Mth.sin(angle);
        float z = Mth.cos(angle);
        float scale = (float) renderData.visualScale() * renderData.bodyScaleMultiplier();

        return new Vec3(x * scale, 0, z * scale);
    }

    public @NotNull Vec3 getRenderOffset(@NotNull final DragonRenderData renderData) {
        return getModelOffset(renderData);
    }

    private void setupRender(final DragonRenderData renderData, final PoseStack pose) {
        // Offset the rendering so that the hitbox for the player is in the correct spot (near the dragon's head)
        Vec3 offset = getRenderOffset(renderData);
        pose.translate(-offset.x(), -offset.y(), -offset.z());

        pose.mulPose(Axis.YN.rotationDegrees((float) renderData.bodyYaw()));

        Player player = renderData.player();

        if (player != null && (ServerFlightHandler.isGliding(player) || (player.isPassenger() && DragonStateProvider.isDragon(player.getVehicle()) && ServerFlightHandler.isGliding((Player) player.getVehicle())))) {
            // Responsible for the pitch (rotating entity downward / upward)
            pose.mulPose(Axis.XN.rotationDegrees(renderData.prevXRot()));
            // Responsible for the roll (rotating entity to the side)
            pose.mulPose(Axis.ZP.rotation(renderData.prevZRot()));
        }
    }

    @Override
    public void performRenderPass(R renderState, PoseStack poseStack, SubmitNodeCollector renderTasks, CameraRenderState cameraState, List<RenderPassInfo.@Nullable BoneUpdater<R>> boneUpdater) {
        DragonRenderData renderData = renderState.getGeckolibData(DRAGON_RENDER_DATA);

        if (renderData == null || renderData.player() == null || renderData.spectator() || renderData.invisibleToLocalPlayer()) {
            return;
        }

        poseStack.pushPose();
        setupRender(renderData, poseStack);
        super.performRenderPass(renderState, poseStack, renderTasks, cameraState, boneUpdater);
        poseStack.popPose();
    }
}
