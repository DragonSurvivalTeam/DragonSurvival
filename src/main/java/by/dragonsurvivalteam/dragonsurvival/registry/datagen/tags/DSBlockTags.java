package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonAltarBlock;
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
    public static final TagKey<Block> REGENERATES_SEA_DRAGON_MANA = key("regenerates_sea_dragon_mana");
    public static final TagKey<Block> REGENERATES_FOREST_DRAGON_MANA = key("regenerates_forest_dragon_mana");

    public static final TagKey<Block> NETHER_BREATH_DESTRUCTIBLE = key("nether_breath_destructible");
    public static final TagKey<Block> STORM_BREATH_DESTRUCTIBLE = key("storm_breath_destructible");
    public static final TagKey<Block> FOREST_BREATH_DESTRUCTIBLE = key("forest_breath_destructible");

    public static final TagKey<Block> HYDRATES_SEA_DRAGON = key("hydrates_sea_dragon");
    public static final TagKey<Block> FOREST_BREATH_GROW_BLACKLIST = key("forest_breath_grow_blacklist");

    public static final TagKey<Block> DRAGON_ALTARS = key("dragon_altars");
    public static final TagKey<Block> DRAGON_TREASURES = key("dragon_treasures");
    public static final TagKey<Block> WOODEN_DRAGON_DOORS = key("wooden_dragon_doors");
    public static final TagKey<Block> WOODEN_DRAGON_DOORS_SMALL = key("wooden_dragon_doors_small");

    public DSBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, DragonSurvival.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        addToVanillaTags();
        addToDragonSpeedUpBlocks();
        addToDragonManaBlocks();
        addToBreathDestructibleBlocks();

        DSBlocks.DS_BLOCKS.getEntries().forEach(holder -> {
            Block block = holder.value();

            switch (block) {
                case DragonAltarBlock ignored -> tag(DRAGON_ALTARS).add(block);
                case TreasureBlock ignored -> tag(DRAGON_TREASURES).add(block);
                // TODO :: Currently not used anywhere?
                case SkeletonPieceBlock ignored -> tag(key("dragon_bones")).add(block);
                default -> { /* Nothing to do */ }
            }
        });

        // FIXME :: give cave dragon it's own tag
        // Blocks that hydrate sea dragons (or damage cave dragons) when standing on them
        tag(HYDRATES_SEA_DRAGON)
                .addTag(REGENERATES_SEA_DRAGON_MANA)
                .add(Blocks.MUDDY_MANGROVE_ROOTS)
                .add(Blocks.MUD)
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

        tag(WOODEN_DRAGON_DOORS_SMALL)
                .add(DSBlocks.OAK_SMALL_DOOR.value())
                .add(DSBlocks.SPRUCE_SMALL_DOOR.value())
                .add(DSBlocks.ACACIA_SMALL_DOOR.value())
                .add(DSBlocks.BIRCH_SMALL_DOOR.value())
                .add(DSBlocks.JUNGLE_SMALL_DOOR.value())
                .add(DSBlocks.DARK_OAK_SMALL_DOOR.value())
                .add(DSBlocks.WARPED_SMALL_DOOR.value())
                .add(DSBlocks.CRIMSON_SMALL_DOOR.value());

        tag(WOODEN_DRAGON_DOORS)
                .addTag(WOODEN_DRAGON_DOORS_SMALL)
                .add(DSBlocks.DRAGON_ALTAR_OAK_LOG.value())
                .add(DSBlocks.DRAGON_ALTAR_BIRCH_LOG.value())
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

        // TODO :: what is this used for?
        tag(key("castle_blocks"))
                .add(Blocks.STONE_BRICKS)
                .add(Blocks.STONE_BRICK_STAIRS)
                .add(Blocks.STONE_BRICK_SLAB)
                .add(Blocks.STONE_BRICK_WALL)
                .add(Blocks.CRACKED_STONE_BRICKS)
                .add(Blocks.MOSSY_STONE_BRICKS)
                .add(Blocks.CHISELED_STONE_BRICKS)
                .add(Blocks.DEEPSLATE_BRICKS)
                .add(Blocks.DEEPSLATE_BRICK_STAIRS)
                .add(Blocks.DEEPSLATE_BRICK_SLAB)
                .add(Blocks.DEEPSLATE_BRICK_WALL)
                .add(Blocks.DEEPSLATE_TILES)
                .add(Blocks.DEEPSLATE_TILE_STAIRS)
                .add(Blocks.DEEPSLATE_TILE_SLAB)
                .add(Blocks.DEEPSLATE_TILE_WALL)
                .add(Blocks.CRACKED_DEEPSLATE_BRICKS)
                .add(Blocks.CRACKED_DEEPSLATE_TILES)
                .add(Blocks.POLISHED_DEEPSLATE)
                .add(Blocks.POLISHED_DEEPSLATE_STAIRS)
                .add(Blocks.POLISHED_DEEPSLATE_SLAB)
                .add(Blocks.POLISHED_DEEPSLATE_WALL)
                .add(Blocks.CHISELED_DEEPSLATE)
                .add(Blocks.CHISELED_POLISHED_BLACKSTONE)
                .add(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS)
                .add(Blocks.CHAIN)
                .add(Blocks.LANTERN);
    }

    /** These blocks can be destroyed by the respective dragon breath */
    private void addToBreathDestructibleBlocks() {
        tag(NETHER_BREATH_DESTRUCTIBLE)
                .addTag(BlockTags.IMPERMEABLE) // Glass
                .addTag(BlockTags.CROPS)
                .addTag(BlockTags.FLOWERS)
                .add(Blocks.COBWEB);

        tag(STORM_BREATH_DESTRUCTIBLE)
                .addTag(BlockTags.IMPERMEABLE) // Glass
                .addTag(BlockTags.FLOWERS);

        tag(FOREST_BREATH_DESTRUCTIBLE)
                .addTag(BlockTags.BANNERS);
    }

    /** These blocks grant mana regeneration when the corresponding dragon type stands on them */
    private void addToDragonManaBlocks() {
        tag(REGENERATES_CAVE_DRAGON_MANA)
                .addTag(BlockTags.FIRE)
                .add(Blocks.LAVA_CAULDRON)
                .add(Blocks.MAGMA_BLOCK)
                .add(Blocks.LAVA)
                .add(DSBlocks.CAVE_SOURCE_OF_MAGIC.value())
                .addOptionalTag(DragonSurvival.location("immersive_weathering", "charred_blocks"))
                .addOptionalTag(DragonSurvival.location("regions_unexplored", "ash"))
                .addOptional(DragonSurvival.location("netherdepthsupgrade", "wet_lava_sponge"))
                .addOptional(DragonSurvival.location("regions_unexplored", "brimwood_log_magma"));

        tag(REGENERATES_SEA_DRAGON_MANA)
                .addTag(BlockTags.SNOW)
                .addTag(BlockTags.ICE)
                .add(Blocks.WATER_CAULDRON)
                .add(Blocks.WET_SPONGE)
                .add(Blocks.WATER)
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
                .addOptional(DragonSurvival.location("immersive_weathering", "snowy_chiseled_stone_bricks"));

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

    /** These blocks grant a speed bonus when the corresponding dragon type stands on them */
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
                .addTag(REGENERATES_SEA_DRAGON_MANA)
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
                .add(DSBlocks.FOREST_PRESSURE_PLATE.value());

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(DSBlocks.DRAGON_ALTAR_STONE.value())
                .add(DSBlocks.DRAGON_ALTAR_SANDSTONE.value())
                .add(DSBlocks.DRAGON_ALTAR_RED_SANDSTONE.value())
                .add(DSBlocks.DRAGON_ALTAR_PURPUR_BLOCK.value())
                .add(DSBlocks.DRAGON_ALTAR_NETHER_BRICKS.value())
                .add(DSBlocks.DRAGON_ALTAR_MOSSY_COBBLESTONE.value())
                .add(DSBlocks.DRAGON_ALTAR_BLACKSTONE.value())
                .add(DSBlocks.CAVE_DRAGON_DOOR.value())
                .add(DSBlocks.SEA_DRAGON_DOOR.value())
                .add(DSBlocks.IRON_DRAGON_DOOR.value())
                .add(DSBlocks.STONE_SMALL_DOOR.value())
                .add(DSBlocks.SLEEPER_SMALL_DOOR.value())
                .add(DSBlocks.CAVE_SMALL_DOOR.value())
                .add(DSBlocks.SEA_SMALL_DOOR.value())
                .add(DSBlocks.IRON_SMALL_DOOR.value())
                .add(DSBlocks.MURDERER_SMALL_DOOR.value())
                .add(DSBlocks.MURDERER_DRAGON_DOOR.value())
                .add(DSBlocks.SLEEPER_DRAGON_DOOR.value())
                .add(DSBlocks.STONE_DRAGON_DOOR.value())
                .add(DSBlocks.CAVE_SOURCE_OF_MAGIC.value())
                .add(DSBlocks.FOREST_SOURCE_OF_MAGIC.value())
                .add(DSBlocks.SEA_SOURCE_OF_MAGIC.value())
                .add(DSBlocks.TREASURE_DEBRIS.value())
                .add(DSBlocks.TREASURE_DIAMOND.value())
                .add(DSBlocks.TREASURE_EMERALD.value())
                .add(DSBlocks.TREASURE_COPPER.value())
                .add(DSBlocks.TREASURE_GOLD.value())
                .add(DSBlocks.TREASURE_IRON.value())
                .add(DSBlocks.HELMET_BLOCK_1.value())
                .add(DSBlocks.HELMET_BLOCK_2.value())
                .add(DSBlocks.HELMET_BLOCK_3.value())
                .add(DSBlocks.DRAGON_BEACON.value())
                .add(DSBlocks.DRAGON_MEMORY_BLOCK.value())
                .add(DSBlocks.PEACE_DRAGON_BEACON.value())
                .add(DSBlocks.MAGIC_DRAGON_BEACON.value())
                .add(DSBlocks.FIRE_DRAGON_BEACON.value())
                .add(DSBlocks.DRAGON_PRESSURE_PLATE.value())
                .add(DSBlocks.HUMAN_PRESSURE_PLATE.value())
                .add(DSBlocks.SEA_PRESSURE_PLATE.value())
                .add(DSBlocks.CAVE_PRESSURE_PLATE.value());
        
        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(DSBlocks.TREASURE_GOLD.value())
                .add(DSBlocks.TREASURE_EMERALD.value())
                .add(DSBlocks.TREASURE_DIAMOND.value())
                .add(DSBlocks.TREASURE_DEBRIS.value())
                .add(DSBlocks.IRON_DRAGON_DOOR.value())
                .add(DSBlocks.IRON_SMALL_DOOR.value())
                .add(DSBlocks.MURDERER_DRAGON_DOOR.value())
                .add(DSBlocks.MURDERER_SMALL_DOOR.value())
                .add(DSBlocks.TREASURE_COPPER.value())
                .add(DSBlocks.TREASURE_IRON.value())
                .add(DSBlocks.HELMET_BLOCK_1.value())
                .add(DSBlocks.HELMET_BLOCK_2.value())
                .add(DSBlocks.HELMET_BLOCK_3.value());
        
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(DSBlocks.DRAGON_BEACON.value())
                .add(DSBlocks.DRAGON_MEMORY_BLOCK.value())
                .add(DSBlocks.PEACE_DRAGON_BEACON.value())
                .add(DSBlocks.MAGIC_DRAGON_BEACON.value())
                .add(DSBlocks.FIRE_DRAGON_BEACON.value());
    }

    private static TagKey<Block> key(@NotNull final String name) {
        return BlockTags.create(ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, name));
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Block tags";
    }
}