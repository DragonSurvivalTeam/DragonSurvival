package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.compat.overgeared.Overgeared;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;

public class ToolUtils {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore
    public static boolean shouldUseDragonTools(final ItemStack itemStack) {
        return !(itemStack.getItem() instanceof TieredItem) && !isHarvestTool(itemStack) && !isWeapon(itemStack);
    }

    public static boolean isHarvestTool(final ItemStack itemStack) {
        return isPickaxe(itemStack) || isAxe(itemStack) || isShovel(itemStack) || isHoe(itemStack) || isShears(itemStack);
    }

    public static boolean isWeapon(final ItemStack itemStack) {
        return itemStack.getItem() instanceof SwordItem || itemStack.canPerformAction(ItemAbilities.SWORD_SWEEP) || itemStack.canPerformAction(ItemAbilities.SWORD_DIG) || itemStack.is(DSItemTags.CLAW_WEAPONS);
    }

    public static boolean isPickaxe(final ItemStack itemStack) {
        return itemStack.getItem() instanceof PickaxeItem || itemStack.canPerformAction(ItemAbilities.PICKAXE_DIG) || itemStack.is(ItemTags.PICKAXES) || itemStack.isCorrectToolForDrops(Blocks.STONE.defaultBlockState());
    }

    public static boolean isAxe(final ItemStack itemStack) {
        return itemStack.getItem() instanceof AxeItem || itemStack.canPerformAction(ItemAbilities.AXE_STRIP) || itemStack.canPerformAction(ItemAbilities.AXE_DIG) || itemStack.canPerformAction(ItemAbilities.AXE_SCRAPE) || itemStack.is(ItemTags.AXES) || itemStack.isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState());
    }

    public static boolean isShovel(final ItemStack itemStack) {
        return itemStack.getItem() instanceof ShovelItem || itemStack.canPerformAction(ItemAbilities.SHOVEL_FLATTEN) || itemStack.canPerformAction(ItemAbilities.SHOVEL_DIG) || itemStack.is(ItemTags.SHOVELS) || itemStack.isCorrectToolForDrops(Blocks.DIRT.defaultBlockState());
    }

    public static boolean isHoe(final ItemStack itemStack) {
        return itemStack.canPerformAction(ItemAbilities.HOE_DIG) || itemStack.canPerformAction(ItemAbilities.HOE_TILL) || itemStack.is(ItemTags.HOES);
    }

    public static boolean isShears(final ItemStack itemStack) {
        return itemStack.canPerformAction(ItemAbilities.SHEARS_CARVE) || itemStack.canPerformAction(ItemAbilities.SHEARS_DIG) || itemStack.canPerformAction(ItemAbilities.SHEARS_DISARM) || itemStack.canPerformAction(ItemAbilities.SHEARS_HARVEST) || itemStack.is(Items.SHEARS);
    }

    public static double getRequiredHarvestLevel(final BlockState state) {
        // It would be possible to check 'incorrect_for_x_tool' for each tier
        // But that would complicate the logic for in-between tiers
        // (e.g., for Overgeared steel, 'needs_steel_tool is added to 'incorrect_for_iron_tool' but steel tools are not equal to diamond tools)

        // We also need to check the lowest 'needs_x_tool' tag first
        // Since some mods don't clean up / adjust the existing tags when adding their own
        // (e.g., for Overgeared steel, obsidian is added to 'needs_steel_tool' but 'needs_diamond_tool' also contains that entry)

        if (state.is(BlockTags.NEEDS_STONE_TOOL)) {
            return 2;
        } else if (state.is(Overgeared.NEEDS_COPPER_TOOL)) {
            return 2.5;
        } else if (state.is(BlockTags.NEEDS_IRON_TOOL)) {
            return 3;
        } else if (state.is(Overgeared.NEEDS_STEEL_TOOL)) {
            return 3.5;
        } else if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return 4;
        } else if (state.is(Tags.Blocks.NEEDS_NETHERITE_TOOL)) {
            return 5;
        } else if (state.is(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)) {
            return 6;
        } else if (state.requiresCorrectToolForDrops()) {
            return 1;
        }

        // There is 'Tags.Blocks.NEEDS_WOOD_TOOL' / 'Tags.Blocks.NEEDS_GOLD_TOOL' but they don't seem to be used

        return 0;
    }

    public static double toolToHarvestLevel(final ItemStack stack) {
        Item item = stack.getItem();
        double level = 0;

        if (item instanceof TieredItem tiered) {
            Tier tier = tiered.getTier();

            level = switch (tier) {
                case Tiers.WOOD, Tiers.GOLD -> 1;
                case Tiers.STONE -> 2;
                case Tiers.IRON -> 3;
                case Tiers.DIAMOND -> 4;
                case Tiers.NETHERITE -> 5;
                default -> 0;
            };

            if (level == 0) {
                level = tagToLevel(tier.getIncorrectBlocksForDrops());
            }
        }

        if (level == 0) {
            // In case it is some custom item / multi-tool
            Tool tool = stack.get(DataComponents.TOOL);

            if (tool == null) {
                return level;
            }

            for (Tool.Rule rule : tool.rules()) {
                if (rule.blocks() instanceof HolderSet.Named<Block> set) {
                    level = tagToLevel(set.key());

                    if (level > 0) {
                        return level;
                    }
                }
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
                // The order of the rules seems relevant
                // Meaning the first entries are the exclusions
                return true;
            }
        }

        return false;
    }

    private static double tagToLevel(final TagKey<Block> tag) {
        double level = 0;

        // Basically - if a tool has this tag, it means it can be considered to be of that tier
        // Because it says "this tool cannot mine these blocks"
        if (tag == BlockTags.INCORRECT_FOR_WOODEN_TOOL ||tag == BlockTags.INCORRECT_FOR_GOLD_TOOL) {
            level = 1;
        } else if (tag == BlockTags.INCORRECT_FOR_STONE_TOOL) {
            level = 2;
        } else if (tag == Overgeared.INCORRECT_FOR_COPPER_TOOL) {
            level = 2.5;
        } else if (tag == BlockTags.INCORRECT_FOR_IRON_TOOL) {
            level = 3;
        } else if (tag == Overgeared.INCORRECT_FOR_STEEL_TOOL) {
            level = 3.5;
        } else if (tag == BlockTags.INCORRECT_FOR_DIAMOND_TOOL) {
            level = 4;
        } else if (tag == BlockTags.INCORRECT_FOR_NETHERITE_TOOL) {
            level = 5;
        }

        return level;
    }

    private static boolean isIncorrectRule(final Tool.Rule rule) {
        if (rule.blocks() instanceof HolderSet.Named<Block> set) {
            // Generic check would be possible
            // But 'toolToHarvestLevel' also needs adjustment for each new entry
            return set.key() == BlockTags.INCORRECT_FOR_WOODEN_TOOL ||
                    set.key() == BlockTags.INCORRECT_FOR_GOLD_TOOL ||
                    set.key() == BlockTags.INCORRECT_FOR_STONE_TOOL ||
                    set.key() == BlockTags.INCORRECT_FOR_IRON_TOOL ||
                    set.key() == BlockTags.INCORRECT_FOR_DIAMOND_TOOL ||
                    set.key() == BlockTags.INCORRECT_FOR_NETHERITE_TOOL ||
                    // Overgeared
                    set.key() == Overgeared.INCORRECT_FOR_COPPER_TOOL ||
                    set.key() == Overgeared.INCORRECT_FOR_STEEL_TOOL;
        }

        return false;
    }
}
