package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonAltarBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonBeacon;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonDoor;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonPressurePlates;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonRiderWorkbenchBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.HelmetBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.PrimordialAnchorBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SkeletonPieceBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SmallDragonDoor;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonSpeciesTags;
import by.dragonsurvivalteam.dragonsurvival.util.CompoundTagBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DSBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK, MODID);
    // TODO :: why are these stored in a map if the map is unused
    public static final HashMap<String, Pair<DeferredHolder<Block, SkeletonPieceBlock>, DeferredHolder<Item, BlockItem>>> SKELETON_PIECES = new HashMap<>();

    // --- Dragon Doors --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Spruce Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> SPRUCE_DRAGON_DOOR = register(
            "spruce_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Acacia Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> ACACIA_DRAGON_DOOR = register(
            "acacia_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.ACACIA_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Birch Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> BIRCH_DRAGON_DOOR = register(
            "birch_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.BIRCH_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Jungle Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> JUNGLE_DRAGON_DOOR = register(
            "jungle_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Oak Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> OAK_DRAGON_DOOR = register(
            "oak_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.OAK_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Dark Oak Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> DARK_OAK_DRAGON_DOOR = register(
            "dark_oak_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Crimson Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> CRIMSON_DRAGON_DOOR = register(
            "crimson_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Warped Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> WARPED_DRAGON_DOOR = register(
            "warped_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.WARPED_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Legacy Dragon Door")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 The very first large door we added to the mod. Just for nostalgia.")
    public static final DeferredHolder<Block, DragonDoor> LEGACY_DRAGON_DOOR = register(
            "legacy_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Iron Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> IRON_DRAGON_DOOR = register(
            "iron_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion(), true)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Gothic Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> GOTHIC_DRAGON_DOOR = register("gothic_dragon_door", () -> new DragonDoor(OAK_DRAGON_DOOR.get().properties()));

    @Translation(type = Translation.Type.BLOCK, comments = "Skyrim Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> SKYRIM_DRAGON_DOOR = register("skyrim_dragon_door", () -> new DragonDoor(OAK_DRAGON_DOOR.get().properties()));

    @Translation(type = Translation.Type.BLOCK, comments = "Stone Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> STONE_DRAGON_DOOR = register("stone_dragon_door", () -> new DragonDoor(OAK_DRAGON_DOOR.get().properties()));

    @Translation(type = Translation.Type.BLOCK, comments = "Cave Dragon Door")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 A large door that only a cave dragon may open.")
    public static final DeferredHolder<Block, DragonDoor> CAVE_DRAGON_DOOR = register(
            "cave_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.BLACKSTONE.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.GILDED_BLACKSTONE)
                    .noOcclusion(), DSDragonSpeciesTags.CAVE_DRAGONS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Forest Dragon Door")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 A large door that only a forest dragon may open.")
    public static final DeferredHolder<Block, DragonDoor> FOREST_DRAGON_DOOR = register(
            "forest_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(Blocks.DARK_PRISMARINE.defaultMapColor())
                    .ignitedByLava()
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion(), DSDragonSpeciesTags.FOREST_DRAGONS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Sea Dragon Door")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 A large door that only a sea dragon may open.")
    public static final DeferredHolder<Block, DragonDoor> SEA_DRAGON_DOOR = register(
            "sea_dragon_door",
            () -> new DragonDoor(Block.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion(), DSDragonSpeciesTags.SEA_DRAGONS)
    );

    // --- Small Dragon Doors --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Small Oak Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_OAK_DRAGON_DOOR = register(
            "small_oak_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of()
                    .mapColor(Blocks.OAK_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Spruce Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_SPRUCE_DRAGON_DOOR = register(
            "small_spruce_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of()
                    .mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Acacia Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_ACACIA_DRAGON_DOOR = register(
            "small_acacia_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of()
                    .mapColor(Blocks.ACACIA_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Birch Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_BIRCH_DRAGON_DOOR = register(
            "small_birch_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of()
                    .mapColor(Blocks.BIRCH_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Jungle Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_JUNGLE_DRAGON_DOOR = register(
            "small_jungle_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of()
                    .mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Dark Oak Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_DARK_OAK_DRAGON_DOOR = register(
            "small_dark_oak_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of()
                    .mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Crimson Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_CRIMSON_DRAGON_DOOR = register(
            "small_crimson_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of()
                    .mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Warped Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_WARPED_DRAGON_DOOR = register(
            "small_warped_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of()
                    .mapColor(Blocks.WARPED_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Stone Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_STONE_DRAGON_DOOR = register(
            "small_stone_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of(/*Material.WOOD*/)
                    .mapColor(Blocks.STONE.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Gothic Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_GOTHIC_DRAGON_DOOR = register(
            "small_gothic_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of(/*Material.WOOD*/)
                    .mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Cave Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_CAVE_DRAGON_DOOR = register(
            "small_cave_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of(/*Material.STONE*/)
                    .mapColor(Blocks.BLACKSTONE.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.GILDED_BLACKSTONE)
                    .noOcclusion(), DSDragonSpeciesTags.CAVE_DRAGONS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Forest Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_FOREST_DRAGON_DOOR = register(
            "small_forest_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of(/*Material.WOOD*/)
                    .mapColor(Blocks.DARK_PRISMARINE.defaultMapColor())
                    .ignitedByLava()
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion(), DSDragonSpeciesTags.FOREST_DRAGONS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Sea Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_SEA_DRAGON_DOOR = register(
            "small_sea_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of(/*Material.STONE*/)
                    .mapColor(MapColor.COLOR_BROWN)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion(), DSDragonSpeciesTags.SEA_DRAGONS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Iron Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_IRON_DRAGON_DOOR = register(
            "small_iron_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of(/*Material.METAL*/)
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion(), true)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Skyrim Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_SKYRIM_DRAGON_DOOR = register(
            "small_skyrim_dragon_door",
            () -> new SmallDragonDoor(Block.Properties.of(/*Material.METAL*/)
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion(), true) // TODO :: shouldn't the large skyrim dragon door also be powered if this one is?
    );

    // --- Source of Magic --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Forest Source of Magic")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Forest dragons can bathe here to temporarily gain infinite mana. Damages other creatures.")
    public static final DeferredHolder<Block, SourceOfMagicBlock> FOREST_SOURCE_OF_MAGIC = REGISTRY.register(
            "forest_source_of_magic",
            () -> new SourceOfMagicBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .randomTicks().strength(3, 100)
                    .noOcclusion().lightLevel(state -> state.getValue(SourceOfMagicBlock.FILLED) ? 10 : 5),
                    DamageSources::cactus
            )
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Cave Source of Magic")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Cave dragons can bathe here to temporarily gain infinite mana. Damages other creatures.")
    public static final DeferredHolder<Block, SourceOfMagicBlock> CAVE_SOURCE_OF_MAGIC = REGISTRY.register(
            "cave_source_of_magic",
            () -> new SourceOfMagicBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3, 100)
                    .noOcclusion().lightLevel(state -> state.getValue(SourceOfMagicBlock.FILLED) ? 10 : 5),
                    DamageSources::hotFloor
            )
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Sea Source of Magic")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Sea dragons can bathe here to temporarily gain infinite mana. Damages other creatures.")
    public static final DeferredHolder<Block, SourceOfMagicBlock> SEA_SOURCE_OF_MAGIC = REGISTRY.register(
            "sea_source_of_magic",
            () -> new SourceOfMagicBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3, 100)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(SourceOfMagicBlock.FILLED) ? 10 : 5),
                    DamageSources::drown
            )
    );

    // --- Dragon Altars --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Stone Dragon Altar")
    public static final DeferredHolder<Block, Block> STONE_DRAGON_ALTAR = register(
            "stone_dragon_altar",
            () -> new DragonAltarBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Sandstone Dragon Altar")
    public static final DeferredHolder<Block, Block> SANDSTONE_DRAGON_ALTAR = register(
            "sandstone_dragon_altar",
            () -> new DragonAltarBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(0.8f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Red Sandstone Dragon Altar")
    public static final DeferredHolder<Block, Block> RED_SANDSTONE_DRAGON_ALTAR = register(
            "red_sandstone_dragon_altar",
            () -> new DragonAltarBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(0.8f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Purpur Dragon Altar")
    public static final DeferredHolder<Block, Block> PURPUR_DRAGON_ALTAR = register(
            "purpur_dragon_altar",
            () -> new DragonAltarBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Oak Dragon Altar")
    public static final DeferredHolder<Block, Block> OAK_DRAGON_ALTAR = register(
            "oak_dragon_altar",
            () -> new DragonAltarBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2f)
                    .sound(SoundType.WOOD))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Birch Dragon Altar")
    public static final DeferredHolder<Block, Block> BIRCH_DRAGON_ALTAR = register(
            "birch_dragon_altar",
            () -> new DragonAltarBlock(Block.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2f)
                    .sound(SoundType.WOOD))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Nether Brick Dragon Altar")
    public static final DeferredHolder<Block, Block> NETHER_BRICK_DRAGON_ALTAR = register(
            "nether_brick_dragon_altar",
            () -> new DragonAltarBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(0.4f)
                    .sound(SoundType.NETHER_BRICKS)
                    .requiresCorrectToolForDrops())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Mossy Dragon Altar")
    public static final DeferredHolder<Block, Block> MOSSY_DRAGON_ALTAR = register(
            "mossy_dragon_altar",
            () -> new DragonAltarBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(2f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Blackstone Dragon Altar")
    public static final DeferredHolder<Block, Block> BLACKSTONE_DRAGON_ALTAR = register(
            "blackstone_dragon_altar",
            () -> new DragonAltarBlock(Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops())
    );

    // --- Dragon Beacons --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Dragon Memory for Beacons")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 If set under any dragon beacon, you will passively receive its effect in an area centered on the beacon at no additional cost, but for reduced duration. You may still activate the beacon to receive the full duration effect.")
    public static final DeferredHolder<Block, RotatedPillarBlock> DRAGON_MEMORY_BLOCK = register(
            "dragon_memory_block",
            () -> new RotatedPillarBlock(Block.Properties.of()
                    .mapColor(MapColor.METAL)
                    .pushReaction(PushReaction.BLOCK)
                    .strength(3, 30)
                    .requiresCorrectToolForDrops())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Dragon Beacon")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Can be activated by dragons to grant special effects.")
    public static final DeferredHolder<Block, DragonBeacon> DRAGON_BEACON = register(
            "dragon_beacon",
            () -> new DragonBeacon(Block.Properties.of()
                    .mapColor(MapColor.METAL).
                    pushReaction(PushReaction.BLOCK)
                    .strength(15, 50)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .noCollission()
                    .lightLevel(value -> value.getValue(BlockStateProperties.LIT) ? 15 : 0)
            )
    );

    // --- Treasures --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Debris Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> DEBRIS_DRAGON_TREASURE = register(
            "debris_dragon_treasure",
            () -> new TreasureBlock(FastColor.ARGB32.color(255, 148, 120, 114),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BROWN)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_METAL)
                            .strength(0.5F))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Diamond Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> DIAMOND_DRAGON_TREASURE = register(
            "diamond_dragon_treasure",
            () -> new TreasureBlock(FastColor.ARGB32.color(255, 212, 255, 255),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DIAMOND)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_GEM)
                            .strength(0.5F))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Emerald Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> EMERALD_DRAGON_TREASURE = register(
            "emerald_dragon_treasure",
            () -> new TreasureBlock(FastColor.ARGB32.color(255, 57, 240, 94),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_GEM)
                            .strength(0.5F))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Copper Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> COPPER_DRAGON_TREASURE = register(
            "copper_dragon_treasure",
            () -> new TreasureBlock(FastColor.ARGB32.color(255, 255, 255, 208),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .instrument(NoteBlockInstrument.HAT)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_METAL)
                            .strength(0.5F))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Gold Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> GOLD_DRAGON_TREASURE = register(
            "gold_dragon_treasure",
            () -> new TreasureBlock(FastColor.ARGB32.color(255, 255, 255, 243),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_METAL)
                            .strength(0.5F))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Iron Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> IRON_DRAGON_TREASURE = register(
            "iron_dragon_treasure",
            () -> new TreasureBlock(FastColor.ARGB32.color(255, 211, 211, 211),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_METAL)
                            .strength(0.5F))
    );

    // --- Dragon Treasure Plates --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Dragon Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal while any dragon stands on it. It will not activate if a human steps on it.")
    public static final DeferredHolder<Block, DragonPressurePlates> DRAGON_PRESSURE_PLATE = register(
            "dragon_pressure_plate",
            () -> new DragonPressurePlates(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops(), DSDragonSpeciesTags.ALL, false)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Human Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal only while a human stands on it. Will not activate for dragons.")
    public static final DeferredHolder<Block, DragonPressurePlates> HUMAN_PRESSURE_PLATE = register(
            "human_pressure_plate",
            () -> new DragonPressurePlates(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops(), DSDragonSpeciesTags.NONE, true)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Cave Dragon Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal while a cave dragon stands on it. Can open a cave dragon door, if adjacent to it.")
    public static final DeferredHolder<Block, DragonPressurePlates> CAVE_DRAGON_PRESSURE_PLATE = register(
            "cave_dragon_pressure_plate",
            () -> new DragonPressurePlates(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops(), DSDragonSpeciesTags.CAVE_DRAGONS, false)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Forest Dragon Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal while a forest dragon stands on it. Can open a forest dragon door, if adjacent to it.")
    public static final DeferredHolder<Block, DragonPressurePlates> FOREST_DRAGON_PRESSURE_PLATE = register(
            "forest_dragon_pressure_plate",
            () -> new DragonPressurePlates(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .requiresCorrectToolForDrops(), DSDragonSpeciesTags.FOREST_DRAGONS, false)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Sea Dragon Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal while a sea dragon stands on it. Can open a sea dragon door, if adjacent to it.")
    public static final DeferredHolder<Block, DragonPressurePlates> SEA_DRAGON_PRESSURE_PLATE = register(
            "sea_dragon_pressure_plate",
            () -> new DragonPressurePlates(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops(), DSDragonSpeciesTags.SEA_DRAGONS, false)
    );

    // --- Helmets --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Gray Knight Helmet")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Poor hunter. Fortunately, you didn't know him.")
    public static final DeferredHolder<Block, HelmetBlock> GRAY_KNIGHT_HELMET = register(
            "gray_knight_helmet",
            () -> new HelmetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F)
                    .sound(SoundType.METAL))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Golden Knight Helmet")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 You surely remember that the knight wore dark armor. Where did the golden helmet come from?")
    public static final DeferredHolder<Block, HelmetBlock> GOLDEN_KNIGHT_HELMET = register(
            "golden_knight_helmet",
            () -> new HelmetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Black Knight Helmet")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 You should have used the Eye of Innos.")
    public static final DeferredHolder<Block, HelmetBlock> BLACK_KNIGHT_HELMET = register(
            "black_knight_helmet",
            () -> new HelmetBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion())
    );

    // --- Misc --- //

    private static final CompoundTag LIGHT_VAULT_TAG = CompoundTagBuilder.tag()
            .putTag("config", CompoundTagBuilder.tag()
                    .putTag("key_item", CompoundTagBuilder.tag()
                            .putInt("count", 1)
                            .putString("id", DragonSurvival.res(DSItems.LIGHT_KEY_ID).toString()).build()
                    ).putString("loot_table", DragonSurvival.res("generic/light_vault").toString()).build()
            ).build();

    private static final CompoundTag DARK_VAULT_TAG = CompoundTagBuilder.tag()
            .putTag("config", CompoundTagBuilder.tag()
                    .putTag("key_item", CompoundTagBuilder.tag()
                            .putInt("count", 1)
                            .putString("id", DragonSurvival.res(DSItems.DARK_KEY_ID).toString()).build()
                    ).putString("loot_table", DragonSurvival.res("generic/dark_vault").toString()).build()
            ).build();

    private static final CompoundTag HUNTER_VAULT_TAG = CompoundTagBuilder.tag()
            .putTag("config", CompoundTagBuilder.tag()
                    .putTag("key_item", CompoundTagBuilder.tag()
                            .putInt("count", 1)
                            .putString("id", DragonSurvival.res(DSItems.HUNTER_KEY_ID).toString()).build()
                    ).putString("loot_table", DragonSurvival.res("generic/hunter_vault").toString()).build()
            ).build();

    // Copied from "vault" entry for Blocks.java
    private static final BlockBehaviour.Properties vaultBlockProperties = BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .noOcclusion()
            .sound(SoundType.VAULT)
            .lightLevel(p_323402_ -> p_323402_.getValue(VaultBlock.STATE).lightLevel())
            .strength(50.0F)
            .isViewBlocking((a, b, c) -> false);

    @Translation(type = Translation.Type.BLOCK, comments = "Light Vault")
    public static final DeferredHolder<Block, VaultBlock> LIGHT_VAULT = REGISTRY.register(
            "light_vault",
            () -> new VaultBlock(vaultBlockProperties)
    );

    public static final DeferredHolder<Item, BlockItem> LIGHT_VAULT_ITEM = DSItems.REGISTRY.register(
            "light_vault",
            () -> new BlockItem(LIGHT_VAULT.get(), new Item.Properties()
                    .component(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(LIGHT_VAULT_TAG)))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Dark Vault")
    public static final DeferredHolder<Block, VaultBlock> DARK_VAULT = REGISTRY.register(
            "dark_vault",
            () -> new VaultBlock(vaultBlockProperties)
    );

    public static final DeferredHolder<Item, BlockItem> DARK_VAULT_ITEM = DSItems.REGISTRY.register(
            "dark_vault",
            () -> new BlockItem(DARK_VAULT.get(), new Item.Properties()
                    .component(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(DARK_VAULT_TAG)))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Hunter's Vault")
    public static final DeferredHolder<Block, VaultBlock> HUNTER_VAULT = REGISTRY.register(
            "hunter_vault",
            () -> new VaultBlock(vaultBlockProperties)
    );

    public static final DeferredHolder<Item, BlockItem> HUNTER_VAULT_ITEM = DSItems.REGISTRY.register(
            "hunter_vault",
            () -> new BlockItem(HUNTER_VAULT.get(), new Item.Properties()
                    .component(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(HUNTER_VAULT_TAG)))
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Dragon Rider Workbench")
    public static final DeferredHolder<Block, Block> DRAGON_RIDER_WORKBENCH = REGISTRY.register("dragon_rider_workbench",
            () -> new DragonRiderWorkbenchBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.TRIAL_SPAWNER)
                    .strength(1.5f)
                    .mapColor(MapColor.WOOD)
            )
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Primordial Anchor")
    public static final DeferredHolder<Block, PrimordialAnchorBlock> PRIMORDIAL_ANCHOR = register("primordial_anchor",
            () -> new PrimordialAnchorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLACK)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(-1.0F, 3600000.0F)
                    .lightLevel(state -> state.getValue(PrimordialAnchorBlock.CHARGED) ? 15 : 0)
            )
    );

    public static final DeferredHolder<Item, BlockItem> DRAGON_RIDER_WORKBENCH_ITEM = DSItems.REGISTRY.register("dragon_rider_workbench",
            () -> new BlockItem(DRAGON_RIDER_WORKBENCH.get(), new Item.Properties()) {
                @Translation(comments = "■§7 A work station for a villager who sells useful dragon enchantments. Knows the secrets to getting into the draconic vaults.")
                private static final String DRAGON_RIDER_WORKBENCH = Translation.Type.DESCRIPTION.wrap("dragon_rider_workbench");

                @Override
                public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
                    super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    pTooltipComponents.add(Component.translatable(DRAGON_RIDER_WORKBENCH));
                }
            }
    );

    private static <B extends Block> DeferredHolder<Block, B> register(final String name, final Supplier<B> supplier) {
        DeferredHolder<Block, B> holder = REGISTRY.register(name, supplier);
        DSItems.REGISTRY.register(name, () -> new BlockItem(holder.value(), new Item.Properties()));
        return holder;
    }

    static {
        for (int i = 1; i < 9; i++) { // TODO :: what does the 9 indicate
            for (SkeletonPieceBlock.Type type : SkeletonPieceBlock.Type.values()) {
                DeferredHolder<Block, SkeletonPieceBlock> block = REGISTRY.register(type.getSerializedName() + "_skin" + i,
                        () -> new SkeletonPieceBlock(type, BlockBehaviour.Properties.of()
                                .mapColor(MapColor.CLAY)
                                .strength(1.0F)
                                .sound(SoundType.BONE_BLOCK)));

                DeferredHolder<Item, BlockItem> item = DSItems.REGISTRY.register(type.getSerializedName() + "_skin" + i,
                        () -> new BlockItem(block.value(), new Item.Properties()));

                SKELETON_PIECES.put(type.getSerializedName(), new Pair<>(block, item));
            }
        }
    }
}