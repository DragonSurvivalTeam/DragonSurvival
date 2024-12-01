package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.mixins.PlayerEndMixin;
import by.dragonsurvivalteam.dragonsurvival.mixins.PlayerStartMixin;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ClawInventoryData implements INBTSerializable<CompoundTag> {

    public enum Slot {
        SWORD,
        PICKAXE,
        AXE,
        SHOVEL;

        /**
         * Equivalent to the container size
         */
        public static int size() {
            return values().length;
        }
    }

    public static ClawInventoryData getData(Player player) {
        return player.getData(DSDataAttachments.CLAW_INVENTORY);
    }

    private final SimpleContainer clawsInventory = new SimpleContainer(4);
    public boolean shouldRenderClaws = true;
    private boolean isMenuOpen;

    /** Used in {@link PlayerStartMixin} and {@link PlayerEndMixin} */
    public ItemStack storedMainHandWeapon = ItemStack.EMPTY;
    public boolean switchedWeapon;

    public ItemStack storedMainHandTool = ItemStack.EMPTY;
    public boolean switchedTool;
    public int switchedToolSlot = -1;

    /** To track the state if a tool swap is triggered within a tool swap (should only swap back if the last tool swap finishes) */
    public int toolSwapLayer;

    /**
     * Puts the relevant claw tool in the main hand and stores said main hand in the dragon state handler<br>
     * This way modded enchantments etc. which check the currently held item will be directly compatible<br>
     * <br>
     * When using this make sure you call {@link ClawInventoryData#swapFinish(Player)} to restore the initial state
     */
    public void swapStart(@Nullable final Player player, final BlockState blockState) {
        if (player == null || player.isCreative() || player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        Pair<ItemStack, Integer> data = ClawToolHandler.getDragonHarvestToolAndSlot(player, blockState);
        ItemStack dragonHarvestTool = data.getFirst();
        int toolSlot = data.getSecond();

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);

        if (toolSlot != -1 && !switchedTool) {
            player.setItemInHand(InteractionHand.MAIN_HAND, dragonHarvestTool);

            clawsInventory.setItem(toolSlot, ItemStack.EMPTY);
            storedMainHandTool = mainHand;
            switchedTool = true;
            switchedToolSlot = toolSlot;
        }

        toolSwapLayer++;
    }

    /**
     * Puts the stored main hand back into the main hand and the claw tool into its slot
     */
    public void swapFinish(@Nullable final Player player) {
        if (player == null || player.isCreative() || player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        toolSwapLayer--;

        if (toolSwapLayer < 0) {
            DragonSurvival.LOGGER.warn("Tool swap layer was lower than 0 - this should not happen");
            toolSwapLayer = 0;
        }

        if (switchedTool && toolSwapLayer == 0) {
            ItemStack originalMainHand = storedMainHandTool;
            ItemStack originalToolSlot = player.getItemInHand(InteractionHand.MAIN_HAND);

            player.setItemInHand(InteractionHand.MAIN_HAND, originalMainHand);

            clawsInventory.setItem(switchedToolSlot, originalToolSlot);
            storedMainHandTool = ItemStack.EMPTY;
            switchedTool = false;
            switchedToolSlot = -1;
        }
    }

    public void set(final ClawInventoryData.Slot slot, final ItemStack stack) {
        clawsInventory.setItem(slot.ordinal(), stack);
    }

    public ItemStack get(final ClawInventoryData.Slot slot) {
        return clawsInventory.getItem(slot.ordinal());
    }

    public ItemStack getSword() {
        return clawsInventory.getItem(ClawInventoryData.Slot.SWORD.ordinal());
    }

    public ItemStack getPickaxe() {
        return clawsInventory.getItem(ClawInventoryData.Slot.PICKAXE.ordinal());
    }

    public ItemStack getAxe() {
        return clawsInventory.getItem(ClawInventoryData.Slot.AXE.ordinal());
    }

    public ItemStack getShovel() {
        return clawsInventory.getItem(ClawInventoryData.Slot.SHOVEL.ordinal());
    }

    public void setMenuOpen(boolean isMenuOpen) {
        this.isMenuOpen = isMenuOpen;
    }

    public SimpleContainer getContainer() {
        return clawsInventory;
    }

    public boolean isMenuOpen() {
        return isMenuOpen;
    }

    public boolean hasValidClawTool(final BlockState state) {
        for (int i = 0; i < 4; i++) {
            ItemStack stack = getContainer().getItem(i);

            if (stack.isCorrectToolForDrops(state) || stack.getDestroySpeed(state) > 1) {
                return true;
            }
        }

        return false;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        tag.putBoolean("clawsMenu", isMenuOpen);
        for (ClawInventoryData.Slot slot : ClawInventoryData.Slot.values()) {
            if (clawsInventory.getItem(slot.ordinal()).isEmpty()) continue;
            tag.put(slot.name(), clawsInventory.getItem(slot.ordinal()).save(provider));
        }
        tag.putBoolean("renderClaws", shouldRenderClaws);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        setMenuOpen(tag.getBoolean("clawsMenu"));
        shouldRenderClaws = tag.getBoolean("renderClaws");

        for (ClawInventoryData.Slot slot : ClawInventoryData.Slot.values()) {
            CompoundTag slotTag = tag.getCompound(slot.name());
            if (slotTag.isEmpty()) continue;
            Optional<ItemStack> stack = ItemStack.parse(provider, slotTag);
            if (stack.isEmpty()) continue;
            clawsInventory.setItem(slot.ordinal(), stack.get());
        }
    }
}