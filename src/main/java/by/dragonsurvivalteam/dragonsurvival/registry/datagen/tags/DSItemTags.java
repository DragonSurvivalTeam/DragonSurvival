package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.DarkDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.LightDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
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
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DSItemTags extends ItemTagsProvider {
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

        DSItems.REGISTRY.getEntries().forEach(holder -> {
            Item item = holder.value();

            if (item instanceof LightDragonArmorItem) {
                tag(LIGHT_ARMOR).add(item);
            } else if (item instanceof DarkDragonArmorItem) {
                tag(DARK_ARMOR).add(item);
            }
        });

        tag(CLAW_WEAPONS)
                .addTag(ItemTags.SWORDS)
                .addTag(Tags.Items.MELEE_WEAPON_TOOLS);

        tag(LIGHT_SOURCE)
                .add(Items.TORCH)
                .add(Items.LANTERN)
                .add(Items.GLOWSTONE);

        // TODO
        tag(ACTIVATES_DRAGON_BEACON).add(Items.NETHERITE_INGOT);

        tag(CHARRED_FOOD)
                .add(DSItems.CHARGED_COAL.value())
                .add(DSItems.CHARGED_SOUP.value())
                .add(DSItems.CHARRED_MEAT.value())
                .add(DSItems.CHARRED_MUSHROOM.value())
                .add(DSItems.CHARRED_SEAFOOD.value())
                .add(DSItems.CHARRED_VEGETABLE.value());

        tag(COLD_ITEMS)
                .add(Items.SNOWBALL)
                .add(Items.ICE)
                .add(Items.PACKED_ICE)
                .add(Items.SNOW)
                .add(Items.SNOW_BLOCK)
                .add(Items.POWDER_SNOW_BUCKET)
                .addOptional(ResourceLocation.fromNamespaceAndPath("immersive_weathering", "icicle"));

        tag(PRIMORDIAL_ANCHOR_FUEL).add(Items.ENDER_PEARL);

        // Used in enchantments
        tag(key("enchantable/chest_armor_and_elytra"))
                .addTag(ItemTags.CHEST_ARMOR_ENCHANTABLE)
                .add(Items.ELYTRA);

        copy(DSBlockTags.DRAGON_ALTARS, DRAGON_ALTARS);
        copy(DSBlockTags.DRAGON_TREASURES, DRAGON_TREASURES);

        copy(DSBlockTags.SMALL_WOODEN_DRAGON_DOORS, SMALL_WOODEN_DRAGON_DOORS);
        copy(DSBlockTags.WOODEN_DRAGON_DOORS, WOODEN_DRAGON_DOORS);
    }

    private void tagDragonSpeciesFood(@NotNull final HolderLookup.Provider provider) {
        provider.lookupOrThrow(DragonSpecies.REGISTRY).listElements().forEach(species -> {
            //noinspection DataFlowIssue -> key is present
            TagKey<Item> dragonFood = key(LangKey.FOOD.apply(species.getKey().location()));
            List<DietEntry> diet;

            // Diet data is not available at this point
            if (species.key() == BuiltInDragonSpecies.CAVE) {
                diet = DietEntryProvider.caveDiet();
            } else if (species.key() == BuiltInDragonSpecies.FOREST) {
                diet = DietEntryProvider.forestDiet();
            } else if (species.key() == BuiltInDragonSpecies.SEA) {
                diet = DietEntryProvider.seaDiet();
            } else {
                throw new IllegalStateException("Diet tag setup is missing for dragon species [" + species.getRegisteredName() + "]");
            }

            for (DietEntry entry : diet) {
                if (entry.items().startsWith("#")) {
                    tag(dragonFood).addOptionalTag(ResourceLocation.parse(entry.items().substring(1)));
                } else {
                    tag(dragonFood).addOptional(ResourceLocation.parse(entry.items()));
                }
            }
        });
    }

    private void addToVanillaTags() {
        DSItems.REGISTRY.getEntries().forEach(holder -> {
            Item item = holder.value();

            if (item instanceof ArmorItem armor) {
                switch (armor.getEquipmentSlot()) {
                    case HEAD -> tag(ItemTags.HEAD_ARMOR).add(item);
                    case CHEST -> tag(ItemTags.CHEST_ARMOR).add(item);
                    case FEET -> tag(ItemTags.FOOT_ARMOR).add(item);
                    case LEGS -> tag(ItemTags.LEG_ARMOR).add(item);
                }
            } else if (item instanceof SwordItem) {
                tag(ItemTags.SWORDS).add(item);
            }
        });
    }

    public static TagKey<Item> key(@NotNull final String name) {
        return ItemTags.create(DragonSurvival.res(name));
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Item tags";
    }
}