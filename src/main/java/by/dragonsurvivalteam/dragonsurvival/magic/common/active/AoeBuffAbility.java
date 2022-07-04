package by.dragonsurvivalteam.dragonsurvival.magic.common.active;

import by.dragonsurvivalteam.dragonsurvival.magic.common.AbilityAnimation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;


public abstract class AoeBuffAbility extends ChargeCastAbility{
	@Override
	public void onCasting(Player player, int currentCastTime){

	}

	@Override
	public void castingComplete(Player player){
		float f5 = (float)Math.PI * getRange() * getRange();

		for(int i = 0; i < 20; i++)
			for(int k1 = 0; (float)k1 < f5; ++k1){
				float f6 = player.level.random.nextFloat() * ((float)Math.PI * 2F);

				float f7 = Mth.sqrt(player.level.random.nextFloat()) * getRange();
				float f8 = Mth.cos(f6) * f7;
				float f9 = Mth.sin(f6) * f7;
				player.level.addAlwaysVisibleParticle(getParticleEffect(), player.getX() + (double)f8, player.getY(), player.getZ() + (double)f9, (0.5D - player.level.random.nextDouble()) * 0.15D, 0.01F, (0.5D - player.level.random.nextDouble()) * 0.15D);
			}

		List<LivingEntity> list1 = player.level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(getRange()));
		if(!list1.isEmpty())
			for(LivingEntity livingentity : list1){
				if(livingentity.isAffectedByPotions()){
					double d0 = livingentity.getX() - player.getX();
					double d1 = livingentity.getZ() - player.getZ();
					double d2 = d0 * d0 + d1 * d1;

					if(d2 <= (double)(getRange() * getRange())){
						livingentity.addEffect(getEffect());
					}
				}
			}
		player.level.playLocalSound(player.position().x, player.position().y + 0.5, player.position().z, SoundEvents.UI_TOAST_OUT, SoundSource.PLAYERS, 5F, 0.1F, false);
	}

//	@Override
//	public ArrayList<Component> getInfo(){
//		ArrayList<Component> components = super.getInfo();
//		components.add(new TranslatableComponent("ds.skill.duration.seconds", getDuration()));
//		components.add(new TranslatableComponent("ds.skill.aoe", getRange() + "x" + getRange()));
//
//
//		//		if(effect.getEffect() == DragonEffects.REVEALING_THE_SOUL){
//		//			components.add(new TranslatableComponent("ds.skill.bonus_exp.multiplier", ServerConfig.revealingTheSoulMultiplier + "x"));
//		//			components.add(new TranslatableComponent("ds.skill.bonus_exp.max_gain", Integer.toString(ServerConfig.revealingTheSoulMaxEXP)));
//		//		}
//
//		if(!KeyInputHandler.ABILITY3.isUnbound()){
//			String key = KeyInputHandler.ABILITY3.getKey().getDisplayName().getContents().toUpperCase(Locale.ROOT);
//
//			if(key.isEmpty())
//				key = KeyInputHandler.ABILITY3.getKey().getDisplayName().getString();
//			components.add(new TranslatableComponent("ds.skill.keybind", key));
//		}
//
//		return components;
//	}
	public abstract int getRange();
	public abstract ParticleOptions getParticleEffect();
	public abstract MobEffectInstance getEffect();


	//	{
	//
	//		if(effect.getEffect() == MobEffects.DIG_SPEED){
	//			return getLevel() * ServerConfig.inspirationDuration;
	//		}else if(effect.getEffect() == DragonEffects.REVEALING_THE_SOUL){
	//			return getLevel() * ServerConfig.revealingTheSoulDuration;
	//		}else if(effect.getEffect() == DragonEffects.STRONG_LEATHER){
	//			return getLevel() * ServerConfig.toughSkinDuration;
	//		}
	//		return getLevel() * (effect.getDuration() > 0 ? effect.getDuration() : 30);
	//	}

	@Override
	public AbilityAnimation getLoopingAnimation(){
		return new AbilityAnimation("cast_mass_buff", true, true);
	}

	@Override
	public AbilityAnimation getStoppingAnimation(){
		return new AbilityAnimation("mass_buff", 0.56 * 20, true, true);
	}
}