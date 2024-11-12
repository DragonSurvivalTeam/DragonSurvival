package by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.*;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.BurnAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.CaveAthleticsAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.CaveMagicAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.ContrastShowerAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.passive.CliffhangerAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.passive.ForestAthleticsAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.passive.ForestMagicAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.passive.LightInDarknessAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.SeaAthleticsAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.SeaMagicAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.SpectralImpactAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.WaterAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.*;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.StructureTags;
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
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements.LangKey.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
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
                        Component.translatable(ROOT),
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

        AdvancementHolder beDragon = create(root, BE_DRAGON, DSItems.STAR_BONE.value(), beDragon(), 12);
        buildBeDragonChildren(beDragon);

        AdvancementHolder collectDust = create(root, COLLECT_DUST, Items.COAL_ORE, InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.ELDER_DRAGON_DUST.value()), 6);
        buildCollectDustChildren(collectDust);

        AdvancementHolder placeAltar = create(root, PLACE_ALTAR, DSBlocks.STONE_DRAGON_ALTAR.value(), placeBlock(DSItemTags.DRAGON_ALTARS), 6);
        buildPlaceAltarChildren(placeAltar);

        AdvancementHolder pathChoice = create(root, PATH_CHOICE, Items.OAK_SIGN, noItemInteract(EntityType.VILLAGER), 6);
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
        AdvancementHolder affectedByHunterOmen = createWithToast(parent, DARK_AFFECTED_BY_HUNTER_OMEN, Items.OMINOUS_BOTTLE, effectWithMinDuration(DSEffects.HUNTER_OMEN, 300), 6);

        // --- Parent: dark/affected_by_hunter_omen --- //

        AdvancementHolder collectKey = createWithToast(affectedByHunterOmen, DARK_COLLECT_KEY, DSItems.DARK_KEY.value(), InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_KEY.value()), 0);

        // --- Parent: dark/collect_key --- //

        AdvancementHolder openVault = createWithToast(collectKey, DARK_OPEN_VAULT, DSBlocks.DARK_VAULT.value(), itemUsedOnBlock(DSBlocks.DARK_VAULT.value(), DSItems.DARK_KEY.value()), 10);

        // --- Parent: dark/open_vault --- //

        AdvancementHolder getArmorItem = createWithToast(openVault, DARK_GET_ARMOR_ITEM, DSItems.DARK_DRAGON_HELMET.value(), List.of(
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_HELMET.value(), DSItems.DARK_DRAGON_CHESTPLATE.value(), DSItems.DARK_DRAGON_LEGGINGS.value(), DSItems.DARK_DRAGON_BOOTS.value())
        ), 0);

        // --- Parent: dark/get_armor_item --- //

        createWithToast(getArmorItem, DARK_GET_ARMOR_SET, DSItems.DARK_DRAGON_HELMET.value(), List.of(
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_HELMET.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_CHESTPLATE.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_LEGGINGS.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_BOOTS.value())
        ), 0);
    }

    private void buildLightAdvancements(final AdvancementHolder parent) {
        // --- Parent: path_choice --- //

        AdvancementHolder dragonRiderWorkbench = create(parent, LIGHT_DRAGON_RIDER_WORKBENCH, DSBlocks.DRAGON_RIDER_WORKBENCH.value(), crafted(DSBlocks.DRAGON_RIDER_WORKBENCH.value()), 6);

        // --- Parent: light/dragon_rider_workbench --- //

        AdvancementHolder collectKey = createWithToast(dragonRiderWorkbench, LIGHT_COLLECT_KEY, DSItems.LIGHT_KEY.value(), InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_KEY.value()), 0);

        // --- Parent: light/collect_key --- //

        AdvancementHolder openVault = createWithToast(collectKey, LIGHT_OPEN_VAULT, DSBlocks.LIGHT_VAULT.value(), itemUsedOnBlock(DSBlocks.LIGHT_VAULT.value(), DSItems.LIGHT_KEY.value()), 10);

        // --- Parent: light/open_vault --- //

        AdvancementHolder getArmorItem = createWithToast(openVault, LIGHT_GET_ARMOR_ITEM, DSItems.LIGHT_DRAGON_HELMET.value(), List.of(
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_HELMET.value(), DSItems.LIGHT_DRAGON_CHESTPLATE.value(), DSItems.LIGHT_DRAGON_LEGGINGS.value(), DSItems.LIGHT_DRAGON_BOOTS.value())
        ), 0);

        // --- Parent: light/get_armor_item --- //

        createWithToast(getArmorItem, LIGHT_GET_ARMOR_SET, DSItems.LIGHT_DRAGON_HELMET.value(), List.of(
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_HELMET.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_CHESTPLATE.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_LEGGINGS.value()),
                InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_BOOTS.value())
        ), 0);
    }

    private void buildHunterAdvancements(final AdvancementHolder parent) {
        // --- Parent: path_choice --- //

        AdvancementHolder promotion = createWithToast(parent, HUNTER_PROMOTION, DSItems.SPEARMAN_PROMOTION.value(), itemInteract(DSEntities.HUNTER_SPEARMAN.value(), DSItems.SPEARMAN_PROMOTION.value()), 6);

        // --- Parent: hunter/promotion --- //

        AdvancementHolder collectKey = createWithToast(promotion, HUNTER_COLLECT_KEY, DSItems.HUNTER_KEY.value(), InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.HUNTER_KEY.value()), 0);

        // --- Parent: hunter/collect_key --- //

        AdvancementHolder openVault = createWithToast(collectKey, HUNTER_OPEN_VAULT, DSBlocks.HUNTER_VAULT.value(), itemUsedOnBlock(DSBlocks.HUNTER_VAULT.value(), DSItems.HUNTER_KEY.value()), 10);

        // --- Parent: hunter/open_vault --- //

        Holder.Reference<Enchantment> bolas = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(DSEnchantments.BOLAS);
        create(openVault, HUNTER_FIRE_BOLAS, DSItems.BOLAS.value(), ShotCrossbowTrigger.TriggerInstance.shotCrossbow(
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

        // TODO :: add a method that supports creating a new display info and supply it with an item stack with the proper data attachment for the dragon type
        AdvancementHolder beCaveDragon = createWithToast(parent, CAVE_BE_DRAGON, DSItems.DRAGON_SOUL.value(), beDragon(DragonTypes.CAVE), 12);
        buildBeCaveDragonChildren(beCaveDragon);

        AdvancementHolder beSeaDragon = createWithToast(parent, SEA_BE_DRAGON, DSItems.DRAGON_SOUL.value(), beDragon(DragonTypes.SEA), 12);
        buildBeSeaDragonChildren(beSeaDragon);

        AdvancementHolder beForestDragon = createWithToast(parent, FOREST_BE_DRAGON, DSItems.DRAGON_SOUL.value(), beDragon(DragonTypes.FOREST), 12);
        buildBeForestDragonChildren(beForestDragon);
    }

    private void buildBeCaveDragonChildren(final AdvancementHolder parent) {
        // --- Parent: cave/be_dragon --- //

        AdvancementHolder rockEater = create(parent, CAVE_ROCK_EATER, DSItems.CHARGED_COAL.value(), List.of(
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

        AdvancementHolder swimInLava = create(parent, CAVE_SWIM_IN_LAVA, Items.LAVA_BUCKET, location(dragonType(DragonTypes.CAVE).located(isInLava(registries))), 20);

        // --- Parent: cave/rock_eater --- //

        create(rockEater, CAVE_WATER_SAFETY, DSItems.CHARGED_SOUP.value(), location(
                dragonType(DragonTypes.CAVE)
                        .located(isInWater(registries))
                        .effects(MobEffectsPredicate.Builder.effects().and(DSEffects.FIRE))
        ), 40);

        // --- Parent: cave/swim_in_lava --- //

        AdvancementHolder diamondsInLava = create(swimInLava, CAVE_DIAMONDS_IN_LAVA, Items.DIAMOND_ORE, mineBlockInLava(registries, Tags.Blocks.ORES_DIAMOND), 40);

        // FIXME :: previously used change dimension but also had the lava vision effect check - what is the intention here?
        createWithToast(diamondsInLava, CAVE_GO_HOME, Items.NETHER_BRICK_STAIRS, location(
                dragonType(DragonTypes.CAVE)
                        .located(dimension(Level.NETHER))
        ), 20);
    }

    private void buildBeSeaDragonChildren(final AdvancementHolder parent) {
        // --- Parent: sea/be_dragon --- //

        // TODO :: have either chat or toast notification for this one?
        // TODO :: previously checked if all loot tables were looted -> might be better to check if all shipwreck (variants) have been visited?
        AdvancementHolder lootShipwreck = create(parent, SEA_LOOT_SHIPWRECK, Items.HEART_OF_THE_SEA, location(dragonType(DragonTypes.SEA).located(structure(StructureTags.SHIPWRECK))), 20);

        AdvancementHolder rainDancing = create(parent, SEA_RAIN_DANCING, Items.WATER_BUCKET, List.of(
                location(ContextAwarePredicate.create(entityCondition(dragonType(DragonTypes.SEA).build()), WeatherCheck.weather().setRaining(true).build())),
                location(ContextAwarePredicate.create(entityCondition(dragonType(DragonTypes.SEA).build()), WeatherCheck.weather().setThundering(true).build()))
        ), 30);

        // --- Parent: sea/loot_shipwreck --- //

        create(lootShipwreck, SEA_FISH_EATER, DSItems.SEASONED_FISH.value(), List.of(
                consumeItem(Items.KELP),
                consumeItem(DSItems.SEASONED_FISH.value()),
                consumeItem(DSItems.GOLDEN_CORAL_PUFFERFISH.value()),
                consumeItem(DSItems.FROZEN_RAW_FISH.value()),
                consumeItem(DSItems.GOLDEN_TURTLE_EGG.value()),
                consumeItem(DSItems.SEA_DRAGON_TREAT.value())
        ), 80);

        // --- Parent: sea/rain_dancing --- //

        AdvancementHolder placeSnowInNether = create(rainDancing, SEA_PLACE_SNOW_IN_NETHER, Items.SNOW_BLOCK, placeBlockAsDragon(
                dragonType(DragonTypes.SEA).located(dimension(Level.NETHER)), Blocks.SNOW_BLOCK
        ), 16);

        // --- Parent: sea/place_snow_in_nether --- //

        create(placeSnowInNether, SEA_PEACE_IN_NETHER, Items.CAULDRON, location(
                dragonType(DragonTypes.SEA)
                        .effects(effect(DSEffects.PEACE))
                        .located(dimension(Level.NETHER))
        ), 0);
    }

    private void buildBeForestDragonChildren(final AdvancementHolder parent) {
        // --- Parent: forest/be_dragon --- //

        AdvancementHolder standOnSweetBerries = create(parent, FOREST_STAND_ON_SWEET_BERRIES, Items.SWEET_BERRIES, location(dragonType(DragonTypes.FOREST).steppingOn(block(Blocks.SWEET_BERRY_BUSH))), 30);

        // --- Parent: forest/stand_on_sweet_berries --- //

        create(standOnSweetBerries, FOREST_PREVENT_DARKNESS_PENALTY, DSItems.LUMINOUS_OINTMENT.value(), location(
                dragonType(DragonTypes.FOREST)
                        .located(light(MinMaxBounds.Ints.between(0, 3)))
                        .effects(MobEffectsPredicate.Builder.effects().and(DSEffects.MAGIC))
        ), 40);

        AdvancementHolder poisonousPotato = create(parent, FOREST_POISONOUS_POTATO, Items.POISONOUS_POTATO, convertPotato(dragonType(DragonTypes.FOREST)), 16);

        // --- Parent: forest/poisonous_potato --- //

        AdvancementHolder meatEater = create(poisonousPotato, FOREST_MEAT_EATER, DSItems.MEAT_WILD_BERRIES.value(), List.of(
                consumeItem(DSItems.SWEET_SOUR_RABBIT.value()),
                consumeItem(DSItems.LUMINOUS_OINTMENT.value()),
                consumeItem(DSItems.DIAMOND_CHORUS.value()),
                consumeItem(DSItems.SMELLY_MEAT_PORRIDGE.value()),
                consumeItem(DSItems.MEAT_WILD_BERRIES.value()),
                consumeItem(DSItems.MEAT_CHORUS_MIX.value()),
                consumeItem(DSItems.FOREST_DRAGON_TREAT.value())
        ), 60);

        // --- Parent: forest/meat_eater --- //

        create(meatEater, FOREST_TRANSPLANT_CHORUS_FRUIT, DSItems.DIAMOND_CHORUS.value(), placeBlockAsDragon(
                dragonType(DragonTypes.FOREST).located(dimension(Level.OVERWORLD)), Blocks.CHORUS_FLOWER
        ), 90);
    }

    private void buildBeDragonChildren(final AdvancementHolder parent) {
        // --- Parent: be_dragon --- //

        AdvancementHolder useStarHeart = createWithToast(parent, USE_STAR_HEART, DSItems.STAR_HEART.value(), useStarHeart(), 30);

        // --- Parent: use_star_heart --- //

        createWithToast(useStarHeart, USE_DRAGON_SOUL, DSItems.DRAGON_SOUL.value(), useDragonSoul(), 120);
    }

    private void buildCollectDustChildren(final AdvancementHolder parent) {
        // --- Parent: collect_dust --- //

        AdvancementHolder beYoungDragon = createWithToast(parent, BE_YOUNG_DRAGON, DSItems.DRAGON_HEART_SHARD.value(), beDragon(19.9), 12);
        buildBeYoungDragonChildren(beYoungDragon);

        AdvancementHolder sleepOnTreasure = createWithAnnouncement(parent, SLEEP_ON_TREASURE, Items.GOLD_NUGGET, sleepOnTreasure(10), 10);
        buildSleepOnTreasureChildren(sleepOnTreasure);

        TagKey<Structure> tag = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("dragon_skeletons")); // FIXME :: use tag from data generation
        createWithToast(parent, FIND_BONES, DSItems.STAR_BONE.value(), PlayerTrigger.TriggerInstance.located(structure(tag)), 12);

        AdvancementHolder useMemoryBlock = createWithToast(parent, USE_MEMORY_BLOCK, DSBlocks.DRAGON_MEMORY_BLOCK.value(), itemUsedOnBlock(DSBlocks.DRAGON_MEMORY_BLOCK.value(), DSItemTags.DRAGON_BEACONS), 10);
        buildUseMemoryBlockChildren(useMemoryBlock);
    }

    private void buildUseMemoryBlockChildren(final AdvancementHolder parent) {
        // --- Parent: use_memory_block --- //

        AdvancementHolder changeBeacon = createWithToast(parent, CHANGE_BEACON, Items.NETHERITE_INGOT, itemUsedOnBlock(DSBlocks.EMPTY_DRAGON_BEACON.value(), Items.DIAMOND_BLOCK, Items.GOLD_BLOCK, Items.NETHERITE_INGOT), 10);

        // --- Parent: change_beacon --- //

        createWithToast(changeBeacon, GET_ALL_BEACONS, DSItems.ELDER_DRAGON_DUST.value(), List.of(
                effectWithMinDuration(DSEffects.PEACE, Functions.secondsToTicks(20)),
                effectWithMinDuration(DSEffects.FIRE, Functions.secondsToTicks(20)),
                effectWithMinDuration(DSEffects.MAGIC, Functions.secondsToTicks(20))
        ), 0);
    }

    private void buildSleepOnTreasureChildren(final AdvancementHolder parent) {
        // --- Parent: sleep_on_treasure --- //

        AdvancementHolder sleepOnHoard = createWithToast(parent, SLEEP_ON_HOARD, Items.GOLD_INGOT, sleepOnTreasure(100), 40);

        // --- Parent: sleep_on_hoard --- //

        createWithToast(sleepOnHoard, SLEEP_ON_MASSIVE_HOARD, DSBlocks.GOLD_DRAGON_TREASURE.value(), sleepOnTreasure(240), 120);
    }

    private void buildBeYoungDragonChildren(final AdvancementHolder parent) {
        // --- Parent: be_young_dragon --- //

        AdvancementHolder beAdultDragon = createWithToast(parent, BE_ADULT_DRAGON, DSItems.WEAK_DRAGON_HEART.value(), beDragon(29.9), 0);

        // --- Parent: be_adult_dragon --- //

        AdvancementHolder collectHeartFromMonster = create(beAdultDragon, COLLECT_HEART_FROM_MONSTER, DSItems.ELDER_DRAGON_HEART.value(), InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.ELDER_DRAGON_HEART.value()), 6);

        // --- Parent: collect_heart_from_monster --- //

        AdvancementHolder beOldCaveDragon = createWithToast(collectHeartFromMonster, CAVE_BE_OLD_DRAGON, DSBlocks.CAVE_DRAGON_BEACON.value(), beDragon(59.9, DragonTypes.CAVE), 120);

        // --- Parent: cave/be_old_dragon --- //

        createWithToast(beOldCaveDragon, CAVE_MASTER_ALL_PASSIVES, DSBlocks.CAVE_SOURCE_OF_MAGIC.value(), List.of(
                upgradeAbilityMax(BurnAbility.class),
                upgradeAbilityMax(CaveAthleticsAbility.class),
                upgradeAbilityMax(ContrastShowerAbility.class),
                upgradeAbilityMax(CaveMagicAbility.class)
        ), 150);

        AdvancementHolder beOldSeaDragon = createWithToast(collectHeartFromMonster, SEA_BE_OLD_DRAGON, DSBlocks.SEA_DRAGON_BEACON.value(), beDragon(59.9, DragonTypes.SEA), 120);

        // --- Parent: sea/be_old_dragon --- //

        createWithToast(beOldSeaDragon, SEA_MASTER_ALL_PASSIVES, DSBlocks.SEA_SOURCE_OF_MAGIC.value(), List.of(
                upgradeAbilityMax(SpectralImpactAbility.class),
                upgradeAbilityMax(SeaAthleticsAbility.class),
                upgradeAbilityMax(WaterAbility.class),
                upgradeAbilityMax(SeaMagicAbility.class)
        ), 150);

        AdvancementHolder beOldForestDragon = createWithToast(collectHeartFromMonster, FOREST_BE_OLD_DRAGON, DSBlocks.FOREST_DRAGON_BEACON.value(), beDragon(59.9, DragonTypes.FOREST), 120);

        // --- Parent: forest/be_old_dragon --- //

        createWithToast(beOldForestDragon, FOREST_MASTER_ALL_PASSIVES, DSBlocks.FOREST_SOURCE_OF_MAGIC.value(), List.of(
                upgradeAbilityMax(CliffhangerAbility.class),
                upgradeAbilityMax(ForestAthleticsAbility.class),
                upgradeAbilityMax(LightInDarknessAbility.class),
                upgradeAbilityMax(ForestMagicAbility.class)
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

    @SafeVarargs
    private MobEffectsPredicate.Builder effect(final Holder<MobEffect>... effects) {
        MobEffectsPredicate.Builder builder = MobEffectsPredicate.Builder.effects();

        for (Holder<MobEffect> effect : effects) {
            builder.and(effect);
        }

        return builder;
    }

    private LocationPredicate.Builder dimension(final ResourceKey<Level> dimension) {
        return LocationPredicate.Builder.inDimension(dimension);
    }

    private LocationPredicate.Builder structure(final TagKey<Structure> tag) {
        HolderSet.Named<Structure> set = registries.lookupOrThrow(Registries.STRUCTURE).getOrThrow(tag);
        return LocationPredicate.Builder.location().setStructures(set);
    }

    private LootItemCondition entityCondition(final EntityPredicate predicate) {
        return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, predicate).build();
    }

    private Optional<ContextAwarePredicate> caveDragonInLava(@NotNull final HolderLookup.Provider registries) {
        return Optional.of(EntityPredicate.wrap(dragonType(DragonTypes.CAVE).located(isInLava(registries))));
    }

    private LocationPredicate.Builder isInLava(@NotNull final HolderLookup.Provider registries) {
        return LocationPredicate.Builder.location().setFluid(FluidPredicate.Builder.fluid().of(registries.lookupOrThrow(Registries.FLUID).getOrThrow(FluidTags.LAVA)));
    }

    private LocationPredicate.Builder isInWater(@NotNull final HolderLookup.Provider registries) {
        return LocationPredicate.Builder.location().setFluid(FluidPredicate.Builder.fluid().of(registries.lookupOrThrow(Registries.FLUID).getOrThrow(FluidTags.WATER)));
    }

    private LocationPredicate.Builder block(final Block block) {
        return LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block));
    }

    private LocationPredicate.Builder light(final MinMaxBounds.Ints bounds) {
        return LocationPredicate.Builder.location().setLight(LightPredicate.Builder.light().setComposite(bounds));
    }

    private EntityPredicate.Builder dragonType(final AbstractDragonType type) {
        return EntityPredicate.Builder.entity().nbt(new NbtPredicate(dragonNBT(type)));
    }

    private CompoundTag dragonNBT(final AbstractDragonType dragonType) {
        CompoundTag type = new CompoundTag();
        type.putString("type", dragonType.getTypeName());

        CompoundTag data = new CompoundTag();
        //noinspection DataFlowIssue -> ignore
        data.put(DragonSurvival.DRAGON_HANDLER.getKey().location().toString(), type);

        CompoundTag nbt = new CompoundTag();
        nbt.put(AttachmentHolder.ATTACHMENTS_NBT_KEY, data);

        return nbt;
    }

    @SuppressWarnings("deprecation") // ignore
    public Criterion<InventoryChangeTrigger.TriggerInstance> dragonHasItem(final AbstractDragonType dragonType, final ItemLike... items) {
        List<ItemPredicate> predicates = new ArrayList<>();

        for (ItemLike item : items) {
            predicates.add(new ItemPredicate(Optional.of(HolderSet.direct(item.asItem().builtInRegistryHolder())), MinMaxBounds.Ints.ANY, DataComponentPredicate.EMPTY, Map.of()));
        }

        return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(new InventoryChangeTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(dragonType(dragonType))), InventoryChangeTrigger.TriggerInstance.Slots.ANY, predicates));
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

    public Criterion<ConvertPotatoTrigger.TriggerInstance> convertPotato(final EntityPredicate.Builder builder) {
        return DSAdvancementTriggers.CONVERT_POTATO.get().createCriterion(new ConvertPotatoTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(builder.build()))));
    }

    // --- Mine Block Under Lava --- //

    @SuppressWarnings("deprecation") // ignore
    public Criterion<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> mineBlockInLava(@NotNull final HolderLookup.Provider registries, final Block... blocks) {
        return DSAdvancementTriggers.MINE_BLOCK_UNDER_LAVA.get().createCriterion(new MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance(caveDragonInLava(registries), Optional.of(HolderSet.direct(Block::builtInRegistryHolder, blocks))));
    }

    public Criterion<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> mineBlockInLava(@NotNull final HolderLookup.Provider registries, final TagKey<Block> blocks) {
        return DSAdvancementTriggers.MINE_BLOCK_UNDER_LAVA.get().createCriterion(new MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance(caveDragonInLava(registries), Optional.of(BuiltInRegistries.BLOCK.getOrCreateTag(blocks))));
    }

    // --- Use Dragon Soul --- //

    public Criterion<UseDragonSoulTrigger.UseDragonSoulInstance> useDragonSoul() {
        return DSAdvancementTriggers.USE_DRAGON_SOUL.get().createCriterion(new UseDragonSoulTrigger.UseDragonSoulInstance(Optional.empty()));
    }

    // --- Use Star Heart --- //

    public Criterion<UseStarHeartTrigger.UseStarHeartInstance> useStarHeart() {
        return DSAdvancementTriggers.USE_STAR_HEART.get().createCriterion(new UseStarHeartTrigger.UseStarHeartInstance(Optional.empty()));
    }


    // --- Sleep On Treasure --- //

    public Criterion<SleepOnTreasureTrigger.SleepOnTreasureInstance> sleepOnTreasure(int nearbyTreasureAmount) {
        return DSAdvancementTriggers.SLEEP_ON_TREASURE.get().createCriterion(new SleepOnTreasureTrigger.SleepOnTreasureInstance(Optional.empty(), Optional.of(nearbyTreasureAmount)));
    }

    // --- Upgrade Ability --- //

    public Criterion<UpgradeAbilityTrigger.UpgradeAbilityInstance> upgradeAbilityMax(final Class<? extends DragonAbility> abilityType) {
        DragonAbility ability;

        try {
            ability = abilityType.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        return upgradeAbility(ability.getName(), ability.getMaxLevel());
    }

    public Criterion<UpgradeAbilityTrigger.UpgradeAbilityInstance> upgradeAbility(final String ability, int level) {
        return DSAdvancementTriggers.UPGRADE_ABILITY.get().createCriterion(new UpgradeAbilityTrigger.UpgradeAbilityInstance(Optional.empty(), Optional.of(ability), Optional.of(level)));
    }

    // --- Be Dragon --- //

    public Criterion<BeDragonTrigger.BeDragonInstance> beDragon() {
        return beDragon(Optional.empty(), Optional.empty());
    }

    public Criterion<BeDragonTrigger.BeDragonInstance> beDragon(final AbstractDragonType type) {
        return beDragon(Optional.empty(), Optional.of(type.getTypeName()));
    }

    public Criterion<BeDragonTrigger.BeDragonInstance> beDragon(double size) {
        return beDragon(Optional.of(size), Optional.empty());
    }

    public Criterion<BeDragonTrigger.BeDragonInstance> beDragon(double size, final AbstractDragonType type) {
        return beDragon(Optional.of(size), Optional.of(type.getTypeName()));
    }

    public Criterion<BeDragonTrigger.BeDragonInstance> beDragon(final Optional<Double> size, final Optional<String> type) {
        return DSAdvancementTriggers.BE_DRAGON.get().createCriterion(new BeDragonTrigger.BeDragonInstance(Optional.empty(), size, type));
    }
}
