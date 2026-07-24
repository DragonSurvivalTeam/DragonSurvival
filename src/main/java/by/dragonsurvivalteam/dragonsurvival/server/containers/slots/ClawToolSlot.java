package by.dragonsurvivalteam.dragonsurvival.server.containers.slots;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClawToolSlot extends Slot {
    private final DragonContainer dragonContainer;
    private final ClawInventoryData.Slot slot;

    public ClawToolSlot(final DragonContainer dragonContainer, final Container container, int index, int x, int y, final ClawInventoryData.Slot slot) {
        super(container, index, x, y);
        this.dragonContainer = dragonContainer;
        this.slot = slot;
    }

    @Override
    public boolean mayPlace(@NotNull final ItemStack itemStack) {
        return switch (slot) {
            case SWORD -> ToolUtils.isWeapon(itemStack);
            case PICKAXE -> ToolUtils.isPickaxe(itemStack);
            case AXE -> ToolUtils.isAxe(itemStack);
            case SHOVEL -> ToolUtils.isShovel(itemStack);
        };
    }

    @Override
    public void set(@NotNull final ItemStack itemStack) {
        super.set(itemStack);
        ClawInventoryData.getData(dragonContainer.player).sync(dragonContainer.player);
    }

    @Override
    public @Nullable Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return Pair.of(InventoryMenu.BLOCK_ATLAS, slot.getEmptyTexture());
    }

    @Override
    public @NotNull ItemStack remove(int amount) {
        ItemStack stack = super.remove(amount);
        ClawInventoryData.getData(dragonContainer.player).sync(dragonContainer.player);
        return stack;
    }

    @Override
    public boolean isActive() {
        return dragonContainer.menuStatus == 1;
    }
}