//package by.dragonsurvivalteam.dragonsurvival.config.server.dragon;
//
//import by.dragonsurvivalteam.dragonsurvival.config.ConfigUtils;
//import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
//import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
//import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
//import by.dragonsurvivalteam.dragonsurvival.config.obj.Validation;
//import by.dragonsurvivalteam.dragonsurvival.config.types.BlockStateConfig;
//import by.dragonsurvivalteam.dragonsurvival.config.types.FoodConfigCollector;
//import by.dragonsurvivalteam.dragonsurvival.config.types.ItemHurtConfig;
//import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
//import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
//import by.dragonsurvivalteam.dragonsurvival.util.Functions;
//import net.minecraft.tags.ItemTags;
//import net.minecraft.world.item.Items;
//
//import java.util.Arrays;
//import java.util.List;
//
//public class CaveDragonConfig {
//    @Translation(key = "cave_conditional_mana_blocks", type = Translation.Type.CONFIGURATION, comments = "Blocks that restore mana for cave dragons when under certain conditions (block states) - Formatting: namespace:path:key=value,key=value (prefix namespace with # for tags)")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"cave_dragon", "magic"}, key = "cave_conditional_mana_blocks")
//    public static List<BlockStateConfig> caveConditionalManaBlocks = List.of(
//            BlockStateConfig.of("#minecraft:campfires:lit=true"),
//            BlockStateConfig.of("#c:player_workstations/furnaces:lit=true"),
//            BlockStateConfig.of("minecraft:smoker:lit=true"),
//            BlockStateConfig.of("minecraft:blast_furnace:lit=true")
//    );
//
//    // --- Food --- //
//
//    @Translation(key = "cave_hurtful_items", type = Translation.Type.CONFIGURATION, comments = "Items which will cause damage to cave dragons when consumed - Formatting: namespace:path:damage (prefix namespace with # for tags)")
//    @ConfigOption(side = ConfigSide.SERVER, category = {"cave_dragon", "food"}, key = "cave_hurtful_items", validation = Validation.RESOURCE_LOCATION_NUMBER)
//    public static List<ItemHurtConfig> caveDragonHurtfulItems = List.of(
//            ItemHurtConfig.of("minecraft:potion:2"),
//            ItemHurtConfig.of("minecraft:water_bottle:2"),
//            ItemHurtConfig.of("minecraft:milk_bucket:2")
//    );
//}
