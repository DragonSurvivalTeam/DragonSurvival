package by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.active;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.Keybind;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.AbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ChargeCastAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Locale;

@RegisterDragonAbility
public class HunterAbility extends ChargeCastAbility{
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunter", comment = "Whether the hunter ability should be enabled" )
	public static Boolean hunter = true;

	@ConfigRange( min = 1.0, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterDuration", comment = "The duration in seconds of the hunter effect given when the ability is used" )
	public static Double hunterDuration = 30.0;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterCooldown", comment = "The cooldown in seconds of the hunter ability" )
	public static Double hunterCooldown = 30.0;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterCasttime", comment = "The cast time in seconds of the hunter ability" )
	public static Double hunterCasttime = 3.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterDamageBonus", comment = "The bonus damage multiplier the hunter effect gives when invisible. This value is multiplied by the skill level." )
	public static Double hunterDamageBonus = 1.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "forest_dragon", "actives", "hunter"}, key = "hunterManaCost", comment = "The mana cost for using the hunter ability" )
	public static Integer hunterManaCost = 1;

	@Override
	public int getSortOrder(){
		return 4;
	}

	@Override
	public int getSkillCastingTime(){
		return Functions.secondsToTicks(hunterCasttime);
	}

	@Override
	public void onCasting(Player player, int currentCastTime){}

	@Override
	public void castingComplete(Player player){
		player.addEffect(new MobEffectInstance(DragonEffects.HUNTER, getDuration(), getLevel() - 1));
		player.level.playLocalSound(player.position().x, player.position().y + 0.5, player.position().z, SoundEvents.UI_TOAST_IN, SoundSource.PLAYERS, 5F, 0.1F, true);
	}

	@Override
	public ArrayList<Component> getInfo(){
		ArrayList<Component> components = super.getInfo();

		if(!Keybind.ABILITY4.isUnbound()){
			String key = Keybind.ABILITY4.getKey().getDisplayName().getString().toUpperCase(Locale.ROOT);

			if(key.isEmpty()){
				key = Keybind.ABILITY4.getKey().getDisplayName().getString();
			}
			components.add(Component.translatable("ds.skill.keybind", key));
		}

		return components;
	}


	@Override
	public int getManaCost(){
		return hunterManaCost;
	}

	@Override
	public Integer[] getRequiredLevels(){
		return new Integer[]{0, 25, 35, 55};
	}

	@Override
	public int getSkillCooldown(){
		return Functions.secondsToTicks(hunterCooldown);
	}

	@Override
	public boolean requiresStationaryCasting(){return false;}

	@Override
	public AbilityAnimation getLoopingAnimation(){
		return new AbilityAnimation("cast_self_buff", true, false);
	}

	@Override
	public AbilityAnimation getStoppingAnimation(){
		return new AbilityAnimation("self_buff", 0.52 * 20, true, false);
	}

	public int getDuration(){
		return Functions.secondsToTicks(hunterDuration * getLevel());
	}

	public double getDamage(){
		return hunterDamageBonus * getLevel();
	}

	@Override
	public Component getDescription(){
		return Component.translatable("ds.skill.description." + getName(), "+" + hunterDamageBonus * getLevel() + "x", getDuration());
	}

	@Override
	public String getName(){
		return "hunter";
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.FOREST;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/hunter_0.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/hunter_1.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/hunter_2.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/hunter_3.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/forest/hunter_4.png")};
	}


	@Override
	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(Component.translatable("ds.skill.duration.seconds", "+" + hunterDuration));
		list.add(Component.translatable("ds.skill.damage", "+" + hunterDamageBonus + "X"));
		return list;
	}

	@Override
	public int getMaxLevel(){
		return 4;
	}

	@Override
	public int getMinLevel(){
		return 0;
	}

	@Override
	public boolean isDisabled(){
		return super.isDisabled() || !hunter;
	}
}