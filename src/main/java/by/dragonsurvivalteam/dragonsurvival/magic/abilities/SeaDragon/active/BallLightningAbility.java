package by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.Keybind;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.BallLightningEntity;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.RegisterDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ChargeCastAbility;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Locale;

@RegisterDragonAbility
public class BallLightningAbility extends ChargeCastAbility{
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightning", comment = "Whether the lightning ball ability should be enabled" )
	public static Boolean ballLightning = true;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightningCooldown", comment = "The cooldown in seconds of the ball lightning ability" )
	public static Double ballLightningCooldown = 20.0;

	@ConfigRange( min = 0.05, max = 10000.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightningCasttime", comment = "The cast time in seconds of the ball lightning ability" )
	public static Double ballLightningCasttime = 2.0;

	@ConfigRange( min = 0.0, max = 100.0 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightningDamage", comment = "The amount of damage the lightning ball ability deals. This value is multiplied by the skill level." )
	public static Double ballLightningDamage = 4.0;

	@ConfigRange( min = 0, max = 100 )
	@ConfigOption( side = ConfigSide.SERVER, category = {"magic", "abilities", "sea_dragon", "actives", "ball_lightning"}, key = "ballLightningManaCost", comment = "The mana cost for using the lightning ball ability" )
	public static Integer ballLightningManaCost = 1;

	@Override
	public int getManaCost(){
		return ballLightningManaCost;
	}

	@Override
	public int getSortOrder(){
		return 2;
	}

	@Override
	public Integer[] getRequiredLevels(){
		return new Integer[]{0, 20, 45, 50};
	}

	@Override
	public int getSkillCooldown(){
		return Functions.secondsToTicks(ballLightningCooldown);
	}

	@Override
	public int getSkillCastingTime(){
		return Functions.secondsToTicks(ballLightningCasttime);
	}

	@Override
	public boolean requiresStationaryCasting(){
		return false;
	}

	@Override
	public void onCasting(Player player, int currentCastTime){

	}

	@Override
	public void castingComplete(Player player){
		Vec3 vector3d = player.getViewVector(1.0F);

		double speed = 1d;

		double d2 = vector3d.x * speed;
		double d3 = vector3d.y * speed;
		double d4 = vector3d.z * speed;

		DragonStateHandler handler = DragonUtils.getHandler(player);

		float f1 = -(float)handler.getMovementData().bodyYaw * ((float)Math.PI / 180F);

		float f4 = Mth.sin(f1);
		float f5 = Mth.cos(f1);

		Double size = DragonStateProvider.getCap(player).map(DragonStateHandler::getSize).get();

		double x = player.getX() + f4;
		double y = player.getY() + size / 20F - 0.2;
		double z = player.getZ() + f5;

		BallLightningEntity entity = new BallLightningEntity(player.level(), player, d2, d3, d4);
		entity.setPos(x + vector3d.x * speed, y, z + vector3d.z * speed);
		entity.setLevel(getLevel());
		entity.shootFromRotation(player, player.xRot, player.yRot, 0.0F, (float)speed, 1.0F);
		player.level().addFreshEntity(entity);
	}

	@Override
	public ArrayList<Component> getInfo(){
		ArrayList<Component> components = super.getInfo();
		components.add(Component.translatable("ds.skill.aoe", getRange() + "x" + getRange() + "x" + getRange()));
		components.add(Component.translatable("ds.skill.damage", getDamage()));

		if(!Keybind.ABILITY2.isUnbound()){
			String key = Keybind.ABILITY2.getKey().getDisplayName().getString().toUpperCase(Locale.ROOT);

			if(key.isEmpty()){
				key = Keybind.ABILITY2.getKey().getDisplayName().getString();
			}
			components.add(Component.translatable("ds.skill.keybind", key));
		}

		return components;
	}

	public int getRange(){
		return 4;
	}

	public float getDamage(){
		return getDamage(getLevel());
	}

	public static float getDamage(int level){
		return (float)(ballLightningDamage * level);
	}

	@Override

	public Component getDescription(){
		return Component.translatable("ds.skill.description." + getName(), getDamage());
	}

	@Override
	public String getName(){
		return "ball_lightning";
	}

	@Override
	public AbstractDragonType getDragonType(){
		return DragonTypes.SEA;
	}

	@Override
	public ResourceLocation[] getSkillTextures(){
		return new ResourceLocation[]{new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/ball_lightning_0.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/ball_lightning_1.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/ball_lightning_2.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/ball_lightning_3.png"),
		                              new ResourceLocation(DragonSurvivalMod.MODID, "textures/skills/sea/ball_lightning_4.png"),};
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public ArrayList<Component> getLevelUpInfo(){
		ArrayList<Component> list = super.getLevelUpInfo();
		list.add(Component.translatable("ds.skill.damage", "+" + ballLightningDamage));
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
		return super.isDisabled() || !ballLightning;
	}
}