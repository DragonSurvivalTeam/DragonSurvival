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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
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
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

@EventBusSubscriber(Dist.CLIENT)
public class DragonRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<DragonEntity, R> {
    public static final Map<Integer, Map<String, Vec3>> BONE_POSITIONS = new HashMap<>();
    private static final List<String> BONES = List.of("BreathSource");
    private static final Map<Integer, DragonAnimationState> ANIMATION_STATES = new HashMap<>();

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

    public static final class DragonRenderData {
        private final int dragonId;
        private @Nullable Player player;
        private @Nullable Player texturePlayer;
        private @Nullable DragonStateHandler handler;
        private Identifier modelResource;

        /**
         * Used for inventory / smithing screen rendering - when set to true changed movement data will not be tracked <br>
         * - Does not set the movement data <br>
         * - Does not apply the molang history (of head pitch, body yaw, etc.) <br>
         * - Does not hide the head when in first person
         */
        private boolean inUI;

        private boolean neckLocked;
        private boolean tailLocked;
        private boolean overrideUUIDWithLocalPlayerForTextureFetch;
        private boolean spectator;
        private boolean invisible;
        private boolean invisibleToLocalPlayer;
        private boolean hunterTransparent;
        private boolean hasWings;
        private List<String> bonesToHideForToggle;
        private float prevXRot;
        private float prevZRot;
        private double bodyYaw;
        private double headYaw;
        private double headPitch;
        private double gravity;
        private double currentBodyYawChange;
        private double currentHeadYawChange;
        private double currentHeadPitchChange;
        private double currentTailMotionUp;
        private double visualScale;
        private float bodyScaleMultiplier;

        private DragonRenderData(final int dragonId) {
            this.dragonId = dragonId;
            this.modelResource = DragonBody.DEFAULT_MODEL;
            this.bonesToHideForToggle = List.of();
            this.hasWings = true;
            this.visualScale = 1;
            this.bodyScaleMultiplier = 1;
        }

        public static DragonRenderData fallback(final DragonEntity dragon) {
            DragonRenderData renderData = new DragonRenderData(dragon.getId());
            renderData.inUI = false;
            renderData.neckLocked = dragon.neckLocked;
            renderData.tailLocked = dragon.tailLocked;
            renderData.overrideUUIDWithLocalPlayerForTextureFetch = dragon.overrideUUIDWithLocalPlayerForTextureFetch;
            renderData.prevXRot = dragon.prevXRot;
            renderData.prevZRot = dragon.prevZRot;
            return renderData;
        }

        public static DragonRenderData live(
            final DragonEntity dragon,
            final Player player,
            final @Nullable Player texturePlayer,
            final DragonStateHandler handler,
            final Identifier modelResource,
            final boolean hasWings,
            final List<String> bonesToHideForToggle,
            final DragonAnimationState animationState,
            final float partialTick
        ) {
            DragonRenderData renderData = fallback(dragon);
            renderData.player = player;
            renderData.texturePlayer = texturePlayer;
            renderData.handler = handler;
            renderData.modelResource = modelResource;
            renderData.spectator = player.isSpectator();
            renderData.invisible = dragon.isInvisible();
            renderData.invisibleToLocalPlayer = player.isInvisibleTo(Minecraft.getInstance().player);
            renderData.hunterTransparent = HunterData.hasTransparency(player);
            renderData.hasWings = hasWings;
            renderData.bonesToHideForToggle = bonesToHideForToggle;
            renderData.bodyYaw = MovementData.getData(player).bodyYaw;
            renderData.headYaw = MovementData.getData(player).headYaw;
            renderData.headPitch = MovementData.getData(player).headPitch;
            renderData.gravity = player.getAttributeValue(Attributes.GRAVITY);
            renderData.currentBodyYawChange = animationState.currentBodyYawChange;
            renderData.currentHeadYawChange = animationState.currentHeadYawChange;
            renderData.currentHeadPitchChange = animationState.currentHeadPitchChange;
            renderData.currentTailMotionUp = animationState.currentTailMotionUp;
            renderData.visualScale = handler.getVisualScale(player, partialTick);
            renderData.bodyScaleMultiplier = handler.body() == null ? 1.0F : (float) handler.body().value().scalingProportions().scaleMultiplier();
            return renderData;
        }

        public DragonRenderData forUIRender(final double bodyYaw, final double headYaw, final double headPitch) {
            DragonRenderData renderData = copy();
            renderData.inUI = true;
            renderData.hunterTransparent = false;
            renderData.bodyYaw = bodyYaw;
            renderData.headYaw = headYaw;
            renderData.headPitch = headPitch;
            renderData.currentBodyYawChange = 0;
            renderData.currentHeadYawChange = 0;
            renderData.currentHeadPitchChange = 0;
            renderData.currentTailMotionUp = 0;
            return renderData;
        }

        public DragonRenderData copy() {
            DragonRenderData renderData = new DragonRenderData(this.dragonId);
            renderData.player = this.player;
            renderData.texturePlayer = this.texturePlayer;
            renderData.handler = this.handler;
            renderData.modelResource = this.modelResource;
            renderData.inUI = this.inUI;
            renderData.neckLocked = this.neckLocked;
            renderData.tailLocked = this.tailLocked;
            renderData.overrideUUIDWithLocalPlayerForTextureFetch = this.overrideUUIDWithLocalPlayerForTextureFetch;
            renderData.spectator = this.spectator;
            renderData.invisible = this.invisible;
            renderData.invisibleToLocalPlayer = this.invisibleToLocalPlayer;
            renderData.hunterTransparent = this.hunterTransparent;
            renderData.hasWings = this.hasWings;
            renderData.bonesToHideForToggle = this.bonesToHideForToggle;
            renderData.prevXRot = this.prevXRot;
            renderData.prevZRot = this.prevZRot;
            renderData.bodyYaw = this.bodyYaw;
            renderData.headYaw = this.headYaw;
            renderData.headPitch = this.headPitch;
            renderData.gravity = this.gravity;
            renderData.currentBodyYawChange = this.currentBodyYawChange;
            renderData.currentHeadYawChange = this.currentHeadYawChange;
            renderData.currentHeadPitchChange = this.currentHeadPitchChange;
            renderData.currentTailMotionUp = this.currentTailMotionUp;
            renderData.visualScale = this.visualScale;
            renderData.bodyScaleMultiplier = this.bodyScaleMultiplier;
            return renderData;
        }

        public int dragonId() { return dragonId; }
        public @Nullable Player player() { return player; }
        public @Nullable Player texturePlayer() { return texturePlayer; }
        public @Nullable DragonStateHandler handler() { return handler; }
        public Identifier modelResource() { return modelResource; }
        public boolean inInventory() { return inUI; }
        public boolean neckLocked() { return neckLocked; }
        public boolean tailLocked() { return tailLocked; }
        public boolean overrideUUIDWithLocalPlayerForTextureFetch() { return overrideUUIDWithLocalPlayerForTextureFetch; }
        public boolean spectator() { return spectator; }
        public boolean invisible() { return invisible; }
        public boolean invisibleToLocalPlayer() { return invisibleToLocalPlayer; }
        public boolean hunterTransparent() { return hunterTransparent; }
        public boolean hasWings() { return hasWings; }
        public List<String> bonesToHideForToggle() { return bonesToHideForToggle; }
        public float prevXRot() { return prevXRot; }
        public float prevZRot() { return prevZRot; }
        public double bodyYaw() { return bodyYaw; }
        public double headYaw() { return headYaw; }
        public double headPitch() { return headPitch; }
        public double gravity() { return gravity; }
        public double currentBodyYawChange() { return currentBodyYawChange; }
        public double currentHeadYawChange() { return currentHeadYawChange; }
        public double currentHeadPitchChange() { return currentHeadPitchChange; }
        public double currentTailMotionUp() { return currentTailMotionUp; }
        public double visualScale() { return visualScale; }
        public float bodyScaleMultiplier() { return bodyScaleMultiplier; }
    }

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
            return DragonRenderData.fallback(dragon);
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        DragonAnimationState animationState = ANIMATION_STATES.computeIfAbsent(dragon.getId(), key -> new DragonAnimationState());

        Identifier modelResource = handler.getModel();
        boolean hasWings = handler.body() == null || !handler.body().value().canHideWings() || handler.getCurrentStageCustomization().wings;
        List<String> bonesToHideForToggle = handler.body() == null ? List.of() : List.copyOf(handler.body().value().bonesToHideForToggle());

        DragonRenderData renderData = DragonRenderData.live(
            dragon,
            player,
            dragon.overrideUUIDWithLocalPlayerForTextureFetch ? Minecraft.getInstance().player : player,
            handler,
            modelResource,
            hasWings,
            bonesToHideForToggle,
            animationState,
            partialTick
        );

        return renderData;
    }

    public static void clearAnimationState(final int dragonId) {
        ANIMATION_STATES.remove(dragonId);
    }

    public static void clearAnimationStates() {
        ANIMATION_STATES.clear();
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

        double bodyYawAvg;
        double headYawAvg;
        double headPitchAvg;
        double verticalVelocityAvg;

        bodyYawAvg = animationState.bodyYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        headYawAvg = animationState.headYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        headPitchAvg = animationState.headPitchHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        verticalVelocityAvg = animationState.verticalVelocityHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);

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

    public static EntityRenderState createUIRenderState(final LivingEntity entity, final float partialTick, final double bodyYaw, final double headYaw, final double headPitch) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, partialTick);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;

        GeoRenderState geoRenderState = (GeoRenderState)renderState;
        DragonRenderData dragonRenderData = geoRenderState.getGeckolibData(DRAGON_RENDER_DATA);

        if (dragonRenderData != null) {
            geoRenderState.addGeckolibData(DRAGON_RENDER_DATA, dragonRenderData.forUIRender(bodyYaw, headYaw, headPitch));

            // GeckoLib compiles controller state during render-state creation, so refresh the query providers
            // after swapping in UI-specific dragon data.
            overrideMolangQueryValues(geoRenderState);
        }

        return renderState;
    }

    public static void overrideMolangQueryValues(GeoRenderState renderState)
    {
        if (renderState.getGeckolibData(DRAGON_RENDER_DATA) == null) {
            return;
        }

        setDragonQueryValue("query.head_yaw", DragonRenderData::headYaw, DragonRenderData::neckLocked);
        setDragonQueryValue("query.head_pitch", DragonRenderData::headPitch, DragonRenderData::neckLocked);
        setDragonQueryValue("query.gravity", DragonRenderData::gravity);
        setDragonQueryValue("query.body_yaw_change", DragonRenderData::currentBodyYawChange, DragonRenderData::tailLocked);
        setDragonQueryValue("query.tail_motion_up", DragonRenderData::currentTailMotionUp, DragonRenderData::tailLocked);
        setDragonQueryValue("query.head_yaw_change", DragonRenderData::currentHeadYawChange);
        setDragonQueryValue("query.head_pitch_change", DragonRenderData::currentHeadPitchChange);
    }

    private static @Nullable DragonRenderData getDragonRenderDataForQuery(final GeoRenderState renderState) {
        return renderState.getGeckolibData(DRAGON_RENDER_DATA);
    }

    private static void setDragonQueryValue(final String queryName, final ToDoubleFunction<DragonRenderData> valueGetter) {
        MathParser.setVariable(queryName, state -> {
            DragonRenderData renderData = getDragonRenderDataForQuery(state.renderState());

            return renderData == null ? 0 : valueGetter.applyAsDouble(renderData);
        });
    }

    private static void setDragonQueryValue(final String queryName, final ToDoubleFunction<DragonRenderData> valueGetter, final Predicate<DragonRenderData> zeroWhen) {
        MathParser.setVariable(queryName, state -> {
            DragonRenderData renderData = getDragonRenderDataForQuery(state.renderState());

            if (renderData == null || zeroWhen.test(renderData)) {
                return 0;
            }

            return valueGetter.applyAsDouble(renderData);
        });
    }

    @Override
    public void setMolangQueryValues(DragonEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        super.setMolangQueryValues(animatable, relatedObject, renderState, partialTick);
        overrideMolangQueryValues(renderState);
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
