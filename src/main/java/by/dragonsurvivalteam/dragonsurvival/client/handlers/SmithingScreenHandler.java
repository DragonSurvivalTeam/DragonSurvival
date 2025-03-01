package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(Dist.CLIENT)
public class SmithingScreenHandler {
    public static int FAKE_PLAYER = 1;

    @SubscribeEvent
    public static void clearArmor(final ScreenEvent.Closing event) {
        if (event.getScreen() instanceof SmithingScreen) {
            //noinspection DataFlowIssue -> player is present
            DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);

            if (handler.isDragon()) {
                clearEquipment(FakeClientPlayerUtils.getFakePlayer(FAKE_PLAYER, handler));
            }
        }
    }

    public static void copyEquipment(final Player source, final Player target) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            target.setItemSlot(slot, source.getItemBySlot(slot).copy());
        }
    }

    private static void clearEquipment(final Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}
