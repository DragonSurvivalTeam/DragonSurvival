package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.sounds.SoundRegistry;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.*;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonPressurePlates.PressurePlateType;
import by.dragonsurvivalteam.dragonsurvival.common.items.HelmetItem;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import software.bernie.geckolib3.core.util.Color;

import java.util.HashMap;

@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.MOD )
public class DSBlocks{
	public static HashMap<String, Block> DS_BLOCKS = new HashMap<>();
	public static HashMap<String, BlockItem> DS_BLOCK_ITEMS = new HashMap<>();

	public static DragonDoor spruceDoor, acaciaDoor, birchDoor, jungleDoor, oakDoor, darkOakDoor, crimsonDoor, warpedDoor;
	public static DragonDoor legacyDoor, ironDoor, murdererDoor, sleeperDoor, stoneDoor;
	public static DragonDoor caveDoor, forestDoor, seaDoor;

	public static SmallDragonDoor spruceSmallDoor, acaciaSmallDoor, birchSmallDoor, jungleSmallDoor, oakSmallDoor, darkOakSmallDoor, crimsonSmallDoor, warpedSmallDoor;

	public static SourceOfMagicBlock forestSourceOfMagic, caveSourceOfMagic, seaSourceOfMagic;

	public static Block dragon_altar_stone;
	public static Block dragon_altar_sandstone;
	public static Block dragon_altar_red_sandstone;
	public static Block dragon_altar_purpur_block;
	public static Block dragon_altar_oak_log;
	public static Block dragon_altar_nether_bricks;
	public static Block dragon_altar_mossy_cobblestone;
	public static Block dragon_altar_blackstone;
	public static Block dragon_altar_birch_log;

	public static HelmetBlock helmet1, helmet2, helmet3;
	public static DragonBeacon dragonBeacon, peaceDragonBeacon, magicDragonBeacon, fireDragonBeacon;
	public static RotatedPillarBlock dragonMemoryBlock;

	public static TreasureBlock treasureDebris, treasureDiamond, treasureEmerald, treasureCopper, treasureGold, treasureIron;

	public static DragonPressurePlates dragonPressurePlate, humanPressurePlate, seaPressurePlate, forestPressurePlate, cavePressurePlate;
	public static SmallDragonDoor stoneSmallDoor;
	public static SmallDragonDoor forestSmallDoor;
	public static SmallDragonDoor sleeperSmallDoor;
	public static SmallDragonDoor caveSmallDoor;
	public static SmallDragonDoor seaSmallDoor;
	public static SmallDragonDoor ironSmallDoor;
	public static SmallDragonDoor murdererSmallDoor;


	@SubscribeEvent
	public static void registerEvent(final RegisterEvent event){
		ResourceKey<? extends Registry<?>> registerKey = event.getRegistryKey();
		if (registerKey.equals(Registry.BLOCK_REGISTRY)) {
			registerBlocks(event);
		} else if (registerKey.equals(Registry.ITEM_REGISTRY)) {
			registerBlockItems(event);
		}
	}
	protected static void registerBlocks(final RegisterEvent event){
		dragon_altar_stone = registerBlock(new DragonAltarBlock(Block.Properties.of(Material.STONE).strength(1.5f).sound(SoundType.STONE).requiresCorrectToolForDrops()), "dragon_altar_stone", event);
		dragon_altar_sandstone = registerBlock(new DragonAltarBlock(Block.Properties.of(Material.STONE).strength(0.8f).sound(SoundType.STONE).requiresCorrectToolForDrops()), "dragon_altar_sandstone", event);
		dragon_altar_red_sandstone = registerBlock(new DragonAltarBlock(Block.Properties.of(Material.STONE).strength(0.8f).sound(SoundType.STONE).requiresCorrectToolForDrops()), "dragon_altar_red_sandstone", event);
		dragon_altar_purpur_block = registerBlock(new DragonAltarBlock(Block.Properties.of(Material.STONE).strength(1.5f).sound(SoundType.STONE).requiresCorrectToolForDrops()), "dragon_altar_purpur_block", event);
		dragon_altar_oak_log = registerBlock(new DragonAltarBlock(Block.Properties.of(Material.WOOD).strength(2f).sound(SoundType.WOOD)), "dragon_altar_oak_log", event);
		dragon_altar_birch_log = registerBlock(new DragonAltarBlock(Block.Properties.of(Material.WOOD).strength(2f).sound(SoundType.WOOD)), "dragon_altar_birch_log", event);
		dragon_altar_nether_bricks = registerBlock(new DragonAltarBlock(Block.Properties.of(Material.STONE).strength(0.4f).sound(SoundType.NETHER_BRICKS).requiresCorrectToolForDrops()), "dragon_altar_nether_bricks", event);
		dragon_altar_mossy_cobblestone = registerBlock(new DragonAltarBlock(Block.Properties.of(Material.STONE).strength(2f).sound(SoundType.STONE).requiresCorrectToolForDrops()), "dragon_altar_mossy_cobblestone", event);
		dragon_altar_blackstone = registerBlock(new DragonAltarBlock(Block.Properties.of(Material.STONE).strength(1.5f).sound(SoundType.STONE).requiresCorrectToolForDrops()), "dragon_altar_blackstone", event);

		oakDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.OAK_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "oak_dragon_door", event);
		spruceDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.SPRUCE_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "spruce_dragon_door", event);
		acaciaDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.ACACIA_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "acacia_dragon_door", event);
		birchDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.BIRCH_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "birch_dragon_door", event);
		jungleDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.JUNGLE_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "jungle_dragon_door", event);
		darkOakDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.DARK_OAK_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "dark_oak_dragon_door", event);
		warpedDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.WARPED_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "warped_dragon_door", event);
		crimsonDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.CRIMSON_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "crimson_dragon_door", event);

		caveDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.STONE, Blocks.BLACKSTONE.defaultMaterialColor()).requiresCorrectToolForDrops().strength(7.0F).sound(SoundType.GILDED_BLACKSTONE).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.CAVE), "cave_dragon_door", event);
		forestDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.DARK_PRISMARINE.defaultMaterialColor()).requiresCorrectToolForDrops().strength(7.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.FOREST), "forest_dragon_door", event);
		seaDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.STONE, MaterialColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(7.0F).sound(SoundType.STONE).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.SEA), "sea_dragon_door", event);

		ironDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.METAL, MaterialColor.METAL).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.POWER), "iron_dragon_door", event);

		// small doors
		oakSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.OAK_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "oak_small_dragon_door", event);
		spruceSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.SPRUCE_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "spruce_small_dragon_door", event);
		acaciaSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.ACACIA_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "acacia_small_dragon_door", event);
		birchSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.BIRCH_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "birch_small_dragon_door", event);
		jungleSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.JUNGLE_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "jungle_small_dragon_door", event);
		darkOakSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.DARK_OAK_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "dark_oak_small_dragon_door", event);
		warpedSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.WARPED_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "warped_small_dragon_door", event);
		crimsonSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.CRIMSON_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "crimson_small_dragon_door", event);

		stoneSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.STONE.defaultMaterialColor()).strength(3.0F).sound(SoundType.STONE).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "stone_small_dragon_door", event);
		sleeperSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.CRIMSON_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.STONE).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "sleeper_small_dragon_door", event);

		caveSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.STONE, Blocks.BLACKSTONE.defaultMaterialColor()).requiresCorrectToolForDrops().strength(7.0F).sound(SoundType.GILDED_BLACKSTONE).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.CAVE), "cave_small_dragon_door", event);
		forestSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.WOOD, Blocks.DARK_PRISMARINE.defaultMaterialColor()).requiresCorrectToolForDrops().strength(7.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.FOREST), "forest_small_dragon_door", event);
		seaSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.STONE, MaterialColor.COLOR_BROWN).requiresCorrectToolForDrops().strength(7.0F).sound(SoundType.STONE).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.SEA), "sea_small_dragon_door", event);

		ironSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.METAL, MaterialColor.METAL).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.POWER), "iron_small_dragon_door", event);
		murdererSmallDoor = registerBlock(new SmallDragonDoor(Block.Properties.of(Material.METAL, MaterialColor.METAL).requiresCorrectToolForDrops().strength(5.0F).sound(SoundType.METAL).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.POWER), "murderer_small_dragon_door", event);
		murdererDoor = registerBlock(new DragonDoor(Block.Properties.copy(oakDoor), DragonDoor.DragonDoorOpenRequirement.NONE), "murderer_dragon_door", event);
		sleeperDoor = registerBlock(new DragonDoor(Block.Properties.copy(oakDoor), DragonDoor.DragonDoorOpenRequirement.NONE), "sleeper_dragon_door", event);
		stoneDoor = registerBlock(new DragonDoor(Block.Properties.copy(oakDoor), DragonDoor.DragonDoorOpenRequirement.NONE), "stone_dragon_door", event);
		legacyDoor = registerBlock(new DragonDoor(Block.Properties.of(Material.WOOD, Blocks.SPRUCE_PLANKS.defaultMaterialColor()).strength(3.0F).sound(SoundType.WOOD).noOcclusion(), DragonDoor.DragonDoorOpenRequirement.NONE), "legacy_dragon_door", event);

		caveSourceOfMagic = registerBlock(new SourceOfMagicBlock(Block.Properties.of(Material.STONE).randomTicks().strength(3, 100).noOcclusion().lightLevel(c1 -> 10)), "cave_source_of_magic", event);
		forestSourceOfMagic = registerBlock(new SourceOfMagicBlock(Block.Properties.of(Material.STONE).strength(3, 100).noOcclusion().lightLevel(c1 -> 10)), "forest_source_of_magic", event);
		seaSourceOfMagic = registerBlock(new SourceOfMagicBlock(Block.Properties.of(Material.STONE).strength(3, 100).noOcclusion().lightLevel(c1 -> 10)), "sea_source_of_magic", event);

		treasureDebris = registerBlock(new TreasureBlock(Color.ofRGB(148, 120, 114), Block.Properties.of(Material.METAL, MaterialColor.COLOR_BROWN).noOcclusion().sound(SoundRegistry.treasureMetal).strength(0.5F)), "treasure_debris", event);
		treasureDiamond = registerBlock(new TreasureBlock(Color.ofRGB(212, 255, 255), Block.Properties.of(Material.METAL, MaterialColor.DIAMOND).noOcclusion().sound(SoundRegistry.treasureGem).strength(0.5F)), "treasure_diamond", event);
		treasureEmerald = registerBlock(new TreasureBlock(Color.ofRGB(57, 240, 94), Block.Properties.of(Material.METAL, MaterialColor.COLOR_GREEN).noOcclusion().sound(SoundRegistry.treasureGem).strength(0.5F)), "treasure_emerald", event);
		treasureCopper = registerBlock(new TreasureBlock(Color.ofRGB(255, 255, 208), Block.Properties.of(Material.GLASS, MaterialColor.COLOR_ORANGE).noOcclusion().sound(SoundRegistry.treasureMetal).strength(0.5F)), "treasure_copper", event);
		treasureGold = registerBlock(new TreasureBlock(Color.ofRGB(255, 255, 243), Block.Properties.of(Material.METAL, MaterialColor.GOLD).noOcclusion().sound(SoundRegistry.treasureMetal).strength(0.5F)), "treasure_gold", event);
		treasureIron = registerBlock(new TreasureBlock(Color.ofRGB(211, 211, 211), Block.Properties.of(Material.METAL, MaterialColor.METAL).noOcclusion().sound(SoundRegistry.treasureMetal).strength(0.5F)), "treasure_iron", event);

		helmet1 = registerBlock(new HelmetBlock(Block.Properties.of(Material.METAL)), "broken_knight_helmet_1", event);
		helmet2 = registerBlock(new HelmetBlock(Block.Properties.of(Material.METAL)), "broken_knight_helmet_2", event);
		helmet3 = registerBlock(new HelmetBlock(Block.Properties.of(Material.METAL)), "broken_knight_helmet_3", event);

		dragonBeacon = registerBlock(new DragonBeacon(Block.Properties.of(Material.HEAVY_METAL).strength(15, 50).requiresCorrectToolForDrops().noOcclusion().noCollission()), "empty_dragon_beacon", event);
		dragonMemoryBlock = registerBlock(new RotatedPillarBlock(Block.Properties.of(Material.HEAVY_METAL).strength(3, 30).requiresCorrectToolForDrops()), "dragon_memory_block", event);
		peaceDragonBeacon = registerBlock(new DragonBeacon(Block.Properties.copy(dragonBeacon).lightLevel(value -> value.getValue(DragonBeacon.LIT) ? 15 : 0)), "dragon_beacon_peace", event);
		magicDragonBeacon = registerBlock(new DragonBeacon(Block.Properties.copy(dragonBeacon).lightLevel(value -> value.getValue(DragonBeacon.LIT) ? 15 : 0)), "dragon_beacon_magic", event);
		fireDragonBeacon = registerBlock(new DragonBeacon(Block.Properties.copy(dragonBeacon).lightLevel(value -> value.getValue(DragonBeacon.LIT) ? 15 : 0)), "dragon_beacon_fire", event);

		dragonPressurePlate = registerBlock(new DragonPressurePlates(Block.Properties.of(Material.STONE).strength(1.5f).sound(SoundType.STONE).requiresCorrectToolForDrops(), PressurePlateType.DRAGON), "dragon_pressure_plate", event);
		humanPressurePlate = registerBlock(new DragonPressurePlates(Block.Properties.of(Material.STONE).strength(1.5f).sound(SoundType.STONE).requiresCorrectToolForDrops(), PressurePlateType.HUMAN), "human_pressure_plate", event);
		seaPressurePlate = registerBlock(new DragonPressurePlates(Block.Properties.of(Material.STONE).strength(1.5f).sound(SoundType.STONE).requiresCorrectToolForDrops(), PressurePlateType.SEA), "sea_dragon_pressure_plate", event);
		forestPressurePlate = registerBlock(new DragonPressurePlates(Block.Properties.of(Material.WOOD).strength(2.0F).sound(SoundType.WOOD).requiresCorrectToolForDrops(), PressurePlateType.FOREST), "forest_dragon_pressure_plate", event);
		cavePressurePlate = registerBlock(new DragonPressurePlates(Block.Properties.of(Material.STONE).strength(1.5f).sound(SoundType.STONE).requiresCorrectToolForDrops(), PressurePlateType.CAVE), "cave_dragon_pressure_plate", event);
	}

	private static <B extends Block> B registerBlock(B block, String identifier, RegisterEvent event){
		event.register(Registry.BLOCK_REGISTRY, new ResourceLocation(DragonSurvivalMod.MODID,identifier), ()->block);
		DS_BLOCKS.put(identifier, block);
		return block;
	}

	public static void registerBlockItems(final RegisterEvent event){
		event.register(Registry.ITEM_REGISTRY, new ResourceLocation(ResourceHelper.getKey(helmet1).toString()), ()->new HelmetItem(helmet1, new Item.Properties().tab(DragonSurvivalMod.items)));
		event.register(Registry.ITEM_REGISTRY, new ResourceLocation(ResourceHelper.getKey(helmet2).toString()), ()->new HelmetItem(helmet2, new Item.Properties().tab(DragonSurvivalMod.items)));
		event.register(Registry.ITEM_REGISTRY, new ResourceLocation(ResourceHelper.getKey(helmet3).toString()), ()->new HelmetItem(helmet3, new Item.Properties().tab(DragonSurvivalMod.items)));

		DSBlocks.DS_BLOCKS.forEach((key, value) -> {
			if(key.startsWith("broken_knight_helmet")) return;
			registerItem(value, new Item.Properties().tab(DragonSurvivalMod.items), event);
		});

//		registerItem(oakSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(spruceSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(acaciaSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(jungleSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(darkOakSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(birchSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(warpedSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(crimsonSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//
//		registerItem(stoneSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(forestSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(sleeperSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(caveSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(seaSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(ironSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(murdererSmallDoor, new Item.Properties().tab(DragonSurvivalMod.items), event);
//
//
//		registerItem(seaSourceOfMagic, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(forestSourceOfMagic, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(caveSourceOfMagic, new Item.Properties().tab(DragonSurvivalMod.items), event);
//
//		registerItem(treasureDebris, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(treasureDiamond, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(treasureEmerald, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(treasureCopper, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(treasureGold, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(treasureIron, new Item.Properties().tab(DragonSurvivalMod.items), event);
//
//		registerItem(dragon_altar_stone, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(dragon_altar_sandstone, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(dragon_altar_red_sandstone, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(dragon_altar_purpur_block, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(dragon_altar_oak_log, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(dragon_altar_nether_bricks, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(dragon_altar_mossy_cobblestone, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(dragon_altar_blackstone, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(dragon_altar_birch_log, new Item.Properties().tab(DragonSurvivalMod.items), event);
//
//
//		registerItem(dragonBeacon, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(peaceDragonBeacon, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(magicDragonBeacon, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(fireDragonBeacon, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(dragonMemoryBlock, new Item.Properties().tab(DragonSurvivalMod.items), event);
//
//		registerItem(dragonPressurePlate, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(humanPressurePlate, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(seaPressurePlate, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(forestPressurePlate, new Item.Properties().tab(DragonSurvivalMod.items), event);
//		registerItem(cavePressurePlate, new Item.Properties().tab(DragonSurvivalMod.items), event);
	}

	@SuppressWarnings( "ConstantConditions" )
	private static void registerItem(Block block, Item.Properties itemProperties, RegisterEvent event){
		BlockItem itm = new BlockItem(block, itemProperties.tab(DragonSurvivalMod.items));
		event.register(Registry.ITEM_REGISTRY, new ResourceLocation(ResourceHelper.getKey(block).toString()), ()->itm);
		DS_BLOCK_ITEMS.put(ResourceHelper.getKey(block).toString(), itm);
	}

	private static void registerSingleItem(Block block, Item.Properties properties, RegisterEvent event){
		registerItem(block, properties.stacksTo(1), event);
	}
}