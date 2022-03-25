package by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.models.MagicalPredatorModel;
import by.dragonsurvivalteam.dragonsurvival.common.entity.monsters.MagicalPredator;
import com.mojang.blaze3d.matrix.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MagicalPredatorRenderer extends MobRenderer<MagicalPredator, MagicalPredatorModel>{

	public static List<ResourceLocation> MAGICAL_BEAST_TEXTURES = new ArrayList<>(Arrays.asList(new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_dark.png"), new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_dark_broken.png"), new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_grass.png"), new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_gray.png"), new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_green.png"), new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_jungle.png"), new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_jungle_flowers.png"), new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_light.png"), new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_sand.png"), new ResourceLocation(DragonSurvivalMod.MODID, "textures/magical_beast/magical_predator_zombie.png")));

	public MagicalPredatorRenderer(EntityRendererManager p_i50961_1_){
		super(p_i50961_1_, new MagicalPredatorModel(RenderType::entityTranslucent), 0.66F);
	}

	@Override
	protected void scale(MagicalPredator entitylivingbaseIn, PoseStack matrixStackIn, float partialTickTime){
		this.shadowRadius = entitylivingbaseIn.size / entitylivingbaseIn.getBbHeight() / 1.44F;
		float scale = entitylivingbaseIn.size / entitylivingbaseIn.getBbHeight();
		matrixStackIn.scale(scale, scale, scale);
	}


	@Override
	public ResourceLocation getTextureLocation(MagicalPredator entity){
		return MAGICAL_BEAST_TEXTURES.get(entity.type);
	}
}