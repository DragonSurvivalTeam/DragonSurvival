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
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.model.GeoModel;

import java.util.Locale;

public class DragonModel extends GeoModel<DragonEntity> {
	private final ResourceLocation defaultTexture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/dragon/cave_newborn.png");
	private final ResourceLocation model = new ResourceLocation(DragonSurvivalMod.MODID, "geo/dragon_model.geo.json");
	private ResourceLocation currentTexture;

	/** Factor to multiply the delta yaw and pitch by, needed for scaling for the animations */
	private static final double DELTA_YAW_PITCH_FACTOR = 0.2;

	/** Factor to multiply the delta movement by, needed for scaling for the animations */
	private static final double DELTA_MOVEMENT_FACTOR = 10;

	/**TODO Body Types Update
	Required:
	 - tips for body types like for magic abilities

	 Extras:
		 - emotes.json - Ability to disallow some emotions for certain Body Types.
	*/

	@Override
	public void applyMolangQueries(final DragonEntity dragon, double currentTick) {
		super.applyMolangQueries(dragon, currentTick);
		Player player = dragon.getPlayer();

		if (player == null) {
			return;
		}

		float deltaTick = AnimationUtils.getRealtimeDeltaTicks();
		float partialTick = Minecraft.getInstance().getPartialTick();
		DragonStateHandler handler = DragonUtils.getHandler(player);
		DragonMovementData md = handler.getMovementData();
		MolangParser parser = MolangParser.INSTANCE;

		parser.setValue("query.head_yaw", () -> md.headYaw);
		parser.setValue("query.head_pitch", () -> md.headPitch);

		double gravity = player.getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
		parser.setValue("query.gravity", () -> gravity);


		double bodyYawAvg;
		double headYawAvg;
		double headPitchAvg;
		double verticalVelocityAvg;
		if (!ClientDragonRender.isOverridingMovementData) {
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
			while (dragon.bodyYawHistory.size() > 10 / deltaTickFor60FPS) {
				dragon.bodyYawHistory.remove(0);
			}
			dragon.bodyYawHistory.add(bodyYawChange);

			while (dragon.headYawHistory.size() > 10 / deltaTickFor60FPS) {
				dragon.headYawHistory.remove(0);
			}
			dragon.headYawHistory.add(headYawChange);

			while (dragon.headPitchHistory.size() > 10 / deltaTickFor60FPS) {
				dragon.headPitchHistory.remove(0);
			}
			dragon.headPitchHistory.add(headPitchChange);

			// Handle the clear case (see DragonEntity.java)
			if (dragon.clearVerticalVelocity) {
				dragon.verticalVelocityHistory.clear();
				while (dragon.verticalVelocityHistory.size() < 10 / deltaTickFor60FPS) {
					dragon.verticalVelocityHistory.add(0.);
				}
			}

			while (dragon.verticalVelocityHistory.size() > 10 / deltaTickFor60FPS) {
				dragon.verticalVelocityHistory.remove(0);
			}
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

		double lerpRate = Math.min(1., deltaTick);
		dragon.currentBodyYawChange = Mth.lerp(lerpRate, dragon.currentBodyYawChange, bodyYawAvg);
		dragon.currentHeadYawChange = Mth.lerp(lerpRate, dragon.currentHeadYawChange, headYawAvg);
		dragon.currentHeadPitchChange = Mth.lerp(lerpRate, dragon.currentHeadPitchChange, headPitchAvg);
		if(dragon.clearVerticalVelocity) {
			dragon.currentTailMotionUp = 0;
			dragon.clearVerticalVelocity = false;
		} else {
			dragon.currentTailMotionUp = Mth.lerp(lerpRate, dragon.currentTailMotionUp, -verticalVelocityAvg);
		}

        double finalBodyYawAvg = bodyYawAvg;
        double finalHeadYawAvg = headYawAvg;
        double finalHeadPitchAvg = headPitchAvg;
        double finalVerticalVelocityAvg = verticalVelocityAvg;
        parser.setValue("query.body_yaw_change", () -> Mth.lerp(lerpRate, dragon.currentBodyYawChange, finalBodyYawAvg));
        parser.setValue("query.head_yaw_change", () -> Mth.lerp(lerpRate, dragon.currentHeadPitchChange, finalHeadYawAvg));
        parser.setValue("query.head_pitch_change", () -> Mth.lerp(lerpRate, dragon.currentHeadYawChange, finalHeadPitchAvg));
        parser.setValue("query.tail_motion_up", () -> Mth.lerp(lerpRate, dragon.currentTailMotionUp, -finalVerticalVelocityAvg));
	}
	
	@Override
	public ResourceLocation getModelResource(final DragonEntity dragon) {
		return model;
	}

	public ResourceLocation getTextureResource(final DragonEntity dragon) {
		if (dragon.playerId != null || dragon.getPlayer() != null) {
			DragonStateHandler handler = DragonUtils.getHandler(dragon.getPlayer());
			SkinAgeGroup ageGroup = handler.getSkinData().skinPreset.skinAges.get(handler.getLevel()).get();

			if (handler.getSkinData().recompileSkin) {
				DragonEditorHandler.generateSkinTextures(dragon);
			}

			if (handler.getSkinData().blankSkin) {
				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/dragon/blank_skin_" + handler.getTypeNameLowerCase() + ".png");
			}

			if (ageGroup.defaultSkin) {
				if (currentTexture != null) {
					return currentTexture;
				}

				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/dragon/" + handler.getTypeNameLowerCase() + "_" + handler.getLevel().name.toLowerCase(Locale.ENGLISH) + ".png");
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
				return new ResourceLocation(DragonSurvivalMod.MODID, String.format("animations/dragon_%s.json", body.getBodyName().toLowerCase(Locale.ENGLISH)));
			}
		}
		return new ResourceLocation(DragonSurvivalMod.MODID, "animations/dragon.animations.json");
	}

	@Override
	public RenderType getRenderType(final DragonEntity animatable, final ResourceLocation texture) {
		return RenderType.entityCutout(texture);
	}
}