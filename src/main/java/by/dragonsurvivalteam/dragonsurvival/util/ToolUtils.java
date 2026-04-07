package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;


public class ToolUtils {
    public static boolean shouldUseDragonTools(final ItemStack itemStack) {
        return !isHarvestTool(itemStack) && !isWeapon(itemStack);
    }

    public static boolean isHarvestTool(final ItemStack itemStack) {
        return isPickaxe(itemStack) || isAxe(itemStack) || isShovel(itemStack) || isHoe(itemStack) || isShears(itemStack);
    }

    public static boolean isClawWeapon(final ItemStack itemStack) {
        return itemStack.is(DSItemTags.CLAW_WEAPONS);
    }

    public static boolean isWeapon(final ItemStack itemStack) {
        return itemStack.is(DSItemTags.CLAW_WEAPONS) || itemStack.getComponents().has(DataComponents.WEAPON) || itemStack.getComponents().has(DataComponents.KINETIC_WEAPON) || itemStack.getComponents().has(DataComponents.KINETIC_WEAPON);
    }

    public static boolean isPickaxe(final ItemStack itemStack) {
        return itemStack.is(ItemTags.PICKAXES) || itemStack.isCorrectToolForDrops(Blocks.STONE.defaultBlockState());
    }

    public static boolean isAxe(final ItemStack itemStack) {
        return itemStack.canPerformAction(ItemAbilities.AXE_STRIP) || itemStack.canPerformAction(ItemAbilities.AXE_SCRAPE) || itemStack.is(ItemTags.AXES) || itemStack.isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState());
    }

    public static boolean isShovel(final ItemStack itemStack) {
        return itemStack.canPerformAction(ItemAbilities.SHOVEL_FLATTEN) || itemStack.is(ItemTags.SHOVELS) || itemStack.isCorrectToolForDrops(Blocks.DIRT.defaultBlockState());
    }

    public static boolean isHoe(final ItemStack itemStack) {
        return itemStack.canPerformAction(ItemAbilities.HOE_TILL) || itemStack.is(ItemTags.HOES);
    }

    public static boolean isShears(final ItemStack itemStack) {
        return itemStack.canPerformAction(ItemAbilities.SHEARS_CARVE) || itemStack.canPerformAction(ItemAbilities.SHEARS_DIG) || itemStack.canPerformAction(ItemAbilities.SHEARS_DISARM) || itemStack.canPerformAction(ItemAbilities.SHEARS_HARVEST) || itemStack.is(Items.SHEARS);
    }

    public static int getRequiredHarvestLevel(final BlockState state) {
        if (state.is(Tags.Blocks.NEEDS_NETHERITE_TOOL)) {
            return 5;
        } else if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return 4;
        } else if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
            return 3;
        } else if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
            return 2;
        } else if (state.requiresCorrectToolForDrops()) {
            return 1;
        }

        // There is 'Tags.Blocks.NEEDS_WOOD_TOOL' / 'Tags.Blocks.NEEDS_GOLD_TOOL' but they don't seem to be used

        return 0;
    }

    public static int toolToHarvestLevel(final ItemStack stack) {
        Tool tool = stack.get(DataComponents.TOOL);

        if (tool == null) {
            return 0;
        }

        int level = 0;

        for (Tool.Rule rule : tool.rules()) {
            // In 26.1 vanilla tool tiers are encoded through the deny-drop rule created from ToolMaterial.incorrectBlocksForDrops.
            if (rule.blocks() instanceof HolderSet.Named<Block> set) {
                level = Math.max(level, mapIncorrectToolTagToHarvestLevel(set.key()));
            }
        }

        return level;
    }

    /** Skips exclusion rules related to requiring a higher tier of the tool */
    public static boolean isCorrectTool(final ItemStack stack, final BlockState state) {
        if (stack.isEmpty()) {
            return false;
        }

        Tool tool = stack.get(DataComponents.TOOL);

        if (tool == null) {
            return stack.isCorrectToolForDrops(state);
        }

        for (Tool.Rule rule : tool.rules()) {
            if (isIncorrectRule(rule)) {
                // Skip these since we just want to know if this tool is the correct type (defined by the rule that has a speed component)
                // We don't skip all rules in case the tool is some sort of custom tool
                continue;
            }

            if (rule.correctForDrops().orElse(false) && state.is(rule.blocks())) {
                // The order of the rules seem relevant
                // Meaning the first entries are the exclusions
                return true;
            }
        }

        return false;
    }

    private static boolean isIncorrectRule(final Tool.Rule rule) {
        if (rule.blocks() instanceof HolderSet.Named<Block> set) {
            return mapIncorrectToolTagToHarvestLevel(set.key()) != 0;
        }

        return false;
    }

    private static int mapIncorrectToolTagToHarvestLevel(final TagKey<Block> key) {
        if (key == BlockTags.INCORRECT_FOR_WOODEN_TOOL || key == BlockTags.INCORRECT_FOR_GOLD_TOOL) {
            return 1;
        } else if (key == BlockTags.INCORRECT_FOR_STONE_TOOL || key == BlockTags.INCORRECT_FOR_COPPER_TOOL) {
            return 2;
        } else if (key == BlockTags.INCORRECT_FOR_IRON_TOOL) {
            return 3;
        } else if (key == BlockTags.INCORRECT_FOR_DIAMOND_TOOL) {
            return 4;
        } else if (key == BlockTags.INCORRECT_FOR_NETHERITE_TOOL) {
            return 5;
        }

        return 0;
    }
}
