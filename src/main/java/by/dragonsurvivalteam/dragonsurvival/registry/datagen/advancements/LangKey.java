package by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;

/** Translation keys only used by advancements */
public class LangKey {
    // --- Root --- //

    @Translation(comments = "Dragon Survival")
    protected static final String ROOT = Translation.Type.ADVANCEMENT.wrap("root");

    // --- Parent: Root --- //

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Elder Dragon Lore")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Collect elder dragon dust by mining ore.")
    protected static final String COLLECT_DUST = "collect_dust";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Be Yourself")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Unless you can be a dragon.")
    protected static final String BE_DRAGON = "be_dragon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Dragon Starter Pack")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Place a Dragon Altar to change the species or appearance of a dragon.")
    protected static final String PLACE_ALTAR = "place_altar";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Here be Villagers")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Find a villager and decide their fate.")
    protected static final String PATH_CHOICE = "path_choice";

    // --- Parent: Collect Dust --- //

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Growth Process")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Become a young dragon using time or heart parts.")
    protected static final String BE_YOUNG_DRAGON = "be_young_dragon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Older And Stronger")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Grow your dragon into an adult! Time to look for a player-rider.")
    protected static final String BE_ADULT_DRAGON = "be_adult_dragon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Repossession")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Get the Elder Dragon Heart by killing a monster with more than 50 health.")
    protected static final String COLLECT_HEART_FROM_MONSTER = "collect_heart_from_monster";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Lava Core, Netherite Hide")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Gain level 60 growth as a cave dragon.")
    protected static final String CAVE_BE_OLD_DRAGON = "cave/be_old_dragon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Resplendent Inferno")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Achieve total mastery of your cave dragon passive skills.")
    protected static final String CAVE_MASTER_ALL_PASSIVES = "cave/master_all_passives";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "True Ancient Guardian")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Gain level 60 growth as a sea dragon.")
    protected static final String SEA_BE_OLD_DRAGON = "sea/be_old_dragon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Hadopelagic Pressure")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Achieve total mastery of your sea dragon passive skills.")
    protected static final String SEA_MASTER_ALL_PASSIVES = "sea/master_all_passives";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Antediluvian Overgrowth")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Gain level 60 growth as a forest dragon.")
    protected static final String FOREST_BE_OLD_DRAGON = "forest/be_old_dragon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Photosynthesis")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Achieve total mastery of your forest dragon passive skills.")
    protected static final String FOREST_MASTER_ALL_PASSIVES = "forest/master_all_passives";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Loot: The Gathering")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Sleep on a pile of 10 or more treasure blocks.")
    protected static final String SLEEP_ON_TREASURE = "sleep_on_treasure";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Finally, a Worthy Bed")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Collect yourself a truly large stash of treasure. 100 blocks will be enough.")
    protected static final String SLEEP_ON_HOARD = "sleep_on_hoard";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Dragon's Greed")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Build yourself a hoard of 240 treasure blocks and experience regeneration while you sleep!")
    protected static final String SLEEP_ON_MASSIVE_HOARD = "sleep_on_massive_hoard";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Echoes of The Elder")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Find the burial place of the Elder Dragon's children.")
    protected static final String FIND_BONES = "find_bones";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Slumbering Dragons")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Find any dragon statue in The Overworld.")
    protected static final String FIND_OVERWORLD_STRUCTURES = "find_overworld_structures";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Peaceful Flames")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Find any dragon statue in The Nether.")
    protected static final String FIND_NETHER_STRUCTURES = "find_nether_structures";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Don't jump into The Void!")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Venture forth into The End as a dragon")
    protected static final String FIND_END_PLATFORM = "find_end_platform";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Draconic Secrets")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Find a special structure in The End to get the Dragon Soul and Spin ability.")
    protected static final String FIND_END_STRUCTURES = "find_end_structures";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Knowledge is Power")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Place any Dragon Beacon on top of the Memory Block.")
    protected static final String USE_MEMORY_BLOCK = "use_memory_block";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Unity of Species")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Upgrade the Empty Dragon Beacon with special item.")
    protected static final String CHANGE_BEACON = "change_beacon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Zmei Gorynich")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Buy all 3 effects from all Dragon Beacon's.")
    protected static final String GET_ALL_BEACONS = "get_all_beacons";

    // --- Parent: Be Dragon --- //

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Forever Young")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Stop your natural growth by using a specific growth item.")
    protected static final String STOP_NATURAL_GROWTH = "stop_natural_growth";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Soul of a Dragon")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Use a Dragon Soul to store all your stats. You can pass it on to another player or keep it for yourself!")
    protected static final String USE_DRAGON_SOUL = "use_dragon_soul";

    // --- Cave --- //

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Ab Igne Ignem")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Turn into a cave dragon.")
    protected static final String CAVE_BE_DRAGON = "cave/be_dragon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Fuel for the Fire")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Eat all unique cave dragon foods from Dragon Survival. You can check what foods you can eat in the dragon altar. Don't forget to try the hot dragon rod!")
    protected static final String CAVE_ROCK_EATER = "cave/rock_eater";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Waterproof")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Get the water resistance effect from food or beacon and swim in the water.")
    protected static final String CAVE_WATER_SAFETY = "cave/water_safety";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Floor is lava")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Swim in a deep pool of lava as cave dragon!")
    protected static final String CAVE_SWIM_IN_LAVA = "cave/swim_in_lava";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Protected Excavations")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Find the diamonds at the bottom of the lava lake and dig them out.")
    protected static final String CAVE_DIAMONDS_IN_LAVA = "cave/diamonds_in_lava";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Smells like... Home")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Submerge yourself in the fiery depths of the nether.")
    protected static final String CAVE_GO_HOME = "cave/go_home";

    // --- Sea --- //

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Heterochromia Iridum")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Become a sea dragon by any means necessary.")
    protected static final String SEA_BE_DRAGON = "sea/be_dragon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Ocean Reclaimer")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Discover and claim the loot of unfortunate shipwrecks.")
    protected static final String SEA_LOOT_SHIPWRECK = "sea/loot_shipwreck";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Dancing in the Rain")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Feel hydration during rain and thunderstorm.")
    protected static final String SEA_RAIN_DANCING = "sea/rain_dancing";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "No Rice in Sushi")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Eat all unique sea dragon foods from Dragon Survival. You can check what foods you can eat in the dragon altar.")
    protected static final String SEA_FISH_EATER = "sea/fish_eater";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Drought")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Place some snow to stay hydrated in the Nether.")
    protected static final String SEA_PLACE_SNOW_IN_NETHER = "sea/place_snow_in_nether";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Heatproof")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Use a Sea Beacon's magic to enter The Nether without drying out.")
    protected static final String SEA_PEACE_IN_NETHER = "sea/peace_in_nether";

    // --- Forest --- //

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Ubi Mel, Ibi Apes")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Become a forest dragon by any means necessary.")
    protected static final String FOREST_BE_DRAGON = "forest/be_dragon";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Green Relative")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Jump into sweet berry bushes and test the strength of your skin!")
    protected static final String FOREST_STAND_ON_SWEET_BERRIES = "forest/stand_on_sweet_berries";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Bioluminescence")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Venture into the darkness, unafraid of what may happen.")
    protected static final String FOREST_PREVENT_DARKNESS_PENALTY = "forest/prevent_darkness_penalty";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Excess Fertilizer")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Use the forest dragon breath to get more poisoned potatoes from the crop fields.")
    protected static final String FOREST_POISONOUS_POTATO = "forest/poisonous_potato";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Meat Eater")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Eat all unique forest dragon foods from Dragon Survival. You can check what foods you can eat in the dragon altar.")
    protected static final String FOREST_MEAT_EATER = "forest/meat_eater";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Favorite Treat Everywhere")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Transplant a tasty purple flower back home on the Overworld.")
    protected static final String FOREST_TRANSPLANT_CHORUS_FRUIT = "forest/transplant_chorus_fruit";

    // --- Light --- //

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Dragon Friends")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Create a Rider Workbench to create the new villager profession.")
    protected static final String LIGHT_DRAGON_RIDER_WORKBENCH = "light/dragon_rider_workbench";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Path of Salvation")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Earn a Light Key by trading with the Dragon Rider villagers.")
    protected static final String LIGHT_COLLECT_KEY = "light/collect_key";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Subterranean Reward")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Find a Light Vault, and open it using a Light Key.")
    protected static final String LIGHT_OPEN_VAULT = "light/open_vault";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Protector of the Light")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Upgrade a piece of Netherite armor to Light Dragon Armor.")
    protected static final String LIGHT_GET_ARMOR_ITEM = "light/get_armor_item";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Draconic Paladin de Lux")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Put together a full set of Light Dragon Armor, and stand defiant against all who would bring you harm.")
    protected static final String LIGHT_GET_ARMOR_SET = "light/get_armor_set";

    // --- Dark --- //

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Marked")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "After a lot of killing, the dragon hunters are interested in you.")
    protected static final String DARK_AFFECTED_BY_HUNTER_OMEN = "dark/affected_by_hunter_omen";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Path of Destruction")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Tear a Dark Key from a hunter knight. Blood will have to be spilled.")
    protected static final String DARK_COLLECT_KEY = "dark/collect_key";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Bloodstained Claim")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Follow a Dark Key to a Dark Vault, and claim your murderous prize.")
    protected static final String DARK_OPEN_VAULT = "dark/open_vault";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Umbral Terror")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Upgrade a piece of Netherite armor to Dark Dragon Armor.")
    protected static final String DARK_GET_ARMOR_ITEM = "dark/get_armor_item";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "World Domination")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Put together a full set of Dark Dragon Armor, and continue your path to total conquest.")
    protected static final String DARK_GET_ARMOR_SET = "dark/get_armor_set";

    // --- Hunter --- //

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Would you take a flyer?")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Find Spearman and make him an offer he can't refuse!")
    protected static final String HUNTER_PROMOTION = "hunter/promotion";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Path of the Dragon Hunter")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Earn a Hunter Key by cooperating with the Dragon Hunters.")
    protected static final String HUNTER_COLLECT_KEY = "hunter/collect_key";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Hunter's Bounty")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Follow a Hunter Key to the secret stash and liberate a Dragon Hunter Vault of its contents.")
    protected static final String HUNTER_OPEN_VAULT = "hunter/open_vault";

    @Translation(type = Translation.Type.ADVANCEMENT, comments = "Trapper")
    @Translation(type = Translation.Type.ADVANCEMENT_DESCRIPTION, comments = "Fire a crossbow with the bolas enchantment. Time to catch dragons!")
    protected static final String HUNTER_FIRE_BOLAS = "hunter/fire_bolas";
}
