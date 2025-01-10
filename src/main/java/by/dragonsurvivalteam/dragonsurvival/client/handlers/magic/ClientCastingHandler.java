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
    private static void handleCastingInputs(InputEvent.Key event) {
        Minecraft instance = Minecraft.getInstance();

        if (instance.screen != null || instance.player == null || instance.level == null) {
            return;
        }

        Player player = instance.player;

        if (player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        if (event.getAction() == InputConstants.PRESS) {
            handleVisibilityToggle(event, player);
            handleSlotSelection(event, player);
            handleCastingKey(event, player);
        } else if (event.getAction() == InputConstants.RELEASE) {
            handleCastingKeyRelease(event, player);
        }
    }

    private static void handleVisibilityToggle(InputEvent.Key event, Player player) {
        MagicData magicData = MagicData.getData(player);
        // Toggle HUD visibility
        if (Keybind.TOGGLE_ABILITIES.get().getKey().getValue() == event.getKey()) {
            magicData.setRenderAbilities(!magicData.shouldRenderAbilities());
        }
    }

    private static void handleSlotSelection(InputEvent.Key event, Player player) {
        MagicData magicData = MagicData.getData(player);

        // Check ability key
        int lastSelectedSlot = magicData.getSelectedAbilitySlot();
        // Check for slot selection
        int selectedSlot = lastSelectedSlot;
        // Check for prev/next keys first
        if (Keybind.NEXT_ABILITY.get().getKey().getValue() == event.getKey()) {
            selectedSlot = (selectedSlot + 1) % slotKeybinds.length;
        } else if (Keybind.PREVIOUS_ABILITY.get().getKey().getValue() == event.getKey()) {
            // Add length because % can return a negative remainder
            selectedSlot = (selectedSlot - 1 + slotKeybinds.length) % slotKeybinds.length;
        }

        // Select the slot of the most recently pressed ability key, check for new keypresses
        // (This overrides the prev/next keypress)
        for (int i = 0; i < slotKeybinds.length; i++) {
            if (slotKeybinds[i].consumeClick()) {
                selectedSlot = i;
            }
        }
        // Was selected slot changed?
        if (selectedSlot != lastSelectedSlot) {
            // Cancel casting if in progress
            if (magicData.isCasting()) {
                magicData.stopCasting(player);
                PacketDistributor.sendToServer(new SyncStopCast(player.getId(), false, false));
            }

            // Update slot
            magicData.setSelectedAbilitySlot(selectedSlot);
        }
    }

    private static boolean isAbilityKey(int keyCode, int selectedSlot) {
        boolean isAbilityKey;
        if (!ClientConfig.alternateCastMode) {
            isAbilityKey = Keybind.USE_ABILITY.get().getKey().getValue() == keyCode;
        } else {
            isAbilityKey = slotKeybinds.length > selectedSlot && slotKeybinds[selectedSlot].get().getKey().getValue() == keyCode;
        }

        return isAbilityKey;
    }

    private static void handleCastingKey(InputEvent.Key event, Player player) {
        MagicData magicData = MagicData.getData(player);
        int selectedSlot = magicData.getSelectedAbilitySlot();

        // Proceed with casting (ignore anything blocking the cast from happening; we'll let the server deny the client later)
        if (isAbilityKey(event.getKey(), magicData.getSelectedAbilitySlot()) && !magicData.isCasting() && magicData.attemptCast(player, selectedSlot)) {
            PacketDistributor.sendToServer(new SyncBeginCast(player.getId(), selectedSlot));
        }
    }

    private static void handleCastingKeyRelease(InputEvent.Key event, Player player) {
        MagicData magicData = MagicData.getData(player);

        // Released the ability key, stop casting
        if (isAbilityKey(event.getKey(), magicData.getSelectedAbilitySlot())) {
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