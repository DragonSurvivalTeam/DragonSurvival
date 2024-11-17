package by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.active;

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
public class LavaVisionAbility extends ChargeCastAbility {
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVision", comment = "Whether the lava vision ability should be enabled" )
	public static Boolean lavaVision = true;

	@ConfigRange( min = 1.0, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVisionDuration", comment = "The duration in seconds of the lava vision effect given when the ability is used" )
	public static Double lavaVisionDuration = 100.0;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVisionCooldown", comment = "The cooldown in seconds of the lava vision ability" )
	public static Double lavaVisionCooldown = 30.0;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVisionCasttime", comment = "The cast time in seconds of the lava vision ability" )
	public static Double lavaVisionCasttime = 1.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "cave_dragon", "actives", "lava_vision"}, key = "lavaVisionManaCost", comment = "The mana cost for using the lava vision ability" )
	public static Integer lavaVisionManaCost = 1;

	@Override
	public int getSortOrder(){
		return 4;
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
		return lavaVisionManaCost;
	}

	@Override
	public Integer[] getRequiredLevels(){
		return new Integer[]{0, 25, 45, 60};
	}

	@Override
	public int getSkillCooldown(){
		return Functions.secondsToTicks(lavaVisionCooldown);
	}

	@Override
	public boolean requiresStationaryCasting(){ return false; }

	@Override
	public AbilityAnimation getLoopingAnimation(){
		return new AbilityAnimation("cast_self_buff", true, false);
	}

	@Override
	public AbilityAnimation getStoppingAnimation(){
		return new AbilityAnimation("self_buff", 0.52 * 20, true, false);
	}

	public int getDuration() {
		return Functions.secondsToTicks(lavaVisionDuration);
	}

	@Override
	public String getName(){
		return "lava_vision";
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.CAVE;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/lava_vision_0.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/lava_vision_1.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/lava_vision_2.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/lava_vision_3.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/cave/lava_vision_4.png")};
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(Component.translatable("ds.skill.duration.seconds", "+" + lavaVisionDuration));
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
		return super.isDisabled() || !lavaVision;
	}

	@Override
	public int getSkillCastingTime(){
		return Functions.secondsToTicks(lavaVisionCasttime);
	}

	@Override
	public void onCasting(Player player, int currentCastTime){}

	@Override
	public void castingComplete(Player player){
		player.addEffect(new MobEffectInstance(DragonEffects.LAVA_VISION, getDuration()));
		player.level().playLocalSound(player.position().x, player.position().y + 0.5, player.position().z, SoundEvents.UI_TOAST_IN, SoundSource.PLAYERS, 5F, 0.1F, false);
	}
}