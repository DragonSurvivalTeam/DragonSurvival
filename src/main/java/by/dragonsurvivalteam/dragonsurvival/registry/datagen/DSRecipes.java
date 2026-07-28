package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class DSRecipes extends RecipeProvider {
    public static final List<ProxyItem> PROXY_ITEMS = List.of(
            new ProxyItem("regions_unexplored", "redstone_bulb"),
            new ProxyItem(ModID.CREATE.value(), "bar_of_chocolate"),
            new ProxyItem(ModID.BEE_ADDON.value(), "caramelized_nectar"),
            new ProxyItem(ModID.SILENTGEMS.value(), "ruby"),
            new ProxyItem(ModID.SILENTGEMS.value(), "carnelian"),
            new ProxyItem(ModID.SILENTGEMS.value(), "topaz"),
            new ProxyItem(ModID.SILENTGEMS.value(), "citrine"),
            new ProxyItem(ModID.SILENTGEMS.value(), "heliodor"),
            new ProxyItem(ModID.SILENTGEMS.value(), "moldavite"),
            new ProxyItem(ModID.SILENTGEMS.value(), "peridot"),
            new ProxyItem(ModID.SILENTGEMS.value(), "turquoise"),
            new ProxyItem(ModID.SILENTGEMS.value(), "kyanite"),
            new ProxyItem(ModID.SILENTGEMS.value(), "sapphire"),
            new ProxyItem(ModID.SILENTGEMS.value(), "iolite"),
            new ProxyItem(ModID.SILENTGEMS.value(), "alexandrite"),
            new ProxyItem(ModID.SILENTGEMS.value(), "ammolite"),
            new ProxyItem(ModID.SILENTGEMS.value(), "rose_quartz"),
            new ProxyItem(ModID.SILENTGEMS.value(), "black_diamond"),
            new ProxyItem(ModID.SILENTGEMS.value(), "white_diamond")
    );

    public DSRecipes(final PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@NotNull final Consumer<FinishedRecipe> output) {
        buildShaped(output);
        buildShapeless(output);
    }

    private void buildShaped(final Consumer<FinishedRecipe> output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, DSItems.ELDER_DRAGON_HEART.get())
                .pattern("DGD")
                .pattern("GHG")
                .pattern("NGN")
                .define('D', Tags.Items.GEMS_DIAMOND)
                .define('G', Tags.Items.INGOTS_GOLD)
                .define('H', DSItems.WEAK_DRAGON_HEART.get())
                .define('N', Items.NETHERITE_SCRAP)
                .unlockedBy(getHasName(DSItems.WEAK_DRAGON_HEART.get()), has(DSItems.WEAK_DRAGON_HEART.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, DSItems.CAVE_DRAGON_TREAT.get())
                .pattern("DDD")
                .pattern("DCD")
                .pattern("DDD")
                .define('D', DSItems.ELDER_DRAGON_DUST.get())
                .define('C', DSItemTags.CHARRED_FOOD)
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, DSItems.CHARGED_COAL.get(), 4)
                .pattern("RRR")
                .pattern("CCR")
                .pattern("CCR")
                .define('R', Tags.Items.DUSTS_REDSTONE)
                .define('C', ItemTags.COALS)
                .unlockedBy("has_redstone_dust", has(Tags.Items.DUSTS_REDSTONE))
                .save(output, DragonSurvival.res("charged_coal_from_dust"));

        // --- Mod support --- //

        ProxyItem proxyItem = new ProxyItem("regions_unexplored", "redstone_bulb");

        ShapedRecipeBuilder.shaped(RecipeCategory.FOOD, DSItems.CHARGED_COAL.get())
                .pattern("RRR")
                .pattern("CCR")
                .pattern("CCR")
                .define('R', proxyItem.tag())
                .define('C', ItemTags.COALS)
                .unlockedBy("has_redstone_bulb", has(proxyItem.tag()))
                .save(withConditions(output, new ModLoadedCondition("regions_unexplored")), DragonSurvival.res("charged_coal_from_bulb"));
    }

    private void buildShapeless(final Consumer<FinishedRecipe> output) {
        buildDragonDoors(output);
        buildSmallDragonDoors(output);
        buildDragonAltars(output);
        buildDragonBeacons(output);
        buildDragonTreasures(output);

        // --- Misc --- //

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, Items.CHARCOAL)
                .requires(DSItemTags.CHARRED_FOOD)
                .unlockedBy("has_charred_food", has(DSItemTags.CHARRED_FOOD))
                .save(output, DragonSurvival.res("charcoal_from_charred_food"));

        // --- Pressure Plates --- //

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.CAVE_DRAGON_PRESSURE_PLATE.get())
                .requires(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE)
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(output);
    }

    private void buildDragonTreasures(final Consumer<FinishedRecipe> output) {
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.COPPER_DRAGON_TREASURE.get())
                .requires(Tags.Items.INGOTS_COPPER)
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.IRON_DRAGON_TREASURE.get())
                .requires(Tags.Items.INGOTS_IRON)
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.GOLD_DRAGON_TREASURE.get())
                .requires(Tags.Items.INGOTS_GOLD)
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.EMERALD_DRAGON_TREASURE.get())
                .requires(Tags.Items.GEMS_EMERALD)
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.DIAMOND_DRAGON_TREASURE.get())
                .requires(Tags.Items.GEMS_DIAMOND)
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.DEBRIS_DRAGON_TREASURE.get())
                .requires(Items.NETHERITE_SCRAP)
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(output);

        ProxyItem barOfChocolate = new ProxyItem(ModID.CREATE.value(), "bar_of_chocolate");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.CHOCOLATE_DRAGON_TREASURE.get())
                .requires(barOfChocolate.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.CREATE.value())));

        ProxyItem caramelized_nectar = new ProxyItem(ModID.BEE_ADDON.value(), "caramelized_nectar");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.BEE_HONEY_TREASURE.get())
                .requires(caramelized_nectar.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.BEE_ADDON.value())));


        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.BOTTLE_CAPS_TREASURE.get(), 3)
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .requires(Items.IRON_NUGGET, 3)
                .requires(Items.BLUE_DYE, 3)
                .requires(Items.GREEN_DYE, 3)
                .requires(Items.RED_DYE, 3)
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.DESERT_ADDON.value())));

        ProxyItem ruby = new ProxyItem(ModID.SILENTGEMS.value(), "ruby");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.RUBY_DRAGON_TREASURE.get())
                .requires(ruby.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem carnelian = new ProxyItem(ModID.SILENTGEMS.value(), "carnelian");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.CARNELIAN_DRAGON_TREASURE.get())
                .requires(carnelian.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem topaz = new ProxyItem(ModID.SILENTGEMS.value(), "topaz");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.TOPAZ_DRAGON_TREASURE.get())
                .requires(topaz.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem citrine = new ProxyItem(ModID.SILENTGEMS.value(), "citrine");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.CITRINE_DRAGON_TREASURE.get())
                .requires(citrine.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem heliodor = new ProxyItem(ModID.SILENTGEMS.value(), "heliodor");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.HELIODOR_DRAGON_TREASURE.get())
                .requires(heliodor.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem moldavite = new ProxyItem(ModID.SILENTGEMS.value(), "moldavite");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.MOLDAVITE_DRAGON_TREASURE.get())
                .requires(moldavite.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem peridot = new ProxyItem(ModID.SILENTGEMS.value(), "peridot");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.PERIDOT_DRAGON_TREASURE.get())
                .requires(peridot.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem turquoise = new ProxyItem(ModID.SILENTGEMS.value(), "turquoise");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.TURQUOISE_DRAGON_TREASURE.get())
                .requires(turquoise.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem kyanite = new ProxyItem(ModID.SILENTGEMS.value(), "kyanite");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.KYANITE_DRAGON_TREASURE.get())
                .requires(kyanite.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem sapphire = new ProxyItem(ModID.SILENTGEMS.value(), "sapphire");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.SAPPHIRE_DRAGON_TREASURE.get())
                .requires(sapphire.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem iolite = new ProxyItem(ModID.SILENTGEMS.value(), "iolite");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.IOLITE_DRAGON_TREASURE.get())
                .requires(iolite.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem alexandrite = new ProxyItem(ModID.SILENTGEMS.value(), "alexandrite");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.ALEXANDRITE_DRAGON_TREASURE.get())
                .requires(alexandrite.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem ammolite = new ProxyItem(ModID.SILENTGEMS.value(), "ammolite");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.AMMOLITE_DRAGON_TREASURE.get())
                .requires(ammolite.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem rose_quartz = new ProxyItem(ModID.SILENTGEMS.value(), "rose_quartz");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.ROSE_QUARTZ_DRAGON_TREASURE.get())
                .requires(rose_quartz.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem black_diamond = new ProxyItem(ModID.SILENTGEMS.value(), "black_diamond");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.BLACK_DIAMOND_DRAGON_TREASURE.get())
                .requires(black_diamond.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ProxyItem white_diamond = new ProxyItem(ModID.SILENTGEMS.value(), "white_diamond");

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.DECORATIONS, DSBlocks.WHITE_DIAMOND_DRAGON_TREASURE.get())
                .requires(white_diamond.tag())
                .requires(DSItems.ELDER_DRAGON_DUST.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_DUST.get()), has(DSItems.ELDER_DRAGON_DUST.get()))
                .save(withConditions(output, new ModLoadedCondition(ModID.SILENTGEMS.value())));

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSItems.ELDER_DRAGON_DUST.get())
                .requires(DSItemTags.DRAGON_TREASURES)
                .unlockedBy("has_dragon_treasures", has(DSItemTags.DRAGON_TREASURES))
                .save(output, DragonSurvival.res("elder_dragon_dust_from_dragon_treasures"));
    }

    private void buildDragonBeacons(final Consumer<FinishedRecipe> output) {
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.DRAGON_BEACON.get())
                .requires(DSItems.STAR_HEART.get())
                .requires(DSItemTags.ACTIVATES_DRAGON_BEACON)
                .unlockedBy(getHasName(Items.BEACON), has(Items.BEACON))
                .save(output);
    }

    private void buildDragonAltars(final Consumer<FinishedRecipe> output) {
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.BIRCH_DRAGON_ALTAR.get())
                .requires(Items.BIRCH_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.BLACKSTONE_DRAGON_ALTAR.get())
                .requires(Items.BLACKSTONE)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.MOSSY_DRAGON_ALTAR.get())
                .requires(Items.MOSSY_COBBLESTONE)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.NETHER_BRICK_DRAGON_ALTAR.get())
                .requires(Items.NETHER_BRICKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.OAK_DRAGON_ALTAR.get())
                .requires(Items.OAK_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.PURPUR_DRAGON_ALTAR.get())
                .requires(Items.PURPUR_BLOCK)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.RED_SANDSTONE_DRAGON_ALTAR.get())
                .requires(Items.RED_SANDSTONE)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.SANDSTONE_DRAGON_ALTAR.get())
                .requires(Items.SANDSTONE)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.STONE_DRAGON_ALTAR.get())
                .requires(Items.STONE)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.BONE_DRAGON_ALTAR.get())
                .requires(Items.BONE_BLOCK)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.QUARTZ_DRAGON_ALTAR.get())
                .requires(Items.QUARTZ_BLOCK)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.ICE_DRAGON_ALTAR.get())
                .requires(Items.PACKED_ICE)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.NETHERRACK_DRAGON_ALTAR.get())
                .requires(Items.NETHERRACK)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.OBSIDIAN_DRAGON_ALTAR.get())
                .requires(Items.OBSIDIAN)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.AMETHYST_DRAGON_ALTAR.get())
                .requires(Items.AMETHYST_BLOCK)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.MUDBRICK_DRAGON_ALTAR.get())
                .requires(Items.PACKED_MUD)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.PRISMARINE_DRAGON_ALTAR.get())
                .requires(Items.PRISMARINE_BRICKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.RED_NETHER_BRICK_DRAGON_ALTAR.get())
                .requires(Items.RED_NETHER_BRICKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.ENDSTONE_DRAGON_ALTAR.get())
                .requires(Items.END_STONE)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.DEEPSLATE_DRAGON_ALTAR.get())
                .requires(Items.COBBLED_DEEPSLATE)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.TUFF_DRAGON_ALTAR.get())
                .requires(Items.TUFF)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.CRIMSON_DRAGON_ALTAR.get())
                .requires(Items.CRIMSON_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.WARPED_DRAGON_ALTAR.get())
                .requires(Items.WARPED_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.MANGROVE_DRAGON_ALTAR.get())
                .requires(Items.MANGROVE_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.BAMBOO_DRAGON_ALTAR.get())
                .requires(Items.BAMBOO_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.CHERRY_DRAGON_ALTAR.get())
                .requires(Items.CHERRY_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.ACACIA_DRAGON_ALTAR.get())
                .requires(Items.ACACIA_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.DARK_OAK_DRAGON_ALTAR.get())
                .requires(Items.DARK_OAK_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.JUNGLE_DRAGON_ALTAR.get())
                .requires(Items.JUNGLE_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.MISC, DSBlocks.SPRUCE_DRAGON_ALTAR.get())
                .requires(Items.SPRUCE_PLANKS)
                .requires(DSItems.ELDER_DRAGON_BONE.get())
                .unlockedBy(getHasName(DSItems.ELDER_DRAGON_BONE.get()), has(DSItems.ELDER_DRAGON_BONE.get()))
                .save(output);
    }

    private void buildDragonDoors(final Consumer<FinishedRecipe> output) {
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.IRON_DRAGON_DOOR.get(), 2)
                .requires(Items.IRON_DOOR, 3)
                .unlockedBy(getHasName(Items.IRON_DOOR), has(Items.IRON_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.ACACIA_DRAGON_DOOR.get(), 2)
                .requires(Items.ACACIA_DOOR, 3)
                .unlockedBy(getHasName(Items.ACACIA_DOOR), has(Items.ACACIA_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.BIRCH_DRAGON_DOOR.get(), 2)
                .requires(Items.BIRCH_DOOR, 3)
                .unlockedBy(getHasName(Items.BIRCH_DOOR), has(Items.BIRCH_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.JUNGLE_DRAGON_DOOR.get(), 2)
                .requires(Items.JUNGLE_DOOR, 3)
                .unlockedBy(getHasName(Items.JUNGLE_DOOR), has(Items.JUNGLE_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.OAK_DRAGON_DOOR.get(), 2)
                .requires(Items.OAK_DOOR, 3)
                .unlockedBy(getHasName(Items.OAK_DOOR), has(Items.OAK_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.DARK_OAK_DRAGON_DOOR.get(), 2)
                .requires(Items.DARK_OAK_DOOR, 3)
                .unlockedBy(getHasName(Items.DARK_OAK_DOOR), has(Items.DARK_OAK_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SPRUCE_DRAGON_DOOR.get(), 2)
                .requires(Items.SPRUCE_DOOR, 3)
                .unlockedBy(getHasName(Items.SPRUCE_DOOR), has(Items.SPRUCE_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.CRIMSON_DRAGON_DOOR.get(), 2)
                .requires(Items.CRIMSON_DOOR, 3)
                .unlockedBy(getHasName(Items.CRIMSON_DOOR), has(Items.CRIMSON_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.WARPED_DRAGON_DOOR.get(), 2)
                .requires(Items.WARPED_DOOR, 3)
                .unlockedBy(getHasName(Items.WARPED_DOOR), has(Items.WARPED_DOOR))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.LEGACY_DRAGON_DOOR.get())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(Items.IRON_HELMET)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.STONE_DRAGON_DOOR.get())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(ItemTags.STONE_BRICKS)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.GOTHIC_DRAGON_DOOR.get())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(ItemTags.BEDS)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SKYRIM_DRAGON_DOOR.get())
                .requires(DSBlocks.OAK_DRAGON_DOOR.get())
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.CAVE_DRAGON_DOOR.get())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(Items.GILDED_BLACKSTONE)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.FOREST_DRAGON_DOOR.get())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(Items.VINE)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SEA_DRAGON_DOOR.get())
                .requires(DSItemTags.WOODEN_DRAGON_DOORS)
                .requires(Items.PRISMARINE)
                .unlockedBy("has_wooden_dragon_doors", has(DSItemTags.WOODEN_DRAGON_DOORS))
                .save(output);
    }

    private void buildSmallDragonDoors(final Consumer<FinishedRecipe> output) {
        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_IRON_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.IRON_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.IRON_DRAGON_DOOR.get()), has(DSBlocks.IRON_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_STONE_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.STONE_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.STONE_DRAGON_DOOR.get()), has(DSBlocks.STONE_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_ACACIA_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.ACACIA_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.ACACIA_DRAGON_DOOR.get()), has(DSBlocks.ACACIA_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_BIRCH_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.BIRCH_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.BIRCH_DRAGON_DOOR.get()), has(DSBlocks.BIRCH_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_JUNGLE_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.JUNGLE_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.JUNGLE_DRAGON_DOOR.get()), has(DSBlocks.JUNGLE_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_OAK_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.OAK_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.OAK_DRAGON_DOOR.get()), has(DSBlocks.OAK_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_DARK_OAK_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.DARK_OAK_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.DARK_OAK_DRAGON_DOOR.get()), has(DSBlocks.DARK_OAK_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_SPRUCE_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.SPRUCE_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.SPRUCE_DRAGON_DOOR.get()), has(DSBlocks.SPRUCE_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_CRIMSON_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.CRIMSON_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.CRIMSON_DRAGON_DOOR.get()), has(DSBlocks.CRIMSON_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_WARPED_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.WARPED_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.WARPED_DRAGON_DOOR.get()), has(DSBlocks.WARPED_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_GOTHIC_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.GOTHIC_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.GOTHIC_DRAGON_DOOR.get()), has(DSBlocks.GOTHIC_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_SKYRIM_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.SKYRIM_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.SKYRIM_DRAGON_DOOR.get()), has(DSBlocks.SKYRIM_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_CAVE_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.CAVE_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.CAVE_DRAGON_DOOR.get()), has(DSBlocks.CAVE_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_FOREST_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.FOREST_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.FOREST_DRAGON_DOOR.get()), has(DSBlocks.FOREST_DRAGON_DOOR.get()))
                .save(output);

        ShapelessRecipeBuilder
                .shapeless(RecipeCategory.REDSTONE, DSBlocks.SMALL_SEA_DRAGON_DOOR.get(), 3)
                .requires(DSBlocks.SEA_DRAGON_DOOR.get())
                .unlockedBy(getHasName(DSBlocks.SEA_DRAGON_DOOR.get()), has(DSBlocks.SEA_DRAGON_DOOR.get()))
                .save(output);
    }

    public record ProxyItem(String namespace, String path) {
        public ResourceLocation id() {
            return DragonSurvival.location(namespace, path);
        }

        public TagKey<Item> tag() {
            return TagKey.create(Registries.ITEM, DragonSurvival.res("compat/" + namespace + "/" + path));
        }
    }

    private static Consumer<FinishedRecipe> withConditions(final Consumer<FinishedRecipe> output, final ICondition... conditions) {
        return recipe -> {
            ConditionalRecipe.Builder builder = ConditionalRecipe.builder();
            for (ICondition condition : conditions) {
                builder.addCondition(condition);
            }
            builder.addRecipe(recipe)
                    .generateAdvancement()
                    .build(output, recipe.getId());
        };
    }
}
