package by.dragonsurvivalteam.dragonsurvival.gametests;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;


@PrefixGameTestTemplate(false)
@GameTestHolder(DragonSurvival.MODID)
public class ConfigTests {
    @BeforeBatch(batch = "config_tests")
    public static void beforeTest(final ServerLevel level) {
        ConfigHandler.resetConfigValues(ConfigSide.SERVER);
    }

    // TODO :: re-add tests

//    @GameTest(template = TestUtils.AIR_CUBE_1X, batch = "config_tests")
//    public static void test_blacklist_config(final GameTestHelper helper) {
//        String configKey = "blacklisted_items";
//
//        List<String> value = List.of("minecraft:potion", "minecraft:swords", "minecraft:.*bow");
//        TestUtils.setAndCheckConfig(helper, configKey, value);
//
//        List<Item> itemsToCheck = List.of(
//                Items.POTION,
//                Items.BOW,
//                Items.CROSSBOW,
//                Items.IRON_SWORD,
//                Items.NETHERITE_SWORD
//        );
//
//        for (Item item : itemsToCheck) {
//            helper.assertTrue(DragonPenaltyHandler.itemIsBlacklisted(item), "[%s] is not in the blacklist - expected item to be blacklisted");
//        }
//
//        helper.succeed();
//    }

//    @GameTest(template = TestUtils.AIR_CUBE_1X, batch = "config_tests")
//    public static void test_invalid_config_list(final GameTestHelper helper) {
//        String configKey = "blacklisted_items";
//        List<String> defaultValue = List.of("minecraft:potion");
//
//        List<List<String>> invalidValues = List.of(
//                List.of("minecraft:Bow"),
//                List.of("minecraft_bow"),
//                List.of("minecraft:#bow")
//        );
//
//        invalidValues.forEach(invalidValue -> {
//            TestUtils.setAndCheckConfig(helper, configKey, defaultValue);
//            ConfigHandler.updateConfigValue(configKey, invalidValue);
//            Field field = ConfigHandler.getField(configKey);
//
//            try {
//                Object fieldValue = field.get(null);
//                helper.assertTrue(fieldValue.equals(defaultValue), "The field value [" + fieldValue + "] did not match the expected value [" + defaultValue + "] (config should not have been updated)");
//            } catch (IllegalAccessException exception) {
//                helper.fail("Failed trying to access a field for the config [" + configKey + "] to validate the config: [" + exception.getMessage() + "]");
//            }
//        });
//
//        helper.succeed();
//    }

//    @GameTest(template = TestUtils.AIR_CUBE_1X, batch = "config_tests")
//    public static void test_invalid_config(final GameTestHelper helper) {
//        String configKey = "normal_mana_regeneration";
//        float valueNotInRange = 0f;
//        float defaultValue = 1f;
//
//        TestUtils.setAndCheckConfig(helper, configKey, defaultValue);
//        ConfigHandler.updateConfigValue(configKey, valueNotInRange);
//        Field field = ConfigHandler.getField(configKey);
//
//        try {
//            Object fieldValue = field.get(null);
//            helper.assertTrue(TestUtils.compare(defaultValue, fieldValue), "The field value [" + fieldValue + "] did not match the expected value [" + defaultValue + "] (config should not have been updated)");
//        } catch (IllegalAccessException exception) {
//            helper.fail("Failed trying to access a field for the config [" + configKey + "] to validate the config: [" + exception.getMessage() + "]");
//        }
//
//        helper.succeed();
//    }
}
