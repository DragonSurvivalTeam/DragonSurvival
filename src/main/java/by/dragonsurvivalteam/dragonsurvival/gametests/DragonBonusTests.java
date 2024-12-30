package by.dragonsurvivalteam.dragonsurvival.gametests;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HarvestBonuses;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@PrefixGameTestTemplate(false)
@GameTestHolder(DragonSurvival.MODID)
public class DragonBonusTests {
    @BeforeBatch(batch = "dragon_bonus_tests")
    public static void beforeTest(final ServerLevel level) {
        ConfigHandler.resetConfigValues(ConfigSide.SERVER);
    }

    @GameTest(template = TestUtils.AIR_CUBE_3X, batch = "dragon_bonus_tests")
    public static void test_harvest_level_bonus(final GameTestHelper helper) {
        Player player = TestUtils.createPlayer(helper);
        TestUtils.setToDragon(helper, player, DragonTypes.CAVE, DragonBodies.center, DragonStages.newborn);

        BlockState state = TestUtils.setBlock(helper, Blocks.IRON_ORE);
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        player.setOnGround(true);

        // Set a level that has no harvest level bonus
        boolean canHarvest = player.hasCorrectToolForDrops(state, helper.getLevel(), position);
        helper.assertFalse(canHarvest, String.format("[%s] can be harvested - expected block to not be harvestable", state));
        TestUtils.setToDragon(helper, player, DragonTypes.CAVE, DragonBodies.center, DragonStages.young);

        // Set a level that has a harvest level bonus
        canHarvest = player.hasCorrectToolForDrops(state, helper.getLevel(), position);
        helper.assertTrue(canHarvest, String.format("[%s] cannot be harvested - expected block to be harvestable", state));

        // Check that the base harvest level bonus does not allow the harvesting of blocks not within its tier
        BlockState newState = TestUtils.setBlock(helper, Blocks.ANCIENT_DEBRIS);
        canHarvest = player.hasCorrectToolForDrops(newState, helper.getLevel(), position);
        helper.assertFalse(!canHarvest, String.format("[%s] can be harvested - expected block to not be harvestable", newState));

        helper.succeed();
    }

    @GameTest(template = TestUtils.AIR_CUBE_3X, batch = "dragon_bonus_tests")
    public static void test_break_speed_bonus(final GameTestHelper helper) {
        // Setup
        Player player = TestUtils.createPlayer(helper);
        HarvestBonuses bonuses = player.getData(DSDataAttachments.HARVEST_BONUSES);
        ClawInventoryData clawInventory = ClawInventoryData.getData(player);
        double defaultSpeed = 1;

        BlockState state = TestUtils.setBlock(helper, Blocks.IRON_ORE);
        BlockPos position = helper.absolutePos(BlockPos.ZERO);
        player.setOnGround(true);

        // Make sure that a reduction exists
        TestUtils.setAndCheckConfig(helper, "break_speed_reduction", 2);

        // Validate the default speed
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        float speed = player.getDigSpeed(state, position);
        helper.assertTrue(speed == defaultSpeed, String.format("Dig speed for [%s] was [%f] - expected [%f]", state, speed, defaultSpeed));

        // Test bonus from dragon level
        TestUtils.setToDragon(helper, player, DragonTypes.CAVE, DragonBodies.center, DragonStages.young);
        clawInventory.set(ClawInventoryData.Slot.PICKAXE, ItemStack.EMPTY);

        speed = player.getDigSpeed(state, position);
        double expectedSpeed = defaultSpeed * bonuses.getSpeedMultiplier(state);
        helper.assertTrue(speed == expectedSpeed, String.format("Dig speed for [%s] was [%f] - expected [%f]", state, speed, expectedSpeed));

        // Test reduction to the bonus when a relevant tool is in the claw inventory
        clawInventory.set(ClawInventoryData.Slot.PICKAXE, Items.WOODEN_PICKAXE.getDefaultInstance());
        speed = player.getDigSpeed(state, position);
        expectedSpeed = defaultSpeed * ClawToolHandler.getReducedBonus(bonuses.getSpeedMultiplier(state));
        helper.assertTrue(speed == expectedSpeed, String.format("Dig speed for [%s] was [%f] - expected [%f]", state, speed, expectedSpeed));

        // Test reduction to the bonus when the block is not part of the harvestable blocks for that dragon type
        state = TestUtils.setBlock(helper, Blocks.OAK_WOOD);
        clawInventory.set(ClawInventoryData.Slot.PICKAXE, ItemStack.EMPTY);
        speed = player.getDigSpeed(state, position);
        expectedSpeed = defaultSpeed * ClawToolHandler.getReducedBonus(bonuses.getSpeedMultiplier(state));
        helper.assertTrue(speed == expectedSpeed, String.format("Dig speed for [%s] was [%f] - expected [%f]", state, speed, expectedSpeed));

        // Test that no bonus applies if the player is holding a tool
        TestUtils.setToDragon(helper, player, DragonTypes.FOREST, DragonBodies.center, DragonStages.young);
        player.setItemInHand(InteractionHand.MAIN_HAND, Items.WOODEN_SWORD.getDefaultInstance());
        speed = player.getDigSpeed(state, position);
        helper.assertTrue(speed == defaultSpeed, String.format("Dig speed for [%s] was [%f] - expected [%f]", state, speed, defaultSpeed));

        helper.succeed();
    }
}
