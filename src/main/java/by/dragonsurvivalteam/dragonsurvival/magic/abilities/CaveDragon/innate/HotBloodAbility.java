package by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.innate;


import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.innate.InnateDragonAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@RegisterDragonAbility
public class HotBloodAbility extends InnateDragonAbility{
	@Override
	public Component getDescription(){
		return Component.translatable("ds.skill.description." + getName(), ServerConfig.caveWaterDamage, 0.5);
	}

	@Override
	public int getMaxLevel(){
		return 1;
	}

	@Override
	public int getMinLevel(){
		return 1;
	}

	@Override
	public String getName(){
		return "hot_blood";
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.CAVE;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/cave/hot_blood_0.png"),
		                              ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/cave/hot_blood_1.png")};
	}

	@Override
	public int getLevel(){
		return ServerConfig.penaltiesEnabled && ServerConfig.caveWaterDamage != 0.0 ? 1 : 0;
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public boolean isDisabled(){
		return super.isDisabled() || !ServerConfig.penaltiesEnabled || ServerConfig.caveWaterDamage == 0.0;
	}

	@Override
	public int getSortOrder(){
		return 4;
	}
}