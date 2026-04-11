package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonAltarBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonBeacon;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonDoor;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonPressurePlates;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonRiderWorkbenchBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonSoulBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.HelmetBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.PrimordialAnchorBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SkeletonPieceBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SmallDragonDoor;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.compat.ModCheck;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonSpeciesTags;
import by.dragonsurvivalteam.dragonsurvival.util.CompoundTagBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DSBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.Blocks.createBlocks(MODID);
    // TODO :: why are these stored in a map if the map is unused
    public static final HashMap<String, Pair<DeferredHolder<Block, SkeletonPieceBlock>, DeferredHolder<Item, BlockItem>>> SKELETON_PIECES = new HashMap<>();

    // --- Dragon Doors --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Spruce Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> SPRUCE_DRAGON_DOOR = registerWithItem(
            "spruce_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Acacia Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> ACACIA_DRAGON_DOOR = registerWithItem(
            "acacia_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.ACACIA_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Birch Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> BIRCH_DRAGON_DOOR = registerWithItem(
            "birch_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.BIRCH_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Jungle Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> JUNGLE_DRAGON_DOOR = registerWithItem(
            "jungle_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Oak Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> OAK_DRAGON_DOOR = registerWithItem(
            "oak_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.OAK_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Dark Oak Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> DARK_OAK_DRAGON_DOOR = registerWithItem(
            "dark_oak_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Crimson Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> CRIMSON_DRAGON_DOOR = registerWithItem(
            "crimson_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Warped Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> WARPED_DRAGON_DOOR = registerWithItem(
            "warped_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.WARPED_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Legacy Dragon Door")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 The very first large door we added to the mod. Just for nostalgia.")
    public static final DeferredHolder<Block, DragonDoor> LEGACY_DRAGON_DOOR = registerWithItem(
            "legacy_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Iron Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> IRON_DRAGON_DOOR = registerWithItem(
            "iron_dragon_door",
            DragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Gothic Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> GOTHIC_DRAGON_DOOR = registerWithItem(
        "gothic_dragon_door",
        DragonDoor::new,
        () -> OAK_DRAGON_DOOR.get().properties()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Skyrim Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> SKYRIM_DRAGON_DOOR = registerWithItem(
        "skyrim_dragon_door",
        DragonDoor::new,
        () -> OAK_DRAGON_DOOR.get().properties()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Stone Dragon Door")
    public static final DeferredHolder<Block, DragonDoor> STONE_DRAGON_DOOR = registerWithItem(
        "stone_dragon_door",
        DragonDoor::new,
        () -> OAK_DRAGON_DOOR.get().properties()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Cave Dragon Door")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 A large door that only a cave dragon may open.")
    public static final DeferredHolder<Block, DragonDoor> CAVE_DRAGON_DOOR = registerWithItem(
            "cave_dragon_door",
            properties -> new DragonDoor(properties, DSDragonSpeciesTags.CAVE_DRAGONS),
            () -> Block.Properties.of()
                    .mapColor(Blocks.BLACKSTONE.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.GILDED_BLACKSTONE)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Forest Dragon Door")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 A large door that only a forest dragon may open.")
    public static final DeferredHolder<Block, DragonDoor> FOREST_DRAGON_DOOR = registerWithItem(
            "forest_dragon_door",
            properties -> new DragonDoor(properties, DSDragonSpeciesTags.FOREST_DRAGONS),
            () -> Block.Properties.of()
                    .mapColor(Blocks.DARK_PRISMARINE.defaultMapColor())
                    .ignitedByLava()
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Sea Dragon Door")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 A large door that only a sea dragon may open.")
    public static final DeferredHolder<Block, DragonDoor> SEA_DRAGON_DOOR = registerWithItem(
            "sea_dragon_door",
                properties -> new DragonDoor(properties, DSDragonSpeciesTags.SEA_DRAGONS),
                () -> Block.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    // --- Small Dragon Doors --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Small Oak Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_OAK_DRAGON_DOOR = registerWithItem(
            "small_oak_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.OAK_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Spruce Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_SPRUCE_DRAGON_DOOR = registerWithItem(
            "small_spruce_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.SPRUCE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Acacia Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_ACACIA_DRAGON_DOOR = registerWithItem(
            "small_acacia_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.ACACIA_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Birch Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_BIRCH_DRAGON_DOOR = registerWithItem(
            "small_birch_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.BIRCH_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Jungle Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_JUNGLE_DRAGON_DOOR = registerWithItem(
            "small_jungle_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.JUNGLE_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Dark Oak Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_DARK_OAK_DRAGON_DOOR = registerWithItem(
            "small_dark_oak_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.DARK_OAK_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Crimson Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_CRIMSON_DRAGON_DOOR = registerWithItem(
            "small_crimson_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Warped Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_WARPED_DRAGON_DOOR = registerWithItem(
            "small_warped_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of()
                    .mapColor(Blocks.WARPED_PLANKS.defaultMapColor())
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(3.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Stone Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_STONE_DRAGON_DOOR = registerWithItem(
            "small_stone_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of(/*Material.WOOD*/)
                    .mapColor(Blocks.STONE.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Gothic Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_GOTHIC_DRAGON_DOOR = registerWithItem(
            "small_gothic_dragon_door",
            SmallDragonDoor::new,
            () -> Block.Properties.of(/*Material.WOOD*/)
                    .mapColor(Blocks.CRIMSON_PLANKS.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Cave Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_CAVE_DRAGON_DOOR = registerWithItem(
            "small_cave_dragon_door",
            properties -> new SmallDragonDoor(properties, DSDragonSpeciesTags.CAVE_DRAGONS),
            () -> Block.Properties.of(/*Material.STONE*/)
                    .mapColor(Blocks.BLACKSTONE.defaultMapColor())
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.GILDED_BLACKSTONE)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Forest Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_FOREST_DRAGON_DOOR = registerWithItem(
            "small_forest_dragon_door",
            properties -> new SmallDragonDoor(properties, DSDragonSpeciesTags.FOREST_DRAGONS),
            () -> Block.Properties.of(/*Material.WOOD*/)
                    .mapColor(Blocks.DARK_PRISMARINE.defaultMapColor())
                    .ignitedByLava()
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Sea Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_SEA_DRAGON_DOOR = registerWithItem(
            "small_sea_dragon_door",
            properties -> new SmallDragonDoor(properties, DSDragonSpeciesTags.SEA_DRAGONS),
            () -> Block.Properties.of(/*Material.STONE*/)
                    .mapColor(MapColor.COLOR_BROWN)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .requiresCorrectToolForDrops()
                    .strength(7.0F)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Iron Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_IRON_DRAGON_DOOR = registerWithItem(
            "small_iron_dragon_door",
            properties -> new SmallDragonDoor(properties, true),
            () -> Block.Properties.of(/*Material.METAL*/)
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Small Skyrim Dragon Door")
    public static final DeferredHolder<Block, SmallDragonDoor> SMALL_SKYRIM_DRAGON_DOOR = registerWithItem(
            "small_skyrim_dragon_door",
            properties -> new SmallDragonDoor(properties, true), // TODO :: shouldn't the large skyrim dragon door also be powered if this one is?
            () -> Block.Properties.of(/*Material.METAL*/)
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
    );

    // --- Source of Magic --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Forest Source of Magic")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Forest dragons can bathe here to temporarily gain infinite mana. Damages other creatures.")
    public static final DeferredHolder<Block, SourceOfMagicBlock> FOREST_SOURCE_OF_MAGIC = REGISTRY.registerBlock(
            "forest_source_of_magic",
            properties -> new SourceOfMagicBlock(properties, DamageSources::cactus),
            () -> Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .randomTicks().strength(3, 100)
                    .noOcclusion().lightLevel(state -> state.getValue(SourceOfMagicBlock.FILLED) ? 10 : 5
            )
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Cave Source of Magic")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Cave dragons can bathe here to temporarily gain infinite mana. Damages other creatures.")
    public static final DeferredHolder<Block, SourceOfMagicBlock> CAVE_SOURCE_OF_MAGIC = REGISTRY.registerBlock(
            "cave_source_of_magic",
            properties -> new SourceOfMagicBlock(properties, DamageSources::hotFloor),
            () -> Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3, 100)
                    .noOcclusion().lightLevel(state -> state.getValue(SourceOfMagicBlock.FILLED) ? 10 : 5
            )
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Sea Source of Magic")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Sea dragons can bathe here to temporarily gain infinite mana. Damages other creatures.")
    public static final DeferredHolder<Block, SourceOfMagicBlock> SEA_SOURCE_OF_MAGIC = REGISTRY.registerBlock(
            "sea_source_of_magic",
            properties -> new SourceOfMagicBlock(properties, DamageSources::drown),
            () -> Block.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3, 100)
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(SourceOfMagicBlock.FILLED) ? 10 : 5
            )
    );

    // --- Dragon Altars --- //
    // TODO :: `ofFullCopy` also copies the loot table defined in `drops` (currently not used by the copied blocks)

    @Translation(type = Translation.Type.BLOCK, comments = "Stone Dragon Altar")
    public static final DeferredHolder<Block, Block> STONE_DRAGON_ALTAR = registerWithItem(
            "stone_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.STONE)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Sandstone Dragon Altar")
    public static final DeferredHolder<Block, Block> SANDSTONE_DRAGON_ALTAR = registerWithItem(
            "sandstone_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.SANDSTONE)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Red Sandstone Dragon Altar")
    public static final DeferredHolder<Block, Block> RED_SANDSTONE_DRAGON_ALTAR = registerWithItem(
            "red_sandstone_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.RED_SANDSTONE)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Purpur Dragon Altar")
    public static final DeferredHolder<Block, Block> PURPUR_DRAGON_ALTAR = registerWithItem(
            "purpur_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.PURPUR_BLOCK)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Oak Dragon Altar")
    public static final DeferredHolder<Block, Block> OAK_DRAGON_ALTAR = registerWithItem(
            "oak_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.OAK_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Birch Dragon Altar")
    public static final DeferredHolder<Block, Block> BIRCH_DRAGON_ALTAR = registerWithItem(
            "birch_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.BIRCH_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Nether Brick Dragon Altar")
    public static final DeferredHolder<Block, Block> NETHER_BRICK_DRAGON_ALTAR = registerWithItem(
            "nether_brick_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.NETHER_BRICKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Mossy Dragon Altar")
    public static final DeferredHolder<Block, Block> MOSSY_DRAGON_ALTAR = registerWithItem(
            "mossy_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.MOSSY_COBBLESTONE)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Blackstone Dragon Altar")
    public static final DeferredHolder<Block, Block> BLACKSTONE_DRAGON_ALTAR = registerWithItem(
            "blackstone_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.BLACKSTONE)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Pale Oak Dragon Altar") // TODO :: unused
    public static final DeferredHolder<Block, Block> PALE_OAK_DRAGON_ALTAR = registerWithItem(
            "pale_oak_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.OAK_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Bone Dragon Altar")
    public static final DeferredHolder<Block, Block> BONE_DRAGON_ALTAR = registerWithItem(
            "bone_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.BONE_BLOCK)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Quartz Dragon Altar")
    public static final DeferredHolder<Block, Block> QUARTZ_DRAGON_ALTAR = registerWithItem(
            "quartz_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.QUARTZ_BLOCK)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Ice Dragon Altar")
    public static final DeferredHolder<Block, Block> ICE_DRAGON_ALTAR = registerWithItem(
            "ice_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.PACKED_ICE)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Netherrack Dragon Altar")
    public static final DeferredHolder<Block, Block> NETHERRACK_DRAGON_ALTAR = registerWithItem(
            "netherrack_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.NETHERRACK)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Obsidian Dragon Altar")
    public static final DeferredHolder<Block, Block> OBSIDIAN_DRAGON_ALTAR = registerWithItem(
            "obsidian_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.OBSIDIAN)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Amethyst Dragon Altar")
    public static final DeferredHolder<Block, Block> AMETHYST_DRAGON_ALTAR = registerWithItem(
            "amethyst_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Mudbrick Dragon Altar")
    public static final DeferredHolder<Block, Block> MUDBRICK_DRAGON_ALTAR = registerWithItem(
            "mudbrick_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.PACKED_MUD)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Prismarine Dragon Altar")
    public static final DeferredHolder<Block, Block> PRISMARINE_DRAGON_ALTAR = registerWithItem(
            "prismarine_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.PRISMARINE_BRICKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Red Nether Brick Dragon Altar")
    public static final DeferredHolder<Block, Block> RED_NETHER_BRICK_DRAGON_ALTAR = registerWithItem(
            "red_nether_brick_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.RED_NETHER_BRICKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Endstone Dragon Altar")
    public static final DeferredHolder<Block, Block> ENDSTONE_DRAGON_ALTAR = registerWithItem(
            "endstone_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.END_STONE)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Deepslate Dragon Altar")
    public static final DeferredHolder<Block, Block> DEEPSLATE_DRAGON_ALTAR = registerWithItem(
            "deepslate_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.COBBLED_DEEPSLATE)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Tuff Dragon Altar")
    public static final DeferredHolder<Block, Block> TUFF_DRAGON_ALTAR = registerWithItem(
            "tuff_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.TUFF)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Crimson Dragon Altar")
    public static final DeferredHolder<Block, Block> CRIMSON_DRAGON_ALTAR = registerWithItem(
            "crimson_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Warped Dragon Altar")
    public static final DeferredHolder<Block, Block> WARPED_DRAGON_ALTAR = registerWithItem(
            "warped_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.WARPED_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Mangrove Dragon Altar")
    public static final DeferredHolder<Block, Block> MANGROVE_DRAGON_ALTAR = registerWithItem(
            "mangrove_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.MANGROVE_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Bamboo Dragon Altar")
    public static final DeferredHolder<Block, Block> BAMBOO_DRAGON_ALTAR = registerWithItem(
            "bamboo_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).mapColor(MapColor.COLOR_YELLOW)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Cherry Dragon Altar")
    public static final DeferredHolder<Block, Block> CHERRY_DRAGON_ALTAR = registerWithItem(
            "cherry_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.CHERRY_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Acacia Dragon Altar")
    public static final DeferredHolder<Block, Block> ACACIA_DRAGON_ALTAR = registerWithItem(
            "acacia_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.ACACIA_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Dark Oak Dragon Altar")
    public static final DeferredHolder<Block, Block> DARK_OAK_DRAGON_ALTAR = registerWithItem(
            "dark_oak_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.DARK_OAK_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Jungle Dragon Altar")
    public static final DeferredHolder<Block, Block> JUNGLE_DRAGON_ALTAR = registerWithItem(
            "jungle_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.JUNGLE_PLANKS)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Spruce Dragon Altar")
    public static final DeferredHolder<Block, Block> SPRUCE_DRAGON_ALTAR = registerWithItem(
            "spruce_dragon_altar",
            DragonAltarBlock::new,
            () -> Block.Properties.ofFullCopy(Blocks.SPRUCE_PLANKS)
    );

    // --- Dragon Beacons --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Dragon Memory for Beacons")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 If set under any dragon beacon, you will passively receive its effect in an area centered on the beacon at no additional cost, but for reduced duration. You may still activate the beacon to receive the full duration effect.")
    public static final DeferredHolder<Block, RotatedPillarBlock> DRAGON_MEMORY_BLOCK = registerWithItem(
            "dragon_memory_block",
            RotatedPillarBlock::new,
            () -> Block.Properties.of()
                    .mapColor(MapColor.METAL)
                    .pushReaction(PushReaction.BLOCK)
                    .strength(3, 30)
                    .requiresCorrectToolForDrops()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Dragon Beacon")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Can be activated by dragons to grant special effects.")
    public static final DeferredHolder<Block, DragonBeacon> DRAGON_BEACON = registerWithItem(
            "dragon_beacon",
            DragonBeacon::new,
            () -> Block.Properties.of()
                    .mapColor(MapColor.METAL)
                    .pushReaction(PushReaction.BLOCK)
                    .strength(15, 50)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .noCollision()
                    .lightLevel(value -> value.getValue(BlockStateProperties.LIT) ? 15 : 0)
    );

    // --- Dragon Soul --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Dragon Soul")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Displays the soul of a dragon.")
    public static final DeferredHolder<Block, DragonSoulBlock> DRAGON_SOUL = REGISTRY.registerBlock(
            "dragon_soul",
            DragonSoulBlock::new,
            () -> Block.Properties.of()
                    .mapColor(MapColor.METAL)
                    .pushReaction(PushReaction.IGNORE)
                    .strength(1, 1200)
                    .noOcclusion()
                    .noCollision()
    );

    // --- Treasures --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Debris Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> DEBRIS_DRAGON_TREASURE = registerWithItem(
            "debris_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 148, 120, 114), properties),
            () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BROWN)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_METAL)
                            .strength(0.5F)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Diamond Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> DIAMOND_DRAGON_TREASURE = registerWithItem(
            "diamond_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 212, 255, 255), properties),
            () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.DIAMOND)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_GEM)
                            .strength(0.5F)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Emerald Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> EMERALD_DRAGON_TREASURE = registerWithItem(
            "emerald_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 57, 240, 94), properties),
            () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_GREEN)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_GEM)
                            .strength(0.5F)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Copper Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> COPPER_DRAGON_TREASURE = registerWithItem(
            "copper_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 255, 255, 208), properties),
            () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_ORANGE)
                            .instrument(NoteBlockInstrument.HAT)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_METAL)
                            .strength(0.5F)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Gold Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> GOLD_DRAGON_TREASURE = registerWithItem(
            "gold_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 255, 255, 243), properties),
            () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.GOLD)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_METAL)
                            .strength(0.5F)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Iron Dragon Treasure")
    public static final DeferredHolder<Block, TreasureBlock> IRON_DRAGON_TREASURE = registerWithItem(
            "iron_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 211, 211, 211), properties),
            () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .noOcclusion()
                            .sound(DSSounds.TREASURE_METAL)
                            .strength(0.5F)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Chocolate Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> CHOCOLATE_DRAGON_TREASURE = registerWithItemModCheck(
            "chocolate_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(0, 0, 0, 0), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT)
                    .noOcclusion()
                    .sound(SoundType.PACKED_MUD)
                    .strength(0.5F),
            ModCheck.CREATE
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Bee Honey Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> BEE_HONEY_TREASURE = registerWithItemModCheck(
            "bee_honey_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 250, 201, 48), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .noOcclusion()
                    .sound(SoundType.HONEY_BLOCK)
                    .strength(0.5F),
            ModCheck.BEE_ADDON
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Ruby Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> RUBY_DRAGON_TREASURE = registerWithItemModCheck(
            "ruby_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 230, 29, 29), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Carnelian Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> CARNELIAN_DRAGON_TREASURE = registerWithItemModCheck(
            "carnelian_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 224, 71, 29), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.NETHER)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Topaz Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> TOPAZ_DRAGON_TREASURE = registerWithItemModCheck(
            "topaz_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 230, 113, 29), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Citrine Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> CITRINE_DRAGON_TREASURE = registerWithItemModCheck(
            "citrine_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 199, 139, 3), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Heliodor Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> HELIODOR_DRAGON_TREASURE = registerWithItemModCheck(
            "heliodor_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 230, 197, 29), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Moldavite Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> MOLDAVITE_DRAGON_TREASURE = registerWithItemModCheck(
            "moldavite_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 41, 219, 24), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Peridot Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> PERIDOT_DRAGON_TREASURE = registerWithItemModCheck(
            "peridot_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 41, 219, 24), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GRASS)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Turquoise Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> TURQUOISE_DRAGON_TREASURE = registerWithItemModCheck(
            "turquoise_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 61, 244, 189), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WARPED_WART_BLOCK)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Kyanite Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> KYANITE_DRAGON_TREASURE = registerWithItemModCheck(
            "kyanite_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 65, 196, 243), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WARPED_NYLIUM)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Sapphire Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> SAPPHIRE_DRAGON_TREASURE = registerWithItemModCheck(
            "sapphire_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 29, 105, 229), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Iolite Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> IOLITE_DRAGON_TREASURE = registerWithItemModCheck(
            "iolite_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 117, 67, 245), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_BLUE)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Alexandrite Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> ALEXANDRITE_DRAGON_TREASURE = registerWithItemModCheck(
            "alexandrite_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 171, 55, 229), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_PURPLE)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Ammolite Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> AMMOLITE_DRAGON_TREASURE = registerWithItemModCheck(
            "ammolite_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 219, 43, 255), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WARPED_HYPHAE)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Rose Quartz Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> ROSE_QUARTZ_DRAGON_TREASURE = registerWithItemModCheck(
            "rose_quartz_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 255, 78, 171), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_WHITE)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Black Diamond Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> BLACK_DIAMOND_DRAGON_TREASURE = registerWithItemModCheck(
            "black_diamond_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 95, 82, 76), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GRAY)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    @Translation(type = Translation.Type.BLOCK, comments = "White Diamond Dragon Treasure")
    public static final @Nullable DeferredHolder<Block, TreasureBlock> WHITE_DIAMOND_DRAGON_TREASURE = registerWithItemModCheck(
            "white_diamond_dragon_treasure",
            properties -> new TreasureBlock(ARGB.color(255, 213, 193, 210), properties, ModCheck.SILENTGEMS),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOL)
                    .noOcclusion()
                    .sound(DSSounds.TREASURE_GEM)
                    .strength(0.5F),
            ModCheck.SILENTGEMS
    );

    // --- Dragon Treasure Plates --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Dragon Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal while any dragon stands on it. It will not activate if a human steps on it.")
    public static final DeferredHolder<Block, DragonPressurePlates> DRAGON_PRESSURE_PLATE = registerWithItem(
            "dragon_pressure_plate",
            properties -> new DragonPressurePlates(properties, DSDragonSpeciesTags.ALL, false),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Human Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal only while a human stands on it. Will not activate for dragons.")
    public static final DeferredHolder<Block, DragonPressurePlates> HUMAN_PRESSURE_PLATE = registerWithItem(
            "human_pressure_plate",
            properties -> new DragonPressurePlates(properties, DSDragonSpeciesTags.NONE, true),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Cave Dragon Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal while a cave dragon stands on it. Can open a cave dragon door, if adjacent to it.")
    public static final DeferredHolder<Block, DragonPressurePlates> CAVE_DRAGON_PRESSURE_PLATE = registerWithItem(
            "cave_dragon_pressure_plate",
            properties -> new DragonPressurePlates(properties, DSDragonSpeciesTags.CAVE_DRAGONS, false),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Forest Dragon Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal while a forest dragon stands on it. Can open a forest dragon door, if adjacent to it.")
    public static final DeferredHolder<Block, DragonPressurePlates> FOREST_DRAGON_PRESSURE_PLATE = registerWithItem(
            "forest_dragon_pressure_plate",
            properties -> new DragonPressurePlates(properties, DSDragonSpeciesTags.FOREST_DRAGONS, false),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .ignitedByLava()
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.0F)
                    .sound(SoundType.WOOD)
                    .requiresCorrectToolForDrops()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Sea Dragon Pressure Plate")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Produces a redstone signal while a sea dragon stands on it. Can open a sea dragon door, if adjacent to it.")
    public static final DeferredHolder<Block, DragonPressurePlates> SEA_DRAGON_PRESSURE_PLATE = registerWithItem(
            "sea_dragon_pressure_plate",
            properties -> new DragonPressurePlates(properties, DSDragonSpeciesTags.SEA_DRAGONS, false),
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(1.5f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops()
    );

    // --- Helmets --- //

    @Translation(type = Translation.Type.BLOCK, comments = "Gray Knight Helmet")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 Poor hunter. Fortunately, you didn't know him.")
    public static final DeferredHolder<Block, HelmetBlock> GRAY_KNIGHT_HELMET = registerWithItem(
            "gray_knight_helmet",
            HelmetBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F)
                    .sound(SoundType.METAL)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Golden Knight Helmet")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 You surely remember that the knight wore dark armor. Where did the golden helmet come from?")
    public static final DeferredHolder<Block, HelmetBlock> GOLDEN_KNIGHT_HELMET = registerWithItem(
            "golden_knight_helmet",
            HelmetBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Black Knight Helmet")
    @Translation(type = Translation.Type.DESCRIPTION_ADDITION, comments = "■§7 You should have used the Eye of Innos.")
    public static final DeferredHolder<Block, HelmetBlock> BLACK_KNIGHT_HELMET = registerWithItem(
            "black_knight_helmet",
            HelmetBlock::new,
            () -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
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
    public static final DeferredHolder<Block, VaultBlock> LIGHT_VAULT = REGISTRY.registerBlock(
            "light_vault",
            VaultBlock::new,
            () -> vaultBlockProperties
    );

    public static final DeferredHolder<Item, BlockItem> LIGHT_VAULT_ITEM = DSItems.REGISTRY.registerItem(
            "light_vault",
            properties -> new BlockItem(LIGHT_VAULT.get(), properties),
            () -> new Item.Properties()
                .component(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BlockEntityType.VAULT, LIGHT_VAULT_TAG))
                .useBlockDescriptionPrefix()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Dark Vault")
    public static final DeferredHolder<Block, VaultBlock> DARK_VAULT = REGISTRY.registerBlock(
            "dark_vault",
            VaultBlock::new,
            () -> vaultBlockProperties
    );

    public static final DeferredHolder<Item, BlockItem> DARK_VAULT_ITEM = DSItems.REGISTRY.registerItem(
            "dark_vault",
            properties -> new BlockItem(DARK_VAULT.get(), properties),
            () -> new Item.Properties()
                .component(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BlockEntityType.VAULT, DARK_VAULT_TAG))
                .useBlockDescriptionPrefix()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Hunter's Vault")
    public static final DeferredHolder<Block, VaultBlock> HUNTER_VAULT = REGISTRY.registerBlock(
            "hunter_vault",
            VaultBlock::new,
            () -> vaultBlockProperties
    );

    public static final DeferredHolder<Item, BlockItem> HUNTER_VAULT_ITEM = DSItems.REGISTRY.registerItem(
            "hunter_vault",
            properties -> new BlockItem(HUNTER_VAULT.get(), properties),
            () -> new Item.Properties()
                .component(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(BlockEntityType.VAULT, HUNTER_VAULT_TAG))
                .useBlockDescriptionPrefix()
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Primordial Anchor")
    public static final DeferredHolder<Block, PrimordialAnchorBlock> PRIMORDIAL_ANCHOR = registerWithItem("primordial_anchor",
            PrimordialAnchorBlock::new,
            () -> BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_BLACK)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .strength(-1.0F, 3600000.0F)
                .lightLevel(state -> state.getValue(PrimordialAnchorBlock.CHARGED) ? 15 : 0)
    );

    @Translation(type = Translation.Type.BLOCK, comments = "Dragon Rider Workbench")
    public static final DeferredHolder<Block, Block> DRAGON_RIDER_WORKBENCH = REGISTRY.registerBlock("dragon_rider_workbench",
        DragonRiderWorkbenchBlock::new,
        () -> BlockBehaviour.Properties.of()
            .sound(SoundType.TRIAL_SPAWNER)
            .strength(1.5f)
            .mapColor(MapColor.WOOD)
    );

    public static final DeferredHolder<Item, BlockItem> DRAGON_RIDER_WORKBENCH_ITEM = DSItems.REGISTRY.registerItem("dragon_rider_workbench",
            properties -> new BlockItem(DRAGON_RIDER_WORKBENCH.get(), properties) {
                @Translation(comments = "■§7 A work station for a villager who sells useful dragon enchantments. Knows the secrets to getting into the draconic vaults.")
                private static final String DRAGON_RIDER_WORKBENCH = Translation.Type.DESCRIPTION.wrap("dragon_rider_workbench");

                @Override
                public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
                    super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
                    tooltipAdder.accept(Component.translatable(DRAGON_RIDER_WORKBENCH));
                }
            },
            () -> new Item.Properties().useBlockDescriptionPrefix()
    );

    private static <B extends Block> DeferredHolder<Block, B> registerWithItem(final String name, Function<BlockBehaviour.Properties, ? extends B> func, Supplier<BlockBehaviour.Properties> properties) {
        DeferredHolder<Block, B> holder = REGISTRY.registerBlock(name, func, properties);
        DSItems.REGISTRY.registerItem(name, itemProperties -> new BlockItem(holder.value(), itemProperties), () -> new Item.Properties().useBlockDescriptionPrefix());
        return holder;
    }

    // TODO :: set the path of the resource location to <compat_id>/<usual_path>
    //         (since that can be checked but the block itself cannot be accessed too early)
    //         this avoids the need of using 'ModCheck' twice and having this separate entry point
    //         (will remove existing blocks, so potentially only do on a major update)
    private static <B extends Block> @Nullable DeferredHolder<Block, B> registerWithItemModCheck(final String name, Function<BlockBehaviour.Properties, ? extends B> func, Supplier<BlockBehaviour.Properties> properties, final String modID) {
        if (ModCheck.isModLoaded(modID) || DatagenModLoader.isRunningDataGen()) {
            return registerWithItem(name, func, properties);
        }

        return null;
    }

    static {
        for (int i = 1; i < 9; i++) { // 8 total types, one for each color
            for (SkeletonPieceBlock.Type type : SkeletonPieceBlock.Type.values()) {
                DeferredHolder<Block, SkeletonPieceBlock> block = REGISTRY.registerBlock(type.getSerializedName() + "_skin" + i,
                        properties -> new SkeletonPieceBlock(type, properties),
                        () -> BlockBehaviour.Properties.of()
                                .mapColor(MapColor.CLAY)
                                .strength(1.0F)
                                .sound(SoundType.BONE_BLOCK));

                DeferredHolder<Item, BlockItem> item = DSItems.REGISTRY.registerItem(type.getSerializedName() + "_skin" + i,
                        properties -> new BlockItem(block.value(), properties),
                        () -> new Item.Properties().useBlockDescriptionPrefix());

                SKELETON_PIECES.put(type.getSerializedName(), new Pair<>(block, item));
            }
        }
    }
}