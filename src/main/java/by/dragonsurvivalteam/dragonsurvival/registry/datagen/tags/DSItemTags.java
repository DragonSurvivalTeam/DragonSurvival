package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.DarkDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.LightDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.DSRecipes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.DietEntryProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DSItemTags extends ItemTagsProvider {
    private static final TagKey<Item> HIDDEN_FROM_RECIPE_VIEWERS = commonKey("hidden_from_recipe_viewers");
    private static final TagKey<Item> HEAD_ARMOR = vanillaKey("head_armor");
    private static final TagKey<Item> CHEST_ARMOR = vanillaKey("chest_armor");
    private static final TagKey<Item> FOOT_ARMOR = vanillaKey("foot_armor");
    private static final TagKey<Item> LEG_ARMOR = vanillaKey("leg_armor");

    @Translation(comments = "Light Armor")
    public static final TagKey<Item> LIGHT_ARMOR = key("light_armor");
    @Translation(comments = "Dark Armor")
    public static final TagKey<Item> DARK_ARMOR = key("dark_armor");
    /** Items that are considered weapons for the claw tool slot */
    @Translation(comments = "Dragon Claw Weapons")
    public static final TagKey<Item> CLAW_WEAPONS = key("claw_weapons");

    @Translation(comments = "Light Source")
    public static final TagKey<Item> LIGHT_SOURCE = key("light_source");

    @Translation(comments = "Activates Dragon Beacon")
    public static final TagKey<Item> ACTIVATES_DRAGON_BEACON = key("activates_dragon_beacon");

    @Translation(comments = "Uncommon Armor (Texture)")
    public static final TagKey<Item> UNCOMMON_ARMOR = key("uncommon_armor");
    @Translation(comments = "Rare Armor (Texture)")
    public static final TagKey<Item> RARE_ARMOR = key("rare_armor");
    @Translation(comments = "Epic Armor (Texture)")
    public static final TagKey<Item> EPIC_ARMOR = key("epic_armor");

    // Used in recipes
    @Translation(comments = "Dragon Altars")
    public static final TagKey<Item> DRAGON_ALTARS = key("dragon_altars");
    @Translation(comments = "Dragon Treasures")
    public static final TagKey<Item> DRAGON_TREASURES = key("dragon_treasures");

    @Translation(comments = "Wooden Dragon Doors")
    public static final TagKey<Item> WOODEN_DRAGON_DOORS = key("wooden_dragon_doors");
    @Translation(comments = "Small Wooden Dragon Doors")
    public static final TagKey<Item> SMALL_WOODEN_DRAGON_DOORS = key("small_wooden_dragon_doors");

    @Translation(comments = "Charred Food")
    public static final TagKey<Item> CHARRED_FOOD = key("charred_food");
    @Translation(comments = "Cold Items")
    public static final TagKey<Item> COLD_ITEMS = key("cold_items");

    @Translation(comments = "Primordial Anchor Fuel")
    public static final TagKey<Item> PRIMORDIAL_ANCHOR_FUEL = key("primordial_anchor_fuel");

    public DSItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper helper) {
        super(output, provider, blockTags, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        addToVanillaTags();
        tagDragonSpeciesFood(provider);

        DSRecipes.PROXY_ITEMS.forEach(proxy -> tag(proxy.tag()).addOptional(proxy.id()));

        DSItems.REGISTRY.getEntries().forEach(holder -> {
            Item item = holder.get();

            if (item instanceof LightDragonArmorItem) {
                tag(LIGHT_ARMOR).add(item);
            } else if (item instanceof DarkDragonArmorItem) {
                tag(DARK_ARMOR).add(item);
            }
        });

        tag(CLAW_WEAPONS)
                .addTag(ItemTags.SWORDS)
                .addTag(ItemTags.AXES)
                .addTag(ItemTags.PICKAXES)
                .addTag(ItemTags.SHOVELS)
                .addTag(ItemTags.HOES);

        tag(LIGHT_SOURCE)
                .addTag(Tags.Items.DUSTS_GLOWSTONE)
                .add(Items.TORCH)
                .add(Items.LANTERN)
                .add(Items.GLOWSTONE);

        tag(ACTIVATES_DRAGON_BEACON)
                .add(DSItems.BEACON_ACTIVATOR.get());

        tag(UNCOMMON_ARMOR);
        tag(RARE_ARMOR);
        tag(EPIC_ARMOR);

        tag(CHARRED_FOOD)
                .add(DSItems.CHARGED_COAL.get())
                .add(DSItems.CHARGED_SOUP.get())
                .add(DSItems.CHARRED_MEAT.get())
                .add(DSItems.CHARRED_MUSHROOM.get())
                .add(DSItems.CHARRED_SEAFOOD.get())
                .add(DSItems.CHARRED_VEGETABLE.get());

        tag(COLD_ITEMS)
                .add(Items.SNOWBALL)
                .add(Items.ICE)
                .add(Items.PACKED_ICE)
                .add(Items.SNOW)
                .add(Items.SNOW_BLOCK)
                .add(Items.POWDER_SNOW_BUCKET)
                .addOptional(new ResourceLocation("immersive_weathering", "icicle"));

        tag(PRIMORDIAL_ANCHOR_FUEL).add(Items.ENDER_PEARL);

        // Used in enchantments
        tag(key("enchantable/chest_armor_and_elytra"))
                .addTag(Tags.Items.ARMORS_CHESTPLATES)
                .add(Items.ELYTRA);

        tag(HIDDEN_FROM_RECIPE_VIEWERS)
                .add(DSItems.MAGIC_STICK.get())
                .add(DSItems.BOLAS.get())
                .add(DSItems.HUNTING_NET.get())
                .add(DSItems.LIGHTNING_TEXTURE_ITEM.get())
                .add(DSItems.FOREST_ICON.get())
                .add(DSItems.CAVE_ICON.get())
                .add(DSItems.SEA_ICON.get())
                .add(DSItems.FOREST_FULL_ICON.get())
                .add(DSItems.CAVE_FULL_ICON.get())
                .add(DSItems.SEA_FULL_ICON.get())
                .add(DSItems.ACTIVATED_DRAGON_BEACON.get())
                .add(DSItems.CAVE_BEACON.get())
                .add(DSItems.FOREST_BEACON.get())
                .add(DSItems.SEA_BEACON.get());

        copy(DSBlockTags.DRAGON_BONES, HIDDEN_FROM_RECIPE_VIEWERS);

        copy(DSBlockTags.DRAGON_ALTARS, DRAGON_ALTARS);
        copy(DSBlockTags.DRAGON_TREASURES, DRAGON_TREASURES);

        copy(DSBlockTags.SMALL_WOODEN_DRAGON_DOORS, SMALL_WOODEN_DRAGON_DOORS);
        copy(DSBlockTags.WOODEN_DRAGON_DOORS, WOODEN_DRAGON_DOORS);
    }

    private void tagDragonSpeciesFood(@NotNull final HolderLookup.Provider provider) {
        provider.lookupOrThrow(DragonSpecies.REGISTRY).listElements().forEach(species -> {
            //noinspection DataFlowIssue -> key is present
            TagKey<Item> dragonFood = key(LangKey.FOOD.apply(species.unwrapKey().orElseThrow().location()));
            List<DietEntry> diet;

            // Diet data is not available at this point
            if (species.key() == BuiltInDragonSpecies.CAVE_DRAGON) {
                diet = DietEntryProvider.caveDiet();
            } else if (species.key() == BuiltInDragonSpecies.FOREST_DRAGON) {
                diet = DietEntryProvider.forestDiet();
            } else if (species.key() == BuiltInDragonSpecies.SEA_DRAGON) {
                diet = DietEntryProvider.seaDiet();
            } else {
                throw new IllegalStateException("Diet tag setup is missing for dragon species [" + species.unwrapKey().orElseThrow().location() + "]");
            }

            for (DietEntry entry : diet) {
                if (entry.items().startsWith("#")) {
                    tag(dragonFood).addOptionalTag(new ResourceLocation(entry.items().substring(1)));
                } else {
                    tag(dragonFood).addOptional(new ResourceLocation(entry.items()));
                }
            }
        });
    }

    private void addToVanillaTags() {
        DSItems.REGISTRY.getEntries().forEach(holder -> {
            Item item = holder.get();

            if (item instanceof ArmorItem armor) {
                switch (armor.getEquipmentSlot()) {
                    case HEAD -> tag(HEAD_ARMOR).add(item);
                    case CHEST -> tag(CHEST_ARMOR).add(item);
                    case FEET -> tag(FOOT_ARMOR).add(item);
                    case LEGS -> tag(LEG_ARMOR).add(item);
                }
            } else if (item instanceof SwordItem) {
                tag(ItemTags.SWORDS).add(item);
            }
        });
    }

    public static TagKey<Item> key(@NotNull final String name) {
        return ItemTags.create(DragonSurvival.res(name));
    }

    private static TagKey<Item> commonKey(final String name) {
        return ItemTags.create(new ResourceLocation("c", name));
    }

    private static TagKey<Item> vanillaKey(final String name) {
        return ItemTags.create(new ResourceLocation(name));
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Item tags";
    }
}
