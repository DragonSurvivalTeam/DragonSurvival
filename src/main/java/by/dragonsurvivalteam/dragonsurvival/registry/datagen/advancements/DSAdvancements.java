package by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonStagePredicate;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.BeDragonTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.ConvertItemFromAbility;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.MineBlockUnderLavaTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.SleepOnTreasureTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.StealFromVillagerTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.StopNaturalGrowthTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.UpgradeAbilityTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.UseDragonSoulTrigger;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSubPredicates;
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
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("deprecation") // ignore
public class DSAdvancements implements AdvancementSubProvider {
    private HolderLookup.Provider registries;
    private Consumer<Advancement> saver;

    @Override
    public void generate(@NotNull final HolderLookup.Provider registries, @NotNull final Consumer<Advancement> saver) {
        this.registries = registries;
        this.saver = saver;

        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        DSSubPredicates.withCodecOps(ops, () -> {
            generateAdvancements();
            return null;
        });
    }

    private void generateAdvancements() {
        Advancement root = create(LangKey.ROOT)
                .type(FrameType.GOAL)
                .displayItem(DSItems.ELDER_DRAGON_BONE.get())
                .background(DragonSurvival.res("textures/block/stone_dragon_door_top.png"))
                .noDescription()
                .criteria("root", PlayerTrigger.TriggerInstance.tick())
                .build(saver);

        // --- Parent: root --- //

        Advancement beDragon = create(LangKey.BE_DRAGON)
                .parent(root)
                .displayItem(DSItems.STAR_BONE.get())
                .criteria("be_dragon", beDragon())
                .experienceReward(12)
                .build(saver);
        buildBeDragonChildren(beDragon);

        Advancement collectDust = create(LangKey.COLLECT_DUST)
                .parent(root)
                .displayItem(Items.COAL_ORE)
                .criteria("collect_elder_dragon_dust", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.ELDER_DRAGON_DUST.get()))
                .experienceReward(6)
                .build(saver);
        buildCollectDustChildren(collectDust);

        Advancement placeAltar = create(LangKey.PLACE_ALTAR)
                .parent(root)
                .displayItem(DSBlocks.STONE_DRAGON_ALTAR.get())
                .criteria("place_dragon_altar", placeBlock(DSItemTags.DRAGON_ALTARS))
                .experienceReward(6)
                .build(saver);
        buildPlaceAltarChildren(placeAltar);

        Advancement pathChoice = create(LangKey.PATH_CHOICE)
                .parent(root)
                .displayItem(Items.OAK_SIGN)
                .criteria("interact_with_villager", noItemInteract(EntityType.VILLAGER))
                .experienceReward(6)
                .build(saver);
        buildHunterAdvancements(pathChoice);
        buildLightAdvancements(pathChoice);
        buildDarkAdvancements(pathChoice);
    }

    private void buildDarkAdvancements(final Advancement parent) {
        // --- Parent: path_choice --- //

        ItemStack head = Items.PLAYER_HEAD.getDefaultInstance();
        head.getOrCreateTag().putString("SkullOwner", "MHF_Villager");
        Advancement affectedByHunterOmen = create(LangKey.DARK_AFFECTED_BY_HUNTER_OMEN)
                .parent(parent)
                .displayItem(head)
                .showToast()
                .announceChat()
                .criteria("affected_by_hunter_omen", effectWithMinDuration(DSEffects.HUNTER_OMEN.get(), 300))
                .experienceReward(6)
                .build(saver);

        // --- Parent: dark/affected_by_hunter_omen --- //

        create(LangKey.DARK_STEAL_FROM_VILLAGER)
                .parent(affectedByHunterOmen)
                .displayItem(DSItems.PARTISAN.get())
                .showToast()
                .announceChat()
                .criteria("steal_from_villager", stealFromVillager())
                .build(saver);

        Advancement collectKey = create(LangKey.DARK_COLLECT_KEY)
                .parent(affectedByHunterOmen)
                .displayItem(DSItems.DARK_KEY.get())
                .showToast()
                .announceChat()
                .criteria("collect_dark_key", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_KEY.get()))
                .build(saver);

        create(LangKey.DARK_OPEN_VAULT)
                .parent(collectKey)
                .displayItem(DSBlocks.DARK_VAULT.get())
                .showToast()
                .announceChat()
                .criteria("open_dark_vault", itemUsedOnBlock(DSBlocks.DARK_VAULT.get(), DSItems.DARK_KEY.get()))
                .experienceReward(10)
                .build(saver);

        Advancement getArmorItem = create(LangKey.DARK_GET_ARMOR_ITEM)
                .parent(collectKey)
                .displayItem(DSItems.DARK_DRAGON_HELMET.get())
                .showToast()
                .announceChat()
                .criteria("collect_dark_armor", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_HELMET.get(), DSItems.DARK_DRAGON_CHESTPLATE.get(), DSItems.DARK_DRAGON_LEGGINGS.get(), DSItems.DARK_DRAGON_BOOTS.get()))
                .build(saver);

        // --- Parent: dark/get_armor_item --- //

        create(LangKey.DARK_GET_ARMOR_SET)
                .parent(getArmorItem)
                .displayItem(DSItems.DARK_DRAGON_HELMET.get())
                .showToast()
                .announceChat()
                .criteria("collect_dark_armor_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_HELMET.get()))
                .criteria("collect_dark_armor_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_CHESTPLATE.get()))
                .criteria("collect_dark_armor_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_LEGGINGS.get()))
                .criteria("collect_dark_armor_boots", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.DARK_DRAGON_BOOTS.get()))
                .build(saver);
    }

    private void buildLightAdvancements(final Advancement parent) {
        // --- Parent: path_choice --- //

        Advancement dragonRiderWorkbench = create(LangKey.LIGHT_DRAGON_RIDER_WORKBENCH)
                .parent(parent)
                .displayItem(DSBlocks.DRAGON_RIDER_WORKBENCH.get())
                .criteria("craft_dragon_rider_workbench", crafted(DSBlocks.DRAGON_RIDER_WORKBENCH.get()))
                .experienceReward(6)
                .build(saver);

        // --- Parent: light/dragon_rider_workbench --- //

        Advancement collectKey = create(LangKey.LIGHT_COLLECT_KEY)
                .parent(dragonRiderWorkbench)
                .displayItem(DSItems.LIGHT_KEY.get())
                .showToast()
                .announceChat()
                .criteria("collect_light_key", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_KEY.get()))
                .build(saver);

        create(LangKey.LIGHT_OPEN_VAULT)
                .parent(collectKey)
                .displayItem(DSBlocks.LIGHT_VAULT.get())
                .showToast()
                .announceChat()
                .criteria("open_light_vault", itemUsedOnBlock(DSBlocks.LIGHT_VAULT.get(), DSItems.LIGHT_KEY.get()))
                .experienceReward(10)
                .build(saver);

        Advancement getArmorItem = create(LangKey.LIGHT_GET_ARMOR_ITEM)
                .parent(collectKey)
                .displayItem(DSItems.LIGHT_DRAGON_HELMET.get())
                .showToast()
                .announceChat()
                .criteria("collect_light_armor", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_HELMET.get(), DSItems.LIGHT_DRAGON_CHESTPLATE.get(), DSItems.LIGHT_DRAGON_LEGGINGS.get(), DSItems.LIGHT_DRAGON_BOOTS.get()))
                .build(saver);

        // --- Parent: light/get_armor_item --- //

        create(LangKey.LIGHT_GET_ARMOR_SET)
                .parent(getArmorItem)
                .displayItem(DSItems.LIGHT_DRAGON_HELMET.get())
                .showToast()
                .announceChat()
                .criteria("collect_light_armor_helmet", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_HELMET.get()))
                .criteria("collect_light_armor_chestplate", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_CHESTPLATE.get()))
                .criteria("collect_light_armor_leggings", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_LEGGINGS.get()))
                .criteria("collect_light_armor_boots", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.LIGHT_DRAGON_BOOTS.get()))
                .build(saver);
    }

    private void buildHunterAdvancements(final Advancement parent) {
        // --- Parent: path_choice --- //

        Advancement promotion = create(LangKey.HUNTER_PROMOTION)
                .parent(parent)
                .displayItem(DSItems.SPEARMAN_PROMOTION.get())
                .showToast()
                .announceChat()
                .criteria("promote_spearman", itemInteract(DSEntities.HUNTER_SPEARMAN.get(), DSItems.SPEARMAN_PROMOTION.get()))
                .experienceReward(6)
                .build(saver);

        // --- Parent: hunter/promotion --- //

        Advancement collectKey = create(LangKey.HUNTER_COLLECT_KEY)
                .parent(promotion)
                .displayItem(DSItems.HUNTER_KEY.get())
                .showToast()
                .announceChat()
                .criteria("collect_hunter_key", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.HUNTER_KEY.get()))
                .build(saver);

        create(LangKey.HUNTER_OPEN_VAULT)
                .parent(collectKey)
                .displayItem(DSBlocks.HUNTER_VAULT.get())
                .showToast()
                .announceChat()
                .criteria("open_hunter_vault", itemUsedOnBlock(DSBlocks.HUNTER_VAULT.get(), DSItems.HUNTER_KEY.get()))
                .experienceReward(10)
                .build(saver);

        Holder.Reference<Enchantment> bolas = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(DSEnchantments.BOLAS);

        create(LangKey.HUNTER_FIRE_BOLAS)
                .parent(collectKey)
                .displayItem(DSItems.BOLAS.get())
                .criteria("fire_bolas", ShotCrossbowTrigger.TriggerInstance.shotCrossbow(
                        ItemPredicate.Builder.item()
                                .of(Items.CROSSBOW)
                                .hasEnchantment(new EnchantmentPredicate(bolas.value(), MinMaxBounds.Ints.atLeast(1)))
                                .build()))
                .build(saver);
    }

    private void buildPlaceAltarChildren(final Advancement parent) {
        // --- Parent: place_altar --- //

        ItemStack caveSoul = DSItems.DRAGON_SOUL.get().getDefaultInstance();
        caveSoul.getOrCreateTag().putInt("CustomModelData", 2);

        Advancement beCaveDragon = create(LangKey.CAVE_BE_DRAGON)
                .parent(parent)
                .displayItem(caveSoul)
                .showToast()
                .announceChat()
                .criteria("be_cave_dragon", beDragon(holder(BuiltInDragonSpecies.CAVE_DRAGON)))
                .experienceReward(12)
                .build(saver);
        buildBeCaveDragonChildren(beCaveDragon);

        ItemStack seaSoul = DSItems.DRAGON_SOUL.get().getDefaultInstance();
        seaSoul.getOrCreateTag().putInt("CustomModelData", 3);

        Advancement beSeaDragon = create(LangKey.SEA_BE_DRAGON)
                .parent(parent)
                .displayItem(seaSoul)
                .showToast()
                .announceChat()
                .criteria("be_sea_dragon", beDragon(holder(BuiltInDragonSpecies.SEA_DRAGON)))
                .experienceReward(12)
                .build(saver);
        buildBeSeaDragonChildren(beSeaDragon);

        ItemStack forestSoul = DSItems.DRAGON_SOUL.get().getDefaultInstance();
        forestSoul.getOrCreateTag().putInt("CustomModelData", 1);

        Advancement beForestDragon = create(LangKey.FOREST_BE_DRAGON)
                .parent(parent)
                .displayItem(forestSoul)
                .showToast()
                .announceChat()
                .criteria(beDragon(holder(BuiltInDragonSpecies.FOREST_DRAGON)))
                .experienceReward(12)
                .build(saver);
        buildBeForestDragonChildren(beForestDragon);
    }

    private void buildBeCaveDragonChildren(final Advancement parent) {
        // --- Parent: cave/be_dragon --- //

        Advancement rockEater = create(LangKey.CAVE_ROCK_EATER)
                .parent(parent)
                .displayItem(DSItems.CHARGED_COAL.get())
                .criteria("consume_charged_coal", consumeItem(DSItems.CHARGED_COAL.get()))
                .criteria("consume_charged_soup", consumeItem(DSItems.CHARGED_SOUP.get()))
                .criteria("consume_charred_meat", consumeItem(DSItems.CHARRED_MEAT.get()))
                .criteria("consume_charred_seafood", consumeItem(DSItems.CHARRED_SEAFOOD.get()))
                .criteria("consume_hot_dragon_rod", consumeItem(DSItems.HOT_DRAGON_ROD.get()))
                .criteria("consume_explosive_copper", consumeItem(DSItems.EXPLOSIVE_COPPER.get()))
                .criteria("consume_quartz_explosive_copper", consumeItem(DSItems.QUARTZ_EXPLOSIVE_COPPER.get()))
                .criteria("consume_double_quartz", consumeItem(DSItems.DOUBLE_QUARTZ.get()))
                .criteria("consume_cave_dragon_treat", consumeItem(DSItems.CAVE_DRAGON_TREAT.get()))
                .experienceReward(60)
                .build(saver);

        Advancement swimInLava = create(LangKey.CAVE_SWIM_IN_LAVA)
                .parent(parent)
                .displayItem(Items.LAVA_BUCKET)
                .criteria("swim_in_lava", location(Condition.dragonSpecies(holder(BuiltInDragonSpecies.CAVE_DRAGON)).located(isInFluid(FluidTags.LAVA).build())))
                .experienceReward(20)
                .build(saver);

        // --- Parent: cave/rock_eater --- //

        create(LangKey.CAVE_WATER_SAFETY)
                .parent(rockEater)
                .displayItem(DSItems.CHARGED_SOUP.get())
                .criteria("swim_safely_in_lava", location(
                        Condition.dragonSpecies(holder(BuiltInDragonSpecies.CAVE_DRAGON))
                                .located(isInFluid(FluidTags.WATER).build())
                                .effects(MobEffectsPredicate.effects().and(DSEffects.FIRE.get()))
                ))
                .experienceReward(40)
                .build(saver);

        // --- Parent: cave/swim_in_lava --- //

        Advancement diamondsInLava = create(LangKey.CAVE_DIAMONDS_IN_LAVA)
                .parent(swimInLava)
                .displayItem(Items.DIAMOND_ORE)
                .criteria("mine_diamond_in_lava", mineBlockInLava(Tags.Blocks.ORES_DIAMOND))
                .experienceReward(40)
                .build(saver);

        create(LangKey.CAVE_GO_HOME)
                .parent(diamondsInLava)
                .displayItem(Items.NETHER_BRICK_STAIRS)
                .showToast()
                .announceChat()
                .criteria("explore_nether_lava_sea", location(
                        Condition.dragonSpecies(holder(BuiltInDragonSpecies.CAVE_DRAGON))
                                .located(inDimension(Level.NETHER).setFluid(fluid(FluidTags.LAVA).build()).build())
                                .effects(hasEffect(DSEffects.LAVA_VISION.get()))
                ))
                .experienceReward(20)
                .build(saver);
    }

    private void buildBeSeaDragonChildren(final Advancement parent) {
        // --- Parent: sea/be_dragon --- //

        Advancement lootShipwreck = create(LangKey.SEA_LOOT_SHIPWRECK)
                .parent(parent)
                .displayItem(Items.HEART_OF_THE_SEA)
                .criteria("explore_shipwreck", location(Condition.dragonSpecies(holder(BuiltInDragonSpecies.SEA_DRAGON)).located(inStructure(BuiltinStructures.SHIPWRECK).build())))
                .criteria("explore_beached_shipwreck", location(Condition.dragonSpecies(holder(BuiltInDragonSpecies.SEA_DRAGON)).located(inStructure(BuiltinStructures.SHIPWRECK_BEACHED).build())))
                .experienceReward(20)
                .build(saver);

        Advancement rainDancing = create(LangKey.SEA_RAIN_DANCING)
                .parent(parent)
                .displayItem(Items.WATER_BUCKET)
                .criteria("experience_rain_and_thunder", location(ContextAwarePredicate.create(
                        entityCondition(Condition.dragonSpecies(holder(BuiltInDragonSpecies.SEA_DRAGON)).build()),
                        WeatherCheck.weather().setRaining(true).setThundering(true).build()
                )))
                .experienceReward(30)
                .build(saver);

        // --- Parent: sea/loot_shipwreck --- //

        create(LangKey.SEA_FISH_EATER)
                .parent(lootShipwreck)
                .displayItem(DSItems.SEASONED_FISH.get())
                .criteria("consume_kelp", consumeItem(Items.KELP))
                .criteria("consume_seasoned_fish", consumeItem(DSItems.SEASONED_FISH.get()))
                .criteria("consume_golden_coral_pufferfish", consumeItem(DSItems.GOLDEN_CORAL_PUFFERFISH.get()))
                .criteria("consume_frozen_raw_fish", consumeItem(DSItems.FROZEN_RAW_FISH.get()))
                .criteria("consume_golden_turtle_egg", consumeItem(DSItems.GOLDEN_TURTLE_EGG.get()))
                .criteria("consume_sea_dragon_treat", consumeItem(DSItems.SEA_DRAGON_TREAT.get()))
                .experienceReward(80)
                .build(saver);

        // --- Parent: sea/rain_dancing --- //

        Advancement placeSnowInNether = create(LangKey.SEA_PLACE_SNOW_IN_NETHER)
                .parent(rainDancing)
                .displayItem(Items.SNOW_BLOCK)
                .criteria("place_snow_in_nether", placeBlockAsDragon(
                        Condition.dragonSpecies(holder(BuiltInDragonSpecies.SEA_DRAGON)).located(inDimension(Level.NETHER).build()), Blocks.SNOW_BLOCK
                ))
                .experienceReward(16)
                .build(saver);

        // --- Parent: sea/place_snow_in_nether --- //

        create(LangKey.SEA_PEACE_IN_NETHER)
                .parent(placeSnowInNether)
                .displayItem(Items.CAULDRON)
                .criteria("be_safe_in_nether", location(
                        Condition.dragonSpecies(holder(BuiltInDragonSpecies.SEA_DRAGON))
                                .effects(hasEffect(DSEffects.PEACE.get()))
                                .located(inDimension(Level.NETHER).build())
                ))
                .build(saver);
    }

    private void buildBeForestDragonChildren(final Advancement parent) {
        // --- Parent: forest/be_dragon --- //

        Advancement standOnSweetBerries = create(LangKey.FOREST_STAND_ON_SWEET_BERRIES)
                .parent(parent)
                .displayItem(Items.SWEET_BERRIES)
                .criteria("stand_on_sweet_berries", location(Condition.dragonSpecies(holder(BuiltInDragonSpecies.FOREST_DRAGON)).steppingOn(block(Blocks.SWEET_BERRY_BUSH).build())))
                .experienceReward(30)
                .build(saver);

        Advancement poisonousPotato = create(LangKey.FOREST_POISONOUS_POTATO)
                .parent(parent)
                .displayItem(Items.POISONOUS_POTATO)
                .criteria("convert_potato", convertPotato(Condition.dragonSpecies(holder(BuiltInDragonSpecies.FOREST_DRAGON))))
                .experienceReward(16)
                .build(saver);

        // --- Parent: forest/stand_on_sweet_berries --- //

        create(LangKey.FOREST_PREVENT_DARKNESS_PENALTY)
                .parent(standOnSweetBerries)
                .displayItem(DSItems.LUMINOUS_OINTMENT.get())
                .criteria("be_safe_in_darkness", location(
                        Condition.dragonSpecies(holder(BuiltInDragonSpecies.FOREST_DRAGON))
                                .located(light(MinMaxBounds.Ints.between(0, 3)).build())
                                .effects(MobEffectsPredicate.effects().and(DSEffects.MAGIC.get()))
                ))
                .experienceReward(40)
                .build(saver);

        // --- Parent: forest/poisonous_potato --- //

        Advancement meatEater = create(LangKey.FOREST_MEAT_EATER)
                .parent(poisonousPotato)
                .displayItem(DSItems.MEAT_WILD_BERRIES.get())
                .criteria("consume_sweet_sour_rabbit", consumeItem(DSItems.SWEET_SOUR_RABBIT.get()))
                .criteria("consume_luminous_ointment", consumeItem(DSItems.LUMINOUS_OINTMENT.get()))
                .criteria("consume_diamond_chorus", consumeItem(DSItems.DIAMOND_CHORUS.get()))
                .criteria("consume_smelly_meat_porridge", consumeItem(DSItems.SMELLY_MEAT_PORRIDGE.get()))
                .criteria("consume_meat_wilderness", consumeItem(DSItems.MEAT_WILD_BERRIES.get()))
                .criteria("consume_meat_chorus_mix", consumeItem(DSItems.MEAT_CHORUS_MIX.get()))
                .criteria("consume_forest_dragon_treat", consumeItem(DSItems.FOREST_DRAGON_TREAT.get()))
                .experienceReward(60)
                .build(saver);

        // --- Parent: forest/meat_eater --- //

        create(LangKey.FOREST_TRANSPLANT_CHORUS_FRUIT)
                .parent(meatEater)
                .displayItem(DSItems.DIAMOND_CHORUS.get())
                .criteria("place_chorus_fruit", placeBlockAsDragon(
                        Condition.dragonSpecies(holder(BuiltInDragonSpecies.FOREST_DRAGON)).located(inDimension(Level.OVERWORLD).build()), Blocks.CHORUS_FLOWER
                ))
                .experienceReward(90)
                .build(saver);
    }

    private void buildBeDragonChildren(final Advancement parent) {
        // --- Parent: be_dragon --- //

        Advancement stopNaturalGrowth = create(LangKey.STOP_NATURAL_GROWTH)
                .parent(parent)
                .displayItem(DSItems.STAR_HEART.get())
                .showToast()
                .announceChat()
                .criteria("stop_natural_growth", stopNaturalGrowth())
                .experienceReward(30)
                .build(saver);

        // --- Parent: stop_natural_growth --- //

        create(LangKey.USE_DRAGON_SOUL)
                .parent(stopNaturalGrowth)
                .displayItem(DSItems.DRAGON_SOUL.get())
                .showToast()
                .announceChat()
                .criteria("use_dragon_soul", useDragonSoul())
                .experienceReward(120)
                .build(saver);
    }

    private void buildCollectDustChildren(final Advancement parent) {
        // --- Parent: collect_dust --- //

        Advancement beYoungDragon = create(LangKey.BE_YOUNG_DRAGON)
                .parent(parent)
                .displayItem(DSItems.DRAGON_HEART_SHARD.get())
                .showToast()
                .announceChat()
                .criteria("reach_young_stage", beDragon(DragonStages.young))
                .experienceReward(12)
                .build(saver);
        buildBeYoungDragonChildren(beYoungDragon);

        Advancement sleepOnTreasure = create(LangKey.SLEEP_ON_TREASURE)
                .parent(parent)
                .displayItem(Items.GOLD_NUGGET)
                .announceChat()
                .criteria("sleep_on_small_treasure_hoard", sleepOnTreasure(10))
                .experienceReward(10)
                .build(saver);
        buildSleepOnTreasureChildren(sleepOnTreasure);

        Advancement findBones = create(LangKey.FIND_BONES)
                .parent(parent)
                .displayItem(DSItems.STAR_BONE.get())
                .showToast()
                .announceChat()
                .criteria("find_cave_dragon_bones", locatedInStructure("dragon_skeleton_cave"))
                .criteria("find_forest_dragon_bones", locatedInStructure("dragon_skeleton_forest"))
                .criteria("find_sea_dragon_bones", locatedInStructure("dragon_skeleton_sea"))
                .orRequirements()
                .experienceReward(12)
                .build(saver);

        buildFindBonesChildren(findBones);

        Advancement useMemoryBlock = create(LangKey.USE_MEMORY_BLOCK)
                .parent(parent)
                .displayItem(DSBlocks.DRAGON_MEMORY_BLOCK.get())
                .showToast()
                .announceChat()
                // TODO :: check for the other way as well (place memory block under a beacon)
                .criteria("place_beacon_on_memory_block", itemUsedOnBlock(DSBlocks.DRAGON_MEMORY_BLOCK.get(), DSBlocks.DRAGON_BEACON.get()))
                .experienceReward(10)
                .build(saver);

        buildUseMemoryBlockChildren(useMemoryBlock);
    }

    private void buildFindBonesChildren(final Advancement parent) {
        // --- Parent: find_bones --- //
        Advancement findOverworldStructure = create(LangKey.FIND_OVERWORLD_STRUCTURES)
                .parent(parent)
                .displayItem(Blocks.GRASS_BLOCK)
                .showToast()
                .announceChat()
                .criteria("find_friendly_cave_treasure", locatedInStructure("treasure_friendly_cave"))
                .criteria("find_friendly_forest_treasure", locatedInStructure("treasure_friendly_forest"))
                .criteria("find_friendly_sea_treasure", locatedInStructure("treasure_friendly_sea"))
                .orRequirements()
                .experienceReward(24)
                .build(saver);

        Advancement findNetherStructure = create(LangKey.FIND_NETHER_STRUCTURES)
                .parent(findOverworldStructure)
                .displayItem(Blocks.NETHERRACK)
                .showToast()
                .announceChat()
                .criteria("find_angry_cave_treasure", locatedInStructure("treasure_angry_cave"))
                .criteria("find_angry_forest_treasure", locatedInStructure("treasure_angry_forest"))
                .criteria("find_angry_sea_treasure", locatedInStructure("treasure_angry_sea"))
                .orRequirements()
                .experienceReward(36)
                .build(saver);

        Advancement findEndPlatform = create(LangKey.FIND_END_PLATFORM)
                .parent(findNetherStructure)
                .displayItem(Items.ENDER_PEARL)
                .showToast()
                .announceChat()
                .criteria("enter_end_as_dragon", beDragon(EntityCondition.inDimension(Level.END)))
                .experienceReward(32)
                .build(saver);

        create(LangKey.FIND_END_STRUCTURES)
                .parent(findEndPlatform)
                .displayItem(DSItems.SPIN_GRANT_ITEM.get())
                .showToast()
                .announceChat()
                .criteria("find_end_structures", locatedInStructure("treasure_end"))
                .experienceReward(64)
                .build(saver);
    }

    private void buildUseMemoryBlockChildren(final Advancement parent) {
        // --- Parent: use_memory_block --- //

        Advancement changeBeacon = create(LangKey.CHANGE_BEACON)
                .parent(parent)
                .displayItem(DSItems.BEACON_ACTIVATOR.get())
                .showToast()
                .announceChat()
                .criteria("activate_beacon", itemUsedOnBlock(DSBlocks.DRAGON_BEACON.get(), DSItems.BEACON_ACTIVATOR.get()))
                .experienceReward(10)
                .build(saver);

        // --- Parent: change_beacon --- //

        create(LangKey.GET_ALL_BEACONS)
                .parent(changeBeacon)
                .displayItem(DSItems.ELDER_DRAGON_DUST.get())
                .showToast()
                .announceChat()
                .criteria("affected_by_peace", effectWithMinDuration(DSEffects.PEACE.get(), Functions.secondsToTicks(20)))
                .criteria("affected_by_fire", effectWithMinDuration(DSEffects.FIRE.get(), Functions.secondsToTicks(20)))
                .criteria("affected_by_magic", effectWithMinDuration(DSEffects.MAGIC.get(), Functions.secondsToTicks(20)))
                .build(saver);
    }

    private void buildSleepOnTreasureChildren(final Advancement parent) {
        // --- Parent: sleep_on_treasure --- //

        Advancement sleepOnHoard = create(LangKey.SLEEP_ON_HOARD)
                .parent(parent)
                .displayItem(Items.GOLD_INGOT)
                .showToast()
                .announceChat()
                .criteria("sleep_on_treasure_hoard", sleepOnTreasure(100))
                .experienceReward(40)
                .build(saver);

        // --- Parent: sleep_on_hoard --- //


        create(LangKey.SLEEP_ON_MASSIVE_HOARD)
                .parent(sleepOnHoard)
                .displayItem(DSBlocks.GOLD_DRAGON_TREASURE.get())
                .showToast()
                .announceChat()
                .criteria("sleep_on_massive_treasure_hoard", sleepOnTreasure(240))
                .experienceReward(120)
                .build(saver);
    }

    private void buildBeYoungDragonChildren(final Advancement parent) {
        // --- Parent: be_young_dragon --- //

        Advancement beAdultDragon = create(LangKey.BE_ADULT_DRAGON)
                .parent(parent)
                .displayItem(DSItems.WEAK_DRAGON_HEART.get())
                .showToast()
                .announceChat()
                .criteria("reach_adult_stage", beDragon(DragonStages.adult))
                .build(saver);

        // --- Parent: be_adult_dragon --- //

        Advancement collectHeartFromMonster = create(LangKey.COLLECT_HEART_FROM_MONSTER)
                .parent(beAdultDragon)
                .displayItem(DSItems.ELDER_DRAGON_HEART.get())
                .criteria("collect_elder_dragon_heart", InventoryChangeTrigger.TriggerInstance.hasItems(DSItems.ELDER_DRAGON_HEART.get()))
                .experienceReward(6)
                .build(saver);

        // --- Parent: collect_heart_from_monster --- //

        Advancement beOldCaveDragon = create(LangKey.CAVE_BE_OLD_DRAGON)
                .parent(collectHeartFromMonster)
                .displayItem(DSItems.CAVE_BEACON.get())
                .showToast()
                .announceChat()
                .criteria("be_fully_grown_adult", beDragon(holder(BuiltInDragonSpecies.CAVE_DRAGON), holder(DragonStages.adult), 1))
                .experienceReward(120)
                .build(saver);

        // --- Parent: cave/be_old_dragon --- //

        create(LangKey.CAVE_MASTER_ALL_PASSIVES)
                .parent(beOldCaveDragon)
                .displayItem(DSBlocks.CAVE_SOURCE_OF_MAGIC.get())
                .showToast()
                .announceChat()
                .criteria("master_burn", upgradeAbilityMax(holder(CaveDragonAbilities.BURN)))
                .criteria("master_cave_athletics", upgradeAbilityMax(holder(CaveDragonAbilities.CAVE_ATHLETICS)))
                .criteria("master_contrast_shower", upgradeAbilityMax(holder(CaveDragonAbilities.CONTRAST_SHOWER)))
                .criteria("master_cave_magic", upgradeAbilityMax(holder(CaveDragonAbilities.CAVE_MAGIC)))
                .experienceReward(150)
                .build(saver);

        Advancement beOldSeaDragon = create(LangKey.SEA_BE_OLD_DRAGON)
                .parent(collectHeartFromMonster)
                .displayItem(DSItems.SEA_BEACON.get())
                .showToast()
                .announceChat()
                .criteria("be_fully_grown_adult", beDragon(holder(BuiltInDragonSpecies.SEA_DRAGON), holder(DragonStages.adult), 1))
                .experienceReward(120)
                .build(saver);

        // --- Parent: sea/be_old_dragon --- //

        create(LangKey.SEA_MASTER_ALL_PASSIVES)
                .parent(beOldSeaDragon)
                .displayItem(DSBlocks.SEA_SOURCE_OF_MAGIC.get())
                .showToast()
                .announceChat()
                .criteria("master_spectral_impact", upgradeAbilityMax(holder(SeaDragonAbilities.SPECTRAL_IMPACT)))
                .criteria("master_sea_athletics", upgradeAbilityMax(holder(SeaDragonAbilities.SEA_ATHLETICS)))
                .criteria("master_hydration", upgradeAbilityMax(holder(SeaDragonAbilities.HYDRATION)))
                .criteria("master_sea_magic", upgradeAbilityMax(holder(SeaDragonAbilities.SEA_MAGIC)))
                .experienceReward(150)
                .build(saver);

        Advancement beOldForestDragon = create(LangKey.FOREST_BE_OLD_DRAGON)
                .parent(collectHeartFromMonster)
                .displayItem(DSItems.FOREST_BEACON.get())
                .showToast()
                .announceChat()
                .criteria("be_fully_grown_adult", beDragon(holder(BuiltInDragonSpecies.FOREST_DRAGON), holder(DragonStages.adult), 1))
                .experienceReward(120)
                .build(saver);

        // --- Parent: forest/be_old_dragon --- //

        create(LangKey.FOREST_MASTER_ALL_PASSIVES)
                .parent(beOldForestDragon)
                .displayItem(DSBlocks.FOREST_SOURCE_OF_MAGIC.get())
                .showToast()
                .announceChat()
                .criteria("master_cliffhanger", upgradeAbilityMax(holder(ForestDragonAbilities.CLIFFHANGER)))
                .criteria("master_forest_athletics", upgradeAbilityMax(holder(ForestDragonAbilities.FOREST_ATHLETICS)))
                .criteria("master_light_in_darkness", upgradeAbilityMax(holder(ForestDragonAbilities.LIGHT_IN_DARKNESS)))
                .criteria("master_forest_magic", upgradeAbilityMax(holder(ForestDragonAbilities.FOREST_MAGIC)))
                .experienceReward(150)
                .build(saver);
    }

    private AdvancementBuilder create(final String path) {
        return new AdvancementBuilder(path);
    }

    // --- Misc --- //

    private <T> Holder.Reference<T> holder(final ResourceKey<T> key) {
        ResourceKey<Registry<T>> registry = ResourceKey.createRegistryKey(key.registry());
        return registries.lookupOrThrow(registry).getOrThrow(key);
    }

    private PlayerTrigger.TriggerInstance tick(final EntityPredicate predicate) {
        return new PlayerTrigger.TriggerInstance(CriteriaTriggers.TICK.getId(), EntityPredicate.wrap(predicate));
    }

    @SuppressWarnings("deprecation") // ignore
    private RecipeCraftedTrigger.TriggerInstance crafted(final ItemLike item) {
        return RecipeCraftedTrigger.TriggerInstance.craftedItem(item.asItem().builtInRegistryHolder().key().location());
    }

    private LootItemCondition entityCondition(final EntityPredicate predicate) {
        return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, predicate).build();
    }

    private MobEffectsPredicate hasEffect(final MobEffect... effects) {
        MobEffectsPredicate predicate = MobEffectsPredicate.effects();

        for (MobEffect effect : effects) {
            predicate.and(effect);
        }

        return predicate;
    }

    private LocationPredicate.Builder inDimension(final ResourceKey<Level> dimension) {
        return LocationPredicate.Builder.location().setDimension(dimension);
    }

    private PlayerTrigger.TriggerInstance locatedInStructure(final String path) {
        return PlayerTrigger.TriggerInstance.located(
                inStructure(ResourceKey.create(Registries.STRUCTURE, DragonSurvival.res(path))).build()
        );
    }

    private LocationPredicate.Builder inStructure(final ResourceKey<Structure> structure) {
        return LocationPredicate.Builder.location().setStructure(structure);
    }

    private LocationPredicate.Builder isInFluid(final TagKey<Fluid> fluids) {
        return LocationPredicate.Builder.location().setFluid(fluid(fluids).build());
    }

    private Optional<ContextAwarePredicate> caveDragonInLava() {
        return Optional.of(EntityPredicate.wrap(
                Condition.dragonSpecies(holder(BuiltInDragonSpecies.CAVE_DRAGON))
                        .located(isInFluid(FluidTags.LAVA).build())
                        .build()
        ));
    }

    private FluidPredicate.Builder fluid(final TagKey<Fluid> fluids) {
        return FluidPredicate.Builder.fluid().of(fluids);
    }

    @SuppressWarnings("SameParameterValue") // ignore
    private LocationPredicate.Builder block(final Block block) {
        return LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).build());
    }

    private LocationPredicate.Builder light(final MinMaxBounds.Ints bounds) {
        return LocationPredicate.Builder.location().setLight(
                LightPredicate.Builder.light().setComposite(bounds).build()
        );
    }

    @SuppressWarnings("deprecation") // ignore
    public InventoryChangeTrigger.TriggerInstance dragonHasItem(final Holder<DragonSpecies> dragonSpecies, final ItemLike... items) {
        ItemPredicate[] predicates = new ItemPredicate[items.length];

        for (int index = 0; index < items.length; index++) {
            predicates[index] = ItemPredicate.Builder.item().of(items[index]).build();
        }

        return new InventoryChangeTrigger.TriggerInstance(
                EntityPredicate.wrap(Condition.dragonSpecies(dragonSpecies).build()),
                MinMaxBounds.Ints.ANY,
                MinMaxBounds.Ints.ANY,
                MinMaxBounds.Ints.ANY,
                predicates
        );
    }

    public PlayerTrigger.TriggerInstance location(final ContextAwarePredicate predicate) {
        return new PlayerTrigger.TriggerInstance(CriteriaTriggers.LOCATION.getId(), predicate);
    }

    public PlayerTrigger.TriggerInstance location(final EntityPredicate.Builder builder) {
        return location(EntityPredicate.wrap(builder.build()));
    }

    public ConsumeItemTrigger.TriggerInstance consumeItem(final Item... items) {
        return ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(items).build());
    }

    public UsingItemTrigger.TriggerInstance usingItem(final Item item) {
        return new UsingItemTrigger.TriggerInstance(
                ContextAwarePredicate.ANY,
                ItemPredicate.Builder.item().of(item).build()
        );
    }

    public ItemUsedOnLocationTrigger.TriggerInstance placeBlockAsDragon(final EntityPredicate.Builder builder, final Block block) {
        ContextAwarePredicate blockPredicate = ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).build());
        return new ItemUsedOnLocationTrigger.TriggerInstance(
                CriteriaTriggers.PLACED_BLOCK.getId(),
                EntityPredicate.wrap(builder.build()),
                blockPredicate
        );
    }

    public ItemUsedOnLocationTrigger.TriggerInstance placeBlock(final Block block) {
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block);
    }

    public ItemUsedOnLocationTrigger.TriggerInstance placeBlock(final TagKey<Item> blocks) {
        return ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(MatchTool.toolMatches(ItemPredicate.Builder.item().of(blocks)));
    }

    public ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnBlock(final Block block, final ItemLike... items) {
        return ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).build()),
                ItemPredicate.Builder.item().of(items)
        );
    }

    public ItemUsedOnLocationTrigger.TriggerInstance itemUsedOnBlock(final Block block, final TagKey<Item> items) {
        return ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).build()),
                ItemPredicate.Builder.item().of(items)
        );
    }

    public EffectsChangedTrigger.TriggerInstance effectWithMinDuration(final MobEffect effect, int minDuration) {
        return EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.effects().and(
                effect,
                new MobEffectsPredicate.MobEffectInstancePredicate(
                        MinMaxBounds.Ints.ANY,
                        MinMaxBounds.Ints.atLeast(minDuration),
                        null,
                        null
                )
        ));
    }

    public PlayerInteractTrigger.TriggerInstance itemInteract(final EntityType<?> type, final ItemLike... items) {
        return new PlayerInteractTrigger.TriggerInstance(
                ContextAwarePredicate.ANY,
                ItemPredicate.Builder.item().of(items).build(),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(type).build())
        );
    }

    public PlayerInteractTrigger.TriggerInstance noItemInteract(final EntityType<?> type) {
        return new PlayerInteractTrigger.TriggerInstance(
                ContextAwarePredicate.ANY,
                ItemPredicate.ANY,
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(type).build())
        );
    }

    // --- Convert Potato --- //

    public ConvertItemFromAbility.TriggerInstance convertPotato(final EntityPredicate.Builder builder) {
        return new ConvertItemFromAbility.TriggerInstance(
                Optional.of(EntityPredicate.wrap(builder.build())),
                Items.POTATO.builtInRegistryHolder(),
                Items.POISONOUS_POTATO.builtInRegistryHolder()
        );
    }

    // --- Mine Block Under Lava --- //

    @SuppressWarnings("deprecation") // ignore
    public MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance mineBlockInLava(final Block... blocks) {
        return new MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance(
                caveDragonInLava(),
                Optional.of(HolderSet.direct(Block::builtInRegistryHolder, blocks))
        );
    }

    public MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance mineBlockInLava(final TagKey<Block> blocks) {
        return new MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance(
                caveDragonInLava(),
                Optional.of(BuiltInRegistries.BLOCK.getOrCreateTag(blocks))
        );
    }

    // --- Use Dragon Soul --- //

    public UseDragonSoulTrigger.UseDragonSoulInstance useDragonSoul() {
        return new UseDragonSoulTrigger.UseDragonSoulInstance(Optional.empty());
    }

    // --- Use Star Heart --- //

    public StopNaturalGrowthTrigger.Instance stopNaturalGrowth() {
        return new StopNaturalGrowthTrigger.Instance(Optional.empty());
    }


    // --- Sleep On Treasure --- //

    public SleepOnTreasureTrigger.SleepOnTreasureInstance sleepOnTreasure(int nearbyTreasureAmount) {
        return new SleepOnTreasureTrigger.SleepOnTreasureInstance(
                Optional.empty(),
                Optional.of(nearbyTreasureAmount)
        );
    }

    // --- Upgrade Ability --- //

    public UpgradeAbilityTrigger.UpgradeAbilityInstance upgradeAbilityMax(final Holder<DragonAbility> ability) {
        return upgradeAbility(ability.unwrapKey().orElseThrow(), ability.value().getMaxLevel());
    }

    public UpgradeAbilityTrigger.UpgradeAbilityInstance upgradeAbility(final ResourceKey<DragonAbility> ability, int level) {
        return new UpgradeAbilityTrigger.UpgradeAbilityInstance(
                Optional.empty(),
                Optional.of(ability),
                Optional.of(level)
        );
    }

    // --- Be Dragon --- //

    public BeDragonTrigger.Instance beDragon() {
        return beDragon(DragonPredicate.Builder.dragon().build());
    }

    public BeDragonTrigger.Instance beDragon(final Holder<DragonSpecies> type) {
        return beDragon(DragonPredicate.Builder.dragon().species(type).build());
    }

    public BeDragonTrigger.Instance beDragon(double growth) {
        return beDragon(DragonPredicate.Builder.dragon()
                .stage(DragonStagePredicate.Builder.start().growthAtLeast(growth).build())
                .build());
    }

    public BeDragonTrigger.Instance beDragon(final ResourceKey<DragonStage> dragonStage) {
        return beDragon(DragonPredicate.Builder.dragon().stage(holder(dragonStage)).build());
    }

    public BeDragonTrigger.Instance beDragon(final Holder<DragonSpecies> species, final Holder<DragonStage> dragonStage, double progress) {
        return beDragon(DragonPredicate.Builder.dragon()
                .species(species)
                .stage(dragonStage, MinMaxBounds.Doubles.atLeast(progress))
                .build());
    }

    public BeDragonTrigger.Instance beDragon(final DragonPredicate predicate) {
        return new BeDragonTrigger.Instance(Optional.empty(), Optional.of(predicate));
    }

    public BeDragonTrigger.Instance beDragon(final EntityPredicate predicate) {
        return new BeDragonTrigger.Instance(Optional.of(EntityPredicate.wrap(predicate)), Optional.empty());
    }

    // -- Steal From Villagers -- //

    public StealFromVillagerTrigger.Instance stealFromVillager() {
        return new StealFromVillagerTrigger.Instance(Optional.empty());
    }
}
