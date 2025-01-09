package by.dragonsurvivalteam.dragonsurvival.client.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncBeginCast;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncStopCast;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(Dist.CLIENT)
public class ClientCastingHandler {
    private static final Keybind[] slotKeybinds = new Keybind[]{
            Keybind.ABILITY1,
            Keybind.ABILITY2,
            Keybind.ABILITY3,
            Keybind.ABILITY4
    };

    @SubscribeEvent
    private static void handleCastingInputs(final InputEvent.Key event) {
        Minecraft instance = Minecraft.getInstance();

        if (instance.screen != null || instance.player == null || instance.level == null) {
            return;
        }

        Player player = instance.player;

        if (player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        if (event.getAction() == InputConstants.PRESS) {
            handleVisibilityToggle(player);
            handleSlotSelection(player);
            handleCastingKey(player);
        } else if (event.getAction() == InputConstants.RELEASE) {
            handleCastingKeyRelease(player);
        }
    }

    private static void handleVisibilityToggle(final Player player) {
        MagicData magicData = MagicData.getData(player);
        // Toggle HUD visibility
        if (Keybind.TOGGLE_ABILITIES.consumeClick()) {
            magicData.setRenderAbilities(!magicData.shouldRenderAbilities());
        }
    }

    private static void handleSlotSelection(final Player player) {
        MagicData magicData = MagicData.getData(player);

        int lastSelectedSlot = magicData.getSelectedAbilitySlot();
        int selectedSlot = lastSelectedSlot;

        if (Keybind.NEXT_ABILITY.consumeClick()) {
            selectedSlot = (selectedSlot + 1) % slotKeybinds.length;
        } else if (Keybind.PREVIOUS_ABILITY.consumeClick()) {
            // Add length because % can return a negative remainder
            selectedSlot = (selectedSlot - 1 + slotKeybinds.length) % slotKeybinds.length;
        }

        // (This overrides the previous / next key press)
        for (int i = 0; i < slotKeybinds.length; i++) {
            if (slotKeybinds[i].consumeClick()) {
                selectedSlot = i;
                break;
            }
        }

        if (selectedSlot != lastSelectedSlot) {
            if (magicData.isCasting()) {
                magicData.stopCasting(player);
                PacketDistributor.sendToServer(new SyncStopCast(player.getId(), false, false));
            }

            magicData.setSelectedAbilitySlot(selectedSlot);
        }
    }

    private static boolean isAbilityKey(int selectedSlot) {
        boolean isAbilityKey;

        if (!ClientConfig.alternateCastMode) {
            isAbilityKey = Keybind.USE_ABILITY.consumeClick();
        } else {
            isAbilityKey = slotKeybinds.length > selectedSlot && slotKeybinds[selectedSlot].consumeClick();
        }

        return isAbilityKey;
    }

    private static void handleCastingKey(final Player player) {
        MagicData magicData = MagicData.getData(player);
        int selectedSlot = magicData.getSelectedAbilitySlot();

        // Proceed with casting (ignore anything blocking the cast from happening; we'll let the server deny the client later)
        if (isAbilityKey(magicData.getSelectedAbilitySlot()) && !magicData.isCasting() && magicData.attemptCast(player, selectedSlot)) {
            PacketDistributor.sendToServer(new SyncBeginCast(player.getId(), selectedSlot));
        }
    }

    private static void handleCastingKeyRelease(final Player player) {
        MagicData magicData = MagicData.getData(player);

        // Released the ability key, stop casting
        if (isAbilityKey(magicData.getSelectedAbilitySlot())) {
            if (magicData.isCasting()) {
                magicData.stopCasting(player);
                PacketDistributor.sendToServer(new SyncStopCast(player.getId(), false, false));
            }

            // Now that the player has released the ability key, we can allow them to attempt to cast again and reset the error message
            magicData.setCastWasDenied(false);
            magicData.setErrorMessageSent(false);
        }
    }
}