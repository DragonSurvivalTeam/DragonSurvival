package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.PrincesHorse;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PrincessHorseModel extends AnimatedGeoModel<PrincesHorse>{
	@Override
	public ResourceLocation getModelLocation(PrincesHorse object){
		return new ResourceLocation(DragonSurvivalMod.MODID, "geo/horseback_rider.geo.json");
	}

	@Override
	public ResourceLocation getTextureLocation(PrincesHorse object){
		DyeColor dyeColor = DyeColor.byId(object.getColor());
		switch(dyeColor){
			case RED:
				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/riders/princess_red.png");
			case BLUE:
				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/riders/princess_blue.png");
			case PURPLE:
				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/riders/princess_purple.png");
			case WHITE:
				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/riders/princess_white.png");
			case YELLOW:
				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/riders/princess_yellow.png");
			case BLACK:
				return new ResourceLocation(DragonSurvivalMod.MODID, "textures/riders/princess_black.png");
		}
		return null;
	}

	@Override
	public ResourceLocation getAnimationFileLocation(PrincesHorse animatable){
		return new ResourceLocation(DragonSurvivalMod.MODID, "animations/horseback_rider.animations.json");
	}
}