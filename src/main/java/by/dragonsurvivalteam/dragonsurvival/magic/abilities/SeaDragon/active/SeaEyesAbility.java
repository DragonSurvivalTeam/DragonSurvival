package by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active;

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
public class SeaEyesAbility extends ChargeCastAbility{
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaVision", comment = "Whether the sea vision ability should be enabled" )
	public static Boolean seaEyes = true;

	@ConfigRange( min = 1.0, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaVisionDuration", comment = "The duration in seconds of the sea vision effect given when the ability is used" )
	public static Double seaEyesDuration = 100.0;

	@ConfigRange( min = 0.05, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaVisionCooldown", comment = "The cooldown in seconds of the sea vision ability" )
	public static Double seaEyesCooldown = 60.0;

	@ConfigRange( min = 0.05, max = 10000 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaEyesCasttime", comment = "The cast time in seconds of the sea vision ability" )
	public static Double seaEyesCasttime = 1.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "sea_vision"}, key = "seaVisionManaCost", comment = "The mana cost for using the sea vision ability" )
	public static Integer seaEyesManaCost = 1;

	@Override
	public int getSkillCastingTime(){
		return Functions.secondsToTicks(seaEyesCasttime);
	}

	@Override
	public int getSortOrder(){
		return 4;
	}

	@Override
	public void onCasting(Player player, int currentCastTime){

	}

	@Override
	public void castingComplete(Player player){
		player.addEffect(new MobEffectInstance(DragonEffects.WATER_VISION, getDuration()));
		player.level().playLocalSound(player.position().x, player.position().y + 0.5, player.position().z, SoundEvents.UI_TOAST_IN, SoundSource.PLAYERS, 5F, 0.1F, true);
	}

	@Override
	public ArrayList<Component> getInfo(){
		ArrayList<Component> components = super.getInfo();
		components.add(Component.translatable("ds.skill.duration.seconds", Functions.ticksToSeconds(getDuration())));

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
		return seaEyesManaCost;
	}

	@Override
	public Integer[] getRequiredLevels(){
		return new Integer[]{0, 15, 45, 60};
	}

	@Override
	public int getSkillCooldown(){
		return Functions.secondsToTicks(seaEyesCooldown);
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
		return Functions.secondsToTicks(seaEyesDuration * getLevel());
	}

	@Override
	public Component getDescription(){
		return Component.translatable("ds.skill.description." + getName(), Functions.ticksToSeconds(getDuration()));
	}

	@Override
	public String getName(){
		return "sea_eyes";
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.SEA;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/sea_eyes_0.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/sea_eyes_1.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/sea_eyes_2.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/sea_eyes_3.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/sea_eyes_4.png")};
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(Component.translatable("ds.skill.duration.seconds", "+" + seaEyesDuration));
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
		return super.isDisabled() || !seaEyes;
	}
}