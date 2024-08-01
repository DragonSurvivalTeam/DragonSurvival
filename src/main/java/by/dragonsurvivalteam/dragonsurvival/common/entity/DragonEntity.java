package by.dragonsurvivalteam.dragonsurvival.common.entity;

import by.dragonsurvivalteam.dragonsurvival.client.emotes.Emote;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.AnimationTimer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.CommonTraits;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.EmoteCap;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.common.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.ISecondAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.Animation.LoopType;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class DragonEntity extends LivingEntity implements GeoEntity, CommonTraits {
	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public final ArrayList<Double> bodyYawAverage = new ArrayList<>();
	public final ArrayList<Double> headYawAverage = new ArrayList<>();
	public final ArrayList<Double> headPitchAverage = new ArrayList<>();
	public final ArrayList<Double> tailSideAverage = new ArrayList<>();
	public final ArrayList<Double> tailUpAverage = new ArrayList<>();
	/** This reference must be updated whenever player is remade, for example, when changing dimensions */
	public volatile Integer playerId; // TODO :: Use string uuid?
	public boolean neckLocked = false;
	public boolean tailLocked = false;
	public float prevZRot;
	public float prevXRot;

	public double tailMotionSide;
	public double tailMotionUp;
	public double body_yaw_change = 0;
	public double head_yaw_change = 0;
	public double head_pitch_change = 0;
	public double tail_motion_up = 0;
	public double tail_motion_side = 0;
	ActiveDragonAbility lastCast = null;
	boolean started, ended;
	AnimationTimer animationTimer = new AnimationTimer();
	private final double defaultPlayerWalkSpeed = 0.1;
	private final double defaultPlayerSneakSpeed = 0.03;
	private final double defaultPlayerFastSwimSpeed = 0.13;
	private final double defaultPlayerSwimSpeed = 0.051;
	private final double defaultPlayerSprintSpeed = 0.165;

	public DragonEntity(EntityType<? extends LivingEntity> type, Level worldIn){
		super(type, worldIn);
	}

	@Override
	public void registerControllers(final AnimatableManager.ControllerRegistrar registrar) {
		for (int slot = 0; slot < EmoteCap.MAX_EMOTES; slot++) {
			int finalSlot = slot;
			registrar.add(new AnimationController<>(this, "2_" + slot, 0, state -> emotePredicate(state, finalSlot)));
		}

		registrar.add(new AnimationController<>(this, "3", 2, this::predicate));
		registrar.add(new AnimationController<>(this, "4", this::bitePredicate));
		registrar.add(new AnimationController<>(this, "5", this::tailPredicate));
		registrar.add(new AnimationController<>(this, "1", this::headPredicate));
	}

	private PlayState tailPredicate(final AnimationState<DragonEntity> state) {
		if (!tailLocked || !ClientConfig.enableTailPhysics) {
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
		DragonStateHandler handler = DragonUtils.getHandler(player);

		ActiveDragonAbility currentCast = handler.getMagicData().getCurrentlyCasting();

		RawAnimation builder = null;

		if (currentCast instanceof ISecondAnimation || lastCast instanceof ISecondAnimation) {
			builder = renderAbility(state, currentCast);
		}

		if (!ClientDragonRender.renderItemsInMouth && doesAnimationExist("use_item") && (player.isUsingItem() || (handler.getMovementData().bite || handler.getMovementData().dig) && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()))) {
			// When the player is using an item
			handler.getMovementData().bite = false;
			return state.setAndContinue(AnimationUtils.createAnimation(builder, USE_ITEM));
		} else if (!ClientDragonRender.renderItemsInMouth && doesAnimationExist("eat_item_right") && player.isUsingItem() && DragonFoodHandler.isEdible(player.getMainHandItem(), handler.getType()) || animationTimer.getDuration("eat_item_right") > 0) {
			// When the player is eating the main hand item
			if (animationTimer.getDuration("eat_item_right") <= 0) {
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("eat_item_right", 0.32 * 20);
			}

			return state.setAndContinue(AnimationUtils.createAnimation(builder, EAT_ITEM_RIGHT));
		} else if (!ClientDragonRender.renderItemsInMouth && doesAnimationExist("eat_item_left") && player.isUsingItem() && DragonFoodHandler.isEdible(player.getMainHandItem(), handler.getType()) || animationTimer.getDuration("eat_item_right") > 0) {
			// When the player is eating the offhand item
			if (animationTimer.getDuration("eat_item_left") <= 0) {
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("eat_item_left", 0.32 * 20);
			}

			return state.setAndContinue(AnimationUtils.createAnimation(builder, EAT_ITEM_LEFT));
		} else if (!ClientDragonRender.renderItemsInMouth && doesAnimationExist("use_item_right") && !player.getMainHandItem().isEmpty() && handler.getMovementData().bite && player.getMainArm() == HumanoidArm.RIGHT || animationTimer.getDuration("use_item_right") > 0) {
			// When the player is using the main hand item
			if (animationTimer.getDuration("use_item_right") <= 0) {
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("use_item_right", 0.32 * 20);
			}

			return state.setAndContinue(AnimationUtils.createAnimation(builder, USE_ITEM_RIGHT));
		} else if (!ClientDragonRender.renderItemsInMouth && doesAnimationExist("use_item_left") && !player.getOffhandItem().isEmpty() && handler.getMovementData().bite && player.getMainArm() == HumanoidArm.LEFT || animationTimer.getDuration("use_item_left") > 0) {
			// When the player is using the offhand item
			if (animationTimer.getDuration("use_item_left") <= 0) {
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("use_item_left", 0.32 * 20);
			}

			return state.setAndContinue(AnimationUtils.createAnimation(builder, USE_ITEM_LEFT));
		} else if (handler.getMovementData().bite && !handler.getMovementData().dig || animationTimer.getDuration("bite") > 0) {
			if (animationTimer.getDuration("bite") <= 0) {
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("bite", 0.44 * 20);
			}

			return state.setAndContinue(AnimationUtils.createAnimation(builder, BITE));
		}

		return PlayState.STOP;
	}

	private boolean doesAnimationExist(final String animation) {
		return GeckoLibCache.getBakedAnimations().get(ClientDragonRender.dragonModel.getAnimationResource(ClientDragonRender.dragonArmor)).getAnimation(animation) != null;
	}

	private PlayState emotePredicate(final AnimationState<DragonEntity> state, int slot) {
		DragonStateHandler handler = DragonUtils.getHandler(getPlayer());

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
		return (Player) level().getEntity(playerId);
	}

	private PlayState predicate(final AnimationState<DragonEntity> state) {
		Player player = getPlayer();

		AnimationController<DragonEntity> animationController = state.getController();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if (handler.refreshBody) {
			animationController.forceAnimationReset();
			handler.refreshBody = false;
		}

		boolean useDynamicScaling = false;
		double animationSpeed = 1;
		double speedFactor = ClientConfig.movementAnimationSpeedFactor;
		double baseSpeed = defaultPlayerWalkSpeed;
		double smallSizeFactor = ClientConfig.smallSizeAnimationSpeedFactor;
		double bigSizeFactor = ClientConfig.largeSizeAnimationSpeedFactor;
		double baseSize = ServerConfig.DEFAULT_MAX_GROWTH_SIZE;
		double distanceFromGround = ServerFlightHandler.distanceFromGround(player);
		double height = DragonSizeHandler.calculateDragonHeight(handler.getSize(), ServerConfig.hitboxGrowsPastHuman);

		if (player == null || Stream.of(handler.getEmoteData().currentEmotes).anyMatch(emote -> emote != null && !emote.blend && emote.animation != null && !emote.animation.isBlank())) {
			state.getController().stop();
			return PlayState.STOP;
		}

		// TODO :: Do these need to be re-set to false when the ability sets it to true?
		neckLocked = false;
		tailLocked = false;

		Vec3 deltaMovement = player.getDeltaMovement();
		ActiveDragonAbility currentCast = handler.getMagicData().getCurrentlyCasting();

		RawAnimation builder = null;

		if (!(currentCast instanceof ISecondAnimation) && !(lastCast instanceof ISecondAnimation)) {
			builder = renderAbility(state, currentCast);
		}

		// The reason the threshold is so high here is that lower thresholds cause the player to be stuck transitioning away from the walk animation longer than they should when they stop moving.
		boolean isMovingHorizontalWalk = player.getDeltaMovement().horizontalDistance() > defaultPlayerWalkSpeed / 5;
		boolean isMovingHorizontalSneak = player.getDeltaMovement().horizontalDistance() > defaultPlayerSneakSpeed / 5;

		if(handler.getMagicData().onMagicSource){
			neckLocked = false;
			tailLocked = false;
			return state.setAndContinue(AnimationUtils.createAnimation(builder, SIT_ON_MAGIC_SOURCE));
		}else if(player.isSleeping() || handler.treasureResting){
			neckLocked = false;
			tailLocked = false;
			return state.setAndContinue(AnimationUtils.createAnimation(builder, SLEEP));
		}else if(player.isPassenger()){
			neckLocked = false;
			tailLocked = false;
			return state.setAndContinue(AnimationUtils.createAnimation(builder, SIT));
		}else if(player.getAbilities().flying || ServerFlightHandler.isFlying(player)){
			if(ServerFlightHandler.isGliding(player)){
				neckLocked = false;
				tailLocked = false;
				if(ServerFlightHandler.isSpin(player)){
					neckLocked = false;
					tailLocked = false;
					animationSpeed = 2;
					state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SPIN));
					animationController.transitionLength(2);
				}else if(deltaMovement.y < -1){
					state.setAnimation(AnimationUtils.createAnimation(builder, FLY_DIVE_ALT));
					animationController.transitionLength(4);
				}else if(deltaMovement.y < -0.25){
					state.setAnimation(AnimationUtils.createAnimation(builder, FLY_DIVE));
					animationController.transitionLength(4);
				}else if(deltaMovement.y > 0.5){
					animationSpeed = 1.5;
					state.setAnimation(AnimationUtils.createAnimation(builder, FLY));
					animationController.transitionLength(2);
				}else{
					state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SOARING));
					animationController.transitionLength(4);
				}
			}else{
				if(player.isCrouching() && deltaMovement.y < 0 && distanceFromGround < 10 && deltaMovement.length() < 4){
					neckLocked = false;
					tailLocked = false;
					state.setAnimation(AnimationUtils.createAnimation(builder, FLY_LAND));
					animationController.transitionLength(2);
				} else if(ServerFlightHandler.isSpin(player)){
					neckLocked = false;
					tailLocked = false;
					state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SPIN));
					animationController.transitionLength(2);
				}else{
					neckLocked = false;
					tailLocked = false;
					if(deltaMovement.y > 0) {
						animationSpeed = 2;
					}
					state.setAnimation(AnimationUtils.createAnimation(builder, FLY));
					animationController.transitionLength(2);
				}
			}
		}else if(player.getPose() == Pose.SWIMMING){
			if(ServerFlightHandler.isSpin(player)){
				neckLocked = false;
				tailLocked = false;
				state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SPIN));
				animationController.transitionLength(2);
			}else{
				useDynamicScaling = true;
				baseSpeed = defaultPlayerFastSwimSpeed; // Default base fast speed for the player
				state.setAnimation(AnimationUtils.createAnimation(builder, SWIM_FAST));
				animationController.transitionLength(2);
			}
		}else if((player.isInLava() || player.isInWaterOrBubble()) && !player.onGround()){
			if(ServerFlightHandler.isSpin(player)){
				neckLocked = false;
				tailLocked = false;
				animationSpeed = 2;
				state.setAnimation(AnimationUtils.createAnimation(builder, FLY_SPIN));
				animationController.transitionLength(2);
			}else{
				useDynamicScaling = true;
				baseSpeed = defaultPlayerSwimSpeed;
				state.setAnimation(AnimationUtils.createAnimation(builder, SWIM));
				animationController.transitionLength(2);
			}
		}else if(AnimationUtils.isAnimationPlaying(animationController, "fly_land")) {
			state.setAnimation(AnimationUtils.createAnimation(builder, FLY_LAND_END));
			animationController.transitionLength(2);
		} else if(AnimationUtils.isAnimationPlaying(animationController, "fly_land_end")) {
			// Don't add any animation
		}else if(!player.onGround() && ClientEvents.dragonsJumpingTicks.getOrDefault(this.playerId, 0) > 0){
			state.setAnimation(AnimationUtils.createAnimation(builder, JUMP));
			animationController.transitionLength(2);
		// Extra condition to prevent the player from triggering the fall animation when falling a trivial distance (this happens when you are really big)
		}else if(!player.onGround()) {
			state.setAnimation(AnimationUtils.createAnimation(builder, FALL_LOOP));
			animationController.transitionLength(2);
		} else if(player.isShiftKeyDown() || !DragonSizeHandler.canPoseFit(player, Pose.STANDING) && DragonSizeHandler.canPoseFit(player, Pose.CROUCHING)){
			// Player is Sneaking
			if(isMovingHorizontalSneak){
				useDynamicScaling = true;
				baseSpeed = defaultPlayerSneakSpeed;
				state.setAnimation(AnimationUtils.createAnimation(builder, SNEAK_WALK));
				animationController.transitionLength(5);
			}else if(handler.getMovementData().dig){
				state.setAnimation(AnimationUtils.createAnimation(builder, DIG_SNEAK));
				animationController.transitionLength(5);
			}else{
				state.setAnimation(AnimationUtils.createAnimation(builder, SNEAK));
				animationController.transitionLength(5);
			}
		}else if(player.isSprinting()){
			useDynamicScaling = true;
			baseSpeed = defaultPlayerSprintSpeed;
			state.setAnimation(AnimationUtils.createAnimation(builder, RUN));
			animationController.transitionLength(2);
		}else if(isMovingHorizontalWalk){
			useDynamicScaling = true;
			state.setAnimation(AnimationUtils.createAnimation(builder, WALK));
			animationController.transitionLength(2);
		}else if(handler.getMovementData().dig){
			state.setAnimation(AnimationUtils.createAnimation(builder, DIG));
			animationController.transitionLength(2);
		} else {
			state.setAnimation(AnimationUtils.createAnimation(builder, IDLE));
			animationController.transitionLength(2);
		}

		double finalAnimationSpeed = animationSpeed;
		if(useDynamicScaling) {
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

	private RawAnimation renderAbility(final AnimationState<DragonEntity> state, final ActiveDragonAbility currentCast) {
		RawAnimation rawAnimation = null;

		if (currentCast != null && lastCast == null) {
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
		}

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
	public @NotNull Iterable<ItemStack> getArmorSlots(){
		return playerId != null ? getPlayer().getArmorSlots() : List.of();
	}

	@Override
	public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slotIn){
		return playerId != null ? getPlayer().getItemBySlot(slotIn) : ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(@NotNull EquipmentSlot slotIn, @NotNull ItemStack stack){
		if(playerId != null){
			getPlayer().setItemSlot(slotIn, stack);
		}
	}

	@Override
	public @NotNull HumanoidArm getMainArm(){
		return playerId != null ? getPlayer().getMainArm() : HumanoidArm.LEFT;
	}

	@Override
	public boolean isAlwaysTicking() {
		return true;
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

	private static final RawAnimation JUMP = RawAnimation.begin().then("jump", LoopType.PLAY_ONCE).thenLoop("fall_loop");
	private static final RawAnimation FLY_LAND_END = RawAnimation.begin().then("fly_land_end", LoopType.PLAY_ONCE).thenLoop("idle");

	private static final RawAnimation TAIL_TURN = RawAnimation.begin().thenLoop("tail_turn");
	private static final RawAnimation HEAD_TURN = RawAnimation.begin().thenLoop("head_turn");
}