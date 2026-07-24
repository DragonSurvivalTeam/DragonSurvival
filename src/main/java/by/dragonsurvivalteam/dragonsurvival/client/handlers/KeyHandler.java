package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.KeyMappingAccess;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSummonedEntitiesBehaviour;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncDragonSoulPlacement;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncKey;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PlayerData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(Dist.CLIENT)
public class KeyHandler {
    @Translation(comments = "%s is now set to %s")
    public static final String CYCLED_ENUM = Translation.Type.GUI.wrap("display.cycle_enum");

    @Translation(comments = "Dragon soul placement is now enabled")
    public static final String DRAGON_SOUL_PLACEMENT_ENABLED = Translation.Type.GUI.wrap("display.toggle_dragon_soul_placement.enabled");

    @Translation(comments = "Dragon soul placement is now disabled")
    public static final String DRAGON_SOUL_PLACEMENT_DISABLED = Translation.Type.GUI.wrap("display.toggle_dragon_soul_placement.disabled");

    public static Component cycledEnum(final Enum<?> enumValue) {
        Component type = DSLanguageProvider.enumClass(enumValue);
        Component value = DSLanguageProvider.enumValue(enumValue);
        return Component.translatable(CYCLED_ENUM, type, value);
    }

    @SubscribeEvent
    public static void handleKey(final InputEvent.Key event) {
        handleKeybinds(InputConstants.getKey(event.getKey(), event.getScanCode()), event.getAction());
    }

    @SubscribeEvent
    public static void handleMouse(final InputEvent.MouseButton.Pre event) {
        handleKeybinds(InputConstants.Type.MOUSE.getOrCreate(event.getButton()), event.getAction());
    }

    private static void handleKeybinds(final InputConstants.Key input, final int action) {
        ClientFlightHandler.toggleWings(KeyHandler.checkAndGet(input, action, Keybind.TOGGLE_FLIGHT, true));
        ClientFlightHandler.triggerSpin(KeyHandler.checkAndGet(input, action, Keybind.SPIN_ABILITY, true));

        DragonDestructionHandler.toggleDestructionMode(KeyHandler.checkAndGet(input, action, Keybind.TOGGLE_LARGE_DRAGON_DESTRUCTION, true));
        DragonDestructionHandler.toggleMultiMining(KeyHandler.checkAndGet(input, action, Keybind.TOGGLE_MULTI_MINING, false));

        toggleSummonBehaviour(checkAndGet(input, action, Keybind.TOGGLE_SUMMON_BEHAVIOUR, false));
        toggleDragonSoulPlacement(checkAndGet(input, action, Keybind.TOGGLE_DRAGON_SOUL_PLACEMENT, false));

        handleKey(input, action);
    }

    public static void handleKey(final InputConstants.Key input, final int action) {
        if (Minecraft.getInstance().getConnection() == null) {
            // Player has not joined a world yet
            return;
        }

        // TODO :: trigger for KeyHeldDown or a predicate?

        // TODO :: parse registered abilities on data reload and cache the relevant keys
        //         then ignore any keys that are not relevant to abilities

        KeyMapping mapping = KeyMappingAccess.dragonSurvival$getAllKeymappings().get(input.getName());

        if (mapping != null && !mapping.isUnbound() && mapping.getKeyConflictContext().conflicts(KeyConflictContext.GUI)) {
            // Don't bother with keys that interact with GUIs
            return;
        }

        boolean isDown;

        if (action == InputConstants.PRESS && Minecraft.getInstance().screen == null) {
            // Only consider it a key press if it happens outside a GUI
            isDown = true;
        } else if (action == InputConstants.RELEASE) {
            // If a player presses a key and then opens a GUI, we need to know when the key is released
            // Meaning we may trigger effects within GUIs, but this might be the preferred approach?
            isDown = false;
        } else {
            return;
        }

        PlayerData data = Minecraft.getInstance().player.getData(DSDataAttachments.PLAYER_DATA);

        if (data.updateKey(input.getName(), isDown)) {
            PacketDistributor.sendToServer(new SyncKey(input.getName(), isDown));
        }
    }

    public static void toggleSummonBehaviour(@Nullable final Pair<Player, DragonStateHandler> data) {
        if (data == null) {
            return;
        }

        data.getFirst().getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(summonData -> {
            if (Screen.hasShiftDown()) {
                summonData.movementBehaviour = Functions.cycleEnum(summonData.movementBehaviour);
                data.getFirst().displayClientMessage(cycledEnum(summonData.movementBehaviour), true);
            } else {
                summonData.attackBehaviour = Functions.cycleEnum(summonData.attackBehaviour);
                data.getFirst().displayClientMessage(cycledEnum(summonData.attackBehaviour), true);
            }

            PacketDistributor.sendToServer(new SyncSummonedEntitiesBehaviour(summonData.attackBehaviour, summonData.movementBehaviour));
        });
    }

    public static void toggleDragonSoulPlacement(@Nullable final Pair<Player, DragonStateHandler> data) {
        if (data == null) {
            return;
        }

        PlayerData playerData = data.getFirst().getData(DSDataAttachments.PLAYER_DATA);
        playerData.enabledDragonSoulPlacement = !playerData.enabledDragonSoulPlacement;
        String message = playerData.enabledDragonSoulPlacement ? DRAGON_SOUL_PLACEMENT_ENABLED : DRAGON_SOUL_PLACEMENT_DISABLED;
        data.getFirst().displayClientMessage(Component.translatable(message), true);

        PacketDistributor.sendToServer(new SyncDragonSoulPlacement(playerData.enabledDragonSoulPlacement));
    }

    /**
     * Returns 'null' if: <br>
     * - The player has a screen open <br>
     * - The key is not {@link InputConstants#PRESS} <br>
     * - The pressed key does not match the passed keybind <br>
     * - The player is null or the player is not a dragon
     */
    public static @Nullable Pair<Player, DragonStateHandler> checkAndGet(final InputConstants.Key input, final int action, final Keybind keybind, boolean dragonOnly) {
        if (Minecraft.getInstance().screen != null || action != InputConstants.PRESS || !keybind.matches(input)) {
            return null;
        }

        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return null;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (dragonOnly && !data.isDragon()) {
            return null;
        }

        return Pair.of(player, data);
    }
}
