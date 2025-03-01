package by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.*;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.ShotCrossbowTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.predicates.WeatherCheck;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings({"deprecation", "unused"}) // ignore
public class DSAdvancements implements AdvancementProvider.AdvancementGenerator {
    private HolderLookup.Provider registries;
    private Consumer<AdvancementHolder> saver;
    private ExistingFileHelper helper;

    @Override
    public void generate(@NotNull HolderLookup.Provider registries, @NotNull Consumer<AdvancementHolder> saver, @NotNull ExistingFileHelper helper) {
        this.registries = registries;
        this.saver = saver;
        this.helper = helper;

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        DSItems.ELDER_DRAGON_BONE.value(),
                        Component.translatable(LangKey.ROOT),
                        Component.empty(),
                        DragonSurvival.res("textures/block/stone_dragon_door_top.png"),
                        AdvancementType.GOAL,
                        false,
                        false,
                        false
                )
                .addCriterion("root", PlayerTrigger.TriggerInstance.tick())
                .save(saver, DragonSurvival.res("root"), helper);

        // --- Parent: root --- //

        AdvancementHolder beDragon = create(root, LangKey.BE_DRAGON, DSItems.STAR_BONE.value(), beDragon(), 12);
        buildBeDragonChildren(beDragon);

        AdvancementHolder collectDust = create(root, LangKey.COLLECT_DUST, Items.COAL_ORE, InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.ELDER_DRAGON_DUST.value()), 6);
        buildCollectDustChildren(collectDust);

        AdvancementHolder placeAltar = create(root, LangKey.PLACE_ALTAR, DSBlocks.STONE_DRAGON_ALTAR.value(), placeBlock(DSItemTags.DRAGON_ALTARS), 6);
        buildPlaceAltarChildren(placeAltar);

        AdvancementHolder pathChoice = create(root, LangKey.PATH_CHOICE, Items.OAK_SIGN, noItemInteract(EntityType.VILLAGER), 6);
        buildHunterAdvancements(pathChoice);
        buildLightAdvancements(pathChoice);
        buildDarkAdvancements(pathChoice);
    }

    private void buildDarkAdvancements(final AdvancementHolder parent) {
        // --- Parent: path_choice --- //

        /* TODO :: previously used the below item stack
            currently using sth. else because providing support for item stack displays for 1 advancement would be annoying
        "id": "minecraft:player_head",
        "components": {
            "minecraft:profile": {
                "name": "MHF_Villager"
            }
        }
        */
        AdvancementHolder affectedByHunterOmen = createWithToast(parent, LangKey.DARK_AFFECTED_BY_HUNTER_OMEN, Items.OMINOUS_BOTTLE, effectWithMinDuration(DSEffects.HUNTER_OMEN, 300), 6);

        // --- Parent: dark/affected_by_hunter_omen --- //

        AdvancementHolder stealFromVillager = createWithToast(affectedByHunterOmen, LangKey.DARK_STEAL_FROM_VILLAGER, DSItems.PARTISAN.value(), stealFromVillager(), 0);

        AdvancementHolder collectKey = createWithToast(affectedByHunterOmen, LangKey.DARK_COLLECT_KEY, DSItems.DARK_KEY.value(), InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_KEY.value()), 0);

        // --- Parent: dark/collect_key --- //

        AdvancementHolder openVault = createWithToast(collectKey, LangKey.DARK_OPEN_VAULT, DSBlocks.DARK_VAULT.value(), itemUsedOnBlock(DSBlocks.DARK_VAULT.value(), DSItems.DARK_KEY.value()), 10);

        // --- Parent: dark/open_vault --- //

        AdvancementHolder getArmorItem = createWithToast(openVault, LangKey.DARK_GET_ARMOR_ITEM, DSItems.DARK_DRAGON_HELMET.value(), List.of(
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_HELMET.value(), DSItems.DARK_DRAGON_CHESTPLATE.value(), DSItems.DARK_DRAGON_LEGGINGS.value(), DSItems.DARK_DRAGON_BOOTS.value())
        ), 0);

        // --- Parent: dark/get_armor_item --- //

        createWithToast(getArmorItem, LangKey.DARK_GET_ARMOR_SET, DSItems.DARK_DRAGON_HELMET.value(), List.of(
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_HELMET.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_CHESTPLATE.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_LEGGINGS.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_BOOTS.value())
        ), 0);
    }

    private void buildLightAdvancements(final AdvancementHolder parent) {
        // --- Parent: path_choice --- //

        AdvancementHolder dragonRiderWorkbench = create(parent, LangKey.LIGHT_DRAGON_RIDER_WORKBENCH, DSBlocks.DRAGON_RIDER_WORKBENCH.value(), crafted(DSBlocks.DRAGON_RIDER_WORKBENCH.value()), 6);

        // --- Parent: light/dragon_rider_workbench --- //

        AdvancementHolder collectKey = createWithToast(dragonRiderWorkbench, LangKey.LIGHT_COLLECT_KEY, DSItems.LIGHT_KEY.value(), InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_KEY.value()), 0);

        // --- Parent: light/collect_key --- //

        AdvancementHolder openVault = createWithToast(collectKey, LangKey.LIGHT_OPEN_VAULT, DSBlocks.LIGHT_VAULT.value(), itemUsedOnBlock(DSBlocks.LIGHT_VAULT.value(), DSItems.LIGHT_KEY.value()), 10);

        // --- Parent: light/open_vault --- //

        AdvancementHolder getArmorItem = createWithToast(openVault, LangKey.LIGHT_GET_ARMOR_ITEM, DSItems.LIGHT_DRAGON_HELMET.value(), List.of(
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_HELMET.value(), DSItems.LIGHT_DRAGON_CHESTPLATE.value(), DSItems.LIGHT_DRAGON_LEGGINGS.value(), DSItems.LIGHT_DRAGON_BOOTS.value())
        ), 0);

        // --- Parent: light/get_armor_item --- //

        createWithToast(getArmorItem, LangKey.LIGHT_GET_ARMOR_SET, DSItems.LIGHT_DRAGON_HELMET.value(), List.of(
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_HELMET.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_CHESTPLATE.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_LEGGINGS.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_BOOTS.value())
        ), 0);
    }

    private void buildHunterAdvancements(final AdvancementHolder parent) {
        // --- Parent: path_choice --- //

        AdvancementHolder promotion = createWithToast(parent, LangKey.HUNTER_PROMOTION, DSItems.SPEARMAN_PROMOTION.value(), itemInteract(DSEntities.HUNTER_SPEARMAN.value(), DSItems.SPEARMAN_PROMOTION.value()), 6);

        // --- Parent: hunter/promotion --- //

        AdvancementHolder collectKey = createWithToast(promotion, LangKey.HUNTER_COLLECT_KEY, DSItems.HUNTER_KEY.value(), InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.HUNTER_KEY.value()), 0);

        // --- Parent: hunter/collect_key --- //

        AdvancementHolder openVault = createWithToast(collectKey, LangKey.HUNTER_OPEN_VAULT, DSBlocks.HUNTER_VAULT.value(), itemUsedOnBlock(DSBlocks.HUNTER_VAULT.value(), DSItems.HUNTER_KEY.value()), 10);

        // --- Parent: hunter/open_vault --- //

        Holder.Reference<Enchantment> bolas = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(DSEnchantments.BOLAS);
        create(openVault, LangKey.HUNTER_FIRE_BOLAS, DSItems.BOLAS.value(), ShotCrossbowTrigger.TriggerInstance.shotCrossbow(
                Optional.of(ItemPredicate.Builder.item().of(Tags.Items.TOOLS_CROSSBOW).withSubPredicate(
                                ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.enchantments(
                                        List.of(new EnchantmentPredicate(bolas, MinMaxBounds.Ints.atLeast(1)))
                                )
                        ).build()
                )
        ), 0);
    }

    private void buildPlaceAltarChildren(final AdvancementHolder parent) {
        // --- Parent: place_altar --- //

        // TODO :: add a method that supports creating a new display info and supply it with an item stack with the proper data attachment for the dragon species
        AdvancementHolder beCaveDragon = createWithToast(parent, LangKey.CAVE_BE_DRAGON, DSItems.DRAGON_SOUL.value(), beDragon(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON)), 12);
        buildBeCaveDragonChildren(beCaveDragon);

        AdvancementHolder beSeaDragon = createWithToast(parent, LangKey.SEA_BE_DRAGON, DSItems.DRAGON_SOUL.value(), beDragon(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)), 12);
        buildBeSeaDragonChildren(beSeaDragon);

        AdvancementHolder beForestDragon = createWithToast(parent, LangKey.FOREST_BE_DRAGON, DSItems.DRAGON_SOUL.value(), beDragon(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON)), 12);
        buildBeForestDragonChildren(beForestDragon);
    }

    private void buildBeCaveDragonChildren(final AdvancementHolder parent) {
        // --- Parent: cave/be_dragon --- //

        AdvancementHolder rockEater = create(parent, LangKey.CAVE_ROCK_EATER, DSItems.CHARGED_COAL.value(), List.of(
                consumeItem(DSItems.CHARGED_COAL.value()),
                consumeItem(DSItems.CHARGED_SOUP.value()),
                consumeItem(DSItems.CHARRED_MEAT.value()),
                consumeItem(DSItems.CHARRED_SEAFOOD.value()),
                consumeItem(DSItems.HOT_DRAGON_ROD.value()),
                consumeItem(DSItems.EXPLOSIVE_COPPER.value()),
                consumeItem(DSItems.QUARTZ_EXPLOSIVE_COPPER.value()),
                consumeItem(DSItems.DOUBLE_QUARTZ.value()),
                consumeItem(DSItems.CAVE_DRAGON_TREAT.value())
        ), 60);

        AdvancementHolder swimInLava = create(parent, LangKey.CAVE_SWIM_IN_LAVA, Items.LAVA_BUCKET, location(
                Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON)).located(isInFluid(FluidTags.LAVA))
        ), 20);

        // --- Parent: cave/rock_eater --- //

        create(rockEater, LangKey.CAVE_WATER_SAFETY, DSItems.CHARGED_SOUP.value(), location(
                Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON))
                        .located(isInFluid(FluidTags.WATER))
                        .effects(MobEffectsPredicate.Builder.effects().and(DSEffects.FIRE))
        ), 40);

        // --- Parent: cave/swim_in_lava --- //

        AdvancementHolder diamondsInLava = create(swimInLava, LangKey.CAVE_DIAMONDS_IN_LAVA, Items.DIAMOND_ORE, mineBlockInLava(Tags.Blocks.ORES_DIAMOND), 40);

        createWithToast(diamondsInLava, LangKey.CAVE_GO_HOME, Items.NETHER_BRICK_STAIRS, location(
                Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON))
                        .located(inDimension(Level.NETHER).setFluid(fluid(FluidTags.LAVA)))
                        .effects(hasEffect(DSEffects.LAVA_VISION))
        ), 20);
    }

    private void buildBeSeaDragonChildren(final AdvancementHolder parent) {
        // --- Parent: sea/be_dragon --- //

        AdvancementHolder lootShipwreck = create(parent, LangKey.SEA_LOOT_SHIPWRECK, Items.HEART_OF_THE_SEA, List.of(
                location(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)).located(inStructure(registries.holderOrThrow(BuiltinStructures.SHIPWRECK)))),
                location(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)).located(inStructure(registries.holderOrThrow(BuiltinStructures.SHIPWRECK_BEACHED))))
        ), 20);

        AdvancementHolder rainDancing = create(parent, LangKey.SEA_RAIN_DANCING, Items.WATER_BUCKET, List.of(
                location(ContextAwarePredicate.create(entityCondition(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)).build()), WeatherCheck.weather().setRaining(true).build())),
                location(ContextAwarePredicate.create(entityCondition(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)).build()), WeatherCheck.weather().setThundering(true).build()))
        ), 30);

        // --- Parent: sea/loot_shipwreck --- //

        create(lootShipwreck, LangKey.SEA_FISH_EATER, DSItems.SEASONED_FISH.value(), List.of(
                consumeItem(Items.KELP),
                consumeItem(DSItems.SEASONED_FISH.value()),
                consumeItem(DSItems.GOLDEN_CORAL_PUFFERFISH.value()),
                consumeItem(DSItems.FROZEN_RAW_FISH.value()),
                consumeItem(DSItems.GOLDEN_TURTLE_EGG.value()),
                consumeItem(DSItems.SEA_DRAGON_TREAT.value())
        ), 80);

        // --- Parent: sea/rain_dancing --- //

        AdvancementHolder placeSnowInNether = create(rainDancing, LangKey.SEA_PLACE_SNOW_IN_NETHER, Items.SNOW_BLOCK, placeBlockAsDragon(
                Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)).located(inDimension(Level.NETHER)), Blocks.SNOW_BLOCK
        ), 16);

        // --- Parent: sea/place_snow_in_nether --- //

        create(placeSnowInNether, LangKey.SEA_PEACE_IN_NETHER, Items.CAULDRON, location(
                Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON))
                        .effects(hasEffect(DSEffects.PEACE))
                        .located(inDimension(Level.NETHER))
        ), 0);
    }

    private void buildBeForestDragonChildren(final AdvancementHolder parent) {
        // --- Parent: forest/be_dragon --- //

        AdvancementHolder standOnSweetBerries = create(parent, LangKey.FOREST_STAND_ON_SWEET_BERRIES, Items.SWEET_BERRIES, location(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON)).steppingOn(block(Blocks.SWEET_BERRY_BUSH))), 30);

        // --- Parent: forest/stand_on_sweet_berries --- //

        create(standOnSweetBerries, LangKey.FOREST_PREVENT_DARKNESS_PENALTY, DSItems.LUMINOUS_OINTMENT.value(), location(
                Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON))
                        .located(light(MinMaxBounds.Ints.between(0, 3)))
                        .effects(MobEffectsPredicate.Builder.effects().and(DSEffects.MAGIC))
        ), 40);

        AdvancementHolder poisonousPotato = create(parent, LangKey.FOREST_POISONOUS_POTATO, Items.POISONOUS_POTATO, convertPotato(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON))), 16);

        // --- Parent: forest/poisonous_potato --- //

        AdvancementHolder meatEater = create(poisonousPotato, LangKey.FOREST_MEAT_EATER, DSItems.MEAT_WILD_BERRIES.value(), List.of(
                consumeItem(DSItems.SWEET_SOUR_RABBIT.value()),
                consumeItem(DSItems.LUMINOUS_OINTMENT.value()),
                consumeItem(DSItems.DIAMOND_CHORUS.value()),
                consumeItem(DSItems.SMELLY_MEAT_PORRIDGE.value()),
                consumeItem(DSItems.MEAT_WILD_BERRIES.value()),
                consumeItem(DSItems.MEAT_CHORUS_MIX.value()),
                consumeItem(DSItems.FOREST_DRAGON_TREAT.value())
        ), 60);

        // --- Parent: forest/meat_eater --- //

        create(meatEater, LangKey.FOREST_TRANSPLANT_CHORUS_FRUIT, DSItems.DIAMOND_CHORUS.value(), placeBlockAsDragon(
                Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON)).located(inDimension(Level.OVERWORLD)), Blocks.CHORUS_FLOWER
        ), 90);
    }

    private void buildBeDragonChildren(final AdvancementHolder parent) {
        // --- Parent: be_dragon --- //

        AdvancementHolder stopNaturalGrowth = createWithToast(parent, LangKey.STOP_NATURAL_GROWTH, DSItems.STAR_HEART.value(), useStarHeart(), 30);

        // --- Parent: stop_natural_growth --- //

        createWithToast(stopNaturalGrowth, LangKey.USE_DRAGON_SOUL, DSItems.DRAGON_SOUL.value(), useDragonSoul(), 120);
    }

    private void buildCollectDustChildren(final AdvancementHolder parent) {
        // --- Parent: collect_dust --- //

        AdvancementHolder beYoungDragon = createWithToast(parent, LangKey.BE_YOUNG_DRAGON, DSItems.DRAGON_HEART_SHARD.value(), beDragon(DragonStages.young), 12);
        buildBeYoungDragonChildren(beYoungDragon);

        AdvancementHolder sleepOnTreasure = createWithAnnouncement(parent, LangKey.SLEEP_ON_TREASURE, Items.GOLD_NUGGET, sleepOnTreasure(10), 10);
        buildSleepOnTreasureChildren(sleepOnTreasure);

        TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("dragon_skeletons")); // FIXME :: use tag from data generation
        AdvancementHolder findBones = createWithToast(parent, LangKey.FIND_BONES, DSItems.STAR_BONE.value(), PlayerTrigger.TriggerInstance.located(inStructure(tag)), 12);
        buildFindBonesChildren(findBones);

        AdvancementHolder useMemoryBlock = createWithToast(parent, LangKey.USE_MEMORY_BLOCK, DSBlocks.DRAGON_MEMORY_BLOCK.value(), itemUsedOnBlock(DSBlocks.DRAGON_MEMORY_BLOCK.value(), DSBlocks.DRAGON_BEACON.value()), 10);
        buildUseMemoryBlockChildren(useMemoryBlock);
    }

    private void buildFindBonesChildren(final AdvancementHolder parent) {
        // --- Parent: find_bones --- //
        TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("light_treasure")); // FIXME :: use tag from data generation
        AdvancementHolder findOverworldStructure = createWithToast(parent, LangKey.FIND_OVERWORLD_STRUCTURES, Blocks.GRASS_BLOCK, PlayerTrigger.TriggerInstance.located(inStructure(tag)), 24);

        TagKey<Structure> tag2 = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("dark_treasure")); // FIXME :: use tag from data generation
        AdvancementHolder findNetherStructure = createWithToast(findOverworldStructure, LangKey.FIND_NETHER_STRUCTURES, Blocks.NETHERRACK, PlayerTrigger.TriggerInstance.located(inStructure(tag2)), 36);

        AdvancementHolder findEndPlatform = createWithToast(findNetherStructure, LangKey.FIND_END_PLATFORM, Items.ENDER_PEARL, List.of(PlayerTrigger.TriggerInstance.located(inDimension(Level.END)), beDragon()), 32);

        TagKey<Structure> tag3 = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("treasure_end")); // FIXME :: use tag from data generation
        createWithToast(findEndPlatform, LangKey.FIND_END_STRUCTURES, DSItems.SPIN_GRANT_ITEM.value(), PlayerTrigger.TriggerInstance.located(inStructure(tag3)), 64);
    }

    private void buildUseMemoryBlockChildren(final AdvancementHolder parent) {
        // --- Parent: use_memory_block --- //

        AdvancementHolder changeBeacon = createWithToast(parent, LangKey.CHANGE_BEACON, DSItems.BEACON_ACTIVATOR.value(), itemUsedOnBlock(DSBlocks.DRAGON_BEACON.value(), DSItems.BEACON_ACTIVATOR.value()), 10);

        // --- Parent: change_beacon --- //

        createWithToast(changeBeacon, LangKey.GET_ALL_BEACONS, DSItems.ELDER_DRAGON_DUST.value(), List.of(
                effectWithMinDuration(DSEffects.PEACE, Functions.secondsToTicks(20)),
                effectWithMinDuration(DSEffects.FIRE, Functions.secondsToTicks(20)),
                effectWithMinDuration(DSEffects.MAGIC, Functions.secondsToTicks(20))
        ), 0);
    }

    private void buildSleepOnTreasureChildren(final AdvancementHolder parent) {
        // --- Parent: sleep_on_treasure --- //

        AdvancementHolder sleepOnHoard = createWithToast(parent, LangKey.SLEEP_ON_HOARD, Items.GOLD_INGOT, sleepOnTreasure(100), 40);

        // --- Parent: sleep_on_hoard --- //

        createWithToast(sleepOnHoard, LangKey.SLEEP_ON_MASSIVE_HOARD, DSBlocks.GOLD_DRAGON_TREASURE.value(), sleepOnTreasure(240), 120);
    }

    private void buildBeYoungDragonChildren(final AdvancementHolder parent) {
        // --- Parent: be_young_dragon --- //

        AdvancementHolder beAdultDragon = createWithToast(parent, LangKey.BE_ADULT_DRAGON, DSItems.WEAK_DRAGON_HEART.value(), beDragon(DragonStages.adult), 0);

        // --- Parent: be_adult_dragon --- //

        AdvancementHolder collectHeartFromMonster = create(beAdultDragon, LangKey.COLLECT_HEART_FROM_MONSTER, DSItems.ELDER_DRAGON_HEART.value(), InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.ELDER_DRAGON_HEART.value()), 6);

        // --- Parent: collect_heart_from_monster --- //

        // TODO :: used beacon texture previously
        AdvancementHolder beOldCaveDragon = createWithToast(collectHeartFromMonster, LangKey.CAVE_BE_OLD_DRAGON, Items.DIRT, beDragon(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON), registries.holderOrThrow(DragonStages.adult), 1), 120);

        // --- Parent: cave/be_old_dragon --- //

        createWithToast(beOldCaveDragon, LangKey.CAVE_MASTER_ALL_PASSIVES, DSBlocks.CAVE_SOURCE_OF_MAGIC.value(), List.of(
                upgradeAbilityMax(registries.holderOrThrow(CaveDragonAbilities.BURN)),
                upgradeAbilityMax(registries.holderOrThrow(CaveDragonAbilities.CAVE_ATHLETICS)),
                upgradeAbilityMax(registries.holderOrThrow(CaveDragonAbilities.CONTRAST_SHOWER)),
                upgradeAbilityMax(registries.holderOrThrow(CaveDragonAbilities.CAVE_MAGIC))
        ), 150);

        // TODO :: used beacon texture previously
        AdvancementHolder beOldSeaDragon = createWithToast(collectHeartFromMonster, LangKey.SEA_BE_OLD_DRAGON, Items.DIRT, beDragon(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON), registries.holderOrThrow(DragonStages.adult), 1), 120);

        // --- Parent: sea/be_old_dragon --- //

        createWithToast(beOldSeaDragon, LangKey.SEA_MASTER_ALL_PASSIVES, DSBlocks.SEA_SOURCE_OF_MAGIC.value(), List.of(
                upgradeAbilityMax(registries.holderOrThrow(SeaDragonAbilities.SPECTRAL_IMPACT)),
                upgradeAbilityMax(registries.holderOrThrow(SeaDragonAbilities.SEA_ATHLETICS)),
                upgradeAbilityMax(registries.holderOrThrow(SeaDragonAbilities.HYDRATION)),
                upgradeAbilityMax(registries.holderOrThrow(SeaDragonAbilities.SEA_MAGIC))
        ), 150);

        // TODO :: used beacon texture previously
        AdvancementHolder beOldForestDragon = createWithToast(collectHeartFromMonster, LangKey.FOREST_BE_OLD_DRAGON, Items.DIRT, beDragon(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON), registries.holderOrThrow(DragonStages.adult), 1), 120);

        // --- Parent: forest/be_old_dragon --- //

        createWithToast(beOldForestDragon, LangKey.FOREST_MASTER_ALL_PASSIVES, DSBlocks.FOREST_SOURCE_OF_MAGIC.value(), List.of(
                upgradeAbilityMax(registries.holderOrThrow(ForestDragonAbilities.CLIFFHANGER)),
                upgradeAbilityMax(registries.holderOrThrow(ForestDragonAbilities.FOREST_ATHLETICS)),
                upgradeAbilityMax(registries.holderOrThrow(ForestDragonAbilities.LIGHT_IN_DARKNESS)),
                upgradeAbilityMax(registries.holderOrThrow(ForestDragonAbilities.FOREST_MAGIC))
        ), 150);
    }

    public AdvancementHolder createWithAnnouncement(final AdvancementHolder parent, final String path, final ItemLike displayItem, final Criterion<?> criterion, int experience) {
        return create(parent, path, displayItem, List.of(criterion), experience, false, true, false);
    }

    public AdvancementHolder createWithAnnouncement(final AdvancementHolder parent, final String path, final ItemLike displayItem, final List<Criterion<?>> criteria, int experience) {
        return create(parent, path, displayItem, criteria, experience, false, true, false);
    }

    public AdvancementHolder createWithToast(final AdvancementHolder parent, final String path, final ItemLike displayItem, final Criterion<?> criterion, int experience) {
        return create(parent, path, displayItem, List.of(criterion), experience, true, true, false);
    }

    public AdvancementHolder createWithToast(final AdvancementHolder parent, final String path, final ItemLike displayItem, final List<Criterion<?>> criteria, int experience) {
        return create(parent, path, displayItem, criteria, experience, true, true, false);
    }

    public AdvancementHolder create(final AdvancementHolder parent, final String path, final ItemLike displayItem, final Criterion<?> criterion, int experience) {
        return create(parent, path, displayItem, List.of(criterion), experience, false, false, false);
    }

    public AdvancementHolder create(final AdvancementHolder parent, final String path, final ItemLike displayItem, final List<Criterion<?>> criteria, int experience) {
        return create(parent, path, displayItem, criteria, experience, false, false, false);
    }

    public AdvancementHolder create(final AdvancementHolder parent, final String path, final ItemLike displayItem, final List<Criterion<?>> criteria, int experience, boolean showToast, boolean announceChat, boolean hidden) {
        Advancement.Builder advancement = Advancement.Builder.advancement()
                .parent(parent)
                .display(
                        displayItem,
                        Component.translatable(Translation.Type.ADVANCEMENT.wrap(path)),
                        Component.translatable(Translation.Type.ADVANCEMENT_DESCRIPTION.wrap(path)),
                        null,
                        AdvancementType.TASK,
                        showToast,
                        announceChat,
                        hidden
                );

        for (int i = 0; i < criteria.size(); i++) {
            advancement.addCriterion("criterion_" + i, criteria.get(i));
        }

        if (experience > 0) {
            advancement.rewards(AdvancementRewards.Builder.experience(experience));
        }

        return advancement.save(saver, DragonSurvival.res(path), helper);
    }

    // --- Misc --- //

    private Criterion<PlayerTrigger.TriggerInstance> tick(final EntityPredicate predicate) {
        return CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(predicate))));
    }

    @SuppressWarnings("deprecation") // ignore
    private Criterion<RecipeCraftedTrigger.TriggerInstance> crafted(final ItemLike item) {
        return RecipeCraftedTrigger.TriggerInstance.craftedItem(item.asItem().builtInRegistryHolder().key().location());
    }

    private LootItemCondition entityCondition(final EntityPredicate predicate) {
        return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, predicate).build();
    }

    @SafeVarargs
    private MobEffectsPredicate.Builder hasEffect(final Holder<MobEffect>... effects) {
        MobEffectsPredicate.Builder builder = MobEffectsPredicate.Builder.effects();

        for (Holder<MobEffect> effect : effects) {
            builder.and(effect);
        }

        return builder;
    }

    private LocationPredicate.Builder inDimension(final ResourceKey<Level> dimension) {
        return LocationPredicate.Builder.inDimension(dimension);
    }

    private LocationPredicate.Builder inStructure(final TagKey<Structure> tag) {
        HolderSet.Named<Structure> set = registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(tag);
        return LocationPredicate.Builder.location().setStructures(set);
    }

    private LocationPredicate.Builder inStructure(final Holder<Structure> structure) {
        return LocationPredicate.Builder.inStructure(structure);
    }

    private LocationPredicate.Builder isInFluid(final TagKey<Fluid> fluids) {
        return LocationPredicate.Builder.location().setFluid(fluid(fluids));
    }

    private Optional<ContextAwarePredicate> caveDragonInLava() {
        return Optional.of(EntityPredicate.wrap(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON)).located(isInFluid(FluidTags.LAVA))));
    }

    private FluidPredicate.Builder fluid(final TagKey<Fluid> fluids) {
        return FluidPredicate.Builder.fluid().of(registries.lookupOrThrow(Registries.FLUID).getOrThrow(fluids));
    }

    @SuppressWarnings("SameParameterValue") // ignore
    private LocationPredicate.Builder block(final Block block) {
        return LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block));
    }

    private LocationPredicate.Builder light(final MinMaxBounds.Ints bounds) {
        return LocationPredicate.Builder.location().setLight(LightPredicate.Builder.light().setComposite(bounds));
    }

    @SuppressWarnings("deprecation") // ignore
    public Criterion<InventoryChangeTrigger.TriggerInstance> dragonHasItem(final Holder<DragonSpecies> dragonSpecies, final ItemLike... items) {
        List<ItemPredicate> predicates = new ArrayList<>();

        for (ItemLike item : items) {
            predicates.add(new ItemPredicate(Optional.of(HolderSet.direct(item.asItem().builtInRegistryHolder())), MinMaxBounds.Ints.ANY, DataComponentPredicate.EMPTY, Map.of()));
        }

        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(new InventoryChangeTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(Condition.dragonSpecies(dragonSpecies))), InventoryChangeTrigger.TriggerInstance.Slots.ANY, predicates));
    }

    public Criterion<PlayerTrigger.TriggerInstance> location(final ContextAwarePredicate predicate) {
        return CriteriaTriggers.LOCATION.createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(predicate)));
    }

    public Criterion<PlayerTrigger.TriggerInstance> location(final EntityPredicate.Builder builder) {
        return CriteriaTriggers.LOCATION.createCriterion(new PlayerTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(builder))));
    }

    public Criterion<ConsumeItemTrigger.TriggerInstance> consumeItem(final Item... items) {
        return ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(items));
    }

    public Criterion<UsingItemTrigger.TriggerInstance> usingItem(final Item item) {
        return CriteriaTriggers.USING_ITEM.createCriterion(new UsingItemTrigger.TriggerInstance(Optional.empty(), Optional.of(ItemPredicate.Builder.item().of(item).build())));
    }

    public Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placeBlockAsDragon(final EntityPredicate.Builder builder, final Block block) {
        ContextAwarePredicate blockPredicate = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).build());
        return CriteriaTriggers.PLACED_BLOCK.createCriterion(new ItemUsedOnLocationTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(builder)), Optional.of(blockPredicate)));
    }

    public Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placeBlock(final Block block) {
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block);
    }

    public Criterion<ItemUsedOnLocationTrigger.TriggerInstance> placeBlock(final TagKey<Item> blocks) {
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(MatchTool.toolMatches(ItemPredicate.Builder.item().of(blocks)));
    }

    public Criterion<ItemUsedOnLocationTrigger.TriggerInstance> itemUsedOnBlock(final Block block, final ItemLike... items) {
        return ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block)), ItemPredicate.Builder.item().of(items));
    }

    public Criterion<ItemUsedOnLocationTrigger.TriggerInstance> itemUsedOnBlock(final Block block, final TagKey<Item> items) {
        return ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block)), ItemPredicate.Builder.item().of(items));
    }

    public Criterion<EffectsChangedTrigger.TriggerInstance> effectWithMinDuration(final Holder<MobEffect> effect, int minDuration) {
        return EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(effect, new MobEffectsPredicate.MobEffectInstancePredicate(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.atLeast(minDuration), Optional.empty(), Optional.empty())));
    }

    public Criterion<PlayerInteractTrigger.TriggerInstance> itemInteract(final EntityType<?> type, final ItemLike... items) {
        Optional<ContextAwarePredicate> entityPredicate = Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(type)));
        Optional<ItemPredicate> itemPredicate = Optional.of(ItemPredicate.Builder.item().of(items).build());
        return CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.createCriterion(new PlayerInteractTrigger.TriggerInstance(Optional.empty(), itemPredicate, entityPredicate));
    }

    public Criterion<PlayerInteractTrigger.TriggerInstance> noItemInteract(final EntityType<?> type) {
        return CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.createCriterion(new PlayerInteractTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(type)))));
    }

    // --- Convert Potato --- //

    public Criterion<ConvertItemFromAbility.TriggerInstance> convertPotato(final EntityPredicate.Builder builder) {
        return DSAdvancementTriggers.CONVERT_ITEM_FROM_ABILITY.get().createCriterion(new ConvertItemFromAbility.TriggerInstance(Optional.of(EntityPredicate.wrap(builder.build())), Items.POTATO.builtInRegistryHolder(), Items.POISONOUS_POTATO.builtInRegistryHolder()));
    }

    // --- Mine Block Under Lava --- //

    @SuppressWarnings("deprecation") // ignore
    public Criterion<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> mineBlockInLava(final Block... blocks) {
        return DSAdvancementTriggers.MINE_BLOCK_UNDER_LAVA.get().createCriterion(new MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance(caveDragonInLava(), Optional.of(HolderSet.direct(Block::builtInRegistryHolder, blocks))));
    }

    public Criterion<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> mineBlockInLava(final TagKey<Block> blocks) {
        return DSAdvancementTriggers.MINE_BLOCK_UNDER_LAVA.get().createCriterion(new MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance(caveDragonInLava(), Optional.of(BuiltInRegistries.BLOCK.getOrCreateTag(blocks))));
    }

    // --- Use Dragon Soul --- //

    public Criterion<UseDragonSoulTrigger.UseDragonSoulInstance> useDragonSoul() {
        return DSAdvancementTriggers.USE_DRAGON_SOUL.get().createCriterion(new UseDragonSoulTrigger.UseDragonSoulInstance(Optional.empty()));
    }

    // --- Use Star Heart --- //

    public Criterion<StopNaturalGrowthTrigger.Instance> useStarHeart() {
        return DSAdvancementTriggers.STOP_NATURAL_GROWTH.get().createCriterion(new StopNaturalGrowthTrigger.Instance(Optional.empty()));
    }


    // --- Sleep On Treasure --- //

    public Criterion<SleepOnTreasureTrigger.SleepOnTreasureInstance> sleepOnTreasure(int nearbyTreasureAmount) {
        return DSAdvancementTriggers.SLEEP_ON_TREASURE.get().createCriterion(new SleepOnTreasureTrigger.SleepOnTreasureInstance(Optional.empty(), Optional.of(nearbyTreasureAmount)));
    }

    // --- Upgrade Ability --- //

    public Criterion<UpgradeAbilityTrigger.UpgradeAbilityInstance> upgradeAbilityMax(final Holder<DragonAbility> ability) {
        return upgradeAbility(ability.getKey(), ability.value().getMaxLevel());
    }

    public Criterion<UpgradeAbilityTrigger.UpgradeAbilityInstance> upgradeAbility(final ResourceKey<DragonAbility> ability, int level) {
        return DSAdvancementTriggers.UPGRADE_ABILITY.get().createCriterion(new UpgradeAbilityTrigger.UpgradeAbilityInstance(Optional.empty(), Optional.of(ability), Optional.of(level)));
    }

    // --- Be Dragon --- //

    public Criterion<BeDragonTrigger.Instance> beDragon() {
        return DSAdvancementTriggers.BE_DRAGON.get().createCriterion(new BeDragonTrigger.Instance(Optional.empty()));
    }

    public Criterion<BeDragonTrigger.Instance> beDragon(final Holder<DragonSpecies> type) {
        return beDragon(Condition.dragonSpecies(type));
    }

    public Criterion<BeDragonTrigger.Instance> beDragon(double growth) {
        return beDragon(Condition.dragonSizeAtLeast(growth));
    }

    public Criterion<BeDragonTrigger.Instance> beDragon(final ResourceKey<DragonStage> dragonStage) {
        return beDragon(Condition.dragonStage(registries.holderOrThrow(dragonStage)));
    }

    public Criterion<BeDragonTrigger.Instance> beDragon(final Holder<DragonSpecies> species, final Holder<DragonStage> dragonStage, double progress) {
        return beDragon(EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().species(species).stage(dragonStage, MinMaxBounds.Doubles.atLeast(progress)).build()));
    }

    public Criterion<BeDragonTrigger.Instance> beDragon(final EntityPredicate.Builder builder) {
        return DSAdvancementTriggers.BE_DRAGON.get().createCriterion(new BeDragonTrigger.Instance(Optional.of(EntityPredicate.wrap(builder.build()))));
    }

    // -- Steal From Villagers -- //

    public Criterion<StealFromVillagerTrigger.Instance> stealFromVillager() {
        return DSAdvancementTriggers.STEAL_FROM_VILLAGER.get().createCriterion(new StealFromVillagerTrigger.Instance(Optional.empty()));
    }
}
