package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

public class ClawInventoryData implements ValueIOSerializable {
    public static final String IS_MENU_OPEN = "is_menu_open";
    public static final String SHOULD_RENDER_CLAWS = "should_render_claws";

    public enum Slot implements StringRepresentable {
        SWORD("gui/dragon_claws_sword"),
        PICKAXE("gui/dragon_claws_pickaxe"),
        AXE("gui/dragon_claws_axe"),
        SHOVEL("gui/dragon_claws_shovel");

        public static Codec<Slot> CODEC = StringRepresentable.fromValues(Slot::values);
        private final Identifier emptyTexture;

        Slot(final String path) {
            emptyTexture = DragonSurvival.res(path);
        }

        public Identifier getEmptyTexture() {
            return emptyTexture;
        }

        public boolean accepts(final ItemStack stack) {
            return switch (this) {
                case SWORD -> ToolUtils.isClawWeapon(stack);
                case PICKAXE -> ToolUtils.isPickaxe(stack);
                case AXE -> ToolUtils.isAxe(stack);
                case SHOVEL -> ToolUtils.isShovel(stack);
            };
        }

        /** Equivalent to the container size */
        public static int size() {
            return values().length;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }

    public boolean shouldRenderClaws = true;

    public ItemStack storedMainHandTool = ItemStack.EMPTY;
    public boolean switchedTool;
    public int switchedToolSlot = -1;

    private final SimpleContainer clawsInventory = new SimpleContainer(4);
    private final ArrayList<SwapFrame> swapFrames = new ArrayList<>();
    private boolean isMenuOpen = true;
    /** To track the state if a tool swap is triggered within a tool swap (should only swap back if the last tool swap finishes) */
    private int toolSwapLayer;

    private record SwapFrame(ItemStack previousMainHand, int previousBorrowedSlot, int newBorrowedSlot, boolean changedHand) {
    }

    public void swapStart(final Player player, final BlockState blockState) {
        Pair<ItemStack, Integer> data = ClawToolHandler.getDragonHarvestToolAndSlot(player, blockState);
        ItemStack dragonHarvestTool = data.getFirst();
        int toolSlot = data.getSecond();

        swapStart(player, dragonHarvestTool, toolSlot);
    }

    /**
     * Puts the relevant claw tool in the main hand and stores said main hand in the dragon state handler<br>
     * This way modded enchantments etc. which check the currently held item will be directly compatible<br>
     * <br>
     * When using this make sure you call {@link ClawInventoryData#swapFinish(Player)} to restore the initial state
     */
    public void swapStart(final Player player, final ItemStack tool, final int slot) {
        // TODO :: allow it in creative? maybe only weapon?
        if (slot == -1 || player.isCreative() || player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean sameBorrowedTool = switchedTool && switchedToolSlot == slot && ItemStack.matches(mainHand, tool);

        if (sameBorrowedTool) {
            swapFrames.add(new SwapFrame(ItemStack.EMPTY, -1, slot, false));
            toolSwapLayer = swapFrames.size();
            return;
        }

        if (!switchedTool) {
            swapFrames.add(new SwapFrame(mainHand, -1, slot, true));
            storedMainHandTool = mainHand;
            switchBorrowedTool(player, tool, slot);
            switchedTool = true;
            switchedToolSlot = slot;
            toolSwapLayer = swapFrames.size();
            return;
        }

        swapFrames.add(new SwapFrame(mainHand, switchedToolSlot, slot, true));
        removeBorrowedToolEffects(player, mainHand);
        clawsInventory.setItem(switchedToolSlot, mainHand);
        switchBorrowedTool(player, tool, slot);
        switchedToolSlot = slot;
        toolSwapLayer = swapFrames.size();
    }

    /** Puts the stored main hand back into the main hand and the claw tool into its slot */
    public void swapFinish(final Player player) {
        if (!switchedTool || swapFrames.isEmpty()) {
            toolSwapLayer = 0;
            return;
        }

        // TODO :: allow it in creative? maybe only weapon?
        if (player.isCreative() || player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        SwapFrame frame = swapFrames.removeLast();
        toolSwapLayer = swapFrames.size();

        if (toolSwapLayer < 0) {
            DragonSurvival.LOGGER.warn("Tool swap layer was lower than 0 - this should not happen");
            toolSwapLayer = 0;
        }

        if (!frame.changedHand()) {
            return;
        }

        ItemStack currentBorrowedTool = player.getItemInHand(InteractionHand.MAIN_HAND);
        removeBorrowedToolEffects(player, currentBorrowedTool);
        clawsInventory.setItem(frame.newBorrowedSlot(), currentBorrowedTool);

        if (frame.previousBorrowedSlot() == -1) {
            player.setItemInHand(InteractionHand.MAIN_HAND, frame.previousMainHand());
            storedMainHandTool = ItemStack.EMPTY;
            switchedTool = false;
            switchedToolSlot = -1;
            return;
        }

        ItemStack previousBorrowedTool = clawsInventory.getItem(frame.previousBorrowedSlot());
        player.setItemInHand(InteractionHand.MAIN_HAND, previousBorrowedTool);
        applyBorrowedToolEffects(player, previousBorrowedTool);
        clawsInventory.setItem(frame.previousBorrowedSlot(), ItemStack.EMPTY);
        switchedTool = true;
        switchedToolSlot = frame.previousBorrowedSlot();
    }

    public static void reInsertClawTools(final Player player) {
        SimpleContainer clawsContainer = ClawInventoryData.getData(player).getContainer();

        for (int i = 0; i < 4; i++) {
            ItemStack stack = clawsContainer.getItem(i);

            if (player instanceof ServerPlayer serverPlayer) {
                if (!serverPlayer.addItem(stack)) {
                    serverPlayer.level().addFreshEntity(new ItemEntity(serverPlayer.level(), serverPlayer.position().x, serverPlayer.position().y, serverPlayer.position().z, stack));
                }
            }
        }

        clawsContainer.clearContent();
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

    /** Returns the tool with the highest harvest level for said block */
    public ItemStack getTool(final BlockState state) {
        ItemStack currentTool = ItemStack.EMPTY;
        int currentLevel = 0;

        for (int slot = 0; slot < Slot.size(); slot++) {
            ItemStack tool = getContainer().getItem(slot);
            int level = ToolUtils.toolToHarvestLevel(tool);

            if (level > currentLevel && ToolUtils.isCorrectTool(tool, state)) {
                currentTool = tool;
                currentLevel = level;
            }
        }

        return currentTool;
    }

    private void switchBorrowedTool(final Player player, final ItemStack tool, final int slot) {
        player.setItemInHand(InteractionHand.MAIN_HAND, tool);
        applyBorrowedToolEffects(player, tool);
        clawsInventory.setItem(slot, ItemStack.EMPTY);
    }

    private void applyBorrowedToolEffects(final Player player, final ItemStack tool) {
        // Copied from collectEquipmentChanges() in LivingEntity.java
        tool.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = player.getAttributes().getInstance(attribute);

            if (instance != null) {
                instance.removeModifier(modifier.id());
                instance.addTransientModifier(modifier);
            }
        });

        if (player.level() instanceof ServerLevel serverLevel) {
            EnchantmentHelper.runLocationChangedEffects(serverLevel, tool, player, EquipmentSlot.MAINHAND);
        }
    }

    private void removeBorrowedToolEffects(final Player player, final ItemStack tool) {
        tool.forEachModifier(EquipmentSlot.MAINHAND, (attributeHolder, attributeModifier) -> {
            AttributeInstance attributeInstance = player.getAttributes().getInstance(attributeHolder);

            if (attributeInstance != null) {
                attributeInstance.removeModifier(attributeModifier);
            }
        });

        EnchantmentHelper.stopLocationBasedEffects(tool, player, EquipmentSlot.MAINHAND);
    }

    public void sync(final Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        TagValueOutput tagValueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
        serialize(tagValueOutput);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncDragonClawsMenu(player.getId(), isMenuOpen, tagValueOutput.buildResult()));
    }

    public static ClawInventoryData getData(final Player player) {
        return player.getData(DSDataAttachments.CLAW_INVENTORY);
    }

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {
        valueOutput.putBoolean(IS_MENU_OPEN, isMenuOpen);

        for (ClawInventoryData.Slot slot : ClawInventoryData.Slot.values()) {
            if (clawsInventory.getItem(slot.ordinal()).isEmpty()) {
                continue;
            }

            valueOutput.store(slot.name(), ItemStack.CODEC, clawsInventory.getItem(slot.ordinal()));
        }

        valueOutput.putBoolean(SHOULD_RENDER_CLAWS, shouldRenderClaws);
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        setMenuOpen(valueInput.getBooleanOr(IS_MENU_OPEN, false));
        shouldRenderClaws = valueInput.getBooleanOr(SHOULD_RENDER_CLAWS, false);

        for (ClawInventoryData.Slot slot : ClawInventoryData.Slot.values()) {
            Optional<ItemStack> stack = valueInput.read(slot.name(), ItemStack.CODEC);

            if (stack.isEmpty()) {
                continue;
            }

            clawsInventory.setItem(slot.ordinal(), stack.get());
        }
    }
}
