package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.compat.overgeared.Overgeared;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.ToolActions;

public class ToolUtils {
    private static final TagKey<Item> PICKAXES = itemTag("pickaxes");
    private static final TagKey<Item> AXES = itemTag("axes");
    private static final TagKey<Item> SHOVELS = itemTag("shovels");
    private static final TagKey<Item> HOES = itemTag("hoes");

    // Keep recognizing the 1.21.1 tier tags when they are supplied by a compatible datapack.
    private static final TagKey<Block> INCORRECT_FOR_WOODEN_TOOL = blockTag("incorrect_for_wooden_tool");
    private static final TagKey<Block> INCORRECT_FOR_GOLD_TOOL = blockTag("incorrect_for_gold_tool");
    private static final TagKey<Block> INCORRECT_FOR_STONE_TOOL = blockTag("incorrect_for_stone_tool");
    private static final TagKey<Block> INCORRECT_FOR_IRON_TOOL = blockTag("incorrect_for_iron_tool");
    private static final TagKey<Block> INCORRECT_FOR_DIAMOND_TOOL = blockTag("incorrect_for_diamond_tool");
    private static final TagKey<Block> INCORRECT_FOR_NETHERITE_TOOL = blockTag("incorrect_for_netherite_tool");

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore
    public static boolean shouldUseDragonTools(final ItemStack itemStack) {
        return !(itemStack.getItem() instanceof TieredItem) && !isHarvestTool(itemStack) && !isWeapon(itemStack);
    }

    public static boolean isHarvestTool(final ItemStack itemStack) {
        return isPickaxe(itemStack) || isAxe(itemStack) || isShovel(itemStack) || isHoe(itemStack) || isShears(itemStack);
    }

    public static boolean isWeapon(final ItemStack itemStack) {
        return itemStack.getItem() instanceof SwordItem || itemStack.canPerformAction(ToolActions.SWORD_SWEEP) || itemStack.canPerformAction(ToolActions.SWORD_DIG) || itemStack.is(DSItemTags.CLAW_WEAPONS);
    }

    public static boolean isPickaxe(final ItemStack itemStack) {
        return itemStack.getItem() instanceof PickaxeItem || itemStack.canPerformAction(ToolActions.PICKAXE_DIG) || itemStack.is(PICKAXES) || itemStack.isCorrectToolForDrops(Blocks.STONE.defaultBlockState());
    }

    public static boolean isAxe(final ItemStack itemStack) {
        return itemStack.getItem() instanceof AxeItem || itemStack.canPerformAction(ToolActions.AXE_STRIP) || itemStack.canPerformAction(ToolActions.AXE_DIG) || itemStack.canPerformAction(ToolActions.AXE_SCRAPE) || itemStack.is(AXES) || itemStack.isCorrectToolForDrops(Blocks.OAK_LOG.defaultBlockState());
    }

    public static boolean isShovel(final ItemStack itemStack) {
        return itemStack.getItem() instanceof ShovelItem || itemStack.canPerformAction(ToolActions.SHOVEL_FLATTEN) || itemStack.canPerformAction(ToolActions.SHOVEL_DIG) || itemStack.is(SHOVELS) || itemStack.isCorrectToolForDrops(Blocks.DIRT.defaultBlockState());
    }

    public static boolean isHoe(final ItemStack itemStack) {
        return itemStack.canPerformAction(ToolActions.HOE_DIG) || itemStack.canPerformAction(ToolActions.HOE_TILL) || itemStack.is(HOES);
    }

    public static boolean isShears(final ItemStack itemStack) {
        return itemStack.canPerformAction(ToolActions.SHEARS_CARVE) || itemStack.canPerformAction(ToolActions.SHEARS_DIG) || itemStack.canPerformAction(ToolActions.SHEARS_DISARM) || itemStack.canPerformAction(ToolActions.SHEARS_HARVEST) || itemStack.is(Items.SHEARS);
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
        } else if (state.is(INCORRECT_FOR_NETHERITE_TOOL)) {
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

            if (tier == Tiers.WOOD || tier == Tiers.GOLD) {
                level = 1;
            } else if (tier == Tiers.STONE) {
                level = 2;
            } else if (tier == Tiers.IRON) {
                level = 3;
            } else if (tier == Tiers.DIAMOND) {
                level = 4;
            } else if (tier == Tiers.NETHERITE) {
                level = 5;
            }

            if (level == 0) {
                level = tagToLevel(tier.getTag());
            }

            if (level == 0) {
                level = tier.getLevel() + 1;
            }
        }

        return level;
    }

    /** Skips exclusion rules related to requiring a higher tier of the tool */
    public static boolean isCorrectTool(final ItemStack stack, final BlockState state) {
        if (stack.isEmpty()) {
            return false;
        }

        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return isPickaxe(stack);
        } else if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return isAxe(stack);
        } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return isShovel(stack);
        } else if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            return isHoe(stack);
        }

        // Custom 1.20 tools express arbitrary mining rules through their destroy speed.
        return stack.isCorrectToolForDrops(state) || stack.getDestroySpeed(state) > 1.0F;
    }

    private static double tagToLevel(final TagKey<Block> tag) {
        // Basically - if a tool has this tag, it means it can be considered to be of that tier
        // Because it says "this tool cannot mine these blocks"
        if (tag == null) {
            return 0;
        } else if (tag.equals(Tags.Blocks.NEEDS_WOOD_TOOL) || tag.equals(Tags.Blocks.NEEDS_GOLD_TOOL) || tag.equals(INCORRECT_FOR_WOODEN_TOOL) || tag.equals(INCORRECT_FOR_GOLD_TOOL)) {
            return 1;
        } else if (tag.equals(BlockTags.NEEDS_STONE_TOOL) || tag.equals(INCORRECT_FOR_STONE_TOOL)) {
            return 2;
        } else if (tag.equals(Overgeared.NEEDS_COPPER_TOOL) || tag.equals(Overgeared.INCORRECT_FOR_COPPER_TOOL)) {
            return 2.5;
        } else if (tag.equals(BlockTags.NEEDS_IRON_TOOL) || tag.equals(INCORRECT_FOR_IRON_TOOL)) {
            return 3;
        } else if (tag.equals(Overgeared.NEEDS_STEEL_TOOL) || tag.equals(Overgeared.INCORRECT_FOR_STEEL_TOOL)) {
            return 3.5;
        } else if (tag.equals(BlockTags.NEEDS_DIAMOND_TOOL) || tag.equals(INCORRECT_FOR_DIAMOND_TOOL)) {
            return 4;
        } else if (tag.equals(Tags.Blocks.NEEDS_NETHERITE_TOOL) || tag.equals(INCORRECT_FOR_NETHERITE_TOOL)) {
            return 5;
        }

        return 0;
    }

    private static TagKey<Item> itemTag(final String path) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", path));
    }

    private static TagKey<Block> blockTag(final String path) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation("minecraft", path));
    }
}
