package by.dragonsurvivalteam.dragonsurvival.compat.curios;

import by.dragonsurvivalteam.dragonsurvival.compat.ModCheck;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class CurioAPIHelper {
    public static ArrayList<ItemStack> getVisibleCurioItems(Player player) {
        if (ModCheck.isModLoaded(ModCheck.CURIOS)) {
            Optional<ICuriosItemHandler> items = CuriosApi.getCuriosInventory(player);
            if (items.isPresent()) {
                Map<String, ICurioStacksHandler> handler = items.get().getCurios();
                ArrayList<ItemStack> visibleCurios = new ArrayList<ItemStack>();
                for (ICurioStacksHandler stacksHandler : handler.values()) {
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        if (stacksHandler.isVisible() && stacksHandler.getRenders().get(i)) {
                            if (stacksHandler.hasCosmetic() && !stacksHandler.getCosmeticStacks().getStackInSlot(i).is(Items.AIR)) {
                                visibleCurios.add(stacksHandler.getCosmeticStacks().getStackInSlot(i));
                            } else if (!stacksHandler.getStacks().getStackInSlot(i).is(Items.AIR)) {
                                visibleCurios.add(stacksHandler.getStacks().getStackInSlot(i));
                            }
                        }
                    }
                }
                return visibleCurios;
            }
        }
        return null;
    }
}
