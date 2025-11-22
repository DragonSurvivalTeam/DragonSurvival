package by.dragonsurvivalteam.dragonsurvival.common.entity;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.AnimationTickTimer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationType;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.create.SkyhookRendererHelper;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmote;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.DragonAnimations;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static by.dragonsurvivalteam.dragonsurvival.client.DragonSurvivalClient.DRAGON_MODEL;

@EventBusSubscriber
public class DragonEntity extends LivingEntity implements GeoEntity {
    private static final int MAX_EMOTES = 4;
    private static final int CONTINUOUS_ANIMATION_SLOTS = 4;

    // Default player values
    private static final double DEFAULT_WALK_SPEED = 0.1; // Abilities#walkingSpeed
    private static final double DEFAULT_SNEAK_SPEED = 0.03; // Attributes#SNEAKING_SPEED default value
    private static final double DEFAULT_SPRINT_SPEED = 0.165;
    private static final double DEFAULT_SWIM_SPEED = 0.051;
    private static final double DEFAULT_FAST_SWIM_SPEED = 0.13;
    private static final double DEFAULT_CLIMB_SPEED = 0.0001;

    // Base "scale" to use when determining animation speed
    private static final double BASE_SCALE = 1.0;

    /** Durations of jumps */
    public static final ConcurrentHashMap<Integer, Boolean> DRAGONS_JUMPING = new ConcurrentHashMap<>();

    private static double globalTickCount;

    public final ArrayList<Double> headYawHistory = new ArrayList<>();
    public double currentHeadYawChange;

    public final ArrayList<Double> bodyYawHistory = new ArrayList<>();
    public double currentBodyYawChange;

    public final ArrayList<Double> headPitchHistory = new ArrayList<>();
    public double currentHeadPitchChange;

    public final ArrayList<Double> verticalVelocityHistory = new ArrayList<>();
    public double currentTailMotionUp;

    public AnimationController<DragonEntity> mainAnimationController;

    /** This reference must be updated whenever player is remade, for example, when changing dimensions */
    public volatile Integer playerId;

    public boolean neckLocked;
    public boolean tailLocked;
    public float prevZRot;
    public float prevXRot;

    // In certain circumstances, we need to override the UUID with the local player's UUID when gathering textures for the dragon entity
    // At the moment, this only happens on the smithing screen, as the player in the inventory panels is actually referring the real player and therefore has the correct UUID for textures
    // The dragon displayed in the editor doesn't want to mirror the local player's UUID, so this isn't used there either
    public boolean overrideUUIDWithLocalPlayerForTextureFetch;

    /**
     * Used for inventory / smithing screen rendering - when set to true changed movement data will not be tracked <br>
     * - Does not set the movement data <br>
     * - Does not apply the molang history (of head pitch, body yaw, etc.) <br>
     * - Does not hide the head when in first person
     */
    public boolean isInInventory;

    public boolean clearVerticalVelocity;

    private final DragonEmote[] currentlyPlayingEmotes = new DragonEmote[MAX_EMOTES];
    private final boolean[] soundForEmoteHasAlreadyPlayedThisTick = new boolean[MAX_EMOTES];
    private final AnimationTickTimer animationTickTimer = new AnimationTickTimer();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private Pair<AbilityAnimation, AnimationType> currentAbilityAnimation;
    private boolean begunPlayingAbilityAnimation;
    public boolean renderingWasCancelled;

    public DragonEntity(EntityType<? extends LivingEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar registrar) {
        mainAnimationController = new AnimationController<>(this, "main", 2, this::predicate);
        registrar.add(mainAnimationController);

        for (int slot = 0; slot < MAX_EMOTES; slot++) {
            int finalSlot = slot;
            registrar.add(new AnimationController<>(this, EMOTE + slot, state -> emotePredicate(state, finalSlot)));
        }

        registrar.add(new AnimationController<>(this, "bite", 2,  this::bitePredicate));
        registrar.add(new AnimationController<>(this, "breath", 2,  this::breathPredicate));

        // Continuous animation slots, used for things that need to play always (wind blowing, breathing, etc...)
        for (int slot = 0; slot < CONTINUOUS_ANIMATION_SLOTS; slot++) {
            int finalSlot = slot;
            registrar.add(new AnimationController<>(this, CONTINUOUS + slot, state -> continousPredicate(state, finalSlot)));
        }
    }

    public void stopAllEmotes() {
        Arrays.fill(currentlyPlayingEmotes, null);
    }

    public int getTicksForEmote(int slot) {
        if (animationTickTimer.isPresent(EMOTE + slot)) {
            return (int) Math.ceil(animationTickTimer.getDuration(EMOTE + slot));
        }

        return -1;
    }

    public void clearSoundsPlayedThisTick() {
        Arrays.fill(soundForEmoteHasAlreadyPlayedThisTick, false);
    }

    public boolean markEmoteSoundPlayedThisTick(int slot) {
        if (soundForEmoteHasAlreadyPlayedThisTick[slot]) {
            return false;
        }

        soundForEmoteHasAlreadyPlayedThisTick[slot] = true;
        return true;
    }

    public void stopEmote(int slot) {
        if (currentlyPlayingEmotes[slot] != null) {
            animationTickTimer.stopAnimation(EMOTE + slot);
            currentlyPlayingEmotes[slot] = null;
        }
    }

    public void stopEmote(DragonEmote emote) {
        for (int i = 0; i < MAX_EMOTES; i++) {
            if (currentlyPlayingEmotes[i] == emote) {
                currentlyPlayingEmotes[i] = null;
                animationTickTimer.stopAnimation(EMOTE + i);
                return;
            }
        }
    }

    public void beginPlayingEmote(DragonEmote emote) {
        if (emote == null) {
            return;
        }

        for (int i = 0; i < MAX_EMOTES; i++) {
            if (currentlyPlayingEmotes[i] == emote) {
                currentlyPlayingEmotes[i] = null;
                animationTickTimer.stopAnimation(EMOTE + i);
                continue;
            }

            if (currentlyPlayingEmotes[i] == null) {
                continue;
            }

            // Remove any emotes from conflicting layers (non-blend removes other non-blends)
            if (!currentlyPlayingEmotes[i].blend() && !emote.blend()) {
                currentlyPlayingEmotes[i] = null;
                animationTickTimer.stopAnimation(EMOTE + i);
            }
        }

        for (int i = 0; i < MAX_EMOTES; i++) {
            if (currentlyPlayingEmotes[i] == null) {
                currentlyPlayingEmotes[i] = emote;

                if (emote.duration() != DragonEmote.NO_DURATION) {
                    animationTickTimer.putAnimation(EMOTE + i, (double) emote.duration());
                } else {
                    animationTickTimer.putAnimation(EMOTE + i, AnimationUtils.animationDuration(DRAGON_MODEL, this, emote.animationKey()));
                }

                if (emote.sound().isPresent()) {
                    if (getPlayer() != null) {
                        emote.sound().get().playSound(getPlayer());
                    }
                }

                return;
            }
        }
    }

    public DragonEmote[] getCurrentlyPlayingEmotes() {
        return currentlyPlayingEmotes;
    }

    /**
     * Checks all non-null (i.e. playing) emotes for the predicate
     *
     * @return 'true' if the predicate is 'true' for any emote
     */
    private boolean checkAllEmotes(final Predicate<DragonEmote> predicate) {
        for (DragonEmote emote : currentlyPlayingEmotes) {
            if (emote != null && predicate.test(emote)) {
                return true;
            }
        }

        return false;
    }

    public boolean isPlayingAnyEmote() {
        return Stream.of(currentlyPlayingEmotes).anyMatch(Objects::nonNull);
    }

    public boolean isPlayingEmote(DragonEmote emote) {
        return Stream.of(currentlyPlayingEmotes).anyMatch(e -> e == emote);
    }

    public void setCurrentAbilityAnimation(Pair<AbilityAnimation, AnimationType> currentAbilityAnimation) {
        if (this.currentAbilityAnimation != null) {
            animationTickTimer.stopAnimation(this.currentAbilityAnimation.getFirst().getName());
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
            currentAbilityAnimation.getFirst().play(state, currentAbilityAnimation.getSecond());
            if (currentAbilityAnimation.getSecond() == AnimationType.PLAY_ONCE) {
                // Only trigger use a timer for PLAY_ONCE animations, as the others are intended to await a future packet to stop them
                animationTickTimer.putAnimation(DRAGON_MODEL, this, currentAbilityAnimation.getFirst().getName());
            }
        } else if (begunPlayingAbilityAnimation && isNotPlayingCurrentAbilityAnimation && currentAbilityAnimation.getSecond() == AnimationType.PLAY_ONCE) {
            begunPlayingAbilityAnimation = false;
            currentAbilityAnimation = null;
        } else if (begunPlayingAbilityAnimation && currentAbilityLayer == layer) {
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

        DragonStateHandler handler = DragonStateProvider.getData(player);
        if (handler.refreshBody) {
            state.getController().forceAnimationReset();
        }

        if (checkAndPlayAbilityAnimation(state, AnimationLayer.BREATH)) {
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
    }

    private PlayState playOrContinueAnimation(RawAnimation animation, AnimationState<DragonEntity> state, MovementData movement) {
        movement.bite = false;

        if (animationTickTimer.getDuration(animation) <= 0) {
            animationTickTimer.putAnimation(DRAGON_MODEL, this, animation);
        }

        return state.setAndContinue(animation);
    }

    private PlayState bitePredicate(final AnimationState<DragonEntity> state) {
        Player player = getPlayer();

        if (player == null) {
            return PlayState.STOP;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        if (handler.refreshBody) {
            state.getController().forceAnimationReset();
        }

        if (checkAndPlayAbilityAnimation(state, AnimationLayer.BITE)) {
            return PlayState.CONTINUE;
        }

        MovementData movement = MovementData.getData(player);
        boolean isUsingItem = player.isUsingItem();
        InteractionHand usedItemHand = getUsedItemHand();
        boolean isUsingEdibleItem = isUsingItem && DragonFoodHandler.isEdible(player, player.getItemInHand(usedItemHand));

        if (!ClientDragonRenderer.renderItemsInMouth) {
            if (isUsingEdibleItem) {
                if (usedItemHand == InteractionHand.MAIN_HAND || animationTickTimer.getDuration(DragonAnimations.EAT_ITEM_RIGHT.getAnimation()) > 0) {
                    return playOrContinueAnimation(DragonAnimations.EAT_ITEM_RIGHT.getAnimation(), state, movement);
                } else if (usedItemHand == InteractionHand.OFF_HAND || animationTickTimer.getDuration(DragonAnimations.EAT_ITEM_LEFT.getAnimation()) > 0) {
                    return playOrContinueAnimation(DragonAnimations.EAT_ITEM_LEFT.getAnimation(), state, movement);
                }
            } else if (isUsingItem) {
                // For the using animations, they are intended to only play once, at the start of the use.
                // So to account for this, we check the getTicksUsingItem time
                if ((usedItemHand == InteractionHand.MAIN_HAND && player.getTicksUsingItem() == 1)  || animationTickTimer.getDuration(DragonAnimations.USE_ITEM_RIGHT.getAnimation()) > 0) {
                    return playOrContinueAnimation(DragonAnimations.USE_ITEM_RIGHT.getAnimation(), state, movement);
                } else if ((usedItemHand == InteractionHand.OFF_HAND && player.getTicksUsingItem() == 1) || animationTickTimer.getDuration(DragonAnimations.USE_ITEM_LEFT.getAnimation()) > 0) {
                    return playOrContinueAnimation(DragonAnimations.USE_ITEM_LEFT.getAnimation(), state, movement);
                }
            } else if (!player.getMainHandItem().isEmpty()) {
                // Still play use item if we are holding an item in our main hand and left click
                if (movement.bite || animationTickTimer.getDuration(DragonAnimations.USE_ITEM_RIGHT.getAnimation()) > 0) {
                    return playOrContinueAnimation(DragonAnimations.USE_ITEM_RIGHT.getAnimation(), state, movement);
                }
            }
        }

        if (movement.bite && !movement.dig || animationTickTimer.getDuration(DragonAnimations.BITE.getAnimation()) > 0) {
            return playOrContinueAnimation(DragonAnimations.BITE.getAnimation(), state, movement);
        }

        return PlayState.STOP;
    }

    private PlayState continousPredicate(final AnimationState<DragonEntity> state, int slot) {
        Player player = getPlayer();

        if (player == null) {
            state.getController().forceAnimationReset();
            return PlayState.STOP;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        if (handler.refreshBody) {
            state.getController().forceAnimationReset();
        }

        if (AnimationUtils.doesAnimationExist(DRAGON_MODEL, this, CONTINUOUS + slot)) {
            RawAnimation continuousAnimation = RawAnimation.begin().thenPlay(CONTINUOUS + slot);
            state.setAndContinue(continuousAnimation);
            return PlayState.CONTINUE;
        } else {
            return PlayState.STOP;
        }
    }

    private PlayState emotePredicate(final AnimationState<DragonEntity> state, int slot) {
        Player player = getPlayer();

        if (player == null) {
            state.getController().forceAnimationReset();
            return PlayState.STOP;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        if (handler.refreshBody) {
            state.getController().forceAnimationReset();
        }

        if (currentlyPlayingEmotes[slot] != null) {
            DragonEmote emote = currentlyPlayingEmotes[slot];

            double duration = animationTickTimer.getDuration(EMOTE + slot);
            if (duration > 0 || emote.loops()) {
                state.getController().setAnimationSpeed(emote.speed());

                if (!emote.loops()) {
                    return state.setAndContinue(RawAnimation.begin().thenPlay(emote.animationKey()));
                } else {
                    // If the emote loops, we need to check if the duration is set to 0, and if so, set it to the default duration so that the sounds can keep playing properly
                    if (duration <= 0) {
                        if (emote.duration() != DragonEmote.NO_DURATION) {
                            animationTickTimer.putAnimation(EMOTE + slot, (double) emote.duration());
                        } else {
                            animationTickTimer.putAnimation(EMOTE + slot, AnimationUtils.animationDuration(DRAGON_MODEL, this, emote.animationKey()));
                        }
                    }

                    return state.setAndContinue(RawAnimation.begin().thenLoop(emote.animationKey()));
                }
            } else {
                currentlyPlayingEmotes[slot] = null;
                state.getController().forceAnimationReset();
                return PlayState.STOP;
            }
        }

        state.getController().forceAnimationReset();
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
    public float getScale() {
        Player player = getPlayer();

        if (player == null) {
            return super.getScale();
        }

        if (player.level().isClientSide()) {
            return (float) DragonStateProvider.getData(player).getVisualScale(player, DragonSurvival.PROXY.getPartialTick());
        }

        return player.getScale();
    }

    @Override
    protected @NotNull EntityDimensions getDefaultDimensions(@NotNull final Pose pose) {
        Player player = getPlayer();

        if (player == null) {
            return super.getDefaultDimensions(pose);
        }

        return player.getDimensions(pose);
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
    public @NotNull Vec3 getDeltaMovement() {
        Player player = getPlayer();

        if (player != null) {
            return player.getDeltaMovement();
        }

        return super.getDeltaMovement();
    }

    @Override
    public float getHealth() {
        Player player = getPlayer();

        if (player != null) {
            return player.getHealth();
        }

        return super.getHealth();
    }

    @Override
    public float getMaxHealth() {
        Player player = getPlayer();

        if (player != null) {
            return player.getMaxHealth();
        }

        return super.getMaxHealth();
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

    /**
     * Is used to determine if the player is considered swimming for animation purposes.
     * We also want to disable head bob and walking sound effect in this case.
     * See dragonSurvival$modifyWalkSoundsWhenWalkingUnderwater and dragonSurvival$consideredSwimmingEvenWhenGroundedInWater
     */
    public static boolean isConsideredSwimmingForAnimation(Player player) {
        boolean isInFluid = player.canSwimInFluidType(player.getInBlockState().getFluidState().getFluidType());
        return isInFluid && !player.isPassenger() && (!player.onGround() || !player.getEyeInFluidType().isAir());
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
        }

        boolean useDynamicScaling = false;
        double animationSpeed = 1;
        double speedFactor = ClientConfig.movementAnimationSpeedFactor;
        double baseSpeed = DEFAULT_WALK_SPEED;
        double smallSizeFactor = ClientConfig.smallSizeAnimationSpeedFactor;
        double bigSizeFactor = ClientConfig.largeSizeAnimationSpeedFactor;
        double distanceFromGround = ServerFlightHandler.distanceFromGround(player);

        if (checkAllEmotes(emote -> !emote.blend())) {
            // Set the lock state once here so it is correct for all the emotes
            neckLocked = checkAllEmotes(DragonEmote::locksHead);
            tailLocked = checkAllEmotes(DragonEmote::locksTail);
            state.getController().stop();
            return PlayState.STOP;
        }

        Vec3 deltaMovement = player.getDeltaMovement();

        // This predicate runs first, so we reset neck and tail lock here. If any animation locks them, they will be re-locked in time before the neck/tail animations are played.
        // It is also important we reset these values before trying to render abilities
        neckLocked = false;
        tailLocked = false;

        if (checkAndPlayAbilityAnimation(state, AnimationLayer.BASE)) {
            return PlayState.CONTINUE;
        }

        MovementData movement = MovementData.getData(player);
        boolean isSwimming = isConsideredSwimmingForAnimation(player);

        boolean animationWasNullBeforePredicate = animationController.getCurrentAnimation() == null;

        // TODO: The transition length of animations doesn't work correctly when the framerate varies too much from 60 FPS
        if (!movement.isMovingHorizontally() && handler.isOnMagicSource) {
            // TODO :: does this need to be synchronized to other players?
            return state.setAndContinue(DragonAnimations.SIT_ON_MAGIC_SOURCE.getAnimation());
        }

        if (player.isSleeping() || treasureRest.isResting()) {
            return state.setAndContinue(DragonAnimations.SLEEP.getAnimation());
        }

        if (SkyhookRendererHelper.isPlayerRidingSkyhook(player.getUUID()) && AnimationUtils.doesAnimationExist(DRAGON_MODEL, this, DragonAnimations.CREATE_SKYHOOK_RIDING.getAnimation())) {
            return state.setAndContinue(DragonAnimations.CREATE_SKYHOOK_RIDING.getAnimation());
        }

        if (player.isPassenger()) {
            return state.setAndContinue(DragonAnimations.SIT.getAnimation());
        }

        if (player.getAbilities().flying || ServerFlightHandler.isFlying(player)) {
            if (ServerFlightHandler.isGliding(player)) {
                if (ServerFlightHandler.isSpin(player)) {
                    animationSpeed = 2;
                    state.setAnimation(DragonAnimations.FLY_SPIN.getAnimation());
                    animationController.transitionLength(5);
                } else if (deltaMovement.y < -1) {
                    state.setAnimation(DragonAnimations.FLY_DIVE_ALT.getAnimation());
                    animationController.transitionLength(4);
                } else if (deltaMovement.y < -0.25) {
                    state.setAnimation(DragonAnimations.FLY_DIVE.getAnimation());
                    animationController.transitionLength(4);
                } else if (deltaMovement.y > 0.5) {
                    animationSpeed = 1.5;
                    state.setAnimation(DragonAnimations.FLY.getAnimation());
                    animationController.transitionLength(2);
                } else {
                    state.setAnimation(DragonAnimations.FLY_SOARING.getAnimation());
                    animationController.transitionLength(4);
                }
            } else {
                if (movement.desiredMoveVec.y < 0 && deltaMovement.y < 0 && distanceFromGround < 10 && deltaMovement.length() < 4) {
                    state.setAnimation(DragonAnimations.FLY_LAND.getAnimation());
                    animationController.transitionLength(2);
                } else if (ServerFlightHandler.isSpin(player)) {
                    state.setAnimation(DragonAnimations.FLY_SPIN.getAnimation());
                    animationController.transitionLength(2);
                } else {
                    if (movement.desiredMoveVec.y > 0) {
                        animationSpeed = 2;
                    }

                    state.setAnimation(DragonAnimations.FLY.getAnimation());
                    animationController.transitionLength(2);
                }
            }
        } else if (player.getPose() == Pose.SWIMMING) {
            if (ServerFlightHandler.isSpin(player)) {
                state.setAnimation(DragonAnimations.FLY_SPIN.getAnimation());
                animationController.transitionLength(2);
            } else {
                // Clear vertical velocity if we just transitioned to this pose, to prevent the dragon from jerking up when landing in water
                if (
                        !AnimationUtils.isAnimationPlaying(animationController, DragonAnimations.SWIM.getAnimation())
                                && !AnimationUtils.isAnimationPlaying(animationController, DragonAnimations.SWIM_FAST.getAnimation())
                                && !AnimationUtils.isAnimationPlaying(animationController, DragonAnimations.FLY_SPIN.getAnimation())
                ) {
                    clearVerticalVelocity();
                }

                useDynamicScaling = true;
                baseSpeed = DEFAULT_FAST_SWIM_SPEED; // Default base fast speed for the player
                state.setAnimation(DragonAnimations.SWIM_FAST.getAnimation());
                animationController.transitionLength(4);
            }
        } else if (isSwimming) {
            if (ServerFlightHandler.isSpin(player)) {
                animationSpeed = 2;
                state.setAnimation(DragonAnimations.FLY_SPIN.getAnimation());
                animationController.transitionLength(2);
            } else {
                // Clear vertical velocity if we just transitioned to this pose, to prevent the dragon from jerking up when landing in water
                if (
                        !AnimationUtils.isAnimationPlaying(animationController, DragonAnimations.SWIM.getAnimation())
                                && !AnimationUtils.isAnimationPlaying(animationController, DragonAnimations.SWIM_FAST.getAnimation())
                                && !AnimationUtils.isAnimationPlaying(animationController, DragonAnimations.FLY_SPIN.getAnimation())
                ) {
                    clearVerticalVelocity();
                }

                useDynamicScaling = true;
                baseSpeed = DEFAULT_SWIM_SPEED;
                state.setAnimation(DragonAnimations.SWIM.getAnimation());
                animationController.transitionLength(2);
            }
        } else if (AnimationUtils.isAnimationPlaying(animationController, DragonAnimations.FLY_LAND.getAnimation())) {
            state.setAnimation(DragonAnimations.FLY_LAND_END.getAnimation());

            if (!DragonAnimations.FLY_LAND_END.getAnimation().getAnimationStages().isEmpty()) {
                animationTickTimer.putAnimation(DRAGON_MODEL, this, DragonAnimations.FLY_LAND_END.getAnimation());
            }

            animationController.transitionLength(2);
        } else if (animationTickTimer.getDuration(DragonAnimations.FLY_LAND_END.getAnimation()) > 0) {
            // Don't add any animation
        } else if (player.onClimbable()) {
            if (movement.deltaMovement.y() < 0) {
                state.setAnimation(DragonAnimations.CLIMBING_DOWN.getAnimation());
            } else {
                state.setAnimation(DragonAnimations.CLIMBING_UP.getAnimation());
            }

            useDynamicScaling = true;
            baseSpeed = DEFAULT_CLIMB_SPEED;
            animationController.transitionLength(2);
        } else if (DRAGONS_JUMPING.getOrDefault(this.playerId, false)) {
            state.resetCurrentAnimation();
            state.setAnimation(DragonAnimations.JUMP.getAnimation());
            animationController.transitionLength(2);
            animationTickTimer.putAnimation(DRAGON_MODEL, this, DragonAnimations.JUMP.getAnimation());
            DRAGONS_JUMPING.remove(this.playerId);
        } else if (animationTickTimer.isPresent(DragonAnimations.JUMP.getAnimation()) && DRAGONS_JUMPING.getOrDefault(this.playerId, true)) {
            // We test here if the jump animation has been flagged with a false value; if this is the case, that means cancel any ongoing jumps that are occurring
            // This happens if we hit the ground
            //
            // Let the jump animation complete
        } else if (!player.onGround()) {
            state.setAnimation(DragonAnimations.FALL_LOOP.getAnimation());
            animationController.transitionLength(2);
        } else if (player.isShiftKeyDown() || !DragonSizeHandler.canPoseFit(player, Pose.STANDING) && DragonSizeHandler.canPoseFit(player, Pose.CROUCHING)) {
            // Player is Sneaking
            if (movement.isMovingHorizontally()) {
                useDynamicScaling = true;
                baseSpeed = DEFAULT_SNEAK_SPEED;
                state.setAnimation(DragonAnimations.SNEAK_WALK.getAnimation());
                animationController.transitionLength(5);
            } else if (movement.dig) {
                state.setAnimation(DragonAnimations.DIG_SNEAK.getAnimation());
                animationController.transitionLength(5);
            } else {
                state.setAnimation(DragonAnimations.SNEAK.getAnimation());
                animationController.transitionLength(5);
            }
        } else if (player.isSprinting()) {
            useDynamicScaling = true;
            baseSpeed = DEFAULT_SPRINT_SPEED;
            state.setAnimation(DragonAnimations.RUN.getAnimation());
            animationController.transitionLength(4);
        } else if (movement.isMovingHorizontally()) {
            useDynamicScaling = true;
            state.setAnimation(DragonAnimations.WALK.getAnimation());
            animationController.transitionLength(2);
        } else if (movement.dig) {
            state.setAnimation(DragonAnimations.DIG.getAnimation());
            animationController.transitionLength(6);
        } else {
            state.setAnimation(DragonAnimations.IDLE.getAnimation());
            animationController.transitionLength(2);
        }

        // If the animation was null, that means we were T-Posing before this animation was triggered
        // So instantly transition to prevent the player from seeing a T-Pose -> animation transition
        // This usually happens when changing dimensions, or new clientside dragons are initialized in the UI
        if(animationWasNullBeforePredicate) {
            animationController.transitionLength(0);
        }

        double finalAnimationSpeed = animationSpeed;
        if (useDynamicScaling) {
            double horizontalDistance = deltaMovement.horizontalDistance();
            double speedComponent = Math.min(ClientConfig.maxAnimationSpeedFactor, (horizontalDistance - baseSpeed) / baseSpeed * speedFactor);
            double sizeDistance = handler.getVisualScale(player, state.getPartialTick()) - BASE_SCALE;
            double sizeFactor = sizeDistance >= 0 ? bigSizeFactor : smallSizeFactor;
            double sizeComponent = BASE_SCALE / (BASE_SCALE + sizeDistance * sizeFactor);
            // We need a minimum speed here to prevent the animation from ever being truly at 0 speed (otherwise the animation state machine implodes)
            finalAnimationSpeed = Math.min(ClientConfig.maxAnimationSpeed, Math.max(ClientConfig.minAnimationSpeed, (animationSpeed + speedComponent) * sizeComponent));
        }
        AnimationUtils.setAnimationSpeed(finalAnimationSpeed, state.getAnimationTick(), animationController);

        if (isPlayingAnyEmote()) {
            // This means we are playing a blend emote; so we want to pass on the head/tail locked state
            neckLocked = checkAllEmotes(DragonEmote::locksHead);
            tailLocked = checkAllEmotes(DragonEmote::locksTail);
        }

        if (currentAbilityAnimation != null) {
            neckLocked = currentAbilityAnimation.getFirst().locksHead();
            tailLocked = currentAbilityAnimation.getFirst().locksTail();
        }

        return PlayState.CONTINUE;
    }

    @SubscribeEvent
    public static void tickEntity(final RenderFrameEvent.Pre event) {
        globalTickCount += event.getPartialTick().getRealtimeDeltaTicks();
    }

    @Override
    public double getTick(Object obj) { // using 'getPlayer' breaks animations even though it returns the same entity...?
        // Prevent being on a negative tick (will cause t-posing!) by adding 200 here
        return (playerId != null ? level().getEntity(playerId).tickCount : globalTickCount) + 200;
    }

    @Override
    public @NotNull Vec3 position() {
        Player player = getPlayer();

        if (player == null) {
            return super.position();
        }

        return player.position();
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

    // Dynamic animation names
    private static final String EMOTE = "emote_";
    private static final String CONTINUOUS = "continuous_";
}