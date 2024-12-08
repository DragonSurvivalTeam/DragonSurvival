package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.*;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.BallLightningEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.DragonSpikeEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.FireBallEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.VillagerRelationsHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import org.spongepowered.asm.launch.Phases;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings( "rawtypes,unchecked" )
@Mod.EventBusSubscriber( modid = DragonSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD )
public class DSEntities{
	public static EntityType<DragonEntity> DRAGON;
	public static EntityType<DragonEntity> DRAGON_ARMOR;
	public static EntityType<HunterHoundEntity> HUNTER_HOUND;
	public static EntityType<Shooter> SHOOTER_HUNTER;
	public static EntityType<SquireEntity> SQUIRE_HUNTER;
	public static EntityType<Princess> PRINCESS;
	public static EntityType<KnightEntity> KNIGHT;
	public static EntityType<PrincesHorseEntity> PRINCESS_ON_HORSE;
	public static EntityType<PrinceHorseEntity> PRINCE_ON_HORSE;
	public static EntityType<Bolas> BOLAS_ENTITY;
	//Magic abilities
	public static EntityType<DragonSpikeEntity> DRAGON_SPIKE;
	public static EntityType<BallLightningEntity> BALL_LIGHTNING;
	public static EntityType<FireBallEntity> FIREBALL;
	public static VillagerProfession PRINCESS_PROFESSION, PRINCE_PROFESSION;

	@SubscribeEvent
	public static void register(RegisterEvent event)
	{
		ResourceKey<? extends Registry<?>> key = event.getRegistryKey();
		if (key.equals(Registry.ENTITY_TYPE_REGISTRY))
			registerEntities(event);
		else if (key.equals(Registry.VILLAGER_PROFESSION_REGISTRY))
			registerVillageTypes(event);
		else if (key.equals(Registry.ITEM_REGISTRY))
			registerSpawnEggs(event);
	}

	@SubscribeEvent
	public static void attributeCreationEvent(EntityAttributeCreationEvent event){
		event.put(DRAGON, DragonEntity.createLivingAttributes().build());
		event.put(DRAGON_ARMOR, DragonEntity.createLivingAttributes().build());
		event.put(HUNTER_HOUND, Wolf.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.houndSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.houndDamage).add(Attributes.MAX_HEALTH, ServerConfig.houndHealth).build());
		event.put(SHOOTER_HUNTER, Pillager.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.hunterSpeed).add(Attributes.MAX_HEALTH, ServerConfig.hunterHealth).add(Attributes.ARMOR, ServerConfig.hunterArmor).add(Attributes.ATTACK_DAMAGE, ServerConfig.hunterDamage).build());
		event.put(SQUIRE_HUNTER, Vindicator.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.squireSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.squireDamage).add(Attributes.ARMOR, ServerConfig.squireArmor).add(Attributes.MAX_HEALTH, ServerConfig.squireHealth).build());
		event.put(KNIGHT, KnightEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.knightSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.knightDamage).add(Attributes.ARMOR, ServerConfig.knightArmor).add(Attributes.MAX_HEALTH, ServerConfig.knightHealth).build());
		event.put(PRINCESS, Villager.createAttributes().add(Attributes.MAX_HEALTH, ServerConfig.princessHealth).add(Attributes.ARMOR, ServerConfig.princessArmor).add(Attributes.MOVEMENT_SPEED, ServerConfig.princessSpeed).build());
		event.put(PRINCESS_ON_HORSE, Villager.createAttributes().add(Attributes.MAX_HEALTH, ServerConfig.princessHealth).add(Attributes.ARMOR, ServerConfig.princessArmor).add(Attributes.MOVEMENT_SPEED, ServerConfig.princessSpeed).build());
		event.put(PRINCE_ON_HORSE, Villager.createAttributes().add(Attributes.ATTACK_DAMAGE, ServerConfig.princeDamage).add(Attributes.MAX_HEALTH, ServerConfig.princeHealth).add(Attributes.ARMOR, ServerConfig.princeArmor).add(Attributes.MOVEMENT_SPEED, ServerConfig.princeSpeed).build());
	}

	@SubscribeEvent
	public static void registerSpawn(SpawnPlacementRegisterEvent event)
	{
		SpawnPlacements.SpawnPredicate predicate = (pEntityType, serverWorld, mobSpawnType, pPos, random) -> serverWorld.getBlockState(pPos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON) && serverWorld.canSeeSky(pPos);
		SpawnPlacements.SpawnPredicate princeSpawn = (pEntityType, serverWorld, mobSpawnType, pPos, random) -> predicate.test(pEntityType,serverWorld, mobSpawnType, pPos, random) && serverWorld.getLevel().isVillage(pPos);

		event.register(SHOOTER_HUNTER, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(SQUIRE_HUNTER, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(KNIGHT, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(PRINCE_ON_HORSE, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, princeSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
		event.register(PRINCESS_ON_HORSE, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, princeSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);

	}

	public static void registerEntities(RegisterEvent event){
		DRAGON = register(event, "dummy_dragon", new EntityType<>(DragonEntity::new, MobCategory.MISC, true, false, false, false, ImmutableSet.of(), EntityDimensions.fixed(0.9f, 1.9f), 0, 0));
		DRAGON_ARMOR = register(event, "dragon_armor", new EntityType<>(DragonEntity::new, MobCategory.MISC, true, false, false, false, ImmutableSet.of(), EntityDimensions.fixed(0.9f, 1.9f), 0, 0));

		BOLAS_ENTITY = register(event, "bolas", cast(EntityType.Builder.of((p_create_1_, p_create_2_) -> new Bolas(p_create_2_), MobCategory.MISC).sized(0.8F, 0.8F).clientTrackingRange(4).updateInterval(10).build("bolas")));

		DRAGON_SPIKE = register(event, "dragon_spike", EntityType.Builder.<DragonSpikeEntity>of(DragonSpikeEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(1).build("dragon_spike"));
		BALL_LIGHTNING = register(event, "ball_lightning", EntityType.Builder.<BallLightningEntity>of(BallLightningEntity::new, MobCategory.MISC).sized(1F, 1F).clientTrackingRange(4).updateInterval(1).build("ball_lightning"));
		FIREBALL = register(event, "fireball", EntityType.Builder.<FireBallEntity>of(FireBallEntity::new, MobCategory.MISC).sized(1F, 1F).clientTrackingRange(4).updateInterval(1).build("fireball"));

		HUNTER_HOUND = register(event, "hunter_hound", EntityType.Builder.of(HunterHoundEntity::new, MobCategory.MONSTER).sized(0.6F, 0.85F).clientTrackingRange(64).updateInterval(1).build("hunter_hound"));
		SHOOTER_HUNTER = register(event, "shooter", EntityType.Builder.of(Shooter::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(64).updateInterval(1).build("shooter"));
		SQUIRE_HUNTER = register(event, "squire", EntityType.Builder.of(SquireEntity::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(64).updateInterval(1).build("squire"));
		PRINCESS = register(event, "princess_entity", EntityType.Builder.<Princess>of(Princess::new, MobCategory.MONSTER).sized(0.6F, 1.9F).clientTrackingRange(64).updateInterval(1).build("princess_entity"));
		KNIGHT = register(event, "knight", EntityType.Builder.of(KnightEntity::new, MobCategory.MONSTER).sized(0.8f, 2.5f).clientTrackingRange(64).updateInterval(1).build("knight"));
		PRINCE_ON_HORSE = register(event, "prince", EntityType.Builder.<PrinceHorseEntity>of(PrinceHorseEntity::new, MobCategory.MONSTER).sized(0.8f, 2.5f).clientTrackingRange(64).updateInterval(1).build("prince"));
		PRINCESS_ON_HORSE = register(event, "princess", EntityType.Builder.<PrincesHorseEntity>of(PrincesHorseEntity::new, MobCategory.MONSTER).sized(0.8f, 2.5f).clientTrackingRange(64).updateInterval(1).build("princess"));


		VillagerRelationsHandler.dragonHunters = new ArrayList<>(4);

		if(ServerConfig.spawnHound){
			VillagerRelationsHandler.dragonHunters.add(cast(HUNTER_HOUND));
		}

		if(ServerConfig.spawnSquire){
			VillagerRelationsHandler.dragonHunters.add(cast(SQUIRE_HUNTER));
		}

		if(ServerConfig.spawnHunter){
			VillagerRelationsHandler.dragonHunters.add(cast(SHOOTER_HUNTER));
		}

		if(ServerConfig.spawnKnight){
			VillagerRelationsHandler.dragonHunters.add(cast(KNIGHT));
		}
	}

	private static EntityType register(RegisterEvent event, String id, EntityType type){
		ResourceLocation location = new ResourceLocation(DragonSurvivalMod.MODID, id);
		event.register(Registry.ENTITY_TYPE_REGISTRY, location, ()->type);
		return type;
	}

	private static <T extends EntityType<?>> T cast(EntityType<?> entityType){
		return (T)entityType;
	}

	public static void registerVillageTypes(RegisterEvent event){
		PRINCESS_PROFESSION = new VillagerProfession("princess", PoiType.NONE, PoiType.NONE, ImmutableSet.of(), ImmutableSet.of(), null);
		//event.register(Registry.VILLAGER_PROFESSION_REGISTRY, new ResourceLocation(DragonSurvivalMod.MODID, "princess"), ()->PRINCESS_PROFESSION);

		PRINCE_PROFESSION = new VillagerProfession("prince", PoiType.NONE, PoiType.NONE, ImmutableSet.of(), ImmutableSet.of(), null);
		//event.register(Registry.VILLAGER_PROFESSION_REGISTRY, new ResourceLocation(DragonSurvivalMod.MODID, "prince"), ()->PRINCE_PROFESSION);
	}

	public static void registerSpawnEggs(RegisterEvent event){

		registerSpawnEgg(event, HUNTER_HOUND, 10510648, 8934192);
		registerSpawnEgg(event, SHOOTER_HUNTER, 12486764, 2690565);
		registerSpawnEgg(event, SQUIRE_HUNTER, 12486764, 5318420);
		registerSpawnEgg(event, KNIGHT,  -15526631, -8750470);
		registerSpawnEgg(event, PRINCE_ON_HORSE, -14210026, -9571315);
		registerSpawnEgg(event, PRINCESS_ON_HORSE, -14804205, -14047);
	}

	private static void registerSpawnEgg(RegisterEvent event, EntityType entity, int eggPrimary, int eggSecondary){
		Item spawnEgg = new ForgeSpawnEggItem(()->entity, eggPrimary, eggSecondary, new Item.Properties().tab(DragonSurvivalMod.items));
		event.register(Registry.ITEM_REGISTRY ,new ResourceLocation(DragonSurvivalMod.MODID, ResourceHelper.getKey(entity).getPath() + "_spawn_egg"),()->spawnEgg);
	}
}