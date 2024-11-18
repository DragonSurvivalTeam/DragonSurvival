package by.dragonsurvivalteam.dragonsurvival.common.entity;

import by.dragonsurvivalteam.dragonsurvival.api.DragonFood;
import by.dragonsurvivalteam.dragonsurvival.client.emotes.Emote;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.AnimationTimer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.CommonTraits;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.EmoteCap;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.common.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.ISecondAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType.EDefaultLoopTypes;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.controller.AnimationController.IAnimationPredicate;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DragonEntity extends LivingEntity implements IAnimatable, CommonTraits {
	public final ArrayList<Double> bodyYawHistory = new ArrayList<>();
	public final ArrayList<Double> headYawHistory = new ArrayList<>();
	public final ArrayList<Double> headPitchHistory = new ArrayList<>();
	public final ArrayList<Double> verticalVelocityHistory = new ArrayList<>();
	/** This reference must be updated whenever player is remade, for example, when changing dimensions */
	public volatile Integer playerId; // TODO :: Use string uuid?
	public boolean neckLocked = false;
	public boolean tailLocked = false;
	public float prevZRot = 0;
	public float prevXRot = 0;

	public boolean clearVerticalVelocity = false;

	ActiveDragonAbility lastCast = null;
	boolean started, ended;
	AnimationTimer animationTimer = new AnimationTimer();
	Emote lastEmote;
	public double seekTime; // Copied from DragonModel.java in order to calculate the correct tickOffset when speed is adjusted for an animation.
	private final double defaultPlayerWalkSpeed = 0.1;
	private final double defaultPlayerSneakSpeed = 0.03;
	private final double defaultPlayerFastSwimSpeed = 0.13;
	private final double defaultPlayerSwimSpeed = 0.051;
	private final double defaultPlayerSprintSpeed = 0.165;
	AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

	public DragonEntity(EntityType<? extends LivingEntity> type, Level worldIn){
		super(type, worldIn);
	}

	public Vec3 getPseudoDeltaMovement() {
		Entity entity = level.getEntity(playerId);

		if (entity instanceof Player player) {
			return getPseudoDeltaMovement(player);
		}

		return new Vec3(0, 0, 0);
	}

	// TODO :: Not really needed while SyncFlightSpeed packet is constantly being synced
	public Vec3 getPseudoDeltaMovement(final Player player) {
		if (player == null) {
			return new Vec3(0, 0, 0);
		}

		if (true/*player == Minecraft.getInstance().player*/) {
			return player.getDeltaMovement();
		}

		return new Vec3(player.getX() - player.xo, player.getY() - player.yo, player.getZ() - player.zo);
	}

	@Override
	public void registerControllers(AnimationData animationData){

		for(int i = 0; i < EmoteCap.MAX_EMOTES; i++){
			int finalI = i;
			IAnimationPredicate<DragonEntity> predicate = s -> emotePredicate(finalI, s);
			animationData.addAnimationController(new AnimationController(this, "2_" + i, 0, predicate));
		}

		animationData.addAnimationController(new AnimationController(this, "3", 2, this::predicate));
		animationData.addAnimationController(new AnimationController(this, "4", 0, this::bitePredicate));
		animationData.addAnimationController(new AnimationController(this, "5", 0, this::tailPredicate));
		animationData.addAnimationController(new AnimationController(this, "1", 0, this::headPredicate));
	}

	private <E extends IAnimatable> PlayState tailPredicate(AnimationEvent<E> animationEvent){
		if(!tailLocked || !ClientConfig.enableTailPhysics){
			animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("tail_turn", EDefaultLoopTypes.LOOP));
			return PlayState.CONTINUE;
		}else{
			animationEvent.getController().setAnimation(null);
			animationEvent.getController().clearAnimationCache();
			return PlayState.STOP;
		}
	}

	private <E extends IAnimatable> PlayState headPredicate(AnimationEvent<E> animationEvent){
		if(!neckLocked){
			animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation("head_turn", EDefaultLoopTypes.LOOP));
			return PlayState.CONTINUE;
		}else{
			animationEvent.getController().setAnimation(null);
			animationEvent.getController().clearAnimationCache();
			return PlayState.STOP;
		}
	}

	private <E extends IAnimatable> PlayState bitePredicate(AnimationEvent<E> animationEvent){
		Player player = getPlayer();
		DragonStateHandler handler = DragonUtils.getHandler(player);
		AnimationBuilder builder = new AnimationBuilder();

		ActiveDragonAbility curCast = handler.getMagicData().getCurrentlyCasting();

		if(curCast instanceof ISecondAnimation || lastCast instanceof ISecondAnimation)
			renderAbility(builder, curCast);

		if(!ClientDragonRender.renderItemsInMouth && animationExists("use_item") && (player.isUsingItem() || (handler.getMovementData().bite || handler.getMovementData().dig) && (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty()))){
			builder.addAnimation("use_item", EDefaultLoopTypes.LOOP);
			handler.getMovementData().bite = false;
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("eat_item_right") && player.isUsingItem() && DragonFood.isEdible(this.getItemInHand(InteractionHand.MAIN_HAND).getItem(), player) || animationTimer.getDuration("eat_item_right") > 0){
			if(animationTimer.getDuration("eat_item_right") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("eat_item_right", 0.32 * 20, builder);
			}

			builder.addAnimation("eat_item_right", EDefaultLoopTypes.LOOP);
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("eat_item_left") && player.isUsingItem() && DragonFood.isEdible(this.getItemInHand(InteractionHand.OFF_HAND).getItem(), player) || animationTimer.getDuration("eat_item_right") > 0){
			if(animationTimer.getDuration("eat_item_left") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("eat_item_left", 0.32 * 20, builder);
			}

			builder.addAnimation("eat_item_left", EDefaultLoopTypes.LOOP);
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("use_item_right") && !player.getMainHandItem().isEmpty() && handler.getMovementData().bite && player.getMainArm() == HumanoidArm.RIGHT || animationTimer.getDuration("use_item_right") > 0){
			if(animationTimer.getDuration("use_item_right") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("use_item_right", 0.32 * 20, builder);
			}

			builder.addAnimation("use_item_right", EDefaultLoopTypes.LOOP);
		}else if(!ClientDragonRender.renderItemsInMouth && animationExists("use_item_left") && !player.getOffhandItem().isEmpty() && handler.getMovementData().bite && player.getMainArm() == HumanoidArm.LEFT || animationTimer.getDuration("use_item_left") > 0){
			if(animationTimer.getDuration("use_item_left") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("use_item_left", 0.32 * 20, builder);
			}

			builder.addAnimation("use_item_left", EDefaultLoopTypes.LOOP);
		}else if(handler.getMovementData().bite && !handler.getMovementData().dig || animationTimer.getDuration("bite") > 0){
			builder.addAnimation("bite", EDefaultLoopTypes.LOOP);
			if(animationTimer.getDuration("bite") <= 0){
				handler.getMovementData().bite = false;
				animationTimer.putAnimation("bite", 0.44 * 20, builder);
			}
		}

		if(builder.getRawAnimationList().size() > 0){
			animationEvent.getController().setAnimation(builder);
			return PlayState.CONTINUE;
		}

		return PlayState.STOP;
	}

	public static boolean animationExists(String key){
		Animation animation = GeckoLibCache.getInstance().getAnimations().get(ClientDragonRender.dragonModel.getAnimationResource(ClientDragonRender.dragonArmor)).getAnimation(key);

		return animation != null;
	}

	private <E extends IAnimatable> PlayState emotePredicate(int num, AnimationEvent<E> animationEvent){
		final Player player = getPlayer();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if(handler.getEmoteData().currentEmotes[num] != null){
			Emote emote = handler.getEmoteData().currentEmotes[num];

			neckLocked = false;
			tailLocked = false;

			animationEvent.getController().animationSpeed = emote.speed;

			if(emote.animation != null && !emote.animation.isEmpty()){
				animationEvent.getController().setAnimation(new AnimationBuilder().addAnimation(emote.animation, emote.loops ? EDefaultLoopTypes.LOOP : EDefaultLoopTypes.PLAY_ONCE));
				lastEmote = emote;
				return PlayState.CONTINUE;
			}
		}

		return PlayState.STOP;
	}

	public @Nullable Player getPlayer(){
		return (Player) level.getEntity(playerId);
	}

	@Override
	public AnimationFactory getFactory(){
		return animationFactory;
	}

	private void lockTailAndNeck() {
		neckLocked = true;
		tailLocked = true;
	}

	private void clearVerticalVelocity() {
		clearVerticalVelocity = true;
	}

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> animationEvent){
		animationEvent.getAnimationTick();
		Player player = getPlayer();
		AnimationController<E> animationController = animationEvent.getController();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		AnimationBuilder builder = new AnimationBuilder();

		boolean useDynamicScaling = false;
		double animationSpeed = 1;
		double speedFactor = ClientConfig.movementAnimationSpeedFactor;
		double baseSpeed = defaultPlayerWalkSpeed;
		double smallSizeFactor = ClientConfig.smallSizeAnimationSpeedFactor;
		double bigSizeFactor = ClientConfig.largeSizeAnimationSpeedFactor;
		double baseSize = ServerConfig.DEFAULT_MAX_GROWTH_SIZE;
		double distanceFromGround = ServerFlightHandler.distanceFromGround(player);
		double height = DragonSizeHandler.calculateDragonHeight(handler.getSize(), ServerConfig.hitboxGrowsPastHuman);

		if(player == null || Stream.of(handler.getEmoteData().currentEmotes).anyMatch(s -> s != null && !s.blend && s.animation != null && !s.animation.isBlank())){
			animationEvent.getController().setAnimation(null);
			animationEvent.getController().clearAnimationCache();
			return PlayState.STOP;
		}

		// This predicate runs first, so we reset neck and tail lock here. If any animation locks them, they will be re-locked in time before the neck/tail animations are played.
		// It is also important we reset these values before trying to render abilities
		neckLocked = false;
		tailLocked = false;

		Vec3 deltaMovement = getPseudoDeltaMovement(player);
		ActiveDragonAbility curCast = handler.getMagicData().getCurrentlyCasting();

		if(!(curCast instanceof ISecondAnimation) && !(lastCast instanceof ISecondAnimation)){
			renderAbility(builder, curCast);
		}

		final double INPUT_EPSILON = 0.0000001D;
		Vec2 rawInput = handler.getMovementData().desiredMoveVec;
		boolean hasMoveInput = rawInput.lengthSquared() > INPUT_EPSILON * INPUT_EPSILON;

		if(handler.getMagicData().onMagicSource){
			builder.addAnimation("sit_on_magic_source", EDefaultLoopTypes.LOOP);
		}else if(player.isSleeping() || handler.treasureResting){
			builder.addAnimation("sleep", EDefaultLoopTypes.LOOP);
		}else if(player.isPassenger()){
			builder.addAnimation("sit", EDefaultLoopTypes.LOOP);
		}else if(player.getAbilities().flying || ServerFlightHandler.isFlying(player)){
			if(ServerFlightHandler.isGliding(player)){
				if(ServerFlightHandler.isSpin(player)){
					animationSpeed = 2;
					lockTailAndNeck();
					AnimationUtils.addAnimation(builder, "fly_spin", EDefaultLoopTypes.LOOP, 5, animationController);
				}else if(deltaMovement.y < -1){
					AnimationUtils.addAnimation(builder, "fly_dive_alt", EDefaultLoopTypes.LOOP, 4, animationController);
				}else if(deltaMovement.y < -0.25){
					AnimationUtils.addAnimation(builder, "fly_dive", EDefaultLoopTypes.LOOP, 4, animationController);
				}else if(deltaMovement.y > 0.5){
					animationSpeed = 1.5;
					AnimationUtils.addAnimation(builder, "fly", EDefaultLoopTypes.LOOP, 2, animationController);
				}else{
					AnimationUtils.addAnimation(builder, "fly_soaring", EDefaultLoopTypes.LOOP, 4, animationController);
				}
			}else{
				if(player.isCrouching() && deltaMovement.y < 0 && distanceFromGround < 10 && deltaMovement.length() < 4){
					AnimationUtils.addAnimation(builder, "fly_land", EDefaultLoopTypes.LOOP, 2, animationController);
				} else if(ServerFlightHandler.isSpin(player)){
					lockTailAndNeck();
					AnimationUtils.addAnimation(builder, "fly_spin", EDefaultLoopTypes.LOOP, 2, animationController);
				}else{
					if(deltaMovement.y > 0) {
						animationSpeed = 2;
					}
					AnimationUtils.addAnimation(builder, "fly", EDefaultLoopTypes.LOOP, 2, animationController);
				}
			}
		}else if(player.getPose() == Pose.SWIMMING){
			if(ServerFlightHandler.isSpin(player)){
				lockTailAndNeck();
				AnimationUtils.addAnimation(builder, "fly_spin", EDefaultLoopTypes.LOOP, 2, animationController);
			}else{
				useDynamicScaling = true;
				baseSpeed = defaultPlayerFastSwimSpeed; // Default base fast speed for the player
				AnimationUtils.addAnimation(builder, "swim_fast", EDefaultLoopTypes.LOOP, 2, animationController);
			}
		}else if((player.isInLava() || player.isInWaterOrBubble()) && !player.isOnGround()){
			if(ServerFlightHandler.isSpin(player)){
				animationSpeed = 2;
				lockTailAndNeck();
				AnimationUtils.addAnimation(builder, "fly_spin", EDefaultLoopTypes.LOOP, 2, animationController);
			}else{
				// Clear vertical velocity if we just transitioned to this pose, to prevent the dragon from jerking up when landing in water
				if (!AnimationUtils.isAnimationPlaying(animationController, "swim") && !AnimationUtils.isAnimationPlaying(animationController, "swim_fast") && !AnimationUtils.isAnimationPlaying(animationController, "fly_spin")) {
					clearVerticalVelocity();
				}

				lockTailAndNeck();
				useDynamicScaling = true;
				baseSpeed = defaultPlayerSwimSpeed;
				AnimationUtils.addAnimation(builder, "swim", EDefaultLoopTypes.LOOP, 2, animationController);
			}
		}else if(animationController.getCurrentAnimation() != null && (Objects.equals(animationController.getCurrentAnimation().animationName, "fly_land"))) {
			AnimationUtils.addAnimation(builder, "fly_land_end", EDefaultLoopTypes.PLAY_ONCE, 2, animationController);
		} else if(animationController.getCurrentAnimation() != null && Objects.equals(animationController.getCurrentAnimation().animationName, "fly_land_end")) {
			// Don't add any animation
		}else if(ClientEvents.dragonsJumpingTicks.getOrDefault(this.playerId, 0) > 0){
			AnimationUtils.addAnimation(builder, "jump", EDefaultLoopTypes.PLAY_ONCE, 1, animationController);
		// Extra condition to prevent the player from triggering the fall animation when falling a trivial distance (this happens when you are really big)
		}else if(!player.isOnGround()) {
			AnimationUtils.addAnimation(builder, "fall_loop", EDefaultLoopTypes.LOOP, 6, animationController);
		} else if(player.isShiftKeyDown() || !DragonSizeHandler.canPoseFit(player, Pose.STANDING) && DragonSizeHandler.canPoseFit(player, Pose.CROUCHING)){
			// Player is Sneaking
			if(hasMoveInput){
				useDynamicScaling = true;
				baseSpeed = defaultPlayerSneakSpeed;
				AnimationUtils.addAnimation(builder, "sneak_walk", EDefaultLoopTypes.LOOP, 5, animationController);
			}else if(handler.getMovementData().dig){
				AnimationUtils.addAnimation(builder, "dig_sneak", EDefaultLoopTypes.LOOP, 5, animationController);
			}else{
				AnimationUtils.addAnimation(builder, "sneak", EDefaultLoopTypes.LOOP, 5, animationController);
			}
		}else if(player.isSprinting()){
			useDynamicScaling = true;
			baseSpeed = defaultPlayerSprintSpeed;
			AnimationUtils.addAnimation(builder, "run", EDefaultLoopTypes.LOOP, 2, animationController);
		}else if(hasMoveInput){
			useDynamicScaling = true;
			AnimationUtils.addAnimation(builder, "walk", EDefaultLoopTypes.LOOP, 2, animationController);
		}else if(handler.getMovementData().dig){
			AnimationUtils.addAnimation(builder, "dig", EDefaultLoopTypes.LOOP, 2, animationController);
		} else {
			AnimationUtils.addAnimation(builder, "idle", EDefaultLoopTypes.LOOP, 2, animationController);
		}

		if(animationController.getAnimationState() == AnimationState.Stopped) {
			AnimationUtils.addAnimation(builder, "idle", EDefaultLoopTypes.LOOP, 2, animationController);
		}

		animationController.setAnimation(builder);
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
		AnimationUtils.setAnimationSpeed(finalAnimationSpeed, animationEvent.getAnimationTick(), animationController);

		return PlayState.CONTINUE;
	}

	private void renderAbility(AnimationBuilder builder, ActiveDragonAbility curCast){
		if(curCast != null && lastCast == null){
			if(curCast.getStartingAnimation() != null){
				AbilityAnimation starAni = curCast.getStartingAnimation();
				neckLocked = starAni.locksNeck;
				tailLocked = starAni.locksTail;

				if(!started){
					animationTimer.putAnimation(starAni.animationKey, starAni.duration, builder);
					started = true;
				}

				builder.addAnimation(starAni.animationKey);

				if(animationTimer.getDuration(starAni.animationKey) <= 0){
					lastCast = curCast;
					started = false;
				}
			}else if(curCast.getLoopingAnimation() != null){
				AbilityAnimation loopingAni = curCast.getLoopingAnimation();
				neckLocked = loopingAni.locksNeck;
				tailLocked = loopingAni.locksTail;

				lastCast = curCast;
				builder.addAnimation(loopingAni.animationKey, EDefaultLoopTypes.LOOP);
			}
		}else if(curCast != null){
			lastCast = curCast;

			if(curCast.getLoopingAnimation() != null){
				AbilityAnimation loopingAni = curCast.getLoopingAnimation();
				neckLocked = loopingAni.locksNeck;
				tailLocked = loopingAni.locksTail;

				builder.addAnimation(loopingAni.animationKey, EDefaultLoopTypes.LOOP);
			}
		}else if(lastCast != null){
			if(lastCast.getStoppingAnimation() != null){
				AbilityAnimation stopAni = lastCast.getStoppingAnimation();
				neckLocked = stopAni.locksNeck;
				tailLocked = stopAni.locksTail;

				if(!ended){
					animationTimer.putAnimation(stopAni.animationKey, stopAni.duration, builder);
					ended = true;
				}

				builder.addAnimation(stopAni.animationKey);

				if(animationTimer.getDuration(stopAni.animationKey) <= 0){
					lastCast = null;
					ended = false;
				}
			}else{
				lastCast = null;
			}
		}
	}

	@Override
	public Iterable<ItemStack> getArmorSlots(){
		return playerId != null ? getPlayer().getArmorSlots() : List.of();
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slotIn){
		return playerId != null ? getPlayer().getItemBySlot(slotIn) : ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(EquipmentSlot slotIn, ItemStack stack){
		if(playerId != null){
			getPlayer().setItemSlot(slotIn, stack);
		}
	}

	@Override
	public HumanoidArm getMainArm(){
		return playerId != null ? getPlayer().getMainArm() : HumanoidArm.LEFT;
	}
}