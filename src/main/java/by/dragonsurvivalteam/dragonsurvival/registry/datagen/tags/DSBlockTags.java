package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonAltarBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SkeletonPieceBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSBlockTags extends BlockTagsProvider {
    @Translation(comments = "Enables Hunter Effect")
    public static final TagKey<Block> ENABLES_HUNTER_EFFECT = key("enables_hunter_effect");
    @Translation(comments = "Destructible by Large Dragons")
    public static final TagKey<Block> LARGE_DRAGON_DESTRUCTIBLE = key("large_dragon_destructible");
    @Translation(comments = "Not replaced by the dragon end platform")
    public static final TagKey<Block> END_PLATFORM_NON_REPLACEABLE = key("end_platform_non_replaceable");

    @Translation(comments = "Speeds up Cave Dragons")
    public static final TagKey<Block> SPEEDS_UP_CAVE_DRAGON = key("speeds_up_cave_dragon");
    @Translation(comments = "Speeds up Sea Dragons")
    public static final TagKey<Block> SPEEDS_UP_SEA_DRAGON = key("speeds_up_sea_dragon");
    @Translation(comments = "Speeds up Forest Dragons")
    public static final TagKey<Block> SPEEDS_UP_FOREST_DRAGON = key("speeds_up_forest_dragon");

    @Translation(comments = "Is Warm")
    public static final TagKey<Block> IS_WARM = key("warm_blocks");
    @Translation(comments = "Is Grassy")
    public static final TagKey<Block> IS_GRASSY = key("is_grassy");
    @Translation(comments = "Is Wet")
    public static final TagKey<Block> IS_WET = key("is_wet");

    @Translation(comments = "General Ores")
    public static final TagKey<Block> GENERAL_ORES = key("general_ores");

    @Translation(comments = "Dragon Altars")
    public static final TagKey<Block> DRAGON_ALTARS = key("dragon_altars");
    @Translation(comments = "Dragon Treasures")
    public static final TagKey<Block> DRAGON_TREASURES = key("dragon_treasures");

    @Translation(comments = "Wooden Dragon Doors")
    public static final TagKey<Block> WOODEN_DRAGON_DOORS = key("wooden_dragon_doors");
    @Translation(comments = "Small Wooden Dragon Doors")
    public static final TagKey<Block> SMALL_WOODEN_DRAGON_DOORS = key("small_wooden_dragon_doors");

    @Translation(comments = "Dragon Dust/Bone Dropping Blocks")
    public static final TagKey<Block> DRAGON_ORE_DROP = key("dragon_ore_drop");

    public DSBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, DragonSurvival.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        addToVanillaTags();
        addToDragonSpeedUpTags();
        addToTypeTags();

        DSBlocks.REGISTRY.getEntries().forEach(holder -> {
            Block block = holder.value();

            switch (block) {
                case DragonAltarBlock ignored -> tag(DRAGON_ALTARS).add(block);
                case TreasureBlock ignored -> tag(DRAGON_TREASURES).add(block);
                case SkeletonPieceBlock ignored -> tag(key("dragon_bones")).add(block);
                default -> { /* Nothing to do */ }
            }
        });

        // Destructible blocks for very large dragon sizes
        tag(LARGE_DRAGON_DESTRUCTIBLE)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.REPLACEABLE); // Potentially has no entries?

        tag(END_PLATFORM_NON_REPLACEABLE).add(Blocks.SNOW);

        tag(SMALL_WOODEN_DRAGON_DOORS)
                .add(DSBlocks.SMALL_OAK_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_SPRUCE_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_ACACIA_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_BIRCH_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_JUNGLE_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_DARK_OAK_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_WARPED_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_CRIMSON_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_FOREST_DRAGON_DOOR.value());

        tag(WOODEN_DRAGON_DOORS)
                .add(DSBlocks.OAK_DRAGON_DOOR.value())
                .add(DSBlocks.SPRUCE_DRAGON_DOOR.value())
                .add(DSBlocks.ACACIA_DRAGON_DOOR.value())
                .add(DSBlocks.BIRCH_DRAGON_DOOR.value())
                .add(DSBlocks.JUNGLE_DRAGON_DOOR.value())
                .add(DSBlocks.DARK_OAK_DRAGON_DOOR.value())
                .add(DSBlocks.WARPED_DRAGON_DOOR.value())
                .add(DSBlocks.CRIMSON_DRAGON_DOOR.value())
                .add(DSBlocks.FOREST_DRAGON_DOOR.value())
                .add(DSBlocks.LEGACY_DRAGON_DOOR.value());

        // Blocks which grant hunter stacks when standing on them with the hunter effect
        tag(ENABLES_HUNTER_EFFECT)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.SAPLINGS)
                .add(Blocks.WARPED_NYLIUM)
                .add(Blocks.CRIMSON_NYLIUM)
                .add(Blocks.GRASS_BLOCK)
                .add(Blocks.FERN)
                .add(Blocks.LARGE_FERN)
                .add(Blocks.DEAD_BUSH)
                .add(Blocks.SWEET_BERRY_BUSH)
                .add(Blocks.TALL_GRASS)
                .add(Blocks.GLOW_LICHEN)
                .add(Blocks.CRIMSON_ROOTS)
                .add(Blocks.WARPED_ROOTS)
                .add(Blocks.NETHER_SPROUTS)
                .add(Blocks.BIG_DRIPLEAF)
                .add(Blocks.SMALL_DRIPLEAF);

        tag(DRAGON_ORE_DROP)
                .addTag(Tags.Blocks.ORES_QUARTZ)
                .addTag(Tags.Blocks.ORES_COAL)
                .addTag(Tags.Blocks.ORES_REDSTONE)
                .addTag(Tags.Blocks.ORES_LAPIS)
                .add(Blocks.NETHER_GOLD_ORE)
                .addTag(Tags.Blocks.ORES_DIAMOND)
                .addTag(Tags.Blocks.ORES_EMERALD);

        tag(GENERAL_ORES)
                .addTag(Tags.Blocks.ORES)
                .add(Blocks.GILDED_BLACKSTONE)
                // Nether
                .remove(Tags.Blocks.ORES_QUARTZ)
                .remove(Tags.Blocks.ORES_NETHERITE_SCRAP)
                // Overworld
                .remove(Tags.Blocks.ORES_COAL)
                .remove(Tags.Blocks.ORES_COPPER)
                .remove(Tags.Blocks.ORES_IRON)
                .remove(Tags.Blocks.ORES_GOLD)
                .remove(Tags.Blocks.ORES_REDSTONE)
                .remove(Tags.Blocks.ORES_LAPIS)
                .remove(Tags.Blocks.ORES_EMERALD)
                .remove(Tags.Blocks.ORES_DIAMOND);
    }

    private void addToTypeTags() {
        tag(IS_WARM)
                .addTag(BlockTags.FIRE)
                .add(Blocks.MAGMA_BLOCK)
                .add(DSBlocks.CAVE_SOURCE_OF_MAGIC.value())
                .addOptionalTag(DragonSurvival.location("immersive_weathering", "charred_blocks"))
                .addOptionalTag(DragonSurvival.location("regions_unexplored", "ash"))
                .addOptional(DragonSurvival.location("netherdepthsupgrade", "wet_lava_sponge"))
                .addOptional(DragonSurvival.location("regions_unexplored", "brimwood_log_magma"));

        tag(IS_GRASSY)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.LEAVES)
                .add(Blocks.BROWN_MUSHROOM_BLOCK)
                .add(Blocks.RED_MUSHROOM_BLOCK)
                .add(Blocks.SWEET_BERRY_BUSH)
                .add(Blocks.BROWN_MUSHROOM)
                .add(Blocks.RED_MUSHROOM)
                .add(Blocks.MOSS_CARPET)
                .add(Blocks.GRASS_BLOCK)
                .add(Blocks.MOSS_BLOCK)
                .add(Blocks.MYCELIUM)
                .add(Blocks.LILY_PAD)
                .add(DSBlocks.FOREST_SOURCE_OF_MAGIC.value())
                .addOptional(DragonSurvival.location("regions_unexplored", "spanish_moss"))
                .addOptional(DragonSurvival.location("regions_unexplored", "mycotoxic_mushrooms"))
                .addOptional(DragonSurvival.location("regions_unexplored", "alpha_grass_block"))
                .addOptional(DragonSurvival.location("regions_unexplored", "chalk_grass_block"))
                .addOptional(DragonSurvival.location("regions_unexplored", "peat_grass_block"))
                .addOptional(DragonSurvival.location("regions_unexplored", "silt_grass_block"))
                .addOptional(DragonSurvival.location("regions_unexplored", "argillite_grass_block"))
                .addOptional(DragonSurvival.location("regions_unexplored", "stone_grass_block"))
                .addOptional(DragonSurvival.location("regions_unexplored", "deepslate_grass_block"))
                .addOptional(DragonSurvival.location("regions_unexplored", "rooted_grass_block"))
                .addOptional(DragonSurvival.location("phantasm", "vivid_nihilium_grass"))
                .addOptional(DragonSurvival.location("vinery", "grass_slab"));

        tag(IS_WET)
                .addTag(BlockTags.SNOW)
                .addTag(BlockTags.ICE)
                .add(Blocks.WATER_CAULDRON)
                .add(Blocks.WET_SPONGE)
                .add(Blocks.MUDDY_MANGROVE_ROOTS)
                .add(Blocks.MUD)
                .add(DSBlocks.SEA_SOURCE_OF_MAGIC.value())
                .addOptional(DragonSurvival.location("immersive_weathering", "thin_ice"))
                .addOptional(DragonSurvival.location("immersive_weathering", "cryosol"))
                .addOptional(DragonSurvival.location("immersive_weathering", "permafrost"))
                .addOptional(DragonSurvival.location("immersive_weathering", "frosty_grass"))
                .addOptional(DragonSurvival.location("immersive_weathering", "frosty_fern"))
                .addOptional(DragonSurvival.location("immersive_weathering", "icicle"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_stone"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_stone_slab"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_stone_wall"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_stone_stairs"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_stone_bricks"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_stone_brick_slab"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_stone_brick_wall"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_stone_brick_stairs"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_cobblestone"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_cobblestone_slab"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_cobblestone_wall"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_cobblestone_stairs"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snow_bricks"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snow_brick_slab"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snow_brick_wall"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snow_brick_stairs"))
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_chiseled_stone_bricks"))
                .addOptional(DragonSurvival.location("regions_unexplored", "plains_mud"))
                .addOptional(DragonSurvival.location("regions_unexplored", "silt_mud"))
                .addOptional(DragonSurvival.location("regions_unexplored", "peat_mud"))
                .addOptional(DragonSurvival.location("regions_unexplored", "forest_mud"));
    }

    /** These blocks grant a speed bonus when the corresponding dragon species stands on them */
    private void addToDragonSpeedUpTags() {
        tag(SPEEDS_UP_CAVE_DRAGON)
                .addTag(BlockTags.BASE_STONE_OVERWORLD)
                .addTag(BlockTags.BEACON_BASE_BLOCKS)
                .addTag(BlockTags.BASE_STONE_NETHER)
                .addTag(BlockTags.STONE_BRICKS)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .addTag(Tags.Blocks.COBBLESTONES)
                .addTag(Tags.Blocks.STONES)
                .addTag(Tags.Blocks.ORES)
                .addTag(IS_WARM);

        tag(SPEEDS_UP_SEA_DRAGON)
                .addTag(BlockTags.CORAL_BLOCKS)
                .addTag(BlockTags.IMPERMEABLE) // Glass
                .addTag(BlockTags.SAND)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .addTag(Tags.Blocks.SANDS)
                .addTag(IS_WET)
                .add(Blocks.DIRT_PATH)
                .add(Blocks.SAND)
                .add(Blocks.MUD);

        tag(SPEEDS_UP_FOREST_DRAGON)
                .addTag(BlockTags.WOODEN_SLABS)
                .addTag(BlockTags.PLANKS)
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.DIRT)
                .addTag(IS_GRASSY)
                .add(Blocks.GRASS_BLOCK);
    }

    private void addToVanillaTags() {
        tag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(WOODEN_DRAGON_DOORS)
                .addTag(SMALL_WOODEN_DRAGON_DOORS)
                .add(DSBlocks.FOREST_DRAGON_PRESSURE_PLATE.value());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(DSBlocks.STONE_DRAGON_ALTAR.value())
                .add(DSBlocks.SANDSTONE_DRAGON_ALTAR.value())
                .add(DSBlocks.RED_SANDSTONE_DRAGON_ALTAR.value())
                .add(DSBlocks.PURPUR_DRAGON_ALTAR.value())
                .add(DSBlocks.NETHER_BRICK_DRAGON_ALTAR.value())
                .add(DSBlocks.MOSSY_DRAGON_ALTAR.value())
                .add(DSBlocks.BLACKSTONE_DRAGON_ALTAR.value())
                .add(DSBlocks.CAVE_DRAGON_DOOR.value())
                .add(DSBlocks.SEA_DRAGON_DOOR.value())
                .add(DSBlocks.IRON_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_STONE_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_GOTHIC_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_CAVE_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_SEA_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_IRON_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_SKYRIM_DRAGON_DOOR.value())
                .add(DSBlocks.SKYRIM_DRAGON_DOOR.value())
                .add(DSBlocks.GOTHIC_DRAGON_DOOR.value())
                .add(DSBlocks.STONE_DRAGON_DOOR.value())
                .add(DSBlocks.CAVE_SOURCE_OF_MAGIC.value())
                .add(DSBlocks.FOREST_SOURCE_OF_MAGIC.value())
                .add(DSBlocks.SEA_SOURCE_OF_MAGIC.value())
                .add(DSBlocks.DEBRIS_DRAGON_TREASURE.value())
                .add(DSBlocks.DIAMOND_DRAGON_TREASURE.value())
                .add(DSBlocks.EMERALD_DRAGON_TREASURE.value())
                .add(DSBlocks.COPPER_DRAGON_TREASURE.value())
                .add(DSBlocks.GOLD_DRAGON_TREASURE.value())
                .add(DSBlocks.IRON_DRAGON_TREASURE.value())
                .add(DSBlocks.GRAY_KNIGHT_HELMET.value())
                .add(DSBlocks.GOLDEN_KNIGHT_HELMET.value())
                .add(DSBlocks.BLACK_KNIGHT_HELMET.value())
                .add(DSBlocks.DRAGON_BEACON.value())
                .add(DSBlocks.DRAGON_MEMORY_BLOCK.value())
                .add(DSBlocks.DRAGON_PRESSURE_PLATE.value())
                .add(DSBlocks.HUMAN_PRESSURE_PLATE.value())
                .add(DSBlocks.SEA_DRAGON_PRESSURE_PLATE.value())
                .add(DSBlocks.CAVE_DRAGON_PRESSURE_PLATE.value());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(DSBlocks.GOLD_DRAGON_TREASURE.value())
                .add(DSBlocks.EMERALD_DRAGON_TREASURE.value())
                .add(DSBlocks.DIAMOND_DRAGON_TREASURE.value())
                .add(DSBlocks.DEBRIS_DRAGON_TREASURE.value())
                .add(DSBlocks.IRON_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_IRON_DRAGON_DOOR.value())
                .add(DSBlocks.SKYRIM_DRAGON_DOOR.value())
                .add(DSBlocks.SMALL_SKYRIM_DRAGON_DOOR.value())
                .add(DSBlocks.COPPER_DRAGON_TREASURE.value())
                .add(DSBlocks.IRON_DRAGON_TREASURE.value())
                .add(DSBlocks.GRAY_KNIGHT_HELMET.value())
                .add(DSBlocks.GOLDEN_KNIGHT_HELMET.value())
                .add(DSBlocks.BLACK_KNIGHT_HELMET.value());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(DSBlocks.DRAGON_BEACON.value())
                .add(DSBlocks.DRAGON_MEMORY_BLOCK.value());
    }

    private static TagKey<Block> key(@NotNull final String name) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, name));
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Block tags";
    }
}