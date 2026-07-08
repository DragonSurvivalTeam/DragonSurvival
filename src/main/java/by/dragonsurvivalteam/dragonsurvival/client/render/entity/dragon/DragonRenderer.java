package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.DragonAnimations;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.math.MathParser;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

@EventBusSubscriber(Dist.CLIENT)
public class DragonRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<DragonEntity, R> {
    public static final Map<Integer, Map<String, Vec3>> BONE_POSITIONS = new HashMap<>();
    private static final List<String> BONES = List.of("BreathSource");
    private static final Map<Integer, DragonAnimationState> ANIMATION_STATES = new HashMap<>();
    private static final Map<Long, Map<String, BonePose>> LAST_RENDERED_BONE_POSES = new HashMap<>();
    private static final Map<Long, InterruptedTransitionBlend> INTERRUPTED_TRANSITION_BLENDS = new HashMap<>();
    private static final Map<Long, Double> UI_RENDER_START_TICKS = new HashMap<>();
    private static final Map<Integer, UIRenderDragonEntity> UI_RENDER_DRAGONS = new HashMap<>();
    private static final ThreadLocal<UIRenderContext> UI_RENDER_CONTEXT = new ThreadLocal<>();
    private static final long UI_RENDER_ID_MASK = 1L << 62;

    private static final int RENDER_COLOR = ARGB.color(255, 255, 255);
    private static final int TRANSPARENT_RENDER_COLOR = ARGB.colorFromFloat(HunterHandler.MIN_ALPHA, 1, 1, 1);

    /** Factor to multiply the delta yaw and pitch by, needed for scaling for the animations */
    private static final double DELTA_YAW_PITCH_FACTOR = 0.2;

    /** Factor to multiply the delta movement by, needed for scaling for the animations */
    private static final double DELTA_MOVEMENT_FACTOR = 10;

    // Data tickets
    public static final DataTicket<DragonRenderData> DRAGON_RENDER_DATA = DataTicket.create("dragonRenderData", DragonRenderData.class);

    public static final class DragonRenderData {
        private final int dragonId;
        private long renderCacheId;
        private final DragonEntity dragon;
        private @Nullable Player player;
        private @Nullable Player texturePlayer;
        private @Nullable DragonStateHandler handler;
        private Identifier modelResource;
        private @Nullable Identifier textureOverride;
        private @Nullable Identifier glowTextureOverride;

        /**
         * Used for inventory / smithing screen rendering - when set to true changed movement data will not be tracked <br>
         * - Does not set the movement data <br>
         * - Does not apply the molang history (of head pitch, body yaw, etc.) <br>
         * - Does not hide the head when in first person
         */
        private boolean inUI;

        private boolean neckLocked;
        private boolean tailLocked;
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

        private DragonRenderData(final DragonEntity dragon) {
            this.dragon = dragon;
            this.dragonId = dragon.getId();
            this.renderCacheId = Integer.toUnsignedLong(dragon.getId());
            this.modelResource = DragonBody.DEFAULT_MODEL;
            this.bonesToHideForToggle = List.of();
            this.hasWings = true;
            this.visualScale = 1;
            this.bodyScaleMultiplier = 1;
        }

        public static DragonRenderData fallback(final DragonEntity dragon) {
            DragonRenderData renderData = new DragonRenderData(dragon);
            renderData.inUI = false;
            renderData.neckLocked = dragon.neckLocked;
            renderData.tailLocked = dragon.tailLocked;
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

        public DragonRenderData forUIRender(
            final double bodyYaw,
            final double headYaw,
            final double headPitch,
            final @Nullable Player texturePlayer,
            final @Nullable Identifier textureOverride,
            final @Nullable Identifier glowTextureOverride
        ) {
            DragonRenderData renderData = copy();
            renderData.renderCacheId = uiRenderCacheId(renderData.dragonId);
            renderData.inUI = true;
            renderData.texturePlayer = texturePlayer != null ? texturePlayer : renderData.texturePlayer;
            renderData.textureOverride = textureOverride;
            renderData.glowTextureOverride = glowTextureOverride;
            renderData.neckLocked = false;
            renderData.tailLocked = false;
            renderData.hunterTransparent = false;
            renderData.prevXRot = 0.0F;
            renderData.prevZRot = 0.0F;
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
            DragonRenderData renderData = new DragonRenderData(this.dragon);
            renderData.renderCacheId = this.renderCacheId;
            renderData.player = this.player;
            renderData.texturePlayer = this.texturePlayer;
            renderData.handler = this.handler;
            renderData.modelResource = this.modelResource;
            renderData.textureOverride = this.textureOverride;
            renderData.glowTextureOverride = this.glowTextureOverride;
            renderData.inUI = this.inUI;
            renderData.neckLocked = this.neckLocked;
            renderData.tailLocked = this.tailLocked;
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
        public long renderCacheId() { return renderCacheId; }
        public DragonEntity dragon() { return dragon; }
        public @Nullable Player player() { return player; }
        public @Nullable Player texturePlayer() { return texturePlayer; }
        public @Nullable DragonStateHandler handler() { return handler; }
        public Identifier modelResource() { return modelResource; }
        public @Nullable Identifier textureOverride() { return textureOverride; }
        public @Nullable Identifier glowTextureOverride() { return glowTextureOverride; }
        public boolean inInventory() { return inUI; }
        public boolean neckLocked() { return neckLocked; }
        public boolean tailLocked() { return tailLocked; }
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

    private record InterruptedTransitionBlend(double startAge, int transitionTicks, Map<String, BonePose> startPoses) { }

    private record UIRenderContext(
        double bodyYaw,
        double headYaw,
        double headPitch,
        @Nullable Player texturePlayer,
        @Nullable Identifier textureOverride,
        @Nullable Identifier glowTextureOverride
    ) { }

    private static final class UIRenderDragonEntity extends DragonEntity {
        private @Nullable Player player;
        private @Nullable Supplier<String> animationSupplier;
        private @Nullable String cachedAnimationName;
        private @Nullable RawAnimation cachedAnimation;

        private UIRenderDragonEntity(final Player player) {
            super(DSEntities.DRAGON.get(), player.level());
            setPlayer(player);
        }

        private void setPlayer(final Player player) {
            this.player = player;
            this.playerId = uiDragonPlayerId(player.getId());
            this.animationSupplier = player instanceof FakeClientPlayer fakePlayer ? fakePlayer.animationSupplier : null;
        }

        @Override
        public @Nullable Player getPlayer() {
            return player;
        }

        @Override
        public @NotNull Vec3 position() {
            return new Vec3(getX(), getY(), getZ());
        }

        @Override
        public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
            mainAnimationController = new AnimationController<>("ui_main", 0, state -> {
                boolean hasReset = false;
                if (player instanceof FakeClientPlayer fakeClientPlayer && fakeClientPlayer.handler.refreshBody) {
                    state.controller().reset();
                    fakeClientPlayer.handler.refreshBody = false;
                    hasReset = true;
                }
                state.controller().setTransitionTicks(cachedAnimation == null || hasReset ? 0 : 2);
                return state.setAndContinue(getUIAnimation());
            });
            controllers.add(mainAnimationController);
        }

        private RawAnimation getUIAnimation() {
            if (animationSupplier == null) {
                cachedAnimationName = null;
                cachedAnimation = null;
                return DragonAnimations.IDLE.getAnimation();
            }

            String animationName = animationSupplier.get();

            if (animationName == null || animationName.isBlank()) {
                cachedAnimationName = null;
                cachedAnimation = null;
                return DragonAnimations.IDLE.getAnimation();
            }

            if (!animationName.equals(cachedAnimationName) || cachedAnimation == null) {
                cachedAnimationName = animationName;
                cachedAnimation = RawAnimation.begin().thenLoop(animationName);
            }

            return cachedAnimation;
        }
    }

    private record BonePose(
        float scaleX,
        float scaleY,
        float scaleZ,
        float translateX,
        float translateY,
        float translateZ,
        float rotX,
        float rotY,
        float rotZ,
        boolean hidden,
        boolean childrenHidden
    ) {
        private static BonePose from(final BoneSnapshot snapshot) {
            return new BonePose(
                snapshot.getScaleX(),
                snapshot.getScaleY(),
                snapshot.getScaleZ(),
                snapshot.getTranslateX(),
                snapshot.getTranslateY(),
                snapshot.getTranslateZ(),
                snapshot.getRotX(),
                snapshot.getRotY(),
                snapshot.getRotZ(),
                snapshot.isHidden(),
                snapshot.areChildrenHidden()
            );
        }

        private BonePose lerp(final BonePose target, final float amount) {
            return new BonePose(
                Mth.lerp(amount, scaleX, target.scaleX),
                Mth.lerp(amount, scaleY, target.scaleY),
                Mth.lerp(amount, scaleZ, target.scaleZ),
                Mth.lerp(amount, translateX, target.translateX),
                Mth.lerp(amount, translateY, target.translateY),
                Mth.lerp(amount, translateZ, target.translateZ),
                Mth.lerp(amount, rotX, target.rotX),
                Mth.lerp(amount, rotY, target.rotY),
                Mth.lerp(amount, rotZ, target.rotZ),
                target.hidden,
                target.childrenHidden
            );
        }

        private void applyTo(final BoneSnapshot snapshot) {
            snapshot.setScale(scaleX, scaleY, scaleZ);
            snapshot.setTranslation(translateX, translateY, translateZ);
            snapshot.setRotation(rotX, rotY, rotZ);
            snapshot.skipRender(hidden);
            snapshot.skipChildrenRender(childrenHidden);
        }
    }

    public DragonRenderer(final EntityRendererProvider.Context context, final GeoModel<DragonEntity> model) {
        super(context, model);

        withRenderLayer(new DragonGlowLayerRenderer<>(this));
        withRenderLayer(new DragonArmorRenderLayer<>(this));
        withRenderLayer(new DragonItemRenderLayer<>(context, this));

        // FIXME :: SOPHISTICATED BACKPACKS
        /*if (ModCheck.isModLoaded(ModCheck.SOPHISTICATED_BACKPACKS)) {
            getRenderLayers().add(new DragonBackpackRenderLayer(this));
        }*/
    }

    private static long uiRenderCacheId(final int dragonId) {
        return UI_RENDER_ID_MASK | Integer.toUnsignedLong(dragonId);
    }

    private static int uiDragonPlayerId(final int playerId) {
        return Integer.MIN_VALUE ^ playerId;
    }

    private static DragonRenderData applyUIRenderContext(final DragonRenderData renderData) {
        UIRenderContext uiRenderContext = UI_RENDER_CONTEXT.get();

        if (uiRenderContext == null) {
            return renderData;
        }

        return renderData.forUIRender(
            uiRenderContext.bodyYaw(),
            uiRenderContext.headYaw(),
            uiRenderContext.headPitch(),
            uiRenderContext.texturePlayer(),
            uiRenderContext.textureOverride(),
            uiRenderContext.glowTextureOverride()
        );
    }

    private static LivingEntity getEntityForUIRender(final LivingEntity entity) {
        Player player = null;

        if (entity instanceof DragonEntity dragon) {
            player = dragon.getPlayer();
        } else if (entity instanceof Player playerEntity) {
            player = playerEntity;
        }

        if (!DragonStateProvider.isDragon(player)) {
            return entity;
        }

        UIRenderDragonEntity uiDragon = getOrCreateUIRenderDragon(player);
        syncUIRenderDragon(player, uiDragon);
        return uiDragon;
    }

    private static UIRenderDragonEntity getOrCreateUIRenderDragon(final Player player) {
        UIRenderDragonEntity dragon = UI_RENDER_DRAGONS.get(player.getId());

        if (dragon == null || dragon.level() != player.level()) {
            if (dragon != null) {
                clearRenderState(dragon.getId());
            }

            dragon = new UIRenderDragonEntity(player);
            UI_RENDER_DRAGONS.put(player.getId(), dragon);
        } else {
            dragon.setPlayer(player);
        }

        return dragon;
    }

    private static void syncUIRenderDragon(final Player player, final DragonEntity targetDragon) {
        Vec3 position = player.position();
        targetDragon.setPos(position);
        targetDragon.xOld = position.x;
        targetDragon.yOld = position.y;
        targetDragon.zOld = position.z;
        targetDragon.xo = position.x;
        targetDragon.yo = position.y;
        targetDragon.zo = position.z;
        targetDragon.tickCount = player.tickCount;
        targetDragon.setYRot(0.0F);
        targetDragon.setYBodyRot(0.0F);
        targetDragon.setYHeadRot(0.0F);
        targetDragon.setXRot(0.0F);
        targetDragon.yRotO = 0.0F;
        targetDragon.yBodyRotO = 0.0F;
        targetDragon.yHeadRotO = 0.0F;
        targetDragon.xRotO = 0.0F;
        targetDragon.neckLocked = false;
        targetDragon.tailLocked = false;
        targetDragon.clearVerticalVelocity = false;
        targetDragon.prevXRot = 0.0F;
        targetDragon.prevZRot = 0.0F;
    }

    @Override
    public long getInstanceId(final DragonEntity animatable, final @Nullable Void relatedObject) {
        if (UI_RENDER_CONTEXT.get() == null) {
            return super.getInstanceId(animatable, relatedObject);
        }

        return uiRenderCacheId(animatable.getId());
    }

    @SubscribeEvent
    public static void updateAnimationStatesOncePerFrame(final RenderFrameEvent.Pre event) {
        ClientDragonRenderer.process(DragonRenderer::updateAnimationState);
        FakeClientPlayerUtils.processDragons(DragonRenderer::updateAnimationState);
    }

    private DragonRenderData captureRenderData(final DragonEntity dragon, final float partialTick) {
        Player player = dragon.getPlayer();

        if (player == null) {
            return applyUIRenderContext(DragonRenderData.fallback(dragon));
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        DragonAnimationState animationState = ANIMATION_STATES.computeIfAbsent(dragon.getId(), key -> new DragonAnimationState());

        Identifier modelResource = handler.getModel();
        boolean hasWings = handler.body() == null || !handler.body().value().canHideWings() || handler.getCurrentStageCustomization().wings;
        List<String> bonesToHideForToggle = handler.body() == null ? List.of() : List.copyOf(handler.body().value().bonesToHideForToggle());

        DragonRenderData renderData = DragonRenderData.live(
            dragon,
            player,
            player,
            handler,
            modelResource,
            hasWings,
            bonesToHideForToggle,
            animationState,
            partialTick
        );

        return applyUIRenderContext(renderData);
    }

    public static void clearRenderState(final int dragonId) {
        BONE_POSITIONS.remove(dragonId);
        clearAnimationState(dragonId);
    }

    public static void clearRenderStates() {
        BONE_POSITIONS.clear();
        clearAnimationStates();
        UI_RENDER_DRAGONS.clear();
    }

    public static void clearUIRenderDragon(final int playerId) {
        DragonEntity dragon = UI_RENDER_DRAGONS.remove(playerId);

        if (dragon != null) {
            clearRenderState(dragon.getId());
        }
    }

    public static void clearAnimationState(final int dragonId) {
        ANIMATION_STATES.remove(dragonId);
        LAST_RENDERED_BONE_POSES.remove(Integer.toUnsignedLong(dragonId));
        LAST_RENDERED_BONE_POSES.remove(uiRenderCacheId(dragonId));
        INTERRUPTED_TRANSITION_BLENDS.remove(Integer.toUnsignedLong(dragonId));
        INTERRUPTED_TRANSITION_BLENDS.remove(uiRenderCacheId(dragonId));
        UI_RENDER_START_TICKS.remove(uiRenderCacheId(dragonId));
    }

    public static void clearAnimationStates() {
        ANIMATION_STATES.clear();
        LAST_RENDERED_BONE_POSES.clear();
        INTERRUPTED_TRANSITION_BLENDS.clear();
        UI_RENDER_START_TICKS.clear();
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
        return createUIRenderState(entity, partialTick, bodyYaw, headYaw, headPitch, null);
    }

    public static EntityRenderState createUIRenderState(final LivingEntity entity, final float partialTick, final double bodyYaw, final double headYaw, final double headPitch, final @Nullable Player texturePlayer) {
        return createUIRenderState(entity, partialTick, bodyYaw, headYaw, headPitch, texturePlayer, null, null);
    }

    public static EntityRenderState createUIRenderState(
        final LivingEntity entity,
        final float partialTick,
        final double bodyYaw,
        final double headYaw,
        final double headPitch,
        final @Nullable Player texturePlayer,
        final @Nullable Identifier textureOverride,
        final @Nullable Identifier glowTextureOverride
    ) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        UIRenderContext previousContext = UI_RENDER_CONTEXT.get();
        UI_RENDER_CONTEXT.set(new UIRenderContext(bodyYaw, headYaw, headPitch, texturePlayer, textureOverride, glowTextureOverride));

        try {
            LivingEntity renderEntity = getEntityForUIRender(entity);
            EntityRenderer<? super LivingEntity, ?> renderer = entityRenderDispatcher.getRenderer(renderEntity);
            EntityRenderState renderState = renderer.createRenderState(renderEntity, partialTick);
            renderState.shadowPieces.clear();
            renderState.outlineColor = 0;

            if (renderState instanceof GeoRenderState geoRenderState) {
                DragonRenderData dragonRenderData = geoRenderState.getGeckolibData(DRAGON_RENDER_DATA);

                if (dragonRenderData != null) {
                    if (!dragonRenderData.inInventory()) {
                        dragonRenderData = dragonRenderData.forUIRender(bodyYaw, headYaw, headPitch, texturePlayer, textureOverride, glowTextureOverride);
                        geoRenderState.addGeckolibData(DRAGON_RENDER_DATA, dragonRenderData);
                    }

                    // GeckoLib sets up controller state during render state creation, so refresh the query providers
                    // after swapping in UI-specific dragon data.
                    overrideMolangQueryValues(geoRenderState);
                }
            }

            return renderState;
        } finally {
            if (previousContext == null) {
                UI_RENDER_CONTEXT.remove();
            } else {
                UI_RENDER_CONTEXT.set(previousContext);
            }
        }
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

    public static boolean isUIRenderState(final GeoRenderState renderState) {
        DragonRenderData renderData = renderState.getGeckolibData(DRAGON_RENDER_DATA);
        return renderData != null && renderData.inInventory();
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
                boolean hideNeckAndHead = !(renderData.inInventory() || Compat.displayNeck()) && RenderingUtils.isFirstPerson(player);
                bone.skipRender(hideNeckAndHead);
                bone.skipChildrenRender(hideNeckAndHead);
                return null;
            });
        };

        renderPassInfo.addBoneUpdater(neckVisibilitySetter);

        Function<String, RenderPassInfo.BonePositionListener> bonePositionListenerCreator = boneName -> (worldPos, modelPos, localPos) -> {
            if (worldPos == null) return;
            if (renderData.inInventory()) return;
            Vec3 position = transformBonePosition(renderData, new Vec3(worldPos.x(), worldPos.y(), worldPos.z()));
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

        if (!renderData.inInventory()) {
            renderPassInfo.addBoneUpdater((renderPassInfoForBones, snapshots) -> smoothInterruptedAnimationTransition(renderData, renderPassInfoForBones, snapshots));
        }

        DragonEditorHandler.ensureSkinTexturesGenerated(renderData.texturePlayer(), renderData.handler());

        super.preRenderPass(renderPassInfo, renderTasks);
    }

    /**
     * GeckoLib 4 had this behavior before, but for some reason it is gone now. So I had to re-implement it myself.
     * Without this, the transitions between animations for the dragon will be snappy/stuttery
     */
    private static void smoothInterruptedAnimationTransition(final DragonRenderData renderData, final RenderPassInfo<?> renderPassInfo, final BoneSnapshots snapshots) {
        long renderCacheId = renderData.renderCacheId();
        DragonEntity dragon = renderData.dragon();

        if (dragon.interruptedAnimationTransition) {
            Map<String, BonePose> startPoses = LAST_RENDERED_BONE_POSES.get(renderCacheId);

            if (startPoses != null && !startPoses.isEmpty()) {
                INTERRUPTED_TRANSITION_BLENDS.put(renderCacheId, new InterruptedTransitionBlend(
                    dragon.interruptedAnimationTransitionStartAge,
                    dragon.interruptedAnimationTransitionTicks,
                    new HashMap<>(startPoses)
                ));
            }

            dragon.interruptedAnimationTransition = false;
        }

        InterruptedTransitionBlend blend = INTERRUPTED_TRANSITION_BLENDS.get(renderCacheId);
        float blendAmount = 1;

        if (blend != null) {
            if (blend.transitionTicks() <= 0) {
                INTERRUPTED_TRANSITION_BLENDS.remove(renderCacheId);
                blend = null;
            } else {
                blendAmount = (float) Math.max(0, Math.min(1, (renderPassInfo.renderState().getAnimatableAge() - blend.startAge()) / blend.transitionTicks()));

                if (blendAmount >= 1) {
                    INTERRUPTED_TRANSITION_BLENDS.remove(renderCacheId);
                }
            }
        }

        Map<String, BonePose> renderedPoses = LAST_RENDERED_BONE_POSES.computeIfAbsent(renderCacheId, key -> new HashMap<>());
        InterruptedTransitionBlend activeBlend = blend;
        float activeBlendAmount = blendAmount;

        renderPassInfo.model().boneLookup().get().forEach((boneName, bone) -> snapshots.get(boneName).ifPresent(snapshot -> {
            BonePose currentPose = BonePose.from(snapshot);

            if (activeBlend != null) {
                BonePose startPose = activeBlend.startPoses().get(boneName);

                if (startPose != null) {
                    currentPose = startPose.lerp(currentPose, activeBlendAmount);
                    currentPose.applyTo(snapshot);
                }
            }

            renderedPoses.put(boneName, currentPose);
        }));
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
        Player player = renderData.player();

        if (player != null) {
            renderState.addGeckolibData(DataTickets.TICK, getRenderTick(renderData, player, partialTick));
        }

        renderState.addGeckolibData(DRAGON_RENDER_DATA, renderData);
        renderState.addGeckolibData(DataTickets.RENDER_COLOR, getRenderColor(renderData));
    }

    private static double getRenderTick(final DragonRenderData renderData, final Player player, final float partialTick) {
        if (!renderData.inInventory() && !(player instanceof FakeClientPlayer)) {
            return player.tickCount + partialTick + 200;
        }

        double tick = System.nanoTime() / 50_000_000.0D;
        double startTick = UI_RENDER_START_TICKS.computeIfAbsent(renderData.renderCacheId(), key -> tick);
        return tick - startTick + 200;
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

        if (!renderData.inInventory()) {
            // If a body refresh was requested, all the animations will have been reset once we are post-render
            renderData.handler().refreshBody = false;
        }

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
        if (renderData.inInventory()) {
            return Vec3.ZERO;
        }

        return getModelOffset(renderData);
    }

    private Vec3 transformBonePosition(final DragonRenderData renderData, final Vec3 position) {
        Player player = renderData.player();

        if (player == null) {
            return position;
        }

        Vec3 relativePosition = position.subtract(player.position());
        PoseStack poseStack = new PoseStack();
        setupRender(renderData, poseStack);
        Vector4f transformedPosition = poseStack.last().pose().transform(new Vector4f((float) relativePosition.x(), (float) relativePosition.y(), (float) relativePosition.z(), 1.0F));

        return new Vec3(transformedPosition.x(), transformedPosition.y(), transformedPosition.z()).add(player.position());
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
