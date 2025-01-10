package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.mixins.Holder$ReferenceAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DSRecipes extends RecipeProvider {
    public DSRecipes(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(@NotNull final RecipeOutput output, @NotNull final HolderLookup.Provider lookup) {
        //noinspection unchecked -> ignore
        MappedRegistry<Item> registry = (MappedRegistry<Item>) BuiltInRegistries.ITEM;
        //noinspection deprecation -> workaround to create recipes with items from other mods (without having to resort to one-entry-tags)
        registry.unfreeze();

        buildShaped(output, lookup);
        buildShapeless(output, lookup);

        // We don't re-freeze the registry because it would complain about unregistered holders
        // Since the data generation is over at this point it doesn't matter anyway
    }

    private void buildShaped(final RecipeOutput output, final HolderLookup.Provider lookup) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DSItems.ELDER_DRAGON_HEART.value())
                .pattern("DGD")
                .pattern("GHG")
                .pattern("NGN")
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('H', DSItems.WEAK_DRAGON_HEART.value())
                .define('N', Items.NETHERITE_SCRAP)
                .unlockedBy(getHasName(DSItems.WEAK_DRAGON_HEART.value()), has(DSItems.WEAK_DRAGON_HEART.value()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, DSItems.CAVE_DRAGON_TREAT.value())
                .pattern("DDD")
                .pattern("DCD")
                .pattern("DDD")
                .define('D', DSItems.ELDER_DRAGON_DUST.value())
                .define('C', DSItemTags.CHARRED_FOOD)
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.value()), has(DSItems.ELDER_DRAGON_DUST.value()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, DSItems.CHARGED_COAL.value())
                .pattern("RRR")
                .pattern("CCR")
                .pattern("CCR")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('C', ItemTags.COALS)
                .unlockedBy("has_redstone_dust", has(Tags.Items.DUSTS_REDSTONE))
                .save(output, DragonSurvival.res("charged_coal_from_dust"));

        // --- Mod support --- //

        ProxyItem proxyItem = new ProxyItem("regions_unexplored", "redstone_bulb");

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, DSItems.CHARGED_COAL.value())
                .pattern("RRR")
                .pattern("CCR")
                .pattern("CCR")
                .define('R', proxyItem)
                .define('C', ItemTags.COALS)
                .unlockedBy("has_redstone_bulb", has(proxyItem))
                .save(output.withConditions(new ModLoadedCondition("regions_unexplored")), DragonSurvival.res("charged_coal_from_bulb"));
    }

    private void buildShapeless(final RecipeOutput output, final HolderLookup.Provider lookup) {
        buildDragonDoors(output, lookup);
        buildSmallDragonDoors(output, lookup);
        buildDragonAltars(output, lookup);
        buildDragonBeacons(output, lookup);
        buildDragonTreasures(output, lookup);

        // --- Misc --- //

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, Items.CHARCOAL)
                .requires(DSItemTags.CHARRED_FOOD)
                .unlockedBy("has_charred_food", has(DSItemTags.CHARRED_FOOD))
                .save(output, DragonSurvival.res("charcoal_from_charred_food"));

        // --- Pressure Plates --- //

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.CAVE_DRAGON_PRESSURE_PLATE.value())
                .requires(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE)
                .requires(DSItems.ELDER_DRAGON_DUST.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.value()), has(DSItems.ELDER_DRAGON_DUST.value()))
                .save(output);
    }

    private void buildDragonTreasures(final RecipeOutput output, final HolderLookup.Provider lookup) {
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.COPPER_DRAGON_TREASURE.value())
                .requires(Tags.Items.INGOTS_COPPER)
                .requires(DSItems.ELDER_DRAGON_DUST.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.value()), has(DSItems.ELDER_DRAGON_DUST.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.IRON_DRAGON_TREASURE.value())
                .requires(Tags.Items.INGOTS_IRON)
                .requires(DSItems.ELDER_DRAGON_DUST.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.value()), has(DSItems.ELDER_DRAGON_DUST.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.GOLD_DRAGON_TREASURE.value())
                .requires(Tags.Items.INGOTS_GOLD)
                .requires(DSItems.ELDER_DRAGON_DUST.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.value()), has(DSItems.ELDER_DRAGON_DUST.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.EMERALD_DRAGON_TREASURE.value())
                .requires(Tags.Items.GEMS_EMERALD)
                .requires(DSItems.ELDER_DRAGON_DUST.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.value()), has(DSItems.ELDER_DRAGON_DUST.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.DIAMOND_DRAGON_TREASURE.value())
                .requires(Tags.Items.GEMS_DIAMOND)
                .requires(DSItems.ELDER_DRAGON_DUST.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.value()), has(DSItems.ELDER_DRAGON_DUST.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.DEBRIS_DRAGON_TREASURE.value())
                .requires(Items.NETHERITE_SCRAP)
                .requires(DSItems.ELDER_DRAGON_DUST.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.value()), has(DSItems.ELDER_DRAGON_DUST.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSItems.ELDER_DRAGON_DUST.value())
                .requires(DSItemTags.DRAGON_TREASURES)
                .unlockedBy("has_dragon_treasures", has(DSItemTags.DRAGON_TREASURES))
                .save(output, DragonSurvival.res("elder_dragon_dust_from_dragon_treasures"));
    }

    private void buildDragonBeacons(final RecipeOutput output, final HolderLookup.Provider lookup) {
        // --- Beacons --- //

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.CAVE_DRAGON_BEACON.value())
                .requires(Tags.Items.INGOTS_NETHERITE)
                .requires(DSBlocks.EMPTY_DRAGON_BEACON.value())
                .unlockedBy(getHasName(DSBlocks.EMPTY_DRAGON_BEACON.value()), has(DSBlocks.EMPTY_DRAGON_BEACON.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.SEA_DRAGON_BEACON.value())
                .requires(Tags.Items.STORAGE_BLOCKS_GOLD)
                .requires(DSBlocks.EMPTY_DRAGON_BEACON.value())
                .unlockedBy(getHasName(DSBlocks.EMPTY_DRAGON_BEACON.value()), has(DSBlocks.EMPTY_DRAGON_BEACON.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.FOREST_DRAGON_BEACON.value())
                .requires(Tags.Items.STORAGE_BLOCKS_DIAMOND)
                .requires(DSBlocks.EMPTY_DRAGON_BEACON.value())
                .unlockedBy(getHasName(DSBlocks.EMPTY_DRAGON_BEACON.value()), has(DSBlocks.EMPTY_DRAGON_BEACON.value()))
                .save(output);

        // --- Empty Beacons --- //

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.EMPTY_DRAGON_BEACON.value())
                .requires(Items.BEACON)
                .requires(DSItemTags.DRAGON_ALTARS)
                .unlockedBy(getHasName(Items.BEACON), has(Items.BEACON))
                .save(output, DragonSurvival.res("empty_dragon_beacon_from_dragon_altars"));

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.EMPTY_DRAGON_BEACON.value())
                .requires(DSBlocks.CAVE_DRAGON_BEACON.value())
                .unlockedBy(getHasName(DSBlocks.CAVE_DRAGON_BEACON.value()), has(DSBlocks.CAVE_DRAGON_BEACON.value()))
                .save(output, DragonSurvival.res("empty_dragon_beacon_from_cave_dragon_beacon"));

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.EMPTY_DRAGON_BEACON.value())
                .requires(DSBlocks.SEA_DRAGON_BEACON.value())
                .unlockedBy(getHasName(DSBlocks.SEA_DRAGON_BEACON.value()), has(DSBlocks.SEA_DRAGON_BEACON.value()))
                .save(output, DragonSurvival.res("empty_dragon_beacon_from_sea_dragon_beacon"));

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.EMPTY_DRAGON_BEACON.value())
                .requires(DSBlocks.FOREST_DRAGON_BEACON.value())
                .unlockedBy(getHasName(DSBlocks.FOREST_DRAGON_BEACON.value()), has(DSBlocks.FOREST_DRAGON_BEACON.value()))
                .save(output, DragonSurvival.res("empty_dragon_beacon_from_forest_dragon_beacon"));
    }

    private void buildDragonAltars(final RecipeOutput output, final HolderLookup.Provider lookup) {
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.BIRCH_DRAGON_ALTAR.value())
                .requires(Items.BIRCH_LOG)
                .requires(DSItems.ELDER_DRAGON_BONE.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.value()), has(DSItems.ELDER_DRAGON_BONE.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.BLACKSTONE_DRAGON_ALTAR.value())
                .requires(Items.BLACKSTONE)
                .requires(DSItems.ELDER_DRAGON_BONE.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.value()), has(DSItems.ELDER_DRAGON_BONE.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.MOSSY_DRAGON_ALTAR.value())
                .requires(Items.MOSSY_COBBLESTONE)
                .requires(DSItems.ELDER_DRAGON_BONE.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.value()), has(DSItems.ELDER_DRAGON_BONE.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.NETHER_BRICK_DRAGON_ALTAR.value())
                .requires(Items.NETHER_BRICK)
                .requires(DSItems.ELDER_DRAGON_BONE.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.value()), has(DSItems.ELDER_DRAGON_BONE.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.OAK_DRAGON_ALTAR.value())
                .requires(Items.OAK_LOG)
                .requires(DSItems.ELDER_DRAGON_BONE.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.value()), has(DSItems.ELDER_DRAGON_BONE.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.PURPUR_DRAGON_ALTAR.value())
                .requires(Items.PURPUR_BLOCK)
                .requires(DSItems.ELDER_DRAGON_BONE.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.value()), has(DSItems.ELDER_DRAGON_BONE.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.RED_SANDSTONE_DRAGON_ALTAR.value())
                .requires(Items.RED_SANDSTONE)
                .requires(DSItems.ELDER_DRAGON_BONE.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.value()), has(DSItems.ELDER_DRAGON_BONE.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.SANDSTONE_DRAGON_ALTAR.value())
                .requires(Items.SANDSTONE)
                .requires(DSItems.ELDER_DRAGON_BONE.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.value()), has(DSItems.ELDER_DRAGON_BONE.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.STONE_DRAGON_ALTAR.value())
                .requires(Items.STONE)
                .requires(DSItems.ELDER_DRAGON_BONE.value())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.value()), has(DSItems.ELDER_DRAGON_BONE.value()))
                .save(output);
    }

    private void buildDragonDoors(final RecipeOutput output, final HolderLookup.Provider lookup) {
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.IRON_DRAGON_DOOR.value(), 2)
                .requires(Items.IRON_DOOR, 3)
                .unlockedBy(getHasName(Items.IRON_DOOR), has(Items.IRON_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.ACACIA_DRAGON_DOOR.value(), 2)
                .requires(Items.ACACIA_DOOR, 3)
                .unlockedBy(getHasName(Items.ACACIA_DOOR), has(Items.ACACIA_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.BIRCH_DRAGON_DOOR.value(), 2)
                .requires(Items.BIRCH_DOOR, 3)
                .unlockedBy(getHasName(Items.BIRCH_DOOR), has(Items.BIRCH_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.JUNGLE_DRAGON_DOOR.value(), 2)
                .requires(Items.JUNGLE_DOOR, 3)
                .unlockedBy(getHasName(Items.JUNGLE_DOOR), has(Items.JUNGLE_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.OAK_DRAGON_DOOR.value(), 2)
                .requires(Items.OAK_DOOR, 3)
                .unlockedBy(getHasName(Items.OAK_DOOR), has(Items.OAK_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.DARK_OAK_DRAGON_DOOR.value(), 2)
                .requires(Items.DARK_OAK_DOOR, 3)
                .unlockedBy(getHasName(Items.DARK_OAK_DOOR), has(Items.DARK_OAK_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SPRUCE_DRAGON_DOOR.value(), 2)
                .requires(Items.SPRUCE_DOOR, 3)
                .unlockedBy(getHasName(Items.SPRUCE_DOOR), has(Items.SPRUCE_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.CRIMSON_DRAGON_DOOR.value(), 2)
                .requires(Items.CRIMSON_DOOR, 3)
                .unlockedBy(getHasName(Items.CRIMSON_DOOR), has(Items.CRIMSON_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.WARPED_DRAGON_DOOR.value(), 2)
                .requires(Items.WARPED_DOOR, 3)
                .unlockedBy(getHasName(Items.WARPED_DOOR), has(Items.WARPED_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.LEGACY_DRAGON_DOOR.value())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(Items.IRON_HELMET)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.STONE_DRAGON_DOOR.value())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(ItemTags.STONE_BRICKS)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.GOTHIC_DRAGON_DOOR.value())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(ItemTags.BEDS)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SKYRIM_DRAGON_DOOR.value())
                .requires(DSBlocks.OAK_DRAGON_DOOR.value())
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.CAVE_DRAGON_DOOR.value())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(Items.GILDED_BLACKSTONE)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.FOREST_DRAGON_DOOR.value())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(Items.VINE)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SEA_DRAGON_DOOR.value())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(Items.PRISMARINE)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);
    }

    private void buildSmallDragonDoors(final RecipeOutput output, final HolderLookup.Provider lookup) {
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_IRON_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.IRON_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.IRON_DRAGON_DOOR.value()), has(DSBlocks.IRON_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_STONE_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.STONE_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.STONE_DRAGON_DOOR.value()), has(DSBlocks.STONE_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_ACACIA_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.ACACIA_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.ACACIA_DRAGON_DOOR.value()), has(DSBlocks.ACACIA_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_BIRCH_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.BIRCH_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.BIRCH_DRAGON_DOOR.value()), has(DSBlocks.BIRCH_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_JUNGLE_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.JUNGLE_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.JUNGLE_DRAGON_DOOR.value()), has(DSBlocks.JUNGLE_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_OAK_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.OAK_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.OAK_DRAGON_DOOR.value()), has(DSBlocks.OAK_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_DARK_OAK_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.DARK_OAK_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.DARK_OAK_DRAGON_DOOR.value()), has(DSBlocks.DARK_OAK_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_SPRUCE_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.SPRUCE_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.SPRUCE_DRAGON_DOOR.value()), has(DSBlocks.SPRUCE_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_CRIMSON_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.CRIMSON_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.CRIMSON_DRAGON_DOOR.value()), has(DSBlocks.CRIMSON_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_WARPED_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.WARPED_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.WARPED_DRAGON_DOOR.value()), has(DSBlocks.WARPED_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_GOTHIC_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.GOTHIC_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.GOTHIC_DRAGON_DOOR.value()), has(DSBlocks.GOTHIC_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_SKYRIM_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.SKYRIM_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.SKYRIM_DRAGON_DOOR.value()), has(DSBlocks.SKYRIM_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_CAVE_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.CAVE_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.CAVE_DRAGON_DOOR.value()), has(DSBlocks.CAVE_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_FOREST_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.FOREST_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.FOREST_DRAGON_DOOR.value()), has(DSBlocks.FOREST_DRAGON_DOOR.value()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_SEA_DRAGON_DOOR.value(), 3)
                .requires(DSBlocks.SEA_DRAGON_DOOR.value())
                .unlockedBy(getHasName(DSBlocks.SEA_DRAGON_DOOR.value()), has(DSBlocks.SEA_DRAGON_DOOR.value()))
                .save(output);
    }

    @SuppressWarnings("deprecation") // ignore
    public record ProxyItem(String namespace, String path) implements ItemLike {
        @Override
        public @NotNull Item asItem() {
            Item item = new Item(new Item.Properties());
            ((Holder$ReferenceAccess) item.builtInRegistryHolder()).dragonSurvival$bindKey(ResourceKey.create(Registries.ITEM, DragonSurvival.location(namespace, path)));
            ((Holder$ReferenceAccess) item.builtInRegistryHolder()).dragonSurvival$bindValue(item);
            return item;
        }
    }
}
