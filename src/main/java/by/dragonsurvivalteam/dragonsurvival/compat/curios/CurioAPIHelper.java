package by.dragonsurvivalteam.dragonsurvival.compat.curios;

import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.Map;

public class CurioAPIHelper {
    private CurioAPIHelper() { }

    public static ArrayList<ItemStack> getVisibleCurioItems(final Player player) {
        ArrayList<ItemStack> visibleCurios = new ArrayList<>();

        if (!ModID.CURIOS.isLoaded()) {
            return visibleCurios;
        }

        CuriosApi.getCuriosInventory(player)
                .map(ICuriosItemHandler::getCurios)
                .map(Map::values)
                .ifPresent(handlers -> {
                    for (ICurioStacksHandler stacksHandler : handlers) {
                        collectVisibleItems(stacksHandler, visibleCurios);
                    }
                });

        return visibleCurios;
    }

    private static void collectVisibleItems(final ICurioStacksHandler stacksHandler, final ArrayList<ItemStack> visibleCurios) {
        if (!stacksHandler.isVisible()) {
            return;
        }

        for (int slot = 0; slot < stacksHandler.getSlots(); slot++) {
            if (slot >= stacksHandler.getRenders().size() || !stacksHandler.getRenders().get(slot)) {
                continue;
            }

            ItemStack cosmetic = stacksHandler.hasCosmetic() ? stacksHandler.getCosmeticStacks().getStackInSlot(slot) : ItemStack.EMPTY;

            if (!cosmetic.isEmpty()) {
                visibleCurios.add(cosmetic);
                continue;
            }

            ItemStack equipped = stacksHandler.getStacks().getStackInSlot(slot);

            if (!equipped.isEmpty()) {
                visibleCurios.add(equipped);
            }
        }
    }
}
