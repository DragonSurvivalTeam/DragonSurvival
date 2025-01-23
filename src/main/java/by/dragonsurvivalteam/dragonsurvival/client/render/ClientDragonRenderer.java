package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.EntityRendererAccessor;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.LivingRendererAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncDeltaMovement;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonMovement;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.DiscTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.DragonBreathTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.LookingAtTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.RenderUtil;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(Dist.CLIENT)
public class ClientDragonRenderer {
    public static DragonModel dragonModel = new DragonModel();

    /**
     * Used for inventory rendering - when set to true changed movement data will not be tracked <br>
     * See {@link ClientDragonRenderer#setDragonMovementData(Player, float)} and {@link DragonModel#applyMolangQueries(AnimationState, double)}
     */
    public static boolean isOverridingMovementData = false;

    /** Instances used for rendering third-person dragon models */
    public static final Map<Integer, DragonEntity> PLAYER_DRAGON_MAP = new HashMap<>();

    @Translation(key = "render_dragon_in_first_person", type = Translation.Type.CONFIGURATION, comments = "If enabled the dragon body will be visible in first person")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_dragon_in_first_person")
    public static Boolean renderInFirstPerson = true;

    @Translation(key = "render_first_person_flight", type = Translation.Type.CONFIGURATION, comments = "If enabled the dragon body will be visible in first person while flying")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_first_person_flight")
    public static Boolean renderFirstPersonFlight = false;

    @Translation(key = "render_items_in_mouth", type = Translation.Type.CONFIGURATION, comments = {"If enabled held items will be rendered neat the mouth of the dragon", "If disabled held items will be displayed on the side of the dragon"})
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_items_in_mouth")
    public static Boolean renderItemsInMouth = false;

    @Translation(key = "render_held_item", type = Translation.Type.CONFIGURATION, comments = "If enabled items will be rendered for dragons while in third person mode")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_held_item")
    public static boolean renderHeldItem = true;

    @Translation(key = "render_dragon_claws", type = Translation.Type.CONFIGURATION, comments = "If enabled dragon claws and teeth will have an overlay depending on the items in the claw slots")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_dragon_claws")
    public static Boolean renderDragonClaws = true;

    @Translation(key = "render_custom_skin", type = Translation.Type.CONFIGURATION, comments = "If enabled your custom dragon skin will be rendered")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_custom_skin")
    public static Boolean renderCustomSkin = true;

    @Translation(key = "render_other_players_custom_skins", type = Translation.Type.CONFIGURATION, comments = "If enabled custom skins of other players will be rendered")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_other_players_custom_skins")
    public static Boolean renderOtherPlayerSkins = true;

    @Translation(key = "dragon_name_tags", type = Translation.Type.CONFIGURATION, comments = "If enabled name tags will be shown for dragons")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "dragon_name_tags")
    public static Boolean dragonNameTags = false;

    @SubscribeEvent
    public static void renderAbilityHitbox(final RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_SOLID_BLOCKS && Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player == null) {
                return;
            }

            if (!DragonStateProvider.isDragon(player)) {
                return;
            }

            MagicData magicData = MagicData.getData(player);
            DragonAbilityInstance ability = magicData.fromSlot(magicData.getSelectedAbilitySlot());

            if (ability == null) {
                return;
            }

            VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.LINES);
            Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

            PoseStack pose = event.getPoseStack();
            pose.pushPose();
            pose.translate(-camera.x(), -camera.y(), -camera.z());

            for (ActionContainer action : ability.value().actions()) {
                AbilityTargeting targeting = action.effect();

                if (targeting instanceof DragonBreathTarget breathTarget) {
                    LevelRenderer.renderLineBox(pose, buffer, breathTarget.calculateBreathArea(player, ability), 1, 0, 0, 1);
                } else if (targeting instanceof LookingAtTarget lookingAtTarget) {
                    HitResult result;

                    if (lookingAtTarget.target().left().isPresent()) {
                        result = lookingAtTarget.getBlockHitResult(player, ability);
                    } else if (lookingAtTarget.target().right().isPresent()) {
                        result = lookingAtTarget.getEntityHitResult(player, entity -> true, ability);
                    } else {
                        continue;
                    }

                    if (result.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockResult = (BlockHitResult) result;
                        LevelRenderer.renderLineBox(pose, buffer, new AABB(blockResult.getBlockPos()), 0, 1, 0, 1);
                    } else if (result.getType() == HitResult.Type.ENTITY) {
                        EntityHitResult entityResult = (EntityHitResult) result;
                        LevelRenderer.renderLineBox(pose, buffer, entityResult.getEntity().getBoundingBox().inflate(1.5), 0, 1, 0, 1);
                    }
                } else if (targeting instanceof AreaTarget areaTarget) {
                    LevelRenderer.renderLineBox(pose, buffer, areaTarget.calculateAffectedArea(player, ability), 0, 0, 1, 1);
                } else if (targeting instanceof DiscTarget discTarget) {
                    int radius = (int) discTarget.radius().calculate(ability.level());
                    int height = (int) discTarget.height().calculate(ability.level());
                    LevelRenderer.renderLineBox(pose, buffer, discTarget.calculateAffectedArea(player.position(), radius, height), 0, 1, 0, 1);
                }
            }

            pose.popPose();
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void cancelNameplatesFromDummyEntities(RenderNameTagEvent renderNameplateEvent) {
        Entity entity = renderNameplateEvent.getEntity();
        if (entity.getType() == DSEntities.DRAGON.get()) {
            renderNameplateEvent.setCanRender(TriState.FALSE);
        }
    }

    public static void adjustCamera(final Camera camera) {
        if (camera.isDetached()) {
            // Zoom out a bit more since the camera is too zoomed in
            camera.move(-0.3f, 0, 0);
        }
    }

    public static float adjustNearDistance() {
        // FIXME :: adjust this
        return 0.01f;
    }

    /** Amount of client ticks the player model will not be rendered if the player was recently a dragon (to avoid player model pop-up after respawning) */
    private static final int MAX_DELAY = 10;
    private static int renderDelay;

    /** Called for every player */
    @SubscribeEvent
    public static void thirdPersonPreRender(final RenderPlayerEvent.Pre renderPlayerEvent) {
        if (!(renderPlayerEvent.getEntity() instanceof AbstractClientPlayer player)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!PLAYER_DRAGON_MAP.containsKey(player.getId())) {
            DragonEntity dummyDragon = DSEntities.DRAGON.get().create(player.level());
            //noinspection DataFlowIssue -> dragon should not be null
            dummyDragon.playerId = player.getId();
            PLAYER_DRAGON_MAP.put(player.getId(), dummyDragon);
        }

        if (handler.isDragon()) {
            if (player == minecraft.player) {
                renderDelay = MAX_DELAY;
            }

            renderPlayerEvent.setCanceled(true);
            if (!isOverridingMovementData) {
                setDragonMovementData(player, Minecraft.getInstance().getTimer().getRealtimeDeltaTicks());
            }
            float partialRenderTick = renderPlayerEvent.getPartialTick();
            float yaw = player.getViewYRot(partialRenderTick);

            ResourceLocation customTexture;

            if (handler.getModel().equals(DragonBody.DEFAULT_MODEL)) {
                customTexture = DragonSkins.getPlayerSkin(player, handler.stageKey());
            } else {
                customTexture = null;
            }

            PoseStack poseStack = renderPlayerEvent.getPoseStack();

            try {
                poseStack.pushPose();

                Vector3f lookVector = getModelOffset(player, partialRenderTick);
                poseStack.translate(-lookVector.x(), lookVector.y(), -lookVector.z());

                // Make sure to update our modifiers on the clientside based on the partial visual size
                float scale = (float) handler.getVisualScale(player, partialRenderTick);
                poseStack.scale(scale, scale, scale);

                EntityRenderer<? extends Player> playerRenderer = renderPlayerEvent.getRenderer();
                int eventLight = renderPlayerEvent.getPackedLight();
                final MultiBufferSource renderTypeBuffer = renderPlayerEvent.getMultiBufferSource();

                if (dragonNameTags && player != minecraft.player) {
                    //noinspection UnstableApiUsage -> intentional
                    RenderNameTagEvent renderNameplateEvent = new RenderNameTagEvent(player, player.getDisplayName(), playerRenderer, poseStack, renderTypeBuffer, eventLight, partialRenderTick);
                    NeoForge.EVENT_BUS.post(renderNameplateEvent);

                    if (renderNameplateEvent.canRender().isTrue() || renderNameplateEvent.canRender().isDefault() && ((LivingRendererAccessor) playerRenderer).dragonSurvival$callShouldShowName(player)) {
                        ((EntityRendererAccessor) playerRenderer).dragonSurvival$renderNameTag(player, renderNameplateEvent.getContent(), poseStack, renderTypeBuffer, eventLight, partialRenderTick);
                    }
                }

                MovementData movement = MovementData.getData(player);
                poseStack.mulPose(Axis.YN.rotationDegrees((float) movement.bodyYaw));

                // What will be rendered in place of the human player model
                DragonEntity playerAsDragon = PLAYER_DRAGON_MAP.get(player.getId());
                EntityRenderer<? super DragonEntity> dragonRenderer = minecraft.getEntityRenderDispatcher().getRenderer(playerAsDragon);
                dragonModel.setOverrideTexture(customTexture);
                boolean isPlayerGliding = ServerFlightHandler.isGliding(player);
                Entity playerVehicle = player.getVehicle();

                if (isPlayerGliding || (player.isPassenger() && DragonStateProvider.isDragon(playerVehicle) && ServerFlightHandler.isGliding((Player) playerVehicle))) {
                    float upRot;

                    if (isPlayerGliding) {
                        upRot = Mth.clamp((float) (player.getDeltaMovement().y * 20), -80, 80);
                    } else {
                        upRot = Mth.clamp((float) (playerVehicle.getDeltaMovement().y * 20), -80, 80);
                    }

                    playerAsDragon.prevXRot = Mth.lerp(0.1F, playerAsDragon.prevXRot, upRot);
                    playerAsDragon.prevXRot = Mth.clamp(playerAsDragon.prevXRot, -80, 80);

                    movement.prevXRot = playerAsDragon.prevXRot;

                    if (Float.isNaN(playerAsDragon.prevXRot)) {
                        playerAsDragon.prevXRot = upRot;
                    }

                    if (Float.isNaN(playerAsDragon.prevXRot)) {
                        playerAsDragon.prevXRot = 0;
                    }

                    poseStack.mulPose(Axis.XN.rotationDegrees(playerAsDragon.prevXRot));

                    float yRot;
                    Vec3 deltaVel;

                    if (isPlayerGliding) {
                        yRot = player.getViewYRot(1f);
                        deltaVel = player.getDeltaMovement();
                    } else {
                        yRot = playerVehicle.getViewYRot(1f);
                        deltaVel = playerVehicle.getDeltaMovement();
                    }

                    // Factor for interpolating to the target bank angle
                    final float ROLL_VEL_LERP_FACTOR = 0.1F;
                    // Minimum velocity to begin banking
                    final double ROLL_VEL_INFLUENCE_MIN = 0.5D;
                    // Maximum velocity at which point the bank angle has full effect
                    final double ROLL_VEL_INFLUENCE_MAX = 2.0D;

                    // Minimum view-velocity delta to start rolling
                    final float ROLL_MIN_DELTA_DEG = 5;
                    // Equivalent maximum, after which the bank angle is maximized
                    final float ROLL_MAX_DELTA_DEG = 90;
                    // Maximum roll angle
                    final float ROLL_MAX_DEG = 60;
                    // Exponent for targetRollNormalized (applied after normalizing relative to ROLL_MAX_DEG)
                    // > 1: soft, starts banking slowly, increases rapidly with higher delta
                    // < 1: sensitive, starts banking even when the difference is tiny, softer towards the limits
                    final double ROLL_EXP = 0.7;

                    float targetRollNormalized;

                    // Note that we're working with the HORIZONTAL move delta
                    if (deltaVel.horizontalDistanceSqr() > ROLL_VEL_INFLUENCE_MIN * ROLL_VEL_INFLUENCE_MIN) {
                        float velAngle = (float) Math.atan2(-deltaVel.x, deltaVel.z) * Mth.RAD_TO_DEG;
                        float viewToVelDeltaDeg = Mth.degreesDifference(velAngle, yRot);

                        // Raw target roll, normalized
                        targetRollNormalized = (float) Functions.deadzoneNormalized(viewToVelDeltaDeg, ROLL_MIN_DELTA_DEG, ROLL_MAX_DELTA_DEG);

                        // Scale via exponent (still normalized)
                        targetRollNormalized = Math.copySign((float) Math.pow(Math.abs(targetRollNormalized), ROLL_EXP), targetRollNormalized);

                        // Scale by velocity influence
                        float velInfluence = (float) Functions.inverseLerpClamped(
                                deltaVel.horizontalDistance(),
                                ROLL_VEL_INFLUENCE_MIN,
                                ROLL_VEL_INFLUENCE_MAX
                        );

                        targetRollNormalized *= velInfluence;
                    } else {
                        targetRollNormalized = 0;
                    }

                    float targetRollDeg = targetRollNormalized * ROLL_MAX_DEG * Mth.DEG_TO_RAD;

                    // NaN/Inf prevention - snap directly
                    if (!Double.isFinite(playerAsDragon.prevZRot)) {
                        playerAsDragon.prevZRot = targetRollDeg;
                    } else {
                        playerAsDragon.prevZRot = Mth.lerp(ROLL_VEL_LERP_FACTOR, playerAsDragon.prevZRot, targetRollDeg);
                    }

                    movement.prevXRot = playerAsDragon.prevXRot;
                    movement.prevZRot = playerAsDragon.prevZRot;

                    poseStack.mulPose(Axis.ZP.rotation(playerAsDragon.prevZRot));
                } else {
                    movement.prevZRot = 0;
                    movement.prevXRot = 0;
                }
                if (player != minecraft.player || !Minecraft.getInstance().options.getCameraType().isFirstPerson() || !isPlayerGliding || renderFirstPersonFlight) {
                    playerAsDragon.currentlyRenderedPose = poseStack.last();
                    dragonRenderer.render(playerAsDragon, yaw, partialRenderTick, poseStack, renderTypeBuffer, eventLight);
                }

                if (!player.isSpectator()) {
                    // FIXME
                    // Render the parrot on the players shoulder
                    /*((LivingRendererAccessor) playerRenderer).dragonSurvival$getRenderLayers().stream().filter(ParrotOnShoulderLayer.class::isInstance).findAny().ifPresent(renderLayer -> {
                       // poseStack.scale(1 / scale, 1 / scale, 1 / scale);
                        poseStack.mulPose(Axis.XN.rotationDegrees(180));
                        double height = 1.3 * scale;
                        double forward = 0.3 * scale;
                        float parrotHeadYaw = Mth.clamp(-1 * ((float) movement.bodyYaw - (float) movement.headYaw), -75, 75);
                        poseStack.translate(0, -height, -forward);
                        //noinspection unchecked -> ignore
                        renderLayer.render(poseStack, renderTypeBuffer, eventLight, player, 0, 0, partialRenderTick, (float) player.tickCount + partialRenderTick, parrotHeadYaw, (float) movement.headPitch);
                        poseStack.translate(0, height, forward);
                        poseStack.mulPose(Axis.XN.rotationDegrees(-180));
                        poseStack.scale(scale, scale, scale);
                    });*/

                    int combinedOverlayIn = LivingEntityRenderer.getOverlayCoords(player, 0);
                    if (player.hasEffect(DSEffects.TRAPPED)) {
                        float bolasScale = player.getEyeHeight();
                        if (handler.isDragon()) {
                            bolasScale = (float) DragonSizeHandler.calculateDragonEyeHeight(handler, player);
                        }
                        BolasOnPlayerRenderer.renderBolas(eventLight, combinedOverlayIn, renderTypeBuffer, poseStack, bolasScale);
                    }
                }
            } catch (Throwable throwable) {
                DragonSurvival.LOGGER.error("A problem occurred while rendering a dragon in third person", throwable);
            } finally {
                poseStack.popPose();
            }
        } else {
            if (renderDelay > 0 && player == minecraft.player) {
                renderDelay--;
                renderPlayerEvent.setCanceled(true);
            } else {
                ((EntityRendererAccessor) renderPlayerEvent.getRenderer()).dragonSurvival$setShadowRadius(0.5F);
            }
        }
        dragonModel.setOverrideTexture(null);
    }

    public static Vector3f getModelOffset(final Player player, float partialRenderTick) {
        float angle = -(float) MovementData.getData(player).bodyYaw * ((float) Math.PI / 180);
        float x = Mth.sin(angle);
        float z = Mth.cos(angle);

        DragonStateHandler handler = DragonStateProvider.getData(player);
        float scale = (float) handler.getVisualScale(player, partialRenderTick) * (float) handler.body().value().scalingProportions().offset();

        return new Vector3f(x * scale, 0, z * scale);
    }

    @SubscribeEvent
    public static void spin(InputEvent.InteractionKeyMappingTriggered keyInputEvent) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        MovementData movement = MovementData.getData(player);
        if (keyInputEvent.isAttack() && keyInputEvent.shouldSwingHand() && !movement.dig) {
            movement.bite = true;
        }
    }

    public static void setDragonMovementData(Player player, float realtimeDeltaTick) {
        if (player == null) return;

        if(DragonStateProvider.isDragon(player)) {
            MovementData movement = MovementData.getData(player);

            Vec3 moveVector;
            if (!ServerFlightHandler.isFlying(player)) {
                moveVector = player.getDeltaMovement();
            } else {
                moveVector = new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
            }

            // Get new body yaw & head angles
            var newAngles = BodyAngles.calculateNext(player, movement, realtimeDeltaTick);
            movement.set(newAngles.bodyYaw, newAngles.headYaw, newAngles.headPitch, moveVector);
        }
    }

    @SubscribeEvent
    public static void updateFirstPersonDataAndSendMovementData(final ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        Input input = player.input;
        MovementData movement = MovementData.getData(player);
        movement.setFirstPerson(Minecraft.getInstance().options.getCameraType().isFirstPerson());
        movement.setFreeLook(Keybind.FREE_LOOK.consumeClick()); // FIXME :: handle this properly
        movement.setDesiredMoveVec(new Vec2(input.leftImpulse, input.forwardImpulse));

        if (player.isPassenger()) {
            // Prevent animation problems while we are riding an entity
            PacketDistributor.sendToServer(new SyncDeltaMovement(player.getId(), Vec3.ZERO));
        } else {
            PacketDistributor.sendToServer(new SyncDeltaMovement(player.getId(), player.getDeltaMovement()));
        }

        PacketDistributor.sendToServer(new SyncDragonMovement(player.getId(), movement.isFirstPerson, movement.bite, movement.isFreeLook, movement.desiredMoveVec));
    }

    @SubscribeEvent // Don't render the fire overlay when fire immune
    public static void removeFireOverlay(final RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() != RenderBlockScreenEffectEvent.OverlayType.FIRE) {
            return;
        }

        //noinspection DataFlowIssue -> player is present
        Minecraft.getInstance().player.getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).ifPresent(data -> {
            if (data.isFireImmune()) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void clearDragonReferences(final LevelEvent.Unload worldEvent) {
        PLAYER_DRAGON_MAP.clear();
    }

    private record BodyAngles(double bodyYaw, double headPitch, double headYaw) {
        /// Minimum magnitude to consider the player to be moving (horizontally)
        /// Applies to world-space horizontal movement (as opposed to raw player input)
        static final double MOVE_DELTA_EPSILON = 0.0001D;

        /// When moving (without input) too slower, the body aligns to the move direction slower too.
        /// This constant determines the move vector magnitude below which it begins to slow down.
        /// The body stops aligning below MOVE_DELTA_EPSILON,
        /// and aligns at full speed above MOVE_DELTA_FULL_EFFECT_MIN_MAG.
        static final double MOVE_DELTA_FULL_EFFECT_MIN_MAG = 0.3D;

        /// Factor to align the body to the move vector
        static final double MOVE_ALIGN_FACTOR = 0.3D;
        /// Multiplier for MOVE_ALIGN_FACTOR when in the air
        static final double MOVE_ALIGN_FACTOR_AIR = 0.12D;
        /// Multiplier for MOVE_ALIGN_FACTOR * MOVE_ALIGN_FACTOR_AIR when there's no player input
        static final double MOVE_ALIGN_FACTOR_AIR_PASSIVE_MUL = 0.75D; // Multiplier for the above

        // Body angle limits in various circumstances
        // 0 is straight ahead, 180 is no restriction

        /// Body angle limits: Third person
        static final double BODY_ANGLE_LIMIT_TP = 180D - 30D;
        /// Body angle limit softness: Third person
        static final double BODY_ANGLE_LIMIT_TP_SOFTNESS = 0.9D;
        /// Body angle limit softness, multiplier when in the air: Third person
        static final double BODY_ANGLE_LIMIT_TP_SOFTNESS_AIR_MUL = 0.15D;

        // Third person + free look is unrestricted
        /// Body angle limits: Third person, free look
        static final double BODY_ANGLE_LIMIT_TP_FREE = 180D;
        /// Body angle limit softness: Third person, free look
        static final double BODY_ANGLE_LIMIT_TP_SOFTNESS_FREE = 0D;
        /// Body angle limit softness, multiplier when in the air: Third person, free look
        static final double BODY_ANGLE_LIMIT_TP_SOFTNESS_AIR_MUL_FREE = 0D;

        /// Body angle limits: First person
        static final double BODY_ANGLE_LIMIT_FP = 10D;
        /// Body angle limit softness: First person
        static final double BODY_ANGLE_LIMIT_FP_SOFTNESS = 0.75D;
        /// Body angle limit softness, multiplier when in the air: First person
        static final double BODY_ANGLE_LIMIT_FP_SOFTNESS_AIR_MUL = 0.4D;

        /// Body angle limits: First person, free look
        static final double BODY_ANGLE_LIMIT_FP_FREE = 60D;
        /// Body angle limit softness: First person, free look
        static final double BODY_ANGLE_LIMIT_FP_FREE_SOFTNESS = 0.85D;
        /// Body angle limit softness, multiplier when in the air: First person, free look
        static final double BODY_ANGLE_LIMIT_FP_FREE_SOFTNESS_AIR_MUL = 0.4D;

        // Head angle values
        // Head yaw has no angle limits defined here, but avoids passing through 180 (behind the player)

        /// Head yaw lerp factor
        static final double HEAD_YAW_FACTOR = 0.3D;
        /// Head yaw pitch factor
        static final double HEAD_PITCH_FACTOR = 0.3D;

        public static BodyAngles calculateNext(Player player, MovementData movement, float realtimeDeltaTick) {
            // Handle headYaw
            float viewYRot = player.getViewYRot(realtimeDeltaTick);
            float viewXRot = player.getViewXRot(realtimeDeltaTick);

            // Head yaw is relative to body
            // Get pos delta since last tick - not scaled by realtimeDeltaTick
            var posDelta = new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);

            var headAngles = calculateNextHeadAngles(realtimeDeltaTick, movement, viewXRot, viewYRot);

            return new BodyAngles(
                    calculateNextBodyYaw(realtimeDeltaTick, player, movement, posDelta, viewYRot),
                    headAngles.getA(),
                    headAngles.getB()
            );
        }

        private static double calculateNextBodyYaw(
                float realtimeDeltaTick,
                Player player,
                MovementData movement,
                Vec3 posDelta,
                float viewYRot) {

            // Handle bodyYaw
            double bodyYaw = movement.bodyYaw;
            boolean isFreeLook = movement.isFreeLook;
            boolean wasFreeLook = movement.wasFreeLook; // TODO: what's this for?
            boolean isFirstPerson = movement.isFirstPerson;
            boolean hasPosDelta = posDelta.horizontalDistanceSqr() > MOVE_DELTA_EPSILON * MOVE_DELTA_EPSILON;

            var rawInput = movement.desiredMoveVec;
            var hasMoveInput = rawInput.lengthSquared() > MovementData.INPUT_EPSILON * MovementData.INPUT_EPSILON;
            boolean isInputBack = rawInput.y < 0;

            if (hasMoveInput) {

                // When providing move input, turn the body towards the input direction
                var targetAngle = Math.toDegrees(Math.atan2(-rawInput.x, rawInput.y)) + viewYRot;

                // If in first person and moving back when not flying, flip the target angle
                // Checks dragon flight or creative/spectator flight
                var isFlying = ServerFlightHandler.isFlying(player) || player.getAbilities().flying;
                if (isFirstPerson && !isFreeLook && isInputBack && !isFlying) {
                    targetAngle += 180;
                }

                var factor = player.onGround() ? MOVE_ALIGN_FACTOR : MOVE_ALIGN_FACTOR_AIR;

                // In first person, force the body to turn away from the view direction if possible
                // This prevents issues with the body yaw and angle limit fighting, never letting the body
                // pass through the area in front of the player when that's the shorter path for the body yaw
                if (isFirstPerson) {
                    bodyYaw = Functions.lerpAngleAwayFrom(realtimeDeltaTick * factor, bodyYaw, targetAngle, viewYRot + 180);
                } else {
                    bodyYaw = RenderUtil.lerpYaw(realtimeDeltaTick * factor, bodyYaw, targetAngle);
                }
            } else if (hasPosDelta && !player.onGround()) {
                // When moving without input and in the air, slowly align to the move vector

                // +Z: 0 deg; -X: 90 deg
                // Move angle that the body will try to align to
                var posDeltaAngle = Math.toDegrees(Math.atan2(-posDelta.x, posDelta.z));

                var factor = MOVE_ALIGN_FACTOR_AIR * MOVE_ALIGN_FACTOR_AIR_PASSIVE_MUL;
                double deltaMagFactor = Math.min(
                        1,
                        (posDelta.horizontalDistance() - MOVE_DELTA_EPSILON) / MOVE_DELTA_FULL_EFFECT_MIN_MAG
                );
                factor *= deltaMagFactor;

                bodyYaw = RenderUtil.lerpYaw(
                        realtimeDeltaTick * factor,
                        bodyYaw,
                        posDeltaAngle);
            }

            {
                // Limit body angle based on view direction and PoV
                var angleLimit = 0D;
                var factor = 0D;
                var airMul = 1D;
                if (isFirstPerson) {
                    if (isFreeLook) {
                        angleLimit = BODY_ANGLE_LIMIT_FP_FREE;
                        factor = BODY_ANGLE_LIMIT_FP_FREE_SOFTNESS;
                        airMul = BODY_ANGLE_LIMIT_FP_FREE_SOFTNESS_AIR_MUL;
                    } else {
                        angleLimit = BODY_ANGLE_LIMIT_FP;
                        factor = BODY_ANGLE_LIMIT_FP_SOFTNESS;
                        airMul = BODY_ANGLE_LIMIT_FP_SOFTNESS_AIR_MUL;
                    }
                } else {
                    if (isFreeLook) {
                        angleLimit = BODY_ANGLE_LIMIT_TP_FREE;
                        factor = BODY_ANGLE_LIMIT_TP_SOFTNESS_FREE;
                        airMul = BODY_ANGLE_LIMIT_TP_SOFTNESS_AIR_MUL_FREE;
                    } else {
                        angleLimit = BODY_ANGLE_LIMIT_TP;
                        factor = BODY_ANGLE_LIMIT_TP_SOFTNESS;
                        airMul = BODY_ANGLE_LIMIT_TP_SOFTNESS_AIR_MUL;
                    }
                }
                if (!player.onGround()) {
                    factor *= airMul;
                }
                bodyYaw = Functions.limitAngleDeltaSoft(bodyYaw, viewYRot, angleLimit, realtimeDeltaTick * factor);
            }
            return bodyYaw;
        }

        private static Tuple<Double, Double> calculateNextHeadAngles(
                float realtimeDeltaTick,
                MovementData movement,
                float viewXRot,
                float viewYRot) {
            // Yaw is relative to the body
            double headYawTarget = Functions.angleDifference(
                    viewYRot,
                    movement.bodyYaw
            );
            double headYaw = Functions.lerpAngleAwayFrom(
                    realtimeDeltaTick * HEAD_YAW_FACTOR,
                    movement.headYaw,
                    headYawTarget,
                    180
            );

            // Pitch is also technically relative, since the body doesn't have pitch
            double headPitch = Mth.lerp(
                    realtimeDeltaTick * HEAD_PITCH_FACTOR,
                    movement.headPitch,
                    viewXRot
            );

            return new Tuple<>(headPitch, headYaw);
        }
    }

}
