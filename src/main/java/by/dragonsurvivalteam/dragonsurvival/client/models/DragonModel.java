package by.dragonsurvivalteam.dragonsurvival.client.models;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset.SkinAgeGroup;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.objects.DragonMovementData;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorMolangParser;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.molang.MolangParser;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

public class DragonModel extends AnimatedGeoModel<DragonEntity> {
	private final ResourceLocation defaultTexture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/dragon/cave_newborn.png");

	private ResourceLocation currentTexture = defaultTexture;

	@Override
	public ResourceLocation getModelResource(final DragonEntity ignored) {
		return new ResourceLocation(DragonSurvivalMod.MODID, "geo/dragon_model.geo.json");
	}

	@Override
	public ResourceLocation getTextureResource(final DragonEntity dragon) {
		if (dragon.playerId != null || dragon.getPlayer() != null) {
			DragonStateHandler handler = DragonUtils.getHandler(dragon.getPlayer());
			SkinAgeGroup ageGroup = handler.getSkinData().skinPreset.skinAges.get(handler.getLevel()).get();

			if (handler.getSkinData().recompileSkin) {
				DragonEditorHandler.generateSkinTextures(dragon);
			}

			if (handler.getSkinData().blankSkin) {
				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/dragon/blank_skin_" + handler.getTypeName().toLowerCase(Locale.ROOT) + ".png");
			}

			if (ageGroup.defaultSkin) {
				if (currentTexture != null) {
					return currentTexture;
				}

				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/dragon/" + handler.getTypeName().toLowerCase(Locale.ROOT) + "_" + handler.getLevel().name.toLowerCase(Locale.ROOT) + ".png");
			}

			if (handler.getSkinData().isCompiled && currentTexture == null) {
				return new ResourceLocation(DragonSurvivalMod.MODID, "dynamic_normal_" + dragon.getPlayer().getStringUUID() + "_" + handler.getLevel().name);
			}
		}

		if (currentTexture == null && dragon.getPlayer() instanceof FakeClientPlayer) {
			LocalPlayer localPlayer = Minecraft.getInstance().player;

			if (localPlayer != null) { // TODO :: Check if skin is compiled?
				return new ResourceLocation(DragonSurvivalMod.MODID, "dynamic_normal_" + localPlayer.getStringUUID() + "_" + DragonUtils.getHandler(dragon.getPlayer()).getLevel().name);
			}
		}

		return currentTexture == null ? defaultTexture : currentTexture;
	}

	public void setCurrentTexture(final ResourceLocation currentTexture) {
		this.currentTexture = currentTexture;
	}

	@Override
	public ResourceLocation getAnimationResource(final DragonEntity dragon) {
		if (dragon.playerId != null || dragon.getPlayer() != null) {
			DragonStateHandler handler = DragonUtils.getHandler(dragon.getPlayer());
			AbstractDragonBody body = handler.getBody();
			if (body != null) {
				return new ResourceLocation(DragonSurvivalMod.MODID, String.format("animations/dragon_%s.json", body.getBodyName().toLowerCase()));
			}
		}
		return new ResourceLocation(DragonSurvivalMod.MODID, "animations/dragon.animations.json");
	}

	/** Factor to multiply the delta yaw and pitch by, needed for scaling for the animations */
	private static final double DELTA_YAW_PITCH_FACTOR = 0.2;

	/** Factor to multiply the delta movement by, needed for scaling for the animations */
	private static final double DELTA_MOVEMENT_FACTOR = 10;

	/**TODO Body Types Update
	Required:
	 - tips for body types like for magic abilities

	 Extras:
     - customization.json - Ability to disallow some details in the editor for some Body Types (for example, wing details are not required for wingless).
	 - emotes.json - Ability to disallow some emotions for certain Body Types.
	 - Change rider sit position height for each Body Types (or bind the rider more dynamically with code and complex rendering).
	 - Change the height of the Breath Source for each Body Types (or make this dependent on the BreathSource position on the model)
	 - Lower the breathing/eyes height for the northern type
	*/

	/**
	 * Copied code from Geckolib pre version 3.0.47 which broke dragon rendering
	 * @link <a href="https://github.com/bernie-g/geckolib/blob/4e864bd2d4a0a8dceea01f600b7031cb2fba3a3b/Forge/src/main/java/software/bernie/geckolib3/model/AnimatedGeoModel.java#L51">Github link</a>
	 */
	@Override
	public void setCustomAnimations(DragonEntity dragon, int uniqueID, final AnimationEvent customPredicate) {
		AnimationData manager = dragon.getFactory().getOrCreateAnimationData(uniqueID);

		if (manager.startTick == -1) {
			manager.startTick = dragon.tickCount + Minecraft.getInstance().getFrameTime();
		}

		if (!Minecraft.getInstance().isPaused() || manager.shouldPlayWhilePaused) {
			manager.tick = getCurrentTick() - manager.startTick;
			double gameTick = manager.tick;
			double deltaTicks = gameTick - lastGameTickTime;
			seekTime += deltaTicks;
			dragon.seekTime = seekTime; // Needed for dynamic speed adjustments to work correctly
			lastGameTickTime = gameTick;
		}

		AnimationEvent<DragonEntity> predicate = Objects.requireNonNullElseGet(customPredicate, () -> new AnimationEvent<>(dragon, 0, 0, (float) (manager.tick - lastGameTickTime), false, Collections.emptyList()));
		predicate.animationTick = seekTime;

		getAnimationProcessor().preAnimationSetup(predicate.getAnimatable(), seekTime);

		if (!getAnimationProcessor().getModelRendererList().isEmpty()) {
			getAnimationProcessor().tickAnimation(dragon, uniqueID, seekTime, predicate, GeckoLibCache.getInstance().parser, shouldCrashOnMissing);
		}
	}

	@Override
	public void setMolangQueries(final IAnimatable animatable, double currentTick) {
		super.setMolangQueries(animatable, currentTick);

		// In case the Integer (id of the player) is null
		if (!(animatable instanceof DragonEntity dragon) || dragon.playerId == null || dragon.getPlayer() == null) {
			return;
		}

		Player player = dragon.getPlayer();
		float deltaTick = AnimationUtils.getRealtimeDeltaTicks();
		float partialTick = Minecraft.getInstance().getPartialTick();
		DragonStateHandler handler = DragonUtils.getHandler(player);
		DragonMovementData md = handler.getMovementData();
		MolangParser parser = GeckoLibCache.getInstance().parser;

		parser.setValue("query.head_yaw", () -> md.headYaw);
		parser.setValue("query.head_pitch", () -> md.headPitch);

		double gravity = player.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
		parser.setValue("query.gravity", () -> gravity);


		double bodyYawAvg;
		double headYawAvg;
		double headPitchAvg;
		double verticalVelocityAvg;
		double bodyYawChange = Functions.angleDifference(md.bodyYaw, md.bodyYawLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;
		double headYawChange = Functions.angleDifference(md.headYaw, md.headYawLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;
		double headPitchChange = Functions.angleDifference(md.headPitch, md.headPitchLastFrame) / deltaTick * DELTA_YAW_PITCH_FACTOR;

		double verticalVelocity = Mth.lerp(partialTick, md.deltaMovementLastFrame.y, md.deltaMovement.y) * DELTA_MOVEMENT_FACTOR;
		// Factor in the vertical angle of the dragon so that the vertical velocity is scaled down when the dragon is looking up or down
		// Ideally, we would just use more precise data (factor in the full rotation of the player in our animations)
		// but this works pretty well in most situations the player will encounter
		verticalVelocity *= 1 - Mth.abs(Mth.clampedMap(md.prevXRot, -90, 90, -1, 1));

		float deltaTickFor60FPS = AnimationUtils.getDeltaTickFor60FPS();
		// Accumulate them in the history
		while(dragon.bodyYawHistory.size() > 10 / deltaTickFor60FPS ) {
			dragon.bodyYawHistory.remove(0);
		}
		dragon.bodyYawHistory.add(bodyYawChange);

		while(dragon.headYawHistory.size() > 10 / deltaTickFor60FPS ) {
			dragon.headYawHistory.remove(0);
		}
		dragon.headYawHistory.add(headYawChange);

		while(dragon.headPitchHistory.size() > 10 / deltaTickFor60FPS ) {
			dragon.headPitchHistory.remove(0);
		}
		dragon.headPitchHistory.add(headPitchChange);

		// Handle the clear case (see DragonEntity.java)
		if(dragon.clearVerticalVelocity) {
			dragon.verticalVelocityHistory.clear();
			while(dragon.verticalVelocityHistory.size() < 10 / deltaTickFor60FPS) {
				dragon.verticalVelocityHistory.add(0.);
			}
		}

		while(dragon.verticalVelocityHistory.size() > 10 / deltaTickFor60FPS ) {
			dragon.verticalVelocityHistory.remove(0);
		}
		dragon.verticalVelocityHistory.add(verticalVelocity);

		bodyYawAvg = dragon.bodyYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
		headYawAvg = dragon.headYawHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
		headPitchAvg = dragon.headPitchHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);
		verticalVelocityAvg = dragon.verticalVelocityHistory.stream().mapToDouble(Double::doubleValue).average().orElse(0);

		double lerpRate = Math.min(1., deltaTick);
		dragon.currentBodyYawChange = Mth.lerp(lerpRate, dragon.currentBodyYawChange, bodyYawAvg);
		dragon.currentHeadYawChange = Mth.lerp(lerpRate, dragon.currentHeadYawChange, headYawAvg);
		dragon.currentHeadPitchChange = Mth.lerp(lerpRate, dragon.currentHeadPitchChange, headPitchAvg);

		// Handle the clear case (see DragonEntity.java)
		if(dragon.clearVerticalVelocity) {
			dragon.currentTailMotionUp = 0;
			dragon.clearVerticalVelocity = false;
		} else {
			dragon.currentTailMotionUp = Mth.lerp(lerpRate, dragon.currentTailMotionUp, verticalVelocityAvg);
		}

		parser.setValue("query.body_yaw_change", () -> Mth.lerp(lerpRate, dragon.currentBodyYawChange, bodyYawAvg));
		parser.setValue("query.head_yaw_change", () -> Mth.lerp(lerpRate, dragon.currentHeadPitchChange, headYawAvg));
		parser.setValue("query.head_pitch_change", () -> Mth.lerp(lerpRate, dragon.currentHeadYawChange, headPitchAvg));
		parser.setValue("query.tail_motion_up", () -> Mth.lerp(lerpRate, dragon.currentTailMotionUp, -verticalVelocityAvg));
	}
}