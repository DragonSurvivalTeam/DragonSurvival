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
    private static void handleMouseInput(final InputEvent.MouseButton.Pre event) {
        handleCastingInput(InputConstants.Type.MOUSE.getOrCreate(event.getButton()), event.getAction());
    }

    @SubscribeEvent
    private static void handleKeyInput(final InputEvent.Key event) {
        handleCastingInput(InputConstants.getKey(event.getKey(), event.getScanCode()), event.getAction());
    }

    private static void handleCastingInput(final InputConstants.Key input, final int action) {
        Minecraft instance = Minecraft.getInstance();

        if (instance.screen != null || instance.player == null || instance.level == null) {
            return;
        }

        Player player = instance.player;

        if (player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        if (action == InputConstants.PRESS) {
            handleVisibilityToggle(player, input);
            handleSlotSelection(player, input);
            beginCast(player, input);
        } else if (action == InputConstants.RELEASE) {
            stopCast(player, input);
        }
    }

    private static void handleVisibilityToggle(final Player player, final InputConstants.Key input) {
        MagicData magicData = MagicData.getData(player);
        // Toggle HUD visibility
        if (Keybind.TOGGLE_ABILITIES.matches(input)) {
            magicData.setRenderAbilities(!magicData.shouldRenderAbilities());
        }
    }

    private static void handleSlotSelection(final Player player, final InputConstants.Key input) {
        MagicData magicData = MagicData.getData(player);

        int lastSelectedSlot = magicData.getSelectedAbilitySlot();
        int selectedSlot = lastSelectedSlot;

        if (Keybind.NEXT_ABILITY.matches(input)) {
            selectedSlot = (selectedSlot + 1) % slotKeybinds.length;
        } else if (Keybind.PREVIOUS_ABILITY.matches(input)) {
            // Add length because % can return a negative remainder
            selectedSlot = (selectedSlot - 1 + slotKeybinds.length) % slotKeybinds.length;
        }

        // (This overrides the previous / next key press)
        for (int i = 0; i < slotKeybinds.length; i++) {
            if (slotKeybinds[i].matches(input)) {
                selectedSlot = i;
                break;
            }
        }

        if (selectedSlot != lastSelectedSlot) {
            if (magicData.isCasting()) {
                magicData.stopCasting(player);
                PacketDistributor.sendToServer(new SyncStopCast(player.getId(), false));
            }

            magicData.setSelectedAbilitySlot(selectedSlot);
        }
    }

    /** Starts the cast if the relevant key is pressed */
    private static void beginCast(final Player player, final InputConstants.Key input) {
        MagicData magicData = MagicData.getData(player);
        int selectedSlot = magicData.getSelectedAbilitySlot();

        // Proceed with casting (ignore anything blocking the cast from happening; we'll let the server deny the client later)
        if (getKey(magicData.getSelectedAbilitySlot()).matches(input) && !magicData.isCasting() && magicData.attemptCast(player, selectedSlot)) {
            PacketDistributor.sendToServer(new SyncBeginCast(player.getId(), selectedSlot));
        }
    }

    /** Stops the cast if the relevant key is released */
    private static void stopCast(final Player player, final InputConstants.Key input) {
        MagicData magicData = MagicData.getData(player);

        if (getKey(magicData.getSelectedAbilitySlot()).isReleased(input)) {
            if (magicData.isCasting()) {
                magicData.stopCasting(player);
                PacketDistributor.sendToServer(new SyncStopCast(player.getId(), false));
            }

            magicData.setErrorMessageSent(false);
        }
    }

    private static Keybind getKey(int selectedSlot) {
        if (ClientConfig.alternateCastMode) {
            return slotKeybinds[selectedSlot];
        } else {
            return Keybind.USE_ABILITY;
        }
    }
}