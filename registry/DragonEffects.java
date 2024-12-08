package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.active.ToughSkinAbility;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.MOD )
public class DragonEffects{

	public static MobEffect STRESS;
	public static MobEffect TRAPPED;
	public static MobEffect ROYAL_CHASE;
	public static MobEffect PEACE, MAGIC, FIRE;
	public static MobEffect ANIMAL_PEACE;

	public static MobEffect SOURCE_OF_MAGIC;

	public static MobEffect ROYAL_DEPARTURE;

	//Magic system effects
	public static MobEffect WATER_VISION, LAVA_VISION;
	public static MobEffect HUNTER;
	public static MobEffect REVEALING_THE_SOUL;
	public static MobEffect BURN, CHARGED, DRAIN;
	public static MobEffect STRONG_LEATHER;

	public static MobEffect cave_wings, sea_wings, forest_wings;

	@SuppressWarnings( "unused" )
	@SubscribeEvent
	public static void registerEffects(RegisterEvent event){
		if (!event.getRegistryKey().equals(Registry.MOB_EFFECT_REGISTRY))
			return;
		STRESS = registerMobEffect(event, "stress", new Stress(0xf4a2e8));
		TRAPPED = registerMobEffect(event, "trapped", new Trapped(MobEffectCategory.HARMFUL, 0xdddddd, true));
		ROYAL_CHASE = registerMobEffect(event, "royal_chase", new RoyalChase(MobEffectCategory.NEUTRAL));

		PEACE = registerMobEffect(event, "peace", new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false));
		MAGIC = registerMobEffect(event, "magic", new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false));
		FIRE = registerMobEffect(event, "fire", new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false));
		ANIMAL_PEACE = registerMobEffect(event, "animal_peace", new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false));
		SOURCE_OF_MAGIC = registerMobEffect(event, "source_of_magic",new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false));

		ROYAL_DEPARTURE = registerMobEffect(event, "royal_departure", new TradeEffect(MobEffectCategory.HARMFUL, -3407617, true));

		//Magic system effects
		WATER_VISION = registerMobEffect(event, "water_vision", new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false));

		LAVA_VISION = registerMobEffect(event, "lava_vision",new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false));

		HUNTER = registerMobEffect(event,"hunter", new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false));

		REVEALING_THE_SOUL = registerMobEffect(event, "revealing_the_soul", new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false));

		STRONG_LEATHER = registerMobEffect(event, "strong_leather", new Effect2(MobEffectCategory.BENEFICIAL, 0x0, false)
			.addAttributeModifier(Attributes.ARMOR, "1640719a-4c40-11ec-81d3-0242ac130003", ToughSkinAbility.toughSkinArmorValue, Operation.ADDITION));

		BURN = registerMobEffect(event, "burn", new Effect2(MobEffectCategory.HARMFUL, 0x0, false));

		CHARGED = registerMobEffect(event, "charged", new Effect2(MobEffectCategory.HARMFUL, 0x0, false));

		DRAIN = registerMobEffect(event, "drain", new Effect2(MobEffectCategory.HARMFUL, 0x0, false));

		forest_wings = registerMobEffect(event, "wings_forest", new Effect2(MobEffectCategory.BENEFICIAL, 0x0, true));

		sea_wings =  registerMobEffect(event,"wings_sea",new Effect2(MobEffectCategory.BENEFICIAL, 0x0, true));

		cave_wings = registerMobEffect(event,"wings_cave",new Effect2(MobEffectCategory.BENEFICIAL, 0x0, true));
	}
	protected static MobEffect registerMobEffect(RegisterEvent event, String identity, MobEffect mobEffect)
	{
		event.register(Registry.MOB_EFFECT_REGISTRY, new ResourceLocation(DragonSurvivalMod.MODID, identity), ()->mobEffect);
		return mobEffect;
	}

	private static class Effect2 extends MobEffect{
		private final boolean uncurable;

		protected Effect2(MobEffectCategory type, int color, boolean uncurable){
			super(type, color);
			this.uncurable = uncurable;
		}

		@Override
		public List<ItemStack> getCurativeItems(){
			return uncurable ? Collections.emptyList() : super.getCurativeItems();
		}
	}

	private static class Stress extends MobEffect{

		protected Stress(int color){
			super(MobEffectCategory.HARMFUL, color);
		}

		@Override
		public void applyEffectTick(LivingEntity living, int p_76394_2_){
			if(living instanceof Player player){
				FoodData food = player.getFoodData();

				if(food.getSaturationLevel() > 0){
					int oldFood = food.getFoodLevel();
					food.eat(1, (float)((-0.5F * food.getSaturationLevel()) * ServerConfig.stressExhaustion));
					if(oldFood != 20){
						food.setFoodLevel((int)(food.getFoodLevel() - 1 * ServerConfig.stressExhaustion));
					}
				}

				player.causeFoodExhaustion((float)(1.0f * ServerConfig.stressExhaustion));
			}
		}

		@Override
		public boolean isDurationEffectTick(int p_76397_1_, int p_76397_2_){
			int i = 20 >> p_76397_2_;
			if(i > 0){
				return p_76397_1_ % i == 0;
			}else{
				return true;
			}
		}
	}

	private static class Trapped extends MobEffect{
		protected Trapped(MobEffectCategory effectType, int color, boolean uncurable){
			super(effectType, color);
		}

		@Override
		public List<ItemStack> getCurativeItems(){
			return Collections.emptyList();
		}

		@Override
		public void applyEffectTick(LivingEntity living, int strength){
			living.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(Bolas.SLOW_MOVEMENT);
		}

		@Override
		public boolean isDurationEffectTick(int timeLeft, int p_76397_2_){
			return timeLeft == 1;
		}
	}

	private static class RoyalChase extends MobEffect{

		protected RoyalChase(MobEffectCategory p_i50391_1_){
			super(p_i50391_1_, 0x0);
		}
		@Override
		public List<ItemStack> getCurativeItems(){
			return Collections.emptyList();
		}
	}
	private static class TradeEffect extends MobEffect{
		private final boolean uncurable;

		protected TradeEffect(MobEffectCategory type, int color, boolean uncurable){
			super(type, color);
			this.uncurable = uncurable;
		}

		public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
			super.removeAttributeModifiers(entity, attributeMap, amplifier);
			if (entity.getType().is(TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation("dragonsurvival:royal_departure_affected")))) {
				if (!entity.level.isClientSide())
					entity.discard();
			}
		}

		@Override
		public boolean isDurationEffectTick(int duration, int amplifier) {
			return true;
		}
	}
}