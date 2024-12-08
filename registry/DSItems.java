package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.SeaDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.items.*;
import by.dragonsurvivalteam.dragonsurvival.common.items.food.ChargedSoupItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.food.DragonFoodItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarBoneItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

@Mod.EventBusSubscriber( modid = DragonSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class DSItems{
	public static HashMap<String, Item> DS_ITEMS = new HashMap<>();

	public static Item dragonHeartShard, weakDragonHeart, elderDragonHeart;
	public static Item starBone, elderDragonBone, elderDragonDust;

	public static Item princeSummon;
	public static Item princessSummon;

	public static Item charredMeat, charredVegetable, charredMushroom, charredSeafood, chargedCoal, chargedSoup, hotDragonRod, explosiveCopper, doubleQuartz, quartzExplosiveCopper, meatChorusMix, meatWildBerries, smellyMeatPorridge, diamondChorus, luminousOintment, sweetSourRabbit, seasonedFish, goldenCoralPufferfish, frozenRawFish, goldenTurtleEgg;
	public static Item seaDragonTreat, caveDragonTreat, forestDragonTreat;
	public static Item huntingNet;
	public static Item passiveFireBeacon, passiveMagicBeacon, passivePeaceBeacon;
	public static Item starHeart;

	public static Item wingGrantItem, spinGrantItem;
	public static Item lightningTextureItem;
	public static Item inactivePeaceDragonBeacon, inactiveMagicDragonBeacon, inactiveFireDragonBeacon;

	@SubscribeEvent
	public static void register(final RegisterEvent event){
		if (!event.getRegistryKey().equals(Registry.ITEM_REGISTRY))
			return ;
		Properties defaultProperties = new Item.Properties().tab(DragonSurvivalMod.items);

		starBone = registerItem(event, new StarBoneItem(defaultProperties), "star_bone");
		starHeart = registerItem(event, new StarHeartItem(defaultProperties), "star_heart");

		elderDragonDust = registerItem(event, "elder_dragon_dust", "ds.description.elderDragonDust");
		elderDragonBone = registerItem(event, "elder_dragon_bone", "ds.description.elderDragonBone");

     	princeSummon = registerItem(event, new RoyalSummonItem(() -> DSEntities.PRINCE_ON_HORSE, defaultProperties), "prince_summon");
		princessSummon = registerItem(event, new RoyalSummonItem(() -> DSEntities.PRINCESS_ON_HORSE, defaultProperties), "princess_summon");

		dragonHeartShard = registerItem(event, "heart_element", "ds.description.heartElement");
		weakDragonHeart = registerItem(event, "weak_dragon_heart", "ds.description.weakDragonHeart");
		elderDragonHeart = registerItem(event, "elder_dragon_heart", "ds.description.elderDragonHeart");

		chargedCoal = registerItem(event, new ChargedCoalItem(defaultProperties, DragonTypes.CAVE , LivingEntity::removeAllEffects), "charged_coal");
		chargedSoup = registerItem(event, new ChargedSoupItem(defaultProperties), "charged_soup");
		charredMeat = registerItem(event, new DragonFoodItem(defaultProperties), "charred_meat");
		charredVegetable = registerItem(event, new DragonFoodItem(defaultProperties), "charred_vegetable");
		charredMushroom = registerItem(event, new DragonFoodItem(defaultProperties), "charred_mushroom");
		charredSeafood = registerItem(event, new DragonFoodItem(defaultProperties), "charred_seafood");
		hotDragonRod = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.CAVE, () -> new MobEffectInstance(DragonEffects.FIRE, Functions.minutesToTicks(1))), "hot_dragon_rod");
		explosiveCopper = registerItem(event, new DragonFoodItem(defaultProperties, null, e -> {
			e.hurt(DamageSource.explosion(e), 1f);
			e.level.addParticle(ParticleTypes.EXPLOSION, e.getX(), e.getEyeY(), e.getZ(), 1.0D, 0.0D, 0.0D);
			e.level.playSound(null, new BlockPos(e.getEyePosition()), SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR, SoundSource.PLAYERS, 1f, 1f);
		}), "explosive_copper");
		quartzExplosiveCopper = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.CAVE, e -> {
			e.removeEffect(MobEffects.POISON);
			e.removeEffect(MobEffects.WITHER);
		}, () -> new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)), () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1)), "quartz_explosive_copper");
		doubleQuartz = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.CAVE, () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5))), "double_quartz");

		sweetSourRabbit = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.FOREST , LivingEntity::removeAllEffects), "sweet_sour_rabbit");
		luminousOintment = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.FOREST, () -> new MobEffectInstance(MobEffects.GLOWING, Functions.minutesToTicks(5)), () -> new MobEffectInstance(DragonEffects.MAGIC, Functions.minutesToTicks(5))), "luminous_ointment");
		diamondChorus = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.FOREST, e -> {
			e.removeEffect(MobEffects.POISON);
			e.removeEffect(MobEffects.WITHER);
		}, () -> new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)), () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1)), "diamond_chorus");
		smellyMeatPorridge = registerItem(event, new DragonFoodItem(defaultProperties), "smelly_meat_porridge");
		meatWildBerries = registerItem(event, new DragonFoodItem(defaultProperties), "meat_wild_berries");
		meatChorusMix = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.FOREST, () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5))), "meat_chorus_mix");

		seasonedFish = registerItem(event, new DragonFoodItem(defaultProperties), "seasoned_fish");
		goldenCoralPufferfish = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.SEA, () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5))), "golden_coral_pufferfish");
		frozenRawFish = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.SEA , e -> {
			e.removeAllEffects();

			if(!e.level.isClientSide){
				if(DragonUtils.getHandler(e).getType() instanceof SeaDragonType type){
					type.timeWithoutWater = 0;
				}
			}
		}), "frozen_raw_fish");
		goldenTurtleEgg = registerItem(event, new DragonFoodItem(defaultProperties, DragonTypes.SEA, e -> {
			e.removeEffect(MobEffects.POISON);
			e.removeEffect(MobEffects.WITHER);
		}, () -> new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)), () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1)), "golden_turtle_egg");

		seaDragonTreat = registerItem(event, new DragonTreatItem(DragonTypes.SEA, defaultProperties), "sea_dragon_treat");
		caveDragonTreat = registerItem(event, new DragonTreatItem(DragonTypes.CAVE, defaultProperties), "cave_dragon_treat");
		forestDragonTreat = registerItem(event, new DragonTreatItem(DragonTypes.FOREST, defaultProperties), "forest_dragon_treat");

		huntingNet = registerItem(event, new Item(new Item.Properties()), "dragon_hunting_mesh");
		lightningTextureItem = registerItem(event, new Item(new Item.Properties()), "lightning");

		passiveMagicBeacon = registerItem(event, new Item(new Item.Properties()), "beacon_magic_1");
		passivePeaceBeacon = registerItem(event, new Item(new Item.Properties()), "beacon_peace_1");
		passiveFireBeacon = registerItem(event, new Item(new Item.Properties()), "beacon_fire_1");

		inactiveMagicDragonBeacon = registerItem(event, new Item(new Item.Properties()), "beacon_magic_0");
		inactivePeaceDragonBeacon = registerItem(event, new Item(new Item.Properties()), "beacon_peace_0");
		inactiveFireDragonBeacon = registerItem(event, new Item(new Item.Properties()), "beacon_fire_0");


		wingGrantItem = registerItem(event, new WingGrantItem(defaultProperties), "wing_grant");
		spinGrantItem = registerItem(event, new SpinGrantItem(defaultProperties), "spin_grant");
	}

	public static Item registerItem(RegisterEvent event, String name, String description){
		Item item = new Item(new Item.Properties().tab(DragonSurvivalMod.items)){
			@Override
			public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> list, @NotNull TooltipFlag tooltipFlag){
				super.appendHoverText(stack, world, list, tooltipFlag);
				list.add(Component.translatable(description));
			}
		};
		event.register(Registry.ITEM_REGISTRY, new ResourceLocation(DragonSurvivalMod.MODID, name), ()->item);
		DS_ITEMS.put(name, item);
		return item;
	}

	public static Item registerItem(RegisterEvent event, Item item, String name){
		event.register(Registry.ITEM_REGISTRY, new ResourceLocation(DragonSurvivalMod.MODID, name),()->item);
		DS_ITEMS.put(name, item);
		return item;
	}
}