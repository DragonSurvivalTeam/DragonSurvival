package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonAltarBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonBeacon;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SkeletonPieceBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
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
    public static final TagKey<Block> ENABLES_HUNTER_EFFECT = key("enables_hunter_effect");
    public static final TagKey<Block> GIANT_DRAGON_DESTRUCTIBLE = key("giant_dragon_destructible");

    public static final TagKey<Block> SPEEDS_UP_CAVE_DRAGON = key("speeds_up_cave_dragon");
    public static final TagKey<Block> SPEEDS_UP_SEA_DRAGON = key("speeds_up_sea_dragon");
    public static final TagKey<Block> SPEEDS_UP_FOREST_DRAGON = key("speeds_up_forest_dragon");

    public static final TagKey<Block> REGENERATES_CAVE_DRAGON_MANA = key("regenerates_cave_dragon_mana");
    public static final TagKey<Block> REGENERATES_FOREST_DRAGON_MANA = key("regenerates_forest_dragon_mana");

    public static final TagKey<Block> CAVE_DRAGON_BREATH_DESTRUCTIBLE = key("cave_dragon_breath_destructible");
    public static final TagKey<Block> SEA_DRAGON_BREATH_DESTRUCTIBLE = key("sea_dragon_breath_destructible");
    public static final TagKey<Block> FOREST_DRAGON_BREATH_DESTRUCTIBLE = key("forest_dragon_breath_destructible");

    public static final TagKey<Block> CAVE_DRAGON_HARVESTABLE = key("cave_dragon_harvestable");
    public static final TagKey<Block> SEA_DRAGON_HARVESTABLE = key("sea_dragon_harvestable");
    public static final TagKey<Block> FOREST_DRAGON_HARVESTABLE = key("forest_dragon_harvestable");

    public static final TagKey<Block> IS_WET = key("is_wet");
    public static final TagKey<Block> FOREST_BREATH_GROW_BLACKLIST = key("forest_breath_grow_blacklist");

    public static final TagKey<Block> DRAGON_ALTARS = key("dragon_altars");
    public static final TagKey<Block> DRAGON_TREASURES = key("dragon_treasures");
    public static final TagKey<Block> DRAGON_BEACONS = key("dragon_beacons");

    public static final TagKey<Block> WOODEN_DRAGON_DOORS = key("wooden_dragon_doors");
    public static final TagKey<Block> SMALL_WOODEN_DRAGON_DOORS = key("small_wooden_dragon_doors");

    public DSBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, DragonSurvival.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        addToVanillaTags();
        addToDragonSpeedUpBlocks();
        addToDragonManaBlocks();
        addToBreathDestructibleBlocks();
        addToHarvestableBlocks();

        DSBlocks.DS_BLOCKS.getEntries().forEach(holder -> {
            Block block = holder.value();

            switch (block) {
                case DragonAltarBlock ignored -> tag(DRAGON_ALTARS).add(block);
                case TreasureBlock ignored -> tag(DRAGON_TREASURES).add(block);
                case DragonBeacon ignored -> tag(DRAGON_BEACONS).add(block);
                case SkeletonPieceBlock ignored -> tag(key("dragon_bones")).add(block); // TODO :: Currently not used anywhere?
                default -> { /* Nothing to do */ }
            }
        });

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

        // Blocks which will not trigger bonemeal-like-growth when hit with the forest breath
        tag(FOREST_BREATH_GROW_BLACKLIST)
                .add(Blocks.GRASS_BLOCK);

        // Destructible blocks for very large dragon sizes
        tag(GIANT_DRAGON_DESTRUCTIBLE)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(BlockTags.FLOWERS)
                .addTag(BlockTags.REPLACEABLE); // Potentially has no entries?

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
    }

    private void addToHarvestableBlocks() {
        tag(CAVE_DRAGON_HARVESTABLE).addTag(BlockTags.MINEABLE_WITH_PICKAXE);
        tag(SEA_DRAGON_HARVESTABLE).addTag(BlockTags.MINEABLE_WITH_SHOVEL);
        tag(FOREST_DRAGON_HARVESTABLE).addTag(BlockTags.MINEABLE_WITH_AXE);
    }

    /** These blocks can be destroyed by the respective dragon breath */
    private void addToBreathDestructibleBlocks() {
        tag(CAVE_DRAGON_BREATH_DESTRUCTIBLE)
                .addTag(BlockTags.IMPERMEABLE) // Glass
                .addTag(BlockTags.CROPS)
                .addTag(BlockTags.FLOWERS)
                .add(Blocks.COBWEB);

        tag(SEA_DRAGON_BREATH_DESTRUCTIBLE)
                .addTag(BlockTags.IMPERMEABLE) // Glass
                .addTag(BlockTags.FLOWERS);

        tag(FOREST_DRAGON_BREATH_DESTRUCTIBLE)
                .addTag(BlockTags.BANNERS);
    }

    /** These blocks grant mana regeneration when the corresponding dragon species stands on them */
    private void addToDragonManaBlocks() {
        tag(REGENERATES_CAVE_DRAGON_MANA)
                .addTag(BlockTags.FIRE)
                .add(Blocks.MAGMA_BLOCK)
                .add(DSBlocks.CAVE_SOURCE_OF_MAGIC.value())
                .addOptionalTag(DragonSurvival.location("immersive_weathering", "charred_blocks"))
                .addOptionalTag(DragonSurvival.location("regions_unexplored", "ash"))
                .addOptional(DragonSurvival.location("netherdepthsupgrade", "wet_lava_sponge"))
                .addOptional(DragonSurvival.location("regions_unexplored", "brimwood_log_magma"));

        tag(REGENERATES_FOREST_DRAGON_MANA)
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
    }

    /** These blocks grant a speed bonus when the corresponding dragon species stands on them */
    private void addToDragonSpeedUpBlocks() {
        tag(SPEEDS_UP_CAVE_DRAGON)
                .addTag(BlockTags.BASE_STONE_OVERWORLD)
                .addTag(BlockTags.BEACON_BASE_BLOCKS)
                .addTag(BlockTags.BASE_STONE_NETHER)
                .addTag(BlockTags.STONE_BRICKS)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .addTag(Tags.Blocks.COBBLESTONES)
                .addTag(Tags.Blocks.STONES)
                .addTag(Tags.Blocks.ORES)
                .addTag(REGENERATES_CAVE_DRAGON_MANA)
                .addOptionalTag(DragonSurvival.location("immersive_weathering", "charred_blocks"));

        tag(SPEEDS_UP_SEA_DRAGON)
                .addTag(BlockTags.CORAL_BLOCKS)
                .addTag(BlockTags.IMPERMEABLE) // Glass
                .addTag(BlockTags.SAND)
                .addTag(Tags.Blocks.SANDSTONE_BLOCKS)
                .addTag(Tags.Blocks.SANDS)
                .addTag(IS_WET)
                .add(Blocks.DIRT_PATH)
                .add(Blocks.MUD);

        tag(SPEEDS_UP_FOREST_DRAGON)
                .addTag(BlockTags.WOODEN_SLABS)
                .addTag(BlockTags.PLANKS)
                .addTag(BlockTags.LOGS)
                .addTag(BlockTags.DIRT)
                .addTag(REGENERATES_FOREST_DRAGON_MANA)
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
                .add(DSBlocks.EMPTY_DRAGON_BEACON.value())
                .add(DSBlocks.DRAGON_MEMORY_BLOCK.value())
                .add(DSBlocks.FOREST_DRAGON_BEACON.value())
                .add(DSBlocks.SEA_DRAGON_BEACON.value())
                .add(DSBlocks.CAVE_DRAGON_BEACON.value())
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
                .add(DSBlocks.EMPTY_DRAGON_BEACON.value())
                .add(DSBlocks.DRAGON_MEMORY_BLOCK.value())
                .add(DSBlocks.FOREST_DRAGON_BEACON.value())
                .add(DSBlocks.SEA_DRAGON_BEACON.value())
                .add(DSBlocks.CAVE_DRAGON_BEACON.value());
    }

    private static TagKey<Block> key(@NotNull final String name) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, name));
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Block tags";
    }
}