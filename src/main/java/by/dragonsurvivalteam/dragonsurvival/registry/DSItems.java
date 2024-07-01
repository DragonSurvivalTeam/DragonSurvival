package by.dragonsurvivalteam.dragonsurvival.registry;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.SeaDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.items.*;
import by.dragonsurvivalteam.dragonsurvival.common.items.food.ChargedSoupItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.food.DragonFoodItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarBoneItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import by.dragonsurvivalteam.dragonsurvival.util.BlockPosHelper;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DSItems {

	private static class CustomHoverTextItem extends Item {
		private final String description;

		public CustomHoverTextItem(Properties properties, String description) {
			super(properties);
			this.description = description;
		}

		@Override
		public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
			super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
			pTooltipComponents.add(Component.translatable(description));
		}
	}

	public static final DeferredRegister<Item> DS_ITEMS = DeferredRegister.create(
			BuiltInRegistries.ITEM,
			MODID
	);

	private static final Properties defaultProperties = new Item.Properties();

	public static final Holder<Item> STAR_BONE = DS_ITEMS.register("star_bone", () -> new StarBoneItem(defaultProperties));
	public static final Holder<Item> STAR_HEART = DS_ITEMS.register("star_heart", () -> new StarHeartItem(defaultProperties));

	public static final Holder<Item> ELDER_DRAGON_DUST = DS_ITEMS.register("elder_dragon_dust", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.elderDragonDust"));
	public static final Holder<Item> ELDER_DRAGON_BONE = DS_ITEMS.register("elder_dragon_bone", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.elderDragonBone"));

	public static final Holder<Item> DRAGON_HEART_SHARD = DS_ITEMS.register("heart_element", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.heartElement"));
	public static final Holder<Item> WEAK_DRAGON_HEART = DS_ITEMS.register("weak_dragon_heart", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.weakDragonHeart"));
	public static final Holder<Item> ELDER_DRAGON_HEART = DS_ITEMS.register("elder_dragon_heart", () -> new CustomHoverTextItem(new Item.Properties(), "ds.description.elderDragonHeart"));

	public static final Holder<Item> CHARGED_COAL = DS_ITEMS.register("charged_coal", () -> new ChargedCoalItem(defaultProperties, DragonTypes.CAVE , LivingEntity::removeAllEffects));
	public static final Holder<Item> CHARGED_SOUP = DS_ITEMS.register("charged_soup", () -> new ChargedSoupItem(defaultProperties));
	public static final Holder<Item> CHARRED_MEAT = DS_ITEMS.register("charred_meat", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> CHARRED_VEGETABLE = DS_ITEMS.register("charred_vegetable", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> CHARRED_MUSHROOM = DS_ITEMS.register("charred_mushroom", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> CHARRED_SEAFOOD = DS_ITEMS.register("charred_seafood", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> HOT_DRAGON_ROD = DS_ITEMS.register("hot_dragon_rod", () -> new DragonFoodItem(defaultProperties, DragonTypes.CAVE, () -> new MobEffectInstance(DSEffects.FIRE, Functions.minutesToTicks(1))));
	public static final Holder<Item> EXPLOSIVE_COPPER = DS_ITEMS.register("explosive_copper", () -> new DragonFoodItem(defaultProperties, null, e -> {
		e.hurt(e.damageSources().explosion(e, e), 1f);
		e.level().addParticle(ParticleTypes.EXPLOSION, e.getX(), e.getEyeY(), e.getZ(), 1.0D, 0.0D, 0.0D);
		e.level().playSound(null, BlockPosHelper.get(e.getEyePosition()), SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR, SoundSource.PLAYERS, 1f, 1f);
	}));
	public static final Holder<Item> QUARTZ_EXPLOSIVE_COPPER = DS_ITEMS.register("quartz_explosive_copper", () -> new DragonFoodItem(defaultProperties, DragonTypes.CAVE, e -> {
		e.removeEffect(MobEffects.POISON);
		e.removeEffect(MobEffects.WITHER);
	}, () -> new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)), () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1)));
	public static final Holder<Item> DOUBLE_QUARTZ = DS_ITEMS.register("double_quartz", () -> new DragonFoodItem(defaultProperties, DragonTypes.CAVE, () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5))));

	public static final Holder<Item> SWEET_SOUR_RABBIT = DS_ITEMS.register("sweet_sour_rabbit", () -> new DragonFoodItem(defaultProperties, DragonTypes.FOREST , LivingEntity::removeAllEffects));
	public static final Holder<Item> LUMINOUS_OINTMENT = DS_ITEMS.register("luminous_ointment", () -> new DragonFoodItem(defaultProperties, DragonTypes.FOREST, () -> new MobEffectInstance(MobEffects.GLOWING, Functions.minutesToTicks(5)), () -> new MobEffectInstance(DSEffects.MAGIC, Functions.minutesToTicks(5))));
	public static final Holder<Item> DIAMOND_CHORUS = DS_ITEMS.register("diamond_chorus", () -> new DragonFoodItem(defaultProperties, DragonTypes.FOREST, e -> {
		e.removeEffect(MobEffects.POISON);
		e.removeEffect(MobEffects.WITHER);
	}, () -> new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)), () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1)));
	public static final Holder<Item> SMELLY_MEAT_PORRIDGE = DS_ITEMS.register("smelly_meat_porridge", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> MEAT_WILD_BERRIES = DS_ITEMS.register("meat_wild_berries", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> MEAT_CHORUS_MIX = DS_ITEMS.register("meat_chorus_mix", () -> new DragonFoodItem(defaultProperties, DragonTypes.FOREST, () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5))));

	public static final Holder<Item> SEASONED_FISH = DS_ITEMS.register("seasoned_fish", () -> new DragonFoodItem(defaultProperties));
	public static final Holder<Item> GOLDEN_CORAL_PUFFERFISH = DS_ITEMS.register("golden_coral_pufferfish", () -> new DragonFoodItem(defaultProperties, DragonTypes.SEA, () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5))));
	public static final Holder<Item> FROZEN_RAW_FISH = DS_ITEMS.register("frozen_raw_fish", () -> new DragonFoodItem(defaultProperties, DragonTypes.SEA , e -> {
		e.removeAllEffects();

		if(!e.level().isClientSide()){
			if(DragonStateProvider.getOrGenerateHandler(e).getType() instanceof SeaDragonType type){
				type.timeWithoutWater = 0;
			}
		}
	}));
	public static final Holder<Item> GOLDEN_TURTLE_EGG = DS_ITEMS.register("golden_turtle_egg", () -> new DragonFoodItem(defaultProperties, DragonTypes.SEA, e -> {
		e.removeEffect(MobEffects.POISON);
		e.removeEffect(MobEffects.WITHER);
	}, () -> new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)), () -> new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1)));

	public static final Holder<Item> SEA_DRAGON_TREAT = DS_ITEMS.register("sea_dragon_treat", () -> new DragonTreatItem(DragonTypes.SEA, defaultProperties));
	public static final Holder<Item> CAVE_DRAGON_TREAT = DS_ITEMS.register("cave_dragon_treat", () -> new DragonTreatItem(DragonTypes.CAVE, defaultProperties));
	public static final Holder<Item> FOREST_DRAGON_TREAT = DS_ITEMS.register("forest_dragon_treat", () -> new DragonTreatItem(DragonTypes.FOREST, defaultProperties));

	public static final Holder<Item> WING_GRANT_ITEM = DS_ITEMS.register("wing_grant", () -> new WingGrantItem(defaultProperties));
	public static final Holder<Item> SPIN_GRANT_ITEM = DS_ITEMS.register("spin_grant", () -> new SpinGrantItem(defaultProperties));

	// Items that shouldn't show up in the creative tab
	public static final Holder<Item> HUNTING_NET = DS_ITEMS.register("dragon_hunting_mesh", () -> new Item(new Item.Properties()));
	public static final Holder<Item> LIGHTNING_TEXTURE_ITEM = DS_ITEMS.register("lightning", () -> new Item(new Item.Properties()));

	public static final Holder<Item> PASSIVE_MAGIC_BEACON = DS_ITEMS.register("beacon_magic_1", () -> new Item(new Item.Properties()));
	public static final Holder<Item> PASSIVE_PEACE_BEACON = DS_ITEMS.register("beacon_peace_1", () -> new Item(new Item.Properties()));
	public static final Holder<Item> PASSIVE_FIRE_BEACON = DS_ITEMS.register("beacon_fire_1", () -> new Item(new Item.Properties()));

	public static final Holder<Item> INACTIVE_MAGIC_DRAGON_BEACON = DS_ITEMS.register("beacon_magic_0", () -> new Item(new Item.Properties()));
	public static final Holder<Item> INACTIVE_PEACE_DRAGON_BEACON = DS_ITEMS.register("beacon_peace_0", () -> new Item(new Item.Properties()));
	public static final Holder<Item> INACTIVE_FIRE_DRAGON_BEACON = DS_ITEMS.register("beacon_fire_0", () -> new Item(new Item.Properties()));
}