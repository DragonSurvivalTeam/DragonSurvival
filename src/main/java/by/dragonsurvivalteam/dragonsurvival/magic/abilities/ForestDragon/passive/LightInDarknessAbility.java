package by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.passive;


import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.passive.PassiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@RegisterDragonAbility
public class LightInDarknessAbility extends PassiveDragonAbility{
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "passives"}, key = "lightInDarkness", comment = "Whether the light in darkness ability should be enabled" )
	public static Boolean lightInDarkness = true;

	@Override
	public Component getDescription(){
		return Component.translatable("ds.skill.description." + getName(), getDuration() + Functions.ticksToSeconds(ServerConfig.forestStressTicks));
	}

	@Override
	public int getSortOrder(){
		return 3;
	}

	@Override
	public String getName(){
		return "light_in_darkness";
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.FOREST;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/light_in_darkness_0.png"),
		                              ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/light_in_darkness_1.png"),
		                              ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/light_in_darkness_2.png"),
		                              ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/light_in_darkness_3.png"),
		                              ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/light_in_darkness_4.png"),
		                              ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/light_in_darkness_5.png"),
		                              ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/light_in_darkness_6.png"),
		                              ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/light_in_darkness_7.png"),
		                              ResourceLocation.fromNamespaceAndPath(MODID, "textures/skills/forest/light_in_darkness_8.png")};
	}

	public int getDuration(){
		return 10 * getLevel();
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(Component.translatable("ds.skill.duration.seconds", "+10"));
		return list;
	}

	@Override
	public int getMaxLevel(){
		return 8;
	}

	@Override
	public int getMinLevel(){
		return 0;
	}

	@Override
	public boolean isDisabled(){
		return super.isDisabled() || !lightInDarkness;
	}
}