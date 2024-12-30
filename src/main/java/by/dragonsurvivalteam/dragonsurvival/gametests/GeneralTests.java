package by.dragonsurvivalteam.dragonsurvival.gametests;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
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
public class GeneralTests {
    @GameTest(template = TestUtils.AIR_CUBE_1X)
    public static void test_tool_swap(final GameTestHelper helper) {
        Player player = TestUtils.createPlayer(helper);
        TestUtils.setToDragon(helper, player, DragonTypes.CAVE, DragonBodies.center, DragonStages.young);

        ClawInventoryData clawInventory = ClawInventoryData.getData(player);
        ItemStack mainHandItem = Items.APPLE.getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, mainHandItem);

        ItemStack clawItem = Items.STONE_PICKAXE.getDefaultInstance();
        clawInventory.set(ClawInventoryData.Slot.PICKAXE, clawItem);
        BlockState state = TestUtils.setBlock(helper, Blocks.IRON_ORE);

        DragonSurvival.LOGGER.info("Starting first tool swap layer");
        clawInventory.swapStart(player, state);
        // Check that:
        // - the main hand item was correctly stored
        // - the item for the pickaxe slot is now in the main hand
        // - the item in the pickaxe slot is empty
        assertMainHandItem(helper, player, clawItem);
        assertStoredItem(helper, player, mainHandItem);
        assertClawItem(helper, player, ClawInventoryData.Slot.PICKAXE, ItemStack.EMPTY);

        DragonSurvival.LOGGER.info("Starting second tool swap layer");
        clawInventory.swapStart(player, state);
        assertMainHandItem(helper, player, clawItem);
        assertStoredItem(helper, player, mainHandItem);
        assertClawItem(helper, player, ClawInventoryData.Slot.PICKAXE, ItemStack.EMPTY);

        DragonSurvival.LOGGER.info("Ending second tool swap layer");
        clawInventory.swapFinish(player);
        // Check that the items are still in their swapped state
        assertMainHandItem(helper, player, clawItem);
        assertStoredItem(helper, player, mainHandItem);
        assertClawItem(helper, player, ClawInventoryData.Slot.PICKAXE, ItemStack.EMPTY);

        DragonSurvival.LOGGER.info("Ending first tool swap layer");
        clawInventory.swapFinish(player);
        // Check that the items were swapped back to their previous places
        assertMainHandItem(helper, player, mainHandItem);
        assertStoredItem(helper, player, ItemStack.EMPTY);
        assertClawItem(helper, player, ClawInventoryData.Slot.PICKAXE, clawItem);

        helper.succeed();
    }

    public static void assertClawItem(final GameTestHelper helper, final Player player, final ClawInventoryData.Slot slot, final ItemStack item) {
        ClawInventoryData clawInventory = ClawInventoryData.getData(player);
        ItemStack clawItem = clawInventory.get(slot);
        helper.assertTrue(clawItem == item, String.format("Claw item for slot [%s] is [%s] - expected [%s]", slot, clawItem, item));
    }

    public static void assertMainHandItem(final GameTestHelper helper, final Player player, final ItemStack item) {
        ItemStack mainHandItem = player.getMainHandItem();
        helper.assertTrue(mainHandItem == item, String.format("Main hand item is [%s] - expected [%s]", mainHandItem, item));
    }

    public static void assertStoredItem(final GameTestHelper helper, final Player player, final ItemStack item) {
        ClawInventoryData clawInventory = ClawInventoryData.getData(player);
        helper.assertTrue(
                clawInventory.storedMainHandTool == item,
                String.format("Stored item is [%s] - expected [%s]", clawInventory.storedMainHandTool, item)
        );
    }
}
