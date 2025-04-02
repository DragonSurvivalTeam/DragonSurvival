package by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.BeDragonTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.ConvertItemFromAbility;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.MineBlockUnderLavaTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.SleepOnTreasureTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.StealFromVillagerTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.StopNaturalGrowthTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.UpgradeAbilityTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.UseDragonSoulTrigger;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
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
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.advancements.AdvancementHolder;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ResolvableProfile;
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

@SuppressWarnings("deprecation") // ignore
public class DSAdvancements implements AdvancementProvider.AdvancementGenerator {
    private HolderLookup.Provider registries;
    private Consumer<AdvancementHolder> saver;
    private ExistingFileHelper helper;

    @Override
    public void generate(@NotNull final HolderLookup.Provider registries, @NotNull final Consumer<AdvancementHolder> saver, @NotNull final ExistingFileHelper helper) {
        this.registries = registries;
        this.saver = saver;
        this.helper = helper;

        AdvancementHolder root = create(LangKey.ROOT)
                .type(AdvancementType.GOAL)
                .displayItem(DSItems.ELDER_DRAGON_BONE.value())
                .background(DragonSurvival.res("textures/block/stone_dragon_door_top.png"))
                .noDescription()
                .criteria("root", PlayerTrigger.TriggerInstance.tick())
                .build(saver, helper);

        // --- Parent: root --- //

        // TODO :: Re-add this when we update to 1.22
        /*AdvancementHolder beDragon = create(LangKey.BE_DRAGON)
                .parent(root)
                .displayItem(DSItems.STAR_BONE.value())
                .criteria("be_dragon", beDragon())
                .experienceReward(12)
                .build(saver, helper);
        buildBeDragonChildren(beDragon);

        AdvancementHolder collectDust = create(LangKey.COLLECT_DUST)
                .parent(root)
                .displayItem(Items.COAL_ORE)
                .criteria("collect_elder_dragon_dust", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.ELDER_DRAGON_DUST.value()))
                .experienceReward(6)
                .build(saver, helper);
        buildCollectDustChildren(collectDust);

        AdvancementHolder placeAltar = create(LangKey.PLACE_ALTAR)
                .parent(root)
                .displayItem(DSBlocks.STONE_DRAGON_ALTAR.value())
                .criteria("place_dragon_altar", placeBlock(DSItemTags.DRAGON_ALTARS))
                .experienceReward(6)
                .build(saver, helper);
        buildPlaceAltarChildren(placeAltar);

        AdvancementHolder pathChoice = create(LangKey.PATH_CHOICE)
                .parent(root)
                .displayItem(Items.OAK_SIGN)
                .criteria("interact_with_villager", noItemInteract(EntityType.VILLAGER))
                .experienceReward(6)
                .build(saver, helper);
        buildHunterAdvancements(pathChoice);
        buildLightAdvancements(pathChoice);
        buildDarkAdvancements(pathChoice);*/
    }

    private void buildDarkAdvancements(final AdvancementHolder parent) {
        // --- Parent: path_choice --- //

        ItemStack head = Items.PLAYER_HEAD.getDefaultInstance();
        head.set(DataComponents.PROFILE, new ResolvableProfile(Optional.of("MHF_Villager"), Optional.empty(), new PropertyMap()));
        AdvancementHolder affectedByHunterOmen = create(LangKey.DARK_AFFECTED_BY_HUNTER_OMEN)
                .parent(parent)
                .displayItem(head)
                .showToast()
                .announceChat()
                .criteria("affected_by_hunter_omen", effectWithMinDuration(DSEffects.HUNTER_OMEN, 300))
                .experienceReward(6)
                .build(saver, helper);

        // --- Parent: dark/affected_by_hunter_omen --- //

        create(LangKey.DARK_STEAL_FROM_VILLAGER)
                .parent(affectedByHunterOmen)
                .displayItem(DSItems.PARTISAN.value())
                .showToast()
                .announceChat()
                .criteria("steal_from_villager", stealFromVillager())
                .build(saver, helper);

        AdvancementHolder collectKey = create(LangKey.DARK_COLLECT_KEY)
                .parent(affectedByHunterOmen)
                .displayItem(DSItems.DARK_KEY.value())
                .showToast()
                .announceChat()
                .criteria("collect_dark_key", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_KEY.value()))
                .build(saver, helper);

        // --- Parent: dark/collect_key --- //

        AdvancementHolder openVault = create(LangKey.DARK_OPEN_VAULT)
                .parent(collectKey)
                .displayItem(DSBlocks.DARK_VAULT.value())
                .showToast()
                .announceChat()
                .criteria("open_dark_vault", itemUsedOnBlock(DSBlocks.DARK_VAULT.value(), DSItems.DARK_KEY.value()))
                .experienceReward(10)
                .build(saver, helper);

        // --- Parent: dark/open_vault --- //

        AdvancementHolder getArmorItem = create(LangKey.DARK_GET_ARMOR_ITEM)
                .parent(openVault)
                .displayItem(DSItems.DARK_DRAGON_HELMET.value())
                .showToast()
                .announceChat()
                .criteria("collect_dark_armor", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_HELMET.value(), DSItems.DARK_DRAGON_CHESTPLATE.value(), DSItems.DARK_DRAGON_LEGGINGS.value(), DSItems.DARK_DRAGON_BOOTS.value()))
                .build(saver, helper);

        // --- Parent: dark/get_armor_item --- //

        create(LangKey.DARK_GET_ARMOR_SET)
                .parent(getArmorItem)
                .displayItem(DSItems.DARK_DRAGON_HELMET.value())
                .showToast()
                .announceChat()
                .criteria("collect_dark_armor_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_HELMET.value()))
                .criteria("collect_dark_armor_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_CHESTPLATE.value()))
                .criteria("collect_dark_armor_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_LEGGINGS.value()))
                .criteria("collect_dark_armor_boots", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_BOOTS.value()))
                .build(saver, helper);
    }

    private void buildLightAdvancements(final AdvancementHolder parent) {
        // --- Parent: path_choice --- //

        AdvancementHolder dragonRiderWorkbench = create(LangKey.LIGHT_DRAGON_RIDER_WORKBENCH)
                .parent(parent)
                .displayItem(DSBlocks.DRAGON_RIDER_WORKBENCH.value())
                .criteria("craft_dragon_rider_workbench", crafted(DSBlocks.DRAGON_RIDER_WORKBENCH.value()))
                .experienceReward(6)
                .build(saver, helper);

        // --- Parent: light/dragon_rider_workbench --- //

        AdvancementHolder collectKey = create(LangKey.LIGHT_COLLECT_KEY)
                .parent(dragonRiderWorkbench)
                .displayItem(DSItems.LIGHT_KEY.value())
                .showToast()
                .announceChat()
                .criteria("collect_light_key", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_KEY.value()))
                .build(saver, helper);

        // --- Parent: light/collect_key --- //

        AdvancementHolder openVault = create(LangKey.LIGHT_OPEN_VAULT)
                .parent(collectKey)
                .displayItem(DSBlocks.LIGHT_VAULT.value())
                .showToast()
                .announceChat()
                .criteria("open_light_vault", itemUsedOnBlock(DSBlocks.LIGHT_VAULT.value(), DSItems.LIGHT_KEY.value()))
                .experienceReward(10)
                .build(saver, helper);

        // --- Parent: light/open_vault --- //

        AdvancementHolder getArmorItem = create(LangKey.LIGHT_GET_ARMOR_ITEM)
                .parent(openVault)
                .displayItem(DSItems.LIGHT_DRAGON_HELMET.value())
                .showToast()
                .announceChat()
                .criteria("collect_light_armor", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_HELMET.value(), DSItems.LIGHT_DRAGON_CHESTPLATE.value(), DSItems.LIGHT_DRAGON_LEGGINGS.value(), DSItems.LIGHT_DRAGON_BOOTS.value()))
                .build(saver, helper);

        // --- Parent: light/get_armor_item --- //

        create(LangKey.LIGHT_GET_ARMOR_SET)
                .parent(getArmorItem)
                .displayItem(DSItems.LIGHT_DRAGON_HELMET.value())
                .showToast()
                .announceChat()
                .criteria("collect_light_armor_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_HELMET.value()))
                .criteria("collect_light_armor_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_CHESTPLATE.value()))
                .criteria("collect_light_armor_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_LEGGINGS.value()))
                .criteria("collect_light_armor_boots", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_BOOTS.value()))
                .build(saver, helper);
    }

    private void buildHunterAdvancements(final AdvancementHolder parent) {
        // --- Parent: path_choice --- //

        AdvancementHolder promotion = create(LangKey.HUNTER_PROMOTION)
                .parent(parent)
                .displayItem(DSItems.SPEARMAN_PROMOTION.value())
                .showToast()
                .announceChat()
                .criteria("promote_spearman", itemInteract(DSEntities.HUNTER_SPEARMAN.value(), DSItems.SPEARMAN_PROMOTION.value()))
                .experienceReward(6)
                .build(saver, helper);

        // --- Parent: hunter/promotion --- //

        AdvancementHolder collectKey = create(LangKey.HUNTER_COLLECT_KEY)
                .parent(promotion)
                .displayItem(DSItems.HUNTER_KEY.value())
                .showToast()
                .announceChat()
                .criteria("collect_hunter_key", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.HUNTER_KEY.value()))
                .build(saver, helper);

        // --- Parent: hunter/collect_key --- //

        AdvancementHolder openVault = create(LangKey.HUNTER_OPEN_VAULT)
                .parent(collectKey)
                .displayItem(DSBlocks.HUNTER_VAULT.value())
                .showToast()
                .announceChat()
                .criteria("open_hunter_vault", itemUsedOnBlock(DSBlocks.HUNTER_VAULT.value(), DSItems.HUNTER_KEY.value()))
                .experienceReward(10)
                .build(saver, helper);

        // --- Parent: hunter/open_vault --- //

        Holder.Reference<Enchantment> bolas = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(DSEnchantments.BOLAS);

        create(LangKey.HUNTER_FIRE_BOLAS)
                .parent(openVault)
                .displayItem(DSItems.BOLAS.value())
                .criteria("fire_bolas", ShotCrossbowTrigger.TriggerInstance.shotCrossbow(
                        Optional.of(ItemPredicate.Builder.item().of(Tags.Items.TOOLS_CROSSBOW).withSubPredicate(
                                ItemSubPredicates.ENCHANTMENTS, ItemEnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(bolas, MinMaxBounds.Ints.atLeast(1))))
                        ).build())))
                .build(saver, helper);
    }

    private void buildPlaceAltarChildren(final AdvancementHolder parent) {
        // --- Parent: place_altar --- //

        ItemStack caveSoul = DSItems.DRAGON_SOUL.value().getDefaultInstance();
        caveSoul.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(2));

        AdvancementHolder beCaveDragon = create(LangKey.CAVE_BE_DRAGON)
                .parent(parent)
                .displayItem(caveSoul)
                .showToast()
                .announceChat()
                .criteria("be_cave_dragon", beDragon(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON)))
                .experienceReward(12)
                .build(saver, helper);
        buildBeCaveDragonChildren(beCaveDragon);

        ItemStack seaSoul = DSItems.DRAGON_SOUL.value().getDefaultInstance();
        seaSoul.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(3));

        AdvancementHolder beSeaDragon = create(LangKey.SEA_BE_DRAGON)
                .parent(parent)
                .displayItem(seaSoul)
                .showToast()
                .announceChat()
                .criteria("be_sea_dragon", beDragon(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)))
                .experienceReward(12)
                .build(saver, helper);
        buildBeSeaDragonChildren(beSeaDragon);

        ItemStack forestSoul = DSItems.DRAGON_SOUL.value().getDefaultInstance();
        forestSoul.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(1));

        AdvancementHolder beForestDragon = create(LangKey.FOREST_BE_DRAGON)
                .parent(parent)
                .displayItem(forestSoul)
                .showToast()
                .announceChat()
                .criteria(beDragon(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON)))
                .experienceReward(12)
                .build(saver, helper);
        buildBeForestDragonChildren(beForestDragon);
    }

    private void buildBeCaveDragonChildren(final AdvancementHolder parent) {
        // --- Parent: cave/be_dragon --- //

        AdvancementHolder rockEater = create(LangKey.CAVE_ROCK_EATER)
                .parent(parent)
                .displayItem(DSItems.CHARGED_COAL.value())
                .criteria("consume_charged_coal", consumeItem(DSItems.CHARGED_COAL.value()))
                .criteria("consume_charged_soup", consumeItem(DSItems.CHARGED_SOUP.value()))
                .criteria("consume_charred_meat", consumeItem(DSItems.CHARRED_MEAT.value()))
                .criteria("consume_charred_seafood", consumeItem(DSItems.CHARRED_SEAFOOD.value()))
                .criteria("consume_hot_dragon_rod", consumeItem(DSItems.HOT_DRAGON_ROD.value()))
                .criteria("consume_explosive_copper", consumeItem(DSItems.EXPLOSIVE_COPPER.value()))
                .criteria("consume_quartz_explosive_copper", consumeItem(DSItems.QUARTZ_EXPLOSIVE_COPPER.value()))
                .criteria("consume_double_quartz", consumeItem(DSItems.DOUBLE_QUARTZ.value()))
                .criteria("consume_cave_dragon_treat", consumeItem(DSItems.CAVE_DRAGON_TREAT.value()))
                .experienceReward(60)
                .build(saver, helper);

        AdvancementHolder swimInLava = create(LangKey.CAVE_SWIM_IN_LAVA)
                .parent(parent)
                .displayItem(Items.LAVA_BUCKET)
                .criteria("swim_in_lava", location(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON)).located(isInFluid(FluidTags.LAVA))))
                .experienceReward(20)
                .build(saver, helper);

        // --- Parent: cave/rock_eater --- //

        create(LangKey.CAVE_WATER_SAFETY)
                .parent(rockEater)
                .displayItem(DSItems.CHARGED_SOUP.value())
                .criteria("swim_safely_in_lava", location(
                        Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON))
                                .located(isInFluid(FluidTags.WATER))
                                .effects(MobEffectsPredicate.Builder.effects().and(DSEffects.FIRE))
                ))
                .experienceReward(40)
                .build(saver, helper);

        // --- Parent: cave/swim_in_lava --- //

        AdvancementHolder diamondsInLava = create(LangKey.CAVE_DIAMONDS_IN_LAVA)
                .parent(swimInLava)
                .displayItem(Items.DIAMOND_ORE)
                .criteria("mine_diamond_in_lava", mineBlockInLava(Tags.Blocks.ORES_DIAMOND))
                .experienceReward(40)
                .build(saver, helper);

        create(LangKey.CAVE_GO_HOME)
                .parent(diamondsInLava)
                .displayItem(Items.NETHER_BRICK_STAIRS)
                .showToast()
                .announceChat()
                .criteria("explore_nether_lava_sea", location(
                        Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON))
                                .located(inDimension(Level.NETHER).setFluid(fluid(FluidTags.LAVA)))
                                .effects(hasEffect(DSEffects.LAVA_VISION))
                ))
                .experienceReward(20)
                .build(saver, helper);
    }

    private void buildBeSeaDragonChildren(final AdvancementHolder parent) {
        // --- Parent: sea/be_dragon --- //

        AdvancementHolder lootShipwreck = create(LangKey.SEA_LOOT_SHIPWRECK)
                .parent(parent)
                .displayItem(Items.HEART_OF_THE_SEA)
                .criteria("explore_shipwreck", location(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)).located(inStructure(registries.holderOrThrow(BuiltinStructures.SHIPWRECK)))))
                .criteria("explore_beached_shipwreck", location(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)).located(inStructure(registries.holderOrThrow(BuiltinStructures.SHIPWRECK_BEACHED)))))
                .experienceReward(20)
                .build(saver, helper);

        AdvancementHolder rainDancing = create(LangKey.SEA_RAIN_DANCING)
                .parent(parent)
                .displayItem(Items.WATER_BUCKET)
                .criteria("experience_rain_and_thunder", location(ContextAwarePredicate.create(
                        entityCondition(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)).build()),
                        WeatherCheck.weather().setRaining(true).setThundering(true).build()
                )))
                .experienceReward(30)
                .build(saver, helper);

        // --- Parent: sea/loot_shipwreck --- //

        create(LangKey.SEA_FISH_EATER)
                .parent(lootShipwreck)
                .displayItem(DSItems.SEASONED_FISH.value())
                .criteria("consume_kelp", consumeItem(Items.KELP))
                .criteria("consume_seasoned_fish", consumeItem(DSItems.SEASONED_FISH.value()))
                .criteria("consume_golden_coral_pufferfish", consumeItem(DSItems.GOLDEN_CORAL_PUFFERFISH.value()))
                .criteria("consume_frozen_raw_fish", consumeItem(DSItems.FROZEN_RAW_FISH.value()))
                .criteria("consume_golden_turtle_egg", consumeItem(DSItems.GOLDEN_TURTLE_EGG.value()))
                .criteria("consume_sea_dragon_treat", consumeItem(DSItems.SEA_DRAGON_TREAT.value()))
                .experienceReward(80)
                .build(saver, helper);

        // --- Parent: sea/rain_dancing --- //

        AdvancementHolder placeSnowInNether = create(LangKey.SEA_PLACE_SNOW_IN_NETHER)
                .parent(rainDancing)
                .displayItem(Items.SNOW_BLOCK)
                .criteria("place_snow_in_nether", placeBlockAsDragon(
                        Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON)).located(inDimension(Level.NETHER)), Blocks.SNOW_BLOCK
                ))
                .experienceReward(16)
                .build(saver, helper);

        // --- Parent: sea/place_snow_in_nether --- //

        create(LangKey.SEA_PEACE_IN_NETHER)
                .parent(placeSnowInNether)
                .displayItem(Items.CAULDRON)
                .criteria("be_safe_in_nether", location(
                        Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON))
                                .effects(hasEffect(DSEffects.PEACE))
                                .located(inDimension(Level.NETHER))
                ))
                .build(saver, helper);
    }

    private void buildBeForestDragonChildren(final AdvancementHolder parent) {
        // --- Parent: forest/be_dragon --- //

        AdvancementHolder standOnSweetBerries = create(LangKey.FOREST_STAND_ON_SWEET_BERRIES)
                .parent(parent)
                .displayItem(Items.SWEET_BERRIES)
                .criteria("stand_on_sweet_berries", location(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON)).steppingOn(block(Blocks.SWEET_BERRY_BUSH))))
                .experienceReward(30)
                .build(saver, helper);

        AdvancementHolder poisonousPotato = create(LangKey.FOREST_POISONOUS_POTATO)
                .parent(parent)
                .displayItem(Items.POISONOUS_POTATO)
                .criteria("convert_potato", convertPotato(Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON))))
                .experienceReward(16)
                .build(saver, helper);

        // --- Parent: forest/stand_on_sweet_berries --- //

        create(LangKey.FOREST_PREVENT_DARKNESS_PENALTY)
                .parent(standOnSweetBerries)
                .displayItem(DSItems.LUMINOUS_OINTMENT.value())
                .criteria("be_safe_in_darkness", location(
                        Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON))
                                .located(light(MinMaxBounds.Ints.between(0, 3)))
                                .effects(MobEffectsPredicate.Builder.effects().and(DSEffects.MAGIC))
                ))
                .experienceReward(40)
                .build(saver, helper);

        // --- Parent: forest/poisonous_potato --- //

        AdvancementHolder meatEater = create(LangKey.FOREST_MEAT_EATER)
                .parent(poisonousPotato)
                .displayItem(DSItems.MEAT_WILD_BERRIES.value())
                .criteria("consume_sweet_sour_rabbit", consumeItem(DSItems.SWEET_SOUR_RABBIT.value()))
                .criteria("consume_luminous_ointment", consumeItem(DSItems.LUMINOUS_OINTMENT.value()))
                .criteria("consume_diamond_chorus", consumeItem(DSItems.DIAMOND_CHORUS.value()))
                .criteria("consume_smelly_meat_porridge", consumeItem(DSItems.SMELLY_MEAT_PORRIDGE.value()))
                .criteria("consume_meat_wilderness", consumeItem(DSItems.MEAT_WILD_BERRIES.value()))
                .criteria("consume_meat_chorus_mix", consumeItem(DSItems.MEAT_CHORUS_MIX.value()))
                .criteria("consume_forest_dragon_treat", consumeItem(DSItems.FOREST_DRAGON_TREAT.value()))
                .experienceReward(60)
                .build(saver, helper);

        // --- Parent: forest/meat_eater --- //

        create(LangKey.FOREST_TRANSPLANT_CHORUS_FRUIT)
                .parent(meatEater)
                .displayItem(DSItems.DIAMOND_CHORUS.value())
                .criteria("place_chorus_fruit", placeBlockAsDragon(
                        Condition.dragonSpecies(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON)).located(inDimension(Level.OVERWORLD)), Blocks.CHORUS_FLOWER
                ))
                .experienceReward(90)
                .build(saver, helper);
    }

    private void buildBeDragonChildren(final AdvancementHolder parent) {
        // --- Parent: be_dragon --- //

        AdvancementHolder stopNaturalGrowth = create(LangKey.STOP_NATURAL_GROWTH)
                .parent(parent)
                .displayItem(DSItems.STAR_HEART.value())
                .showToast()
                .announceChat()
                .criteria("stop_natural_growth", stopNaturalGrowth())
                .experienceReward(30)
                .build(saver, helper);

        // --- Parent: stop_natural_growth --- //

        create(LangKey.USE_DRAGON_SOUL)
                .parent(stopNaturalGrowth)
                .displayItem(DSItems.DRAGON_SOUL.value())
                .showToast()
                .announceChat()
                .criteria("use_dragon_soul", useDragonSoul())
                .experienceReward(120)
                .build(saver, helper);
    }

    private void buildCollectDustChildren(final AdvancementHolder parent) {
        // --- Parent: collect_dust --- //

        AdvancementHolder beYoungDragon = create(LangKey.BE_YOUNG_DRAGON)
                .parent(parent)
                .displayItem(DSItems.DRAGON_HEART_SHARD.value())
                .showToast()
                .announceChat()
                .criteria("reach_young_stage", beDragon(DragonStages.young))
                .experienceReward(12)
                .build(saver, helper);
        buildBeYoungDragonChildren(beYoungDragon);

        AdvancementHolder sleepOnTreasure = create(LangKey.SLEEP_ON_TREASURE)
                .parent(parent)
                .displayItem(Items.GOLD_NUGGET)
                .announceChat()
                .criteria("sleep_on_small_treasure_hoard", sleepOnTreasure(10))
                .experienceReward(10)
                .build(saver, helper);
        buildSleepOnTreasureChildren(sleepOnTreasure);

        TagKey<Structure> dragonBones = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("dragon_skeletons")); // FIXME :: use tag from data generation
        AdvancementHolder findBones = create(LangKey.FIND_BONES)
                .parent(parent)
                .displayItem(DSItems.STAR_BONE.value())
                .showToast()
                .announceChat()
                .criteria("find_dragon_bones", PlayerTrigger.TriggerInstance.located(inStructure(dragonBones)))
                .experienceReward(12)
                .build(saver, helper);

        buildFindBonesChildren(findBones);

        AdvancementHolder useMemoryBlock = create(LangKey.USE_MEMORY_BLOCK)
                .parent(parent)
                .displayItem(DSBlocks.DRAGON_MEMORY_BLOCK.value())
                .showToast()
                .announceChat()
                // TODO :: check for the other way as well (place memory block under a beacon)
                .criteria("place_beacon_on_memory_block", itemUsedOnBlock(DSBlocks.DRAGON_MEMORY_BLOCK.value(), DSBlocks.DRAGON_BEACON.value()))
                .experienceReward(10)
                .build(saver, helper);

        buildUseMemoryBlockChildren(useMemoryBlock);
    }

    private void buildFindBonesChildren(final AdvancementHolder parent) {
        // --- Parent: find_bones --- //
        TagKey<Structure> lightTreasure = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("light_treasure")); // FIXME :: use tag from data generation
        AdvancementHolder findOverworldStructure = create(LangKey.FIND_OVERWORLD_STRUCTURES)
                .parent(parent)
                .displayItem(Blocks.GRASS_BLOCK)
                .showToast()
                .announceChat()
                .criteria("find_light_treasure", PlayerTrigger.TriggerInstance.located(inStructure(lightTreasure)))
                .experienceReward(24)
                .build(saver, helper);

        TagKey<Structure> darkTreasure = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("dark_treasure")); // FIXME :: use tag from data generation
        AdvancementHolder findNetherStructure = create(LangKey.FIND_NETHER_STRUCTURES)
                .parent(findOverworldStructure)
                .displayItem(Blocks.NETHERRACK)
                .showToast()
                .announceChat()
                .criteria("find_dark_treasure", PlayerTrigger.TriggerInstance.located(inStructure(darkTreasure)))
                .experienceReward(36)
                .build(saver, helper);

        AdvancementHolder findEndPlatform = create(LangKey.FIND_END_PLATFORM)
                .parent(findNetherStructure)
                .displayItem(Items.ENDER_PEARL)
                .showToast()
                .announceChat()
                .criteria("enter_end_as_dragon", beDragon(EntityCondition.inDimension(Level.END)))
                .experienceReward(32)
                .build(saver, helper);

        TagKey<Structure> endTreasure = TagKey.create(Registries.STRUCTURE, DragonSurvival.res("treasure_end")); // FIXME :: use tag from data generation
        create(LangKey.FIND_END_STRUCTURES)
                .parent(findEndPlatform)
                .displayItem(DSItems.SPIN_GRANT_ITEM.value())
                .showToast()
                .announceChat()
                .criteria("find_end_structures", PlayerTrigger.TriggerInstance.located(inStructure(endTreasure)))
                .experienceReward(64)
                .build(saver, helper);
    }

    private void buildUseMemoryBlockChildren(final AdvancementHolder parent) {
        // --- Parent: use_memory_block --- //

        AdvancementHolder changeBeacon = create(LangKey.CHANGE_BEACON)
                .parent(parent)
                .displayItem(DSItems.BEACON_ACTIVATOR.value())
                .showToast()
                .announceChat()
                .criteria("activate_beacon", itemUsedOnBlock(DSBlocks.DRAGON_BEACON.value(), DSItems.BEACON_ACTIVATOR.value()))
                .experienceReward(10)
                .build(saver, helper);

        // --- Parent: change_beacon --- //

        create(LangKey.GET_ALL_BEACONS)
                .parent(changeBeacon)
                .displayItem(DSItems.ELDER_DRAGON_DUST.value())
                .showToast()
                .announceChat()
                .criteria("affected_by_peace", effectWithMinDuration(DSEffects.PEACE, Functions.secondsToTicks(20)))
                .criteria("affected_by_fire", effectWithMinDuration(DSEffects.FIRE, Functions.secondsToTicks(20)))
                .criteria("affected_by_magic", effectWithMinDuration(DSEffects.MAGIC, Functions.secondsToTicks(20)))
                .build(saver, helper);
    }

    private void buildSleepOnTreasureChildren(final AdvancementHolder parent) {
        // --- Parent: sleep_on_treasure --- //

        AdvancementHolder sleepOnHoard = create(LangKey.SLEEP_ON_HOARD)
                .parent(parent)
                .displayItem(Items.GOLD_INGOT)
                .showToast()
                .announceChat()
                .criteria("sleep_on_treasure_hoard", sleepOnTreasure(100))
                .experienceReward(40)
                .build(saver, helper);

        // --- Parent: sleep_on_hoard --- //


        create(LangKey.SLEEP_ON_MASSIVE_HOARD)
                .parent(sleepOnHoard)
                .displayItem(DSBlocks.GOLD_DRAGON_TREASURE.value())
                .showToast()
                .announceChat()
                .criteria("sleep_on_massive_treasure_hoard", sleepOnTreasure(240))
                .experienceReward(120)
                .build(saver, helper);
    }

    private void buildBeYoungDragonChildren(final AdvancementHolder parent) {
        // --- Parent: be_young_dragon --- //

        AdvancementHolder beAdultDragon = create(LangKey.BE_ADULT_DRAGON)
                .parent(parent)
                .displayItem(DSItems.WEAK_DRAGON_HEART.value())
                .showToast()
                .announceChat()
                .criteria("reach_adult_stage", beDragon(DragonStages.adult))
                .build(saver, helper);

        // --- Parent: be_adult_dragon --- //

        AdvancementHolder collectHeartFromMonster = create(LangKey.COLLECT_HEART_FROM_MONSTER)
                .parent(beAdultDragon)
                .displayItem(DSItems.ELDER_DRAGON_HEART.value())
                .criteria("collect_elder_dragon_heart", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.ELDER_DRAGON_HEART.value()))
                .experienceReward(6)
                .build(saver, helper);

        // --- Parent: collect_heart_from_monster --- //

        AdvancementHolder beOldCaveDragon = create(LangKey.CAVE_BE_OLD_DRAGON)
                .parent(collectHeartFromMonster)
                .displayItem(DSItems.CAVE_BEACON.value())
                .showToast()
                .announceChat()
                .criteria("be_fully_grown_adult", beDragon(registries.holderOrThrow(BuiltInDragonSpecies.CAVE_DRAGON), registries.holderOrThrow(DragonStages.adult), 1))
                .experienceReward(120)
                .build(saver, helper);

        // --- Parent: cave/be_old_dragon --- //

        create(LangKey.CAVE_MASTER_ALL_PASSIVES)
                .parent(beOldCaveDragon)
                .displayItem(DSBlocks.CAVE_SOURCE_OF_MAGIC.value())
                .showToast()
                .announceChat()
                .criteria("master_burn", upgradeAbilityMax(registries.holderOrThrow(CaveDragonAbilities.BURN)))
                .criteria("master_cave_athletics", upgradeAbilityMax(registries.holderOrThrow(CaveDragonAbilities.CAVE_ATHLETICS)))
                .criteria("master_contrast_shower", upgradeAbilityMax(registries.holderOrThrow(CaveDragonAbilities.CONTRAST_SHOWER)))
                .criteria("master_cave_magic", upgradeAbilityMax(registries.holderOrThrow(CaveDragonAbilities.CAVE_MAGIC)))
                .experienceReward(150)
                .build(saver, helper);

        AdvancementHolder beOldSeaDragon = create(LangKey.SEA_BE_OLD_DRAGON)
                .parent(collectHeartFromMonster)
                .displayItem(DSItems.SEA_BEACON.value())
                .showToast()
                .announceChat()
                .criteria("be_fully_grown_adult", beDragon(registries.holderOrThrow(BuiltInDragonSpecies.SEA_DRAGON), registries.holderOrThrow(DragonStages.adult), 1))
                .experienceReward(120)
                .build(saver, helper);

        // --- Parent: sea/be_old_dragon --- //

        create(LangKey.SEA_MASTER_ALL_PASSIVES)
                .parent(beOldSeaDragon)
                .displayItem(DSBlocks.SEA_SOURCE_OF_MAGIC.value())
                .showToast()
                .announceChat()
                .criteria("master_spectral_impact", upgradeAbilityMax(registries.holderOrThrow(SeaDragonAbilities.SPECTRAL_IMPACT)))
                .criteria("master_sea_athletics", upgradeAbilityMax(registries.holderOrThrow(SeaDragonAbilities.SEA_ATHLETICS)))
                .criteria("master_hydration", upgradeAbilityMax(registries.holderOrThrow(SeaDragonAbilities.HYDRATION)))
                .criteria("master_sea_magic", upgradeAbilityMax(registries.holderOrThrow(SeaDragonAbilities.SEA_MAGIC)))
                .experienceReward(150)
                .build(saver, helper);

        AdvancementHolder beOldForestDragon = create(LangKey.FOREST_BE_OLD_DRAGON)
                .parent(collectHeartFromMonster)
                .displayItem(DSItems.FOREST_BEACON.value())
                .showToast()
                .announceChat()
                .criteria("be_fully_grown_adult", beDragon(registries.holderOrThrow(BuiltInDragonSpecies.FOREST_DRAGON), registries.holderOrThrow(DragonStages.adult), 1))
                .experienceReward(120)
                .build(saver, helper);

        // --- Parent: forest/be_old_dragon --- //

        create(LangKey.FOREST_MASTER_ALL_PASSIVES)
                .parent(beOldForestDragon)
                .displayItem(DSBlocks.FOREST_SOURCE_OF_MAGIC.value())
                .showToast()
                .announceChat()
                .criteria("master_cliffhanger", upgradeAbilityMax(registries.holderOrThrow(ForestDragonAbilities.CLIFFHANGER)))
                .criteria("master_forest_athletics", upgradeAbilityMax(registries.holderOrThrow(ForestDragonAbilities.FOREST_ATHLETICS)))
                .criteria("master_light_in_darkness", upgradeAbilityMax(registries.holderOrThrow(ForestDragonAbilities.LIGHT_IN_DARKNESS)))
                .criteria("master_forest_magic", upgradeAbilityMax(registries.holderOrThrow(ForestDragonAbilities.FOREST_MAGIC)))
                .experienceReward(150)
                .build(saver, helper);
    }

    private Builder create(final String path) {
        return new Builder(path);
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

    public Criterion<StopNaturalGrowthTrigger.Instance> stopNaturalGrowth() {
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
        return beDragon(Condition.dragonSpecies(type).build());
    }

    public Criterion<BeDragonTrigger.Instance> beDragon(double growth) {
        return beDragon(Condition.dragonSizeAtLeast(growth).build());
    }

    public Criterion<BeDragonTrigger.Instance> beDragon(final ResourceKey<DragonStage> dragonStage) {
        return beDragon(Condition.dragonStage(registries.holderOrThrow(dragonStage)).build());
    }

    public Criterion<BeDragonTrigger.Instance> beDragon(final Holder<DragonSpecies> species, final Holder<DragonStage> dragonStage, double progress) {
        return beDragon(EntityPredicate.Builder.entity().subPredicate(DragonPredicate.Builder.dragon().species(species).stage(dragonStage, MinMaxBounds.Doubles.atLeast(progress)).build()).build());
    }

    public Criterion<BeDragonTrigger.Instance> beDragon(final EntityPredicate predicate) {
        return DSAdvancementTriggers.BE_DRAGON.get().createCriterion(new BeDragonTrigger.Instance(Optional.of(EntityPredicate.wrap(predicate))));
    }

    // -- Steal From Villagers -- //

    public Criterion<StealFromVillagerTrigger.Instance> stealFromVillager() {
        return DSAdvancementTriggers.STEAL_FROM_VILLAGER.get().createCriterion(new StealFromVillagerTrigger.Instance(Optional.empty()));
    }
}
