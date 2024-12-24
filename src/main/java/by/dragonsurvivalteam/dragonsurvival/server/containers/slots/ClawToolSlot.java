package by.dragonsurvivalteam.dragonsurvival.server.containers.slots;

import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class ClawToolSlot extends Slot {
    static final ResourceLocation AXE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "gui/dragon_claws_axe");
    static final ResourceLocation PICKAXE_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "gui/dragon_claws_pickaxe");
    static final ResourceLocation SHOVEL_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "gui/dragon_claws_shovel");
    static final ResourceLocation SWORD_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "gui/dragon_claws_sword");
    private final DragonContainer dragonContainer;
    private final int clawSlot;

    public ClawToolSlot(final DragonContainer dragonContainer, final Container container, int index, int x, int y, int clawSlot) {
        super(container, index, x, y);
        this.dragonContainer = dragonContainer;
        this.clawSlot = clawSlot;
    }

    @Override
    public boolean mayPlace(@NotNull final ItemStack itemStack) {
        // FIXME -> handle through container event or sth. like that (ItemStackedOnOtherEvent?)
//        if (DragonPenaltyHandler.itemIsBlacklisted(itemStack.getItem())) return false;
        return switch (ClawInventoryData.Slot.values()[clawSlot]) {
            case SWORD -> ToolUtils.isWeapon(itemStack);
            case PICKAXE -> ToolUtils.isPickaxe(itemStack);
            case AXE -> ToolUtils.isAxe(itemStack);
            case SHOVEL -> ToolUtils.isShovel(itemStack);
        };
    }

    @Override
    public void set(@NotNull final ItemStack itemStack) {
        super.set(itemStack);
        syncSlots();
    }

    @Nullable @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, clawSlot == 0 ? SWORD_TEXTURE : clawSlot == 2 ? AXE_TEXTURE : clawSlot == 1 ? PICKAXE_TEXTURE : SHOVEL_TEXTURE);
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        ItemStack stack = super.remove(amount);
        syncSlots();
        return stack;
    }

    @Override
    public boolean isActive() {
        return dragonContainer.menuStatus == 1;
    }

    private void syncSlots() {
        if (!dragonContainer.player.level().isClientSide()) {
            ClawInventoryData data = ClawInventoryData.getData(dragonContainer.player);
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(dragonContainer.player, new SyncDragonClawsMenu.Data(dragonContainer.player.getId(), data.isMenuOpen(), data.serializeNBT(dragonContainer.player.registryAccess())));
        }
    }
}