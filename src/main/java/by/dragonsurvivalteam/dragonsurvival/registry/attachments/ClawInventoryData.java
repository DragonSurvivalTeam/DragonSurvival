package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public class ClawInventoryData implements INBTSerializable<CompoundTag> {
    public static final String IS_MENU_OPEN = "is_menu_open";
    public static final String SHOULD_RENDER_CLAWS = "should_render_claws";

    public enum Slot implements StringRepresentable {
        SWORD("gui/dragon_claws_sword"),
        PICKAXE("gui/dragon_claws_pickaxe"),
        AXE("gui/dragon_claws_axe"),
        SHOVEL("gui/dragon_claws_shovel");

        public static Codec<Slot> CODEC = StringRepresentable.fromValues(Slot::values);
        private final ResourceLocation emptyTexture;

        Slot(final String path) {
            emptyTexture = DragonSurvival.res(path);
        }

        public ResourceLocation getEmptyTexture() {
            return emptyTexture;
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
    private boolean isMenuOpen = true;
    /** To track the state if a tool swap is triggered within a tool swap (should only swap back if the last tool swap finishes) */
    private int toolSwapLayer;

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

        if (!switchedTool) {
            player.setItemInHand(InteractionHand.MAIN_HAND, tool);

            // Copied from collectEquipmentChanges() in LivingEntity.java
            tool.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                AttributeInstance instance = player.getAttributes().getInstance(attribute);

                if (instance != null) {
                    instance.removeModifier(modifier.id());
                    instance.addTransientModifier(modifier);
                }

                if (player.level() instanceof ServerLevel serverlevel) {
                    EnchantmentHelper.runLocationChangedEffects(serverlevel, tool, player, EquipmentSlot.MAINHAND);
                }
            });

            clawsInventory.setItem(slot, ItemStack.EMPTY);
            storedMainHandTool = mainHand;
            switchedTool = true;
            switchedToolSlot = slot;
        }

        toolSwapLayer++;
    }

    /** Puts the stored main hand back into the main hand and the claw tool into its slot */
    public void swapFinish(final Player player) {
        if (!switchedTool) {
            toolSwapLayer = 0;
            return;
        }

        // TODO :: allow it in creative? maybe only weapon?
        if (player.isCreative() || player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        toolSwapLayer--;

        if (toolSwapLayer < 0) {
            DragonSurvival.LOGGER.warn("Tool swap layer was lower than 0 - this should not happen");
            toolSwapLayer = 0;
        }

        if (toolSwapLayer == 0) {
            ItemStack originalMainHand = storedMainHandTool;
            ItemStack originalToolSlot = player.getItemInHand(InteractionHand.MAIN_HAND);

            // Copied from collectEquipmentChanges() in LivingEntity.java
            originalToolSlot.forEachModifier(EquipmentSlot.MAINHAND, (attributeHolder, attributeModifier) -> {
                AttributeInstance attributeinstance = player.getAttributes().getInstance(attributeHolder);
                if (attributeinstance != null) {
                    attributeinstance.removeModifier(attributeModifier);
                }

                EnchantmentHelper.stopLocationBasedEffects(originalToolSlot, player, EquipmentSlot.MAINHAND);
            });

            player.setItemInHand(InteractionHand.MAIN_HAND, originalMainHand);

            clawsInventory.setItem(switchedToolSlot, originalToolSlot);
            storedMainHandTool = ItemStack.EMPTY;
            switchedTool = false;
            switchedToolSlot = -1;
        }
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

    public void sync(final Player player) {
        if (player.level().isClientSide()) {
            return;
        }

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncDragonClawsMenu(player.getId(), isMenuOpen, serializeNBT(player.registryAccess())));
    }

    public static ClawInventoryData getData(final Player player) {
        return player.getData(DSDataAttachments.CLAW_INVENTORY);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(IS_MENU_OPEN, isMenuOpen);

        for (ClawInventoryData.Slot slot : ClawInventoryData.Slot.values()) {
            if (clawsInventory.getItem(slot.ordinal()).isEmpty()) {
                continue;
            }

            tag.put(slot.name(), clawsInventory.getItem(slot.ordinal()).save(provider));
        }

        tag.putBoolean(SHOULD_RENDER_CLAWS, shouldRenderClaws);

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        setMenuOpen(tag.getBoolean(IS_MENU_OPEN));
        shouldRenderClaws = tag.getBoolean(SHOULD_RENDER_CLAWS);

        for (ClawInventoryData.Slot slot : ClawInventoryData.Slot.values()) {
            CompoundTag slotTag = tag.getCompound(slot.name());

            if (slotTag.isEmpty()) {
                continue;
            }

            Optional<ItemStack> stack = ItemStack.parse(provider, slotTag);

            if (stack.isEmpty()) {
                continue;
            }

            clawsInventory.setItem(slot.ordinal(), stack.get());
        }
    }
}
