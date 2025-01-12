package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DietEntry;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthIcon;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscDragonTextures;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.AncientDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalties;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Optional;

public class BuiltInDragonSpecies {
    @Translation(type = Translation.Type.DRAGON_SPECIES_DESCRIPTION, comments = {
            "§c■ Cave dragon.§r",
            "§2■ Features:§f§r fire resistance, pickaxe claws, fire magic, faster movement on stone and magma blocks.",
            "§4■ Weakness:§r water.",
            "§6■ Diet:§r"
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES_DESCRIPTION_NO_DIET, comments = {
            "§c■ Cave dragon.§r",
            "§2■ Features:§f§r fire resistance, pickaxe claws, fire magic, faster movement on stone and magma blocks.",
            "§4■ Weakness:§r water.",
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES, comments = "Cave Dragon")
    public static final ResourceKey<DragonSpecies> CAVE = key("cave");

    @Translation(type = Translation.Type.DRAGON_SPECIES_DESCRIPTION, comments = {
            "§a■ Forest dragon.§r",
            "§2■ Features:§f§r soft fall, axe claws, poison magic, faster movement on wooden and grass blocks.",
            "§4■ Weakness:§r dark caves.",
            "§6■ Diet:§r"
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES_DESCRIPTION_NO_DIET, comments = {
            "§a■ Forest dragon.§r",
            "§2■ Features:§f§r soft fall, axe claws, poison magic, faster movement on wooden and grass blocks.",
            "§4■ Weakness:§r dark caves."
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES, comments = "Forest Dragon")
    public static final ResourceKey<DragonSpecies> FOREST = key("forest");

    @Translation(type = Translation.Type.DRAGON_SPECIES_DESCRIPTION, comments = {
            "§3■ Sea dragon.§r",
            "§2■ Features:§f§r underwater breathing, shovel claws, electric magic, faster movement on ice and beach blocks.",
            "§4■ Weakness:§r dehydration.",
            "§6■ Diet:§r"
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES_DESCRIPTION_NO_DIET, comments = {
            "§3■ Sea dragon.§r",
            "§2■ Features:§f§r underwater breathing, shovel claws, electric magic, faster movement on ice and beach blocks.",
            "§4■ Weakness:§r dehydration."
    })
    @Translation(type = Translation.Type.DRAGON_SPECIES, comments = "Sea Dragon")
    public static final ResourceKey<DragonSpecies> SEA = key("sea");

    public static void registerTypes(final BootstrapContext<DragonSpecies> context) {
        context.register(CAVE, new DragonSpecies(
                Optional.empty(),
                Optional.empty(),
                HolderSet.empty(),
                HolderSet.direct(
                        // Active
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.NETHER_BREATH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.FIRE_BALL),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.STURDY_SKIN),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.LAVA_VISION),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.FRIENDLY_FIRE),
                        // Passive
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_ATHLETICS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.BURN),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_MAGIC),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CONTRAST_SHOWER),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.FIRE_IMMUNITY),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_WINGS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_SPIN),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_CLAWS_AND_TEETH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.LAVA_SWIMMING)
                ),
                HolderSet.direct(
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.SNOW_AND_RAIN_WEAKNESS),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.WATER_WEAKNESS),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.ITEM_BLACKLIST),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.WATER_POTION_WEAKNESS),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.SNOWBALL_WEAKNESS),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.WATER_SPLASH_POTION_WEAKNESS)
                ),
                List.of(),
                List.of(
                        DietEntry.from(ItemTags.COALS, new FoodProperties(1, 1, false, 0.8f, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.CHARGED_COAL.value(), new FoodProperties(6, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.CHARRED_MEAT.value(), new FoodProperties(8, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.CAVE_DRAGON_TREAT.value(), new FoodProperties(4, 8, true, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.CHARRED_SEAFOOD.value(), new FoodProperties(7, 11, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.CHARRED_VEGETABLE.value(), new FoodProperties(8, 9, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.CHARRED_MUSHROOM.value(), new FoodProperties(9, 9, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.CHARGED_SOUP.value(), new FoodProperties(15, 15, true, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.HOT_DRAGON_ROD.value(), new FoodProperties(4, 15, true, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.EXPLOSIVE_COPPER.value(), new FoodProperties(6, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.DOUBLE_QUARTZ.value(), new FoodProperties(8, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.QUARTZ_EXPLOSIVE_COPPER.value(), new FoodProperties(12, 18, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:blazefish", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:cooked_magmacubefish_slice", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:blazefish_slice", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:magmacubefish", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:fortress_grouper", new FoodProperties(3, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("desolation:cinder_fruit", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("desolation:powered_cinder_fruit", new FoodProperties(8, 12, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("desolation:activatedcharcoal", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("desolation:infused_powder", new FoodProperties(10, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("desolation:primed_ash", new FoodProperties(7, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("undergarden:ditchbulb", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("xreliquary:molten_core", new FoodProperties(1, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("mekanism:dust_coal", new FoodProperties(1, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("mekanism:dust_charcoal", new FoodProperties(1, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("thermal:coal_coke", new FoodProperties(1, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("thermal:basalz_rod", new FoodProperties(2, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("thermal:basalz_powder", new FoodProperties(1, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("create:blaze_cake", new FoodProperties(10, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("create:creative_blaze_cake", new FoodProperties(50, 50, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("nethersdelight:nether_skewer", new FoodProperties(6, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of()))
                ),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/custom/food_icons/cave_food_icons.png"),
                        new MiscDragonTextures.ManaSprites(
                                DragonSurvival.res("textures/gui/custom/mana_icons/cave/full.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/cave/reserved.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/cave/recovery.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/cave/empty.png")
                        ),
                        DragonSurvival.res("textures/gui/custom/altar/cave/altar_icon.png"),
                        DragonSurvival.res("textures/gui/custom/casting_bars/cave_cast_bar.png"),
                        DragonSurvival.res("textures/gui/custom/help_button/cave_help_button.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/cave/newborn_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/cave/newborn_stage_main.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/cave/young_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/cave/young_stage_main.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/cave/adult_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/cave/adult_stage_main.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/cave/ancient_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/cave/ancient_stage_main.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/cave/left_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/cave/left_arrow_main.png")
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/cave/right_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/cave/right_arrow_main.png")
                        ),
                        new MiscDragonTextures.FillIcon(
                                DragonSurvival.res("textures/gui/custom/stage/cave/point_main.png"),
                                DragonSurvival.res("textures/gui/custom/stage/cave/point_hover.png")
                        ),
                        new MiscDragonTextures.FoodTooltip(MiscDragonTextures.FoodTooltip.DEFAULT_FOOD_TOOLTIP_FONT, "\\uEA02", "\\uEA05", Optional.empty()),
                        TextColor.fromRgb(0xE84141),
                        TextColor.fromRgb(0x730B0B)
                )
        ));

        context.register(SEA, new DragonSpecies(
                Optional.empty(),
                Optional.empty(),
                HolderSet.empty(),
                HolderSet.direct(
                        // Active
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.BALL_LIGHTNING),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.STORM_BREATH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_EYES),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SOUL_REVELATION),
                        // Passive
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_ATHLETICS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_MAGIC),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SPECTRAL_IMPACT),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.HYDRATION),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_CLAWS_AND_TEETH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.SEA_WINGS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.ELECTRIC_IMMUNITY),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(SeaDragonAbilities.AMPHIBIOUS)
                ),
                HolderSet.direct(
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.THIN_SKIN),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.ITEM_BLACKLIST)
                ),
                List.of(
                        // We need to give the sea dragon some starting resistance time so they don't immediately dehydrate
                        new Modifier(
                                DSAttributes.PENALTY_RESISTANCE_TIME,
                                LevelBasedValue.constant(Functions.secondsToTicks(60)),
                                AttributeModifier.Operation.ADD_VALUE,
                                Optional.empty()
                        )
                ),
                List.of(
                        DietEntry.from(ItemTags.FISHES, new FoodProperties(6, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.KELP, new FoodProperties(1, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.PUFFERFISH, new FoodProperties(8, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.SEA_DRAGON_TREAT.value(), new FoodProperties(4, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.SEASONED_FISH.value(), new FoodProperties(12, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.GOLDEN_CORAL_PUFFERFISH.value(), new FoodProperties(12, 14, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.FROZEN_RAW_FISH.value(), new FoodProperties(2, 1, true, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.GOLDEN_TURTLE_EGG.value(), new FoodProperties(15, 12, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("additionaldragons:slippery_sushi", new FoodProperties(10, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_candlefish", new FoodProperties(9, 9, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_crimson_skipper", new FoodProperties(8, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_fingerfish", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_pearl_stripefish", new FoodProperties(5, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_limefish", new FoodProperties(5, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_sailback", new FoodProperties(6, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:soulsucker", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:obsidianfish", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:lava_pufferfish", new FoodProperties(8, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:searing_cod", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:glowdine", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:warped_kelp", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:lava_pufferfish_slice", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:glowdine_slice", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:soulsucker_slice", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:obsidianfish_slice", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:searing_cod_slice", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("crittersandcompanions:clam", new FoodProperties(10, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_golden_gullfish", new FoodProperties(10, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_turquoise_stripefish", new FoodProperties(7, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_violet_skipper", new FoodProperties(7, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_rocketfish", new FoodProperties(4, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_crimson_stripefish", new FoodProperties(8, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_sapphire_strider", new FoodProperties(9, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_dark_hatchetfish", new FoodProperties(9, 9, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_ironback", new FoodProperties(10, 9, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_rainbowfish", new FoodProperties(11, 11, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_razorfish", new FoodProperties(12, 14, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("alexsmobs:lobster_tail", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("alexsmobs:blobfish", new FoodProperties(8, 9, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oddwatermobs:raw_ghost_shark", new FoodProperties(8, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oddwatermobs:raw_isopod", new FoodProperties(4, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oddwatermobs:raw_mudskipper", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oddwatermobs:raw_coelacanth", new FoodProperties(9, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oddwatermobs:raw_anglerfish", new FoodProperties(6, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oddwatermobs:deep_sea_fish", new FoodProperties(4, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oddwatermobs:crab_leg", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("simplefarming:raw_calamari", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unnamedanimalmod:elephantnose_fish", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unnamedanimalmod:flashlight_fish", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("born_in_chaos_v1:sea_terror_eye", new FoodProperties(10, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("born_in_chaos_v1:rotten_fish", new FoodProperties(4, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unnamedanimalmod:rocket_killifish", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unnamedanimalmod:leafy_seadragon", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unnamedanimalmod:elephantnose_fish", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("betteranimalsplus:eel_meat_raw", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("betteranimalsplus:calamari_raw", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("betteranimalsplus:crab_meat_raw", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:fish_fillet_raw", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:goldfish", new FoodProperties(8, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:algae", new FoodProperties(3, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("betterendforge:end_fish_raw", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("betterendforge:hydralux_petal", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("betterendforge:charnia_green", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("shroomed:raw_shroomfin", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("undergarden:raw_gwibling", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("bettas:betta_fish", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("quark:crab_leg", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("pamhc2foodextended:rawtofishitem", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:banded_redback_shrimp", new FoodProperties(6, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:night_light_squid", new FoodProperties(6, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:night_light_squid_tentacle", new FoodProperties(6, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:emerald_spindly_gem_crab", new FoodProperties(7, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:amber_spindly_gem_crab", new FoodProperties(7, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:rubby_spindly_gem_crab", new FoodProperties(7, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:sapphire_spindly_gem_crab", new FoodProperties(7, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:pearl_spindly_gem_crab", new FoodProperties(7, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:papa_wee", new FoodProperties(6, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:bugmeat", new FoodProperties(4, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:raw_golden_river_ray_wing", new FoodProperties(6, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:red_bull_crab_claw", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:white_bull_crab_claw", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("fins:wherble_fin", new FoodProperties(1, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("forbidden_arcanus:tentacle", new FoodProperties(5, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("pneumaticcraft:raw_salmon_tempura", new FoodProperties(6, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("rats:ratfish", new FoodProperties(4, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("upgrade_aquatic:purple_pickerelweed", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("upgrade_aquatic:blue_pickerelweed", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("upgrade_aquatic:polar_kelp", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("upgrade_aquatic:tongue_kelp", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("upgrade_aquatic:thorny_kelp", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("upgrade_aquatic:ochre_kelp", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("upgrade_aquatic:lionfish", new FoodProperties(8, 9, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:sushi", new FoodProperties(6, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("freshwarriors:fresh_soup", new FoodProperties(15, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("freshwarriors:beluga_caviar", new FoodProperties(10, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("freshwarriors:piranha", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("freshwarriors:tilapia", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("freshwarriors:stuffed_piranha", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("freshwarriors:tigerfish", new FoodProperties(5, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("freshwarriors:toe_biter_leg", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("mysticalworld:raw_squid", new FoodProperties(6, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:fresh_soup", new FoodProperties(10, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:beluga_caviar", new FoodProperties(10, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:raw_piranha", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:raw_tilapia", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:stuffed_piranha", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:tigerfish", new FoodProperties(5, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:toe_biter_leg", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:raw_angelfish", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:raw_football_fish", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:raw_foxface_fish", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:raw_royal_gramma", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:raw_starfish", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:spider_crab_leg", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquafina:raw_stingray_slice", new FoodProperties(4, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("prehistoricfauna:raw_ceratodus", new FoodProperties(5, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("prehistoricfauna:raw_cyclurus", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("prehistoricfauna:raw_potamoceratodus", new FoodProperties(5, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("prehistoricfauna:raw_myledaphus", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("prehistoricfauna:raw_gar", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("prehistoricfauna:raw_oyster", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("prehistoric_delight:prehistoric_fillet", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("seadwellers:rainbow_trout", new FoodProperties(10, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("crittersandcompanions:koi_fish", new FoodProperties(5, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquamirae:elodea", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("croptopia:clam", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("croptopia:calamari", new FoodProperties(2, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("croptopia:anchovy", new FoodProperties(3, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("croptopia:crab", new FoodProperties(6, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("croptopia:glowing_calamari", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("croptopia:oyster", new FoodProperties(2, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("croptopia:roe", new FoodProperties(1, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("croptopia:shrimp", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("croptopia:tuna", new FoodProperties(6, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquamirae:spinefish", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("alexsmobs:flying_fish", new FoodProperties(6, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:eyeball", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:eyeball_fish", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oceansdelight:guardian", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oceansdelight:guardian_tail", new FoodProperties(1, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oceansdelight:cut_tentacles", new FoodProperties(3, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oceansdelight:tentacles", new FoodProperties(3, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oceansdelight:tentacle_on_a_stick", new FoodProperties(3, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oceansdelight:fugu_slice", new FoodProperties(5, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oceansdelight:elder_guardian_slice", new FoodProperties(8, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("oceansdelight:elder_guardian_slab", new FoodProperties(15, 15, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("upgrade_aquatic:elder_eye", new FoodProperties(15, 15, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unusualprehistory:golden_scau", new FoodProperties(15, 15, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unusualprehistory:raw_scau", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unusualprehistory:raw_stetha", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unusualprehistory:stetha_eggs", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unusualprehistory:beelze_eggs", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unusualprehistory:scau_eggs", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unusualprehistory:ammon_eggs", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unusualprehistory:dunk_eggs", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:crimson_seagrass", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:crimson_kelp", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:warped_seagrass", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("undergarden:glitterkelp", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("enlightened_end:raw_stalker", new FoodProperties(10, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of()))
                ),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/custom/food_icons/sea_food_icons.png"),
                        new MiscDragonTextures.ManaSprites(
                                DragonSurvival.res("textures/gui/custom/mana_icons/sea/full.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/sea/reserved.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/sea/recovery.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/sea/empty.png")
                        ),
                        DragonSurvival.res("textures/gui/custom/altar/sea/altar_icon.png"),
                        DragonSurvival.res("textures/gui/custom/casting_bars/sea_cast_bar.png"),
                        DragonSurvival.res("textures/gui/custom/help_button/sea_help_button.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/sea/newborn_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/sea/newborn_stage_main.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/sea/young_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/sea/young_stage_main.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/sea/adult_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/sea/adult_stage_main.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/sea/ancient_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/sea/ancient_stage_main.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/sea/left_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/sea/left_arrow_main.png")
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/sea/right_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/sea/right_arrow_main.png")
                        ),
                        new MiscDragonTextures.FillIcon(
                                DragonSurvival.res("textures/gui/custom/stage/sea/point_main.png"),
                                DragonSurvival.res("textures/gui/custom/stage/sea/point_hover.png")
                        ),
                        new MiscDragonTextures.FoodTooltip(MiscDragonTextures.FoodTooltip.DEFAULT_FOOD_TOOLTIP_FONT, "\\uEA03", "\\uEA06", Optional.empty()),
                        TextColor.fromRgb(0x2DA5E0),
                        TextColor.fromRgb(0x1D7099)
                )
        ));

        context.register(FOREST, new DragonSpecies(
                Optional.empty(),
                Optional.empty(),
                HolderSet.empty(),
                HolderSet.direct(
                        // Active
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.SPIKE),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FERTILIZER_BREATH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.HUNTER),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.INSPIRATION),
                        // Passive
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_IMMUNITY),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_MAGIC),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_CLAWS_AND_TEETH),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_WINGS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.FOREST_ATHLETICS),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.CLIFFHANGER),
                        context.lookup(DragonAbility.REGISTRY).getOrThrow(ForestDragonAbilities.LIGHT_IN_DARKNESS)
                ),
                HolderSet.direct(
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.FEAR_OF_DARKNESS),
                        context.lookup(DragonPenalty.REGISTRY).getOrThrow(DragonPenalties.ITEM_BLACKLIST)
                ),
                List.of(),
                List.of(
                        DietEntry.from(Tags.Items.FOODS_RAW_MEAT, new FoodProperties(6, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Tags.Items.FOODS_BERRY, new FoodProperties(2, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Tags.Items.MUSHROOMS, new FoodProperties(1, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.ROTTEN_FLESH, new FoodProperties(2, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.SPIDER_EYE, new FoodProperties(6, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.RABBIT, new FoodProperties(7, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.POISONOUS_POTATO, new FoodProperties(7, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.CHORUS_FRUIT, new FoodProperties(9, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.BROWN_MUSHROOM, new FoodProperties(2, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.RED_MUSHROOM, new FoodProperties(2, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.HONEY_BOTTLE, new FoodProperties(6, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.of(new ItemStack(Items.GLASS_BOTTLE, 1)), List.of())),
                        DietEntry.from(Items.WARPED_FUNGUS, new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(Items.CRIMSON_FUNGUS, new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.FOREST_DRAGON_TREAT.value(), new FoodProperties(4, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.MEAT_CHORUS_MIX.value(), new FoodProperties(12, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.MEAT_WILD_BERRIES.value(), new FoodProperties(12, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.SMELLY_MEAT_PORRIDGE.value(), new FoodProperties(6, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.DIAMOND_CHORUS.value(), new FoodProperties(15, 12, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.LUMINOUS_OINTMENT.value(), new FoodProperties(5, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from(DSItems.SWEET_SOUR_RABBIT.value(), new FoodProperties(10, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("additionaldragons:cursed_marrow", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:turtle_soup", new FoodProperties(8, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:wither_bonefish", new FoodProperties(4, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("netherdepthsupgrade:bonefish", new FoodProperties(4, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("phantasm:chorus_fruit_salad", new FoodProperties(10, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:fiery_chops", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_chimera_chop", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_furlion_chop", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_halycon_beef", new FoodProperties(7, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:raw_charger_shank", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aoa3:trilliad_leaves", new FoodProperties(8, 11, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("pamhc2foodextended:rawtofabbititem", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("pamhc2foodextended:rawtofickenitem", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("quark:golden_frog_leg", new FoodProperties(12, 14, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("pamhc2foodextended:rawtofuttonitem", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("alexsmobs:kangaroo_meat", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("alexsmobs:moose_ribs", new FoodProperties(6, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("simplefarming:raw_horse_meat", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("simplefarming:raw_bacon", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("simplefarming:raw_chicken_wings", new FoodProperties(2, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("simplefarming:raw_sausage", new FoodProperties(3, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("xenoclustwo:raw_tortice", new FoodProperties(7, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unnamedanimalmod:musk_ox_shank", new FoodProperties(7, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unnamedanimalmod:frog_legs", new FoodProperties(5, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("unnamedanimalmod:mangrove_fruit", new FoodProperties(4, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("betteranimalsplus:venisonraw", new FoodProperties(7, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("born_in_chaos_v1:corpse_maggot", new FoodProperties(1, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("born_in_chaos_v1:monster_flesh", new FoodProperties(1, 1, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("betteranimalsplus:pheasantraw", new FoodProperties(7, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("betteranimalsplus:turkey_leg_raw", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("infernalexp:raw_hogchop", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("infernalexp:cured_jerky", new FoodProperties(10, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("rats:raw_rat", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:frog", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:frog_legs_raw", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:box_turtle", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:arrau_turtle", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("aquaculture:starshell_turtle", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("undergarden:raw_gloomper_leg", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("undergarden:raw_dweller_meat", new FoodProperties(6, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("farmersdelight:chicken_cuts", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("farmersdelight:bacon", new FoodProperties(3, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("farmersdelight:ham", new FoodProperties(9, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("farmersdelight:minced_beef", new FoodProperties(5, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("farmersdelight:mutton_chops", new FoodProperties(5, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("abnormals_delight:duck_fillet", new FoodProperties(2, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("abnormals_delight:venison_shanks", new FoodProperties(7, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("autumnity:foul_berries", new FoodProperties(2, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("autumnity:turkey", new FoodProperties(7, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("autumnity:turkey_piece", new FoodProperties(2, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("autumnity:foul_soup", new FoodProperties(12, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("endergetic:bolloom_fruit", new FoodProperties(3, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("quark:frog_leg", new FoodProperties(4, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("nethers_delight:hoglin_loin", new FoodProperties(8, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("nethers_delight:raw_stuffed_hoglin", new FoodProperties(18, 10, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("xreliquary:zombie_heart", new FoodProperties(4, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("xreliquary:bat_wing", new FoodProperties(2, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("eidolon:zombie_heart", new FoodProperties(7, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("forbidden_arcanus:bat_wing", new FoodProperties(5, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("twilightforest:raw_venison", new FoodProperties(7, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("twilightforest:raw_meef", new FoodProperties(9, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("twilightforest:hydra_chop", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("cyclic:chorus_flight", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("cyclic:chorus_spectral", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("cyclic:toxic_carrot", new FoodProperties(15, 15, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("artifacts:everlasting_beef", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("byg:soul_shroom", new FoodProperties(9, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("byg:death_cap", new FoodProperties(9, 8, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("minecolonies:chorus_bread", new FoodProperties(0, 0, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("wyrmroost:raw_lowtier_meat", new FoodProperties(3, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("wyrmroost:raw_common_meat", new FoodProperties(5, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("wyrmroost:raw_apex_meat", new FoodProperties(8, 6, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("wyrmroost:raw_behemoth_meat", new FoodProperties(11, 12, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("wyrmroost:desert_wyrm", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("eanimod:rawchicken_darkbig", new FoodProperties(9, 5, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("eanimod:rawchicken_dark", new FoodProperties(5, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("eanimod:rawchicken_darksmall", new FoodProperties(3, 2, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("eanimod:rawchicken_pale", new FoodProperties(5, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("eanimod:rawchicken_palesmall", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("eanimod:rawrabbit_small", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("environmental:duck", new FoodProperties(4, 3, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("environmental:venison", new FoodProperties(7, 7, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("cnb:lizard_item_jungle", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("cnb:lizard_item_mushroom", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("cnb:lizard_item_jungle_2", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of())),
                        DietEntry.from("cnb:lizard_item_desert_2", new FoodProperties(4, 4, false, DietEntry.DEFAULT_EAT_SECONDS, Optional.empty(), List.of()))
                ),
                new MiscDragonTextures(
                        DragonSurvival.res("textures/gui/custom/food_icons/forest_food_icons.png"),
                        new MiscDragonTextures.ManaSprites(
                                DragonSurvival.res("textures/gui/custom/mana_icons/forest/full.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/forest/reserved.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/forest/recovery.png"),
                                DragonSurvival.res("textures/gui/custom/mana_icons/forest/empty.png")
                        ),
                        DragonSurvival.res("textures/gui/custom/altar/forest/altar_icon.png"),
                        DragonSurvival.res("textures/gui/custom/casting_bars/forest_cast_bar.png"),
                        DragonSurvival.res("textures/gui/custom/help_button/forest_help_button.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/forest/newborn_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/forest/newborn_stage_main.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/forest/young_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/forest/young_stage_main.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/forest/adult_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/forest/adult_stage_main.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("textures/gui/custom/stage/forest/ancient_stage_hover.png"),
                                        DragonSurvival.res("textures/gui/custom/stage/forest/ancient_stage_main.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/forest/left_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/forest/left_arrow_main.png")
                        ),
                        new MiscDragonTextures.HoverIcon(
                                DragonSurvival.res("textures/gui/custom/stage/forest/right_arrow_hover.png"),
                                DragonSurvival.res("textures/gui/custom/stage/forest/right_arrow_main.png")
                        ),
                        new MiscDragonTextures.FillIcon(
                                DragonSurvival.res("textures/gui/custom/stage/forest/point_main.png"),
                                DragonSurvival.res("textures/gui/custom/stage/forest/point_hover.png")
                        ),
                        new MiscDragonTextures.FoodTooltip(MiscDragonTextures.FoodTooltip.DEFAULT_FOOD_TOOLTIP_FONT, "\\uEA01", "\\uEA04", Optional.empty()),
                        TextColor.fromRgb(0x41CC48),
                        TextColor.fromRgb(0x117816)
                )
        ));
    }

    public static ResourceKey<DragonSpecies> key(final ResourceLocation location) {
        return ResourceKey.create(DragonSpecies.REGISTRY, location);
    }

    private static ResourceKey<DragonSpecies> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
