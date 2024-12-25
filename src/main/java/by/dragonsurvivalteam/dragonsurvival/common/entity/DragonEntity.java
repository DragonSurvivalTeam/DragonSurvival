package by.dragonsurvivalteam.dragonsurvival.common.entity;

import by.dragonsurvivalteam.dragonsurvival.client.emotes.Emote;
import by.dragonsurvivalteam.dragonsurvival.client.models.DragonModel;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.AnimationTickTimer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.EmoteCap;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationType;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@EventBusSubscriber
public class DragonEntity extends LivingEntity implements GeoEntity {
    /**
     * Durations of jumps
     */
    public static ConcurrentHashMap<Integer, Integer> dragonsJumpingTicks = new ConcurrentHashMap<>(20);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public PoseStack.Pose currentlyRenderedPose;

    public final ArrayList<Double> bodyYawHistory = new ArrayList<>();
    public double currentBodyYawChange = 0;
    public final ArrayList<Double> headYawHistory = new ArrayList<>();
    public double currentHeadYawChange = 0;
    public final ArrayList<Double> headPitchHistory = new ArrayList<>();
    public double currentHeadPitchChange = 0;
    public final ArrayList<Double> verticalVelocityHistory = new ArrayList<>();
    public double currentTailMotionUp = 0;
    /**
     * This reference must be updated whenever player is remade, for example, when changing dimensions
     */
    public volatile Integer playerId; // TODO :: Use string uuid?
    public boolean neckLocked = false;
    public boolean tailLocked = false;
    public float prevZRot = 0;
    public float prevXRot = 0;
    // In certain circumstances, we need to override the UUID with the local player's UUID when gathering textures for the dragon entity
    // At the moment, this only happens on the smithing screen, as the player in the inventory panels is actually referring the real player and therefore has the correct UUID for textures
    // The dragon displayed in the editor doesn't want to mirror the local player's UUID, so this isn't used there either
    public boolean overrideUUIDWithLocalPlayerForTextureFetch = false;

    public boolean clearVerticalVelocity = false;
    DragonAbilityInstance lastCast = null;
    public boolean started, ended;
    AnimationTickTimer animationTickTimer = new AnimationTickTimer();

    // Default player values
    private static final double DEFAULT_WALK_SPEED = 0.1; // Abilities#walkingSpeed
    private static final double DEFAULT_SNEAK_SPEED = 0.03; // Attributes#SNEAKING_SPEED default value
    private static final double DEFAULT_SPRINT_SPEED = 0.165;
    private static final double DEFAULT_SWIM_SPEED = 0.051;
    private static final double DEFAULT_FAST_SWIM_SPEED = 0.13;

    public AnimationController<DragonEntity> mainAnimationController;
    private Pair<AbilityAnimation, AnimationType> currentAbilityAnimation;
    private boolean begunPlayingAbilityAnimation = false;

    private static double globalTickCount = 0;

    public DragonEntity(EntityType<? extends LivingEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @SubscribeEvent
    public static void decreaseJumpDuration(PlayerTickEvent.Post playerTickEvent) {
        Player player = playerTickEvent.getEntity();
        dragonsJumpingTicks.computeIfPresent(player.getId(), (playerEntity1, integer) -> integer > 0 ? integer - 1 : integer);
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar registrar) {
        for (int slot = 0; slot < EmoteCap.MAX_EMOTES; slot++) {
            int finalSlot = slot;
            registrar.add(new AnimationController<>(this, "emote_" + slot, 0, state -> emotePredicate(state, finalSlot)));
        }

        mainAnimationController = new AnimationController<>(this, "main", 2, this::predicate);
        registrar.add(mainAnimationController);
        registrar.add(new AnimationController<>(this, "bite", this::bitePredicate));
        registrar.add(new AnimationController<>(this, "tail", this::tailPredicate));
        registrar.add(new AnimationController<>(this, "head", this::headPredicate));
        registrar.add(new AnimationController<>(this, "breath", this::breathPredicate));
    }

    public void setCurrentAbilityAnimation(Pair<AbilityAnimation, AnimationType> currentAbilityAnimation) {
        if(this.currentAbilityAnimation != null) {
            animationTickTimer.putAnimation(this.currentAbilityAnimation.getFirst().getName(), 0.0);
        }
        this.currentAbilityAnimation = currentAbilityAnimation;
        begunPlayingAbilityAnimation = false;
    }

    private boolean checkAndPlayAbilityAnimation(final AnimationState<DragonEntity> state, AnimationLayer layer) {
        AnimationLayer currentAbilityLayer = currentAbilityAnimation != null ? currentAbilityAnimation.getFirst().getLayer() : null;
        boolean isNotPlayingCurrentAbilityAnimation = currentAbilityAnimation != null && currentAbilityLayer == layer && animationTickTimer.getDuration(currentAbilityAnimation.getFirst().getName()) <= 0;
        if (!begunPlayingAbilityAnimation && isNotPlayingCurrentAbilityAnimation) {
            begunPlayingAbilityAnimation = true;
            state.getController().setAnimationSpeed(1.0);
            currentAbilityAnimation.getFirst().play(state, this, currentAbilityAnimation.getSecond());
            if(currentAbilityAnimation.getSecond() == AnimationType.PLAY_ONCE) {
                // Only trigger use a timer for PLAY_ONCE animations, as the others are intended to await a future packet to stop them
                animationTickTimer.putAnimation(currentAbilityAnimation.getFirst().getName(), animationDuration(getPlayer(), currentAbilityAnimation.getFirst().getName()));
            }
        } else if(begunPlayingAbilityAnimation && isNotPlayingCurrentAbilityAnimation && currentAbilityAnimation.getSecond() == AnimationType.PLAY_ONCE) {
            begunPlayingAbilityAnimation = false;
            currentAbilityAnimation = null;
        } else if(begunPlayingAbilityAnimation && currentAbilityLayer == layer) {
            state.getController().setAnimationSpeed(1.0);
            return true;
        }

        return begunPlayingAbilityAnimation && currentAbilityLayer == layer;
    }

    // For the breath weapon only, we want it to play on a separate controller,
    // so it can play at the same time as other animations
    private PlayState breathPredicate(final AnimationState<DragonEntity> state) {
        Player player = getPlayer();

        if (player == null) {
            return PlayState.STOP;
        }

        if (checkAndPlayAbilityAnimation(state, AnimationLayer.BREATH)) {
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
    }

    private PlayState tailPredicate(final AnimationState<DragonEntity> state) {
        if (!tailLocked) {
            return state.setAndContinue(TAIL_TURN);
        } else {
            return PlayState.STOP;
        }
    }

    private PlayState headPredicate(final AnimationState<DragonEntity> state) {
        if (!neckLocked) {
            return state.setAndContinue(HEAD_TURN);
        } else {
            return PlayState.STOP;
        }
    }

    private PlayState bitePredicate(final AnimationState<DragonEntity> state) {
        Player player = getPlayer();

        if (player == null) {
            return PlayState.STOP;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        RawAnimation builder = null;

        if (checkAndPlayAbilityAnimation(state, AnimationLayer.BITE)) {
            return PlayState.CONTINUE;
        }

        MovementData movement = MovementData.getData(player);
        if (!ClientDragonRenderer.renderItemsInMouth && doesAnimationExist(player, "use_item") && (player.isUsingItem() || (movement.bite || movement.dig) && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()))) {
            // When the player is using an item
            movement.bite = false;
            return state.setAndContinue(AnimationUtils.createAnimation(builder, USE_ITEM));
        } else if (!ClientDragonRenderer.renderItemsInMouth && doesAnimationExist(player, "eat_item_right") && player.isUsingItem() && DragonFoodHandler.isEdible(player.getMainHandItem(), handler.getType()) || animationTickTimer.getDuration("eat_item_right") > 0) {
            // When the player is eating the main hand item
            if (animationTickTimer.getDuration("eat_item_right") <= 0) {
                movement.bite = false;
                animationTickTimer.putAnimation("eat_item_right", 0.32 * 20);
            }

            return state.setAndContinue(AnimationUtils.createAnimation(builder, EAT_ITEM_RIGHT));
        } else if (!ClientDragonRenderer.renderItemsInMouth && doesAnimationExist(player, "eat_item_left") && player.isUsingItem() && DragonFoodHandler.isEdible(player.getMainHandItem(), handler.getType()) || animationTickTimer.getDuration("eat_item_right") > 0) {
            // When the player is eating the offhand item
            if (animationTickTimer.getDuration("eat_item_left") <= 0) {
                movement.bite = false;
                animationTickTimer.putAnimation("eat_item_left", 0.32 * 20);
            }

            return state.setAndContinue(AnimationUtils.createAnimation(builder, EAT_ITEM_LEFT));
        } else if (!ClientDragonRenderer.renderItemsInMouth && doesAnimationExist(player, "use_item_right") && !player.getMainHandItem().isEmpty() && movement.bite && player.getMainArm() == HumanoidArm.RIGHT || animationTickTimer.getDuration("use_item_right") > 0) {
            // When the player is using the main hand item
            if (animationTickTimer.getDuration("use_item_right") <= 0) {
                movement.bite = false;
                animationTickTimer.putAnimation("use_item_right", 0.32 * 20);
            }

            return state.setAndContinue(AnimationUtils.createAnimation(builder, USE_ITEM_RIGHT));
        } else if (!ClientDragonRenderer.renderItemsInMouth && doesAnimationExist(player, "use_item_left") && !player.getOffhandItem().isEmpty() && movement.bite && player.getMainArm() == HumanoidArm.LEFT || animationTickTimer.getDuration("use_item_left") > 0) {
            // When the player is using the offhand item
            if (animationTickTimer.getDuration("use_item_left") <= 0) {
                movement.bite = false;
                animationTickTimer.putAnimation("use_item_left", 0.32 * 20);
            }

            return state.setAndContinue(AnimationUtils.createAnimation(builder, USE_ITEM_LEFT));
        } else if (movement.bite && !movement.dig || animationTickTimer.getDuration("bite") > 0) {
            if (animationTickTimer.getDuration("bite") <= 0) {
                movement.bite = false;
                animationTickTimer.putAnimation("bite", 0.44 * 20);
            }

            return state.setAndContinue(AnimationUtils.createAnimation(builder, BITE));
        }

        return PlayState.STOP;
    }

    private boolean doesAnimationExist(final Player player, final String animation) {
        return GeckoLibCache.getBakedAnimations().get(DragonModel.getAnimationResource(player)).getAnimation(animation) != null;
    }

    private double animationDuration(final Player player, final String animation) {
        if(!doesAnimationExist(player, animation)) {
            return 0;
        }

        return GeckoLibCache.getBakedAnimations().get(DragonModel.getAnimationResource(player)).getAnimation(animation).length();
    }

    private PlayState emotePredicate(final AnimationState<DragonEntity> state, int slot) {
        Player player = getPlayer();

        if (player == null) {
            return PlayState.STOP;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.getEmoteData().currentEmotes[slot] != null) {
            Emote emote = handler.getEmoteData().currentEmotes[slot];

            neckLocked = emote.locksHead;
            tailLocked = emote.locksTail;

            state.getController().setAnimationSpeed(emote.speed);

            if (emote.animation != null && !emote.animation.isEmpty()) {
                if (!emote.loops) {
                    return state.setAndContinue(RawAnimation.begin().thenPlay(emote.animation));
                } else {
                    return state.setAndContinue(RawAnimation.begin().thenLoop(emote.animation));
                }
            }
        }

        return PlayState.STOP;
    }

    public @Nullable Player getPlayer() {
        if (playerId == null) {
            return null;
        }

        Entity entity = level().getEntity(playerId);

        if (entity instanceof Player player) {
            return player;
        } else {
            return null;
        }
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        Player player = getPlayer();

        if (player != null) {
            return player.getTeam();
        }

        return super.getTeam();
    }

    @Override
    public boolean isInvisible() {
        if (super.isInvisible()) {
            return true;
        }

        Player player = getPlayer();
        return player != null && player.isInvisible();
    }

    @Override
    public boolean isCurrentlyGlowing() {
        if (super.isCurrentlyGlowing()) {
            return true;
        }

        Player player = getPlayer();
        return player != null && player.isCurrentlyGlowing();
    }

    private void lockTailAndNeck() {
        neckLocked = true;
        tailLocked = true;
    }

    private void clearVerticalVelocity() {
        clearVerticalVelocity = true;
    }

    private PlayState predicate(final AnimationState<DragonEntity> state) {
        Player player = getPlayer();

        if (player == null) {
            return PlayState.STOP;
        }

        AnimationController<DragonEntity> animationController = state.getController();
        DragonStateHandler handler = DragonStateProvider.getData(player);
        TreasureRestData treasureRest = TreasureRestData.getData(player);

        if (handler.refreshBody) {
            animationController.forceAnimationReset();
            handler.refreshBody = false;
        }

        boolean useDynamicScaling = false;
        double animationSpeed = 1;
        double speedFactor = ClientConfig.movementAnimationSpeedFactor;
        double baseSpeed = DEFAULT_WALK_SPEED;
        double smallSizeFactor = ClientConfig.smallSizeAnimationSpeedFactor;
        double bigSizeFactor = ClientConfig.largeSizeAnimationSpeedFactor;
        double baseSize = DragonStage.MAX_HANDLED_SIZE;
        double distanceFromGround = ServerFlightHandler.distanceFromGround(player);

        if (Stream.of(handler.getEmoteData().currentEmotes).anyMatch(emote -> emote != null && !emote.blend && emote.animation != null && !emote.animation.isBlank())) {
            state.getController().stop();
            return PlayState.STOP;
        }

        Vec3 deltaMovement = player.getDeltaMovement();
        RawAnimation builder = null;

        // This predicate runs first, so we reset neck and tail lock here. If any animation locks them, they will be re-locked in time before the neck/tail animations are played.
        // It is also important we reset these values before trying to render abilities
        neckLocked = false;
        tailLocked = false;

        if (checkAndPlayAbilityAnimation(state, AnimationLayer.BASE)) {
            return PlayState.CONTINUE;
        }

        final double INPUT_EPSILON = 0.0000001D;
        MovementData movement = MovementData.getData(player);
        Vec2 rawInput = movement.desiredMoveVec;
        boolean hasMoveInput = rawInput.lengthSquared() > INPUT_EPSILON * INPUT_EPSILON;

        // TODO: The transition length of animations doesn't work correctly when the framerate varies too much from 60 FPS
        // FIXME
        /*if (magicData.onMagicSource) {
            return state.setAndContinue(AnimationUtils.createAnimation(builder, SIT_ON_MAGIC_SOURCE));
        } else*/ if (player.isSleeping() || treasureRest.isResting) {
            return state.setAndContinue(AnimationUtils.createAnimation(builder, SLEEP));
        } else if (player.isPassenger()) {
            return state.setAndContinue(AnimationUtils.createAnimation(builder, SIT));
        } else if (player.getAbilities().flying || ServerFlightHandler.isFlying(player)) {
            if (ServerFlightHandler.isGliding(player)) {
                if (ServerFlightHandler.isSpin(player)) {
                    animationSpeed = 2;
                    lockTailAndNeck();
                    state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SPIN));
                    animationController.transitionLength(5);
                } else if (deltaMovement.y < -1) {
                    lockTailAndNeck();
                    state.setAnimation(AnimationUtils.createAnimation(builder, FLY_DIVE_ALT));
                    animationController.transitionLength(4);
                } else if (deltaMovement.y < -0.25) {
                    lockTailAndNeck();
                    state.setAnimation(AnimationUtils.createAnimation(builder, FLY_DIVE));
                    animationController.transitionLength(4);
                } else if (deltaMovement.y > 0.5) {
                    animationSpeed = 1.5;
                    state.setAnimation(AnimationUtils.createAnimation(builder, FLY));
                    animationController.transitionLength(2);
                } else {
                    state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SOARING));
                    animationController.transitionLength(4);
                }
            } else {
                if (player.isCrouching() && deltaMovement.y < 0 && distanceFromGround < 10 && deltaMovement.length() < 4) {
                    state.setAnimation(AnimationUtils.createAnimation(builder, FLY_LAND));
                    animationController.transitionLength(2);
                } else if (ServerFlightHandler.isSpin(player)) {
                    lockTailAndNeck();
                    state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SPIN));
                    animationController.transitionLength(2);
                } else {
                    if (deltaMovement.y > 0) {
                        animationSpeed = 2;
                    }
                    state.setAnimation(AnimationUtils.createAnimation(builder, FLY));
                    animationController.transitionLength(2);
                }
            }
        } else if (player.getPose() == Pose.SWIMMING) {
            if (ServerFlightHandler.isSpin(player)) {
                lockTailAndNeck();
                state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SPIN));
                animationController.transitionLength(2);
            } else {
                // Clear vertical velocity if we just transitioned to this pose, to prevent the dragon from jerking up when landing in water
                if (!AnimationUtils.isAnimationPlaying(animationController, SWIM) && !AnimationUtils.isAnimationPlaying(animationController, SWIM_FAST) && !AnimationUtils.isAnimationPlaying(animationController, FLY_SPIN)) {
                    clearVerticalVelocity();
                }

                lockTailAndNeck();
                useDynamicScaling = true;
                baseSpeed = DEFAULT_FAST_SWIM_SPEED; // Default base fast speed for the player
                state.setAnimation(AnimationUtils.createAnimation(builder, SWIM_FAST));
                animationController.transitionLength(2);
            }
        } else if ((player.isInLava() || player.isInWaterOrBubble()) && !player.onGround()) {
            if (ServerFlightHandler.isSpin(player)) {
                animationSpeed = 2;
                lockTailAndNeck();
                state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SPIN));
                animationController.transitionLength(2);
            } else {
                // Clear vertical velocity if we just transitioned to this pose, to prevent the dragon from jerking up when landing in water
                if (!AnimationUtils.isAnimationPlaying(animationController, SWIM) && !AnimationUtils.isAnimationPlaying(animationController, SWIM_FAST) && !AnimationUtils.isAnimationPlaying(animationController, FLY_SPIN)) {
                    clearVerticalVelocity();
                }

                lockTailAndNeck();
                useDynamicScaling = true;
                baseSpeed = DEFAULT_SWIM_SPEED;
                state.setAnimation(AnimationUtils.createAnimation(builder, SWIM));
                animationController.transitionLength(2);
            }
        } else if (AnimationUtils.isAnimationPlaying(animationController, FLY_LAND)) {
            state.setAnimation(AnimationUtils.createAnimation(builder, FLY_LAND_END));
            animationController.transitionLength(2);
        } else if (AnimationUtils.isAnimationPlaying(animationController, FLY_LAND_END)) {
            // Don't add any animation
        } else if (!player.onGround() && dragonsJumpingTicks.getOrDefault(this.playerId, 0) > 0) {
            state.setAnimation(AnimationUtils.createAnimation(builder, JUMP));
            animationController.transitionLength(1);
        } else if (!player.onGround()) {
            state.setAnimation(AnimationUtils.createAnimation(builder, FALL_LOOP));
            animationController.transitionLength(6);
        } else if (player.isShiftKeyDown() || !DragonSizeHandler.canPoseFit(player, Pose.STANDING) && DragonSizeHandler.canPoseFit(player, Pose.CROUCHING)) {
            // Player is Sneaking
            if (hasMoveInput) {
                useDynamicScaling = true;
                baseSpeed = DEFAULT_SNEAK_SPEED;
                state.setAnimation(AnimationUtils.createAnimation(builder, SNEAK_WALK));
                animationController.transitionLength(5);
            } else if (movement.dig) {
                state.setAnimation(AnimationUtils.createAnimation(builder, DIG_SNEAK));
                animationController.transitionLength(5);
            } else {
                state.setAnimation(AnimationUtils.createAnimation(builder, SNEAK));
                animationController.transitionLength(5);
            }
        } else if (player.isSprinting()) {
            useDynamicScaling = true;
            baseSpeed = DEFAULT_SPRINT_SPEED;
            state.setAnimation(AnimationUtils.createAnimation(builder, RUN));
            animationController.transitionLength(2);
        } else if (hasMoveInput) {
            useDynamicScaling = true;
            state.setAnimation(AnimationUtils.createAnimation(builder, WALK));
            animationController.transitionLength(2);
        } else if (movement.dig) {
            state.setAnimation(AnimationUtils.createAnimation(builder, DIG));
            animationController.transitionLength(6);
        } else {
            state.setAnimation(AnimationUtils.createAnimation(builder, IDLE));
            animationController.transitionLength(2);
        }

        double finalAnimationSpeed = animationSpeed;
        if (useDynamicScaling) {
            double horizontalDistance = deltaMovement.horizontalDistance();
            double speedComponent = Math.min(ClientConfig.maxAnimationSpeedFactor, (horizontalDistance - baseSpeed) / baseSpeed * speedFactor);
            double sizeDistance = handler.getSize() - baseSize;
            double sizeFactor = sizeDistance >= 0 ? bigSizeFactor : smallSizeFactor;
            double sizeComponent = baseSize / (baseSize + sizeDistance * sizeFactor);
            // We need a minimum speed here to prevent the animation from ever being truly at 0 speed (otherwise the animation state machine implodes)
            finalAnimationSpeed = Math.min(ClientConfig.maxAnimationSpeed, Math.max(ClientConfig.minAnimationSpeed, (animationSpeed + speedComponent) * sizeComponent));
        }
        AnimationUtils.setAnimationSpeed(finalAnimationSpeed, state.getAnimationTick(), animationController);

        return PlayState.CONTINUE;
    }

    @SubscribeEvent
    public static void tickEntity(RenderFrameEvent.Pre event) {
        globalTickCount += event.getPartialTick().getRealtimeDeltaTicks();
    }

    @Override
    public double getTick(Object obj) { // using 'getPlayer' breaks animations even though it returns the same entity...?
        // Prevent being on a negative tick (will cause t-posing!) by adding 200 here
        return (playerId != null ? level().getEntity(playerId).tickCount : globalTickCount) + 200;
    }

    private RawAnimation renderAbility(final AnimationState<DragonEntity> state, final DragonAbilityInstance currentCast) {
        RawAnimation rawAnimation = null;

        // FIXME
        /*if (currentCast != null && lastCast == null) {
            // Need to animate cast and there was no previous animation
            if (currentCast.getStartingAnimation() != null) {
                AbilityAnimation animation = currentCast.getStartingAnimation();
                neckLocked = animation.locksNeck;
                tailLocked = animation.locksTail;

                if (!started) {
                    animationTimer.putAnimation(animation.animationKey, animation.duration);
                    started = true;
                }

                rawAnimation = RawAnimation.begin().thenLoop(animation.animationKey);

                if (animationTimer.getDuration(animation.animationKey) <= 0) {
                    lastCast = currentCast;
                    started = false;
                }
            } else if (currentCast.getLoopingAnimation() != null) {
                AbilityAnimation animation = currentCast.getLoopingAnimation();
                neckLocked = animation.locksNeck;
                tailLocked = animation.locksTail;

                lastCast = currentCast;

                rawAnimation = RawAnimation.begin().thenLoop(animation.animationKey);
            }
        } else if (currentCast != null) {
            // Save the current cast as the previous cast
            lastCast = currentCast;

            if (currentCast.getLoopingAnimation() != null) {
                AbilityAnimation animation = currentCast.getLoopingAnimation();
                neckLocked = animation.locksNeck;
                tailLocked = animation.locksTail;

                rawAnimation = RawAnimation.begin().thenLoop(animation.animationKey);
            }
        } else if (lastCast != null) {
            // Play the stopping animation
            if (lastCast.getStoppingAnimation() != null) {
                AbilityAnimation stopAnimation = lastCast.getStoppingAnimation();
                neckLocked = stopAnimation.locksNeck;
                tailLocked = stopAnimation.locksTail;

                if (!ended) {
                    animationTimer.putAnimation(stopAnimation.animationKey, stopAnimation.duration);
                    ended = true;
                }

                rawAnimation = RawAnimation.begin().thenPlay(stopAnimation.animationKey);

                if (animationTimer.getDuration(stopAnimation.animationKey) <= 0) {
                    lastCast = null;
                    ended = false;
                }
            } else {
                lastCast = null;
            }
        }*/

        return rawAnimation;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public boolean shouldPlayAnimsWhileGamePaused() {
        // Important to play animations inside menus (e.g. for fake player / dragons)
        return true;
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        Player player = getPlayer();

        if (player != null) {
            return player.getArmorSlots();
        }

        return List.of();
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slotIn) {
        Player player = getPlayer();

        if (player != null) {
            return player.getItemBySlot(slotIn);
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slotIn, @NotNull ItemStack stack) {
        Player player = getPlayer();

        if (player != null) {
            player.setItemSlot(slotIn, stack);
        }
    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        Player player = getPlayer();

        if (player != null) {
            return player.getMainArm();
        }

        return HumanoidArm.LEFT;
    }

    // Animations
    private static final RawAnimation BITE = RawAnimation.begin().thenLoop("bite");
    private static final RawAnimation USE_ITEM = RawAnimation.begin().thenLoop("use_item");
    private static final RawAnimation USE_ITEM_RIGHT = RawAnimation.begin().thenLoop("use_item_right");
    private static final RawAnimation USE_ITEM_LEFT = RawAnimation.begin().thenLoop("use_item_left");
    private static final RawAnimation EAT_ITEM_RIGHT = RawAnimation.begin().thenLoop("eat_item_right");
    private static final RawAnimation EAT_ITEM_LEFT = RawAnimation.begin().thenLoop("eat_item_left");

    private static final RawAnimation SIT_ON_MAGIC_SOURCE = RawAnimation.begin().thenLoop("sit_on_magic_source");
    private static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("sleep");
    private static final RawAnimation SIT = RawAnimation.begin().thenLoop("sit");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
    private static final RawAnimation FLY_SOARING = RawAnimation.begin().thenLoop("fly_soaring");
    private static final RawAnimation FLY_DIVE = RawAnimation.begin().thenLoop("fly_dive");
    private static final RawAnimation FLY_DIVE_ALT = RawAnimation.begin().thenLoop("fly_dive_alt");
    private static final RawAnimation FLY_SPIN = RawAnimation.begin().thenLoop("fly_spin");
    private static final RawAnimation FLY_LAND = RawAnimation.begin().thenLoop("fly_land");
    private static final RawAnimation SWIM = RawAnimation.begin().thenLoop("swim");
    private static final RawAnimation SWIM_FAST = RawAnimation.begin().thenLoop("swim_fast");
    private static final RawAnimation FALL_LOOP = RawAnimation.begin().thenLoop("fall_loop");
    private static final RawAnimation SNEAK = RawAnimation.begin().thenLoop("sneak");
    private static final RawAnimation SNEAK_WALK = RawAnimation.begin().thenLoop("sneak_walk");
    private static final RawAnimation DIG_SNEAK = RawAnimation.begin().thenLoop("dig_sneak");
    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation DIG = RawAnimation.begin().thenLoop("dig");

    private static final RawAnimation JUMP = RawAnimation.begin().then("jump", Animation.LoopType.PLAY_ONCE).thenLoop("fall_loop");
    private static final RawAnimation FLY_LAND_END = RawAnimation.begin().then("fly_land_end", Animation.LoopType.PLAY_ONCE).thenLoop("idle");

    private static final RawAnimation TAIL_TURN = RawAnimation.begin().thenLoop("tail_turn");
    private static final RawAnimation HEAD_TURN = RawAnimation.begin().thenLoop("head_turn");
}