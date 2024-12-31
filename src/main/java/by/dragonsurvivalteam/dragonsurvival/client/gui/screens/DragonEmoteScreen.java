package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEmoteKeybindings;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmote;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class DragonEmoteScreen extends Screen {
    public static final int NO_KEY = -1;
    public static final int REMOVE_KEY = 256;
    private static final int PER_PAGE = 10;

    private static int emotePage = 0;
    private static boolean keybinding = false;
    private static String currentlyKeybinding = null;
    private static final List<ExtendedButton> emoteButtons = new ArrayList<>();
    private static final List<ExtendedButton> keybindingButtons = new ArrayList<>();
    private static boolean emoteMenuOpen = false;

    private int guiLeft;
    private int guiTop;

    private static final ResourceLocation BACKGROUND_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/emote_main.png");

    public DragonEmoteScreen() {
        super(Component.empty());
    }

    @Override
    public void setFocused(@Nullable final GuiEventListener listener) {
        if (listener instanceof DragonBodyButton) {
            // Button can be clicked (and therefor focused) but there is no reason to do so in this screen
            return;
        }

        super.setFocused(listener);
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        int startX = guiLeft;
        int startY = guiTop;

        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);
        graphics.blit(BACKGROUND_MAIN, startX, startY, 0, 0, 256, 256);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Don't render the vanilla background, it darkens the UI in an undesirable way
    }

    @Override
    public void init() {
        //noinspection DataFlowIssue -> player is present
        int xSize = 256;
        int ySize = 256;

        guiLeft = (width - xSize) / 2 + 155;
        guiTop = (height - ySize / 2) / 2 - 20;

        int startX = guiLeft - 15;
        int startY = guiTop + 30;

        TabButton.addTabButtonsToScreen(this, startX + 17, startY - 56, TabButton.Type.EMOTES_TAB);
        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);
    }

    public static void addEmote(String key) {
        //noinspection DataFlowIssue -> player is present
        AtomicReference<DragonEntity> atomicDragon = ClientDragonRenderer.playerDragonHashMap.get(Minecraft.getInstance().player.getId());

        if (atomicDragon == null) {
            return;
        }

        DragonEntity dragon = atomicDragon.get();
        DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
        DragonEmote emote = handler.body().value().emotes().value().getEmote(key);
        dragon.beginPlayingEmote(emote);
        PacketDistributor.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), emote, false));
    }

    public static void addEmote(DragonEmote emote) {
        //noinspection DataFlowIssue -> player is present
        AtomicReference<DragonEntity> atomicDragon = ClientDragonRenderer.playerDragonHashMap.get(Minecraft.getInstance().player.getId());

        if (atomicDragon == null) {
            return;
        }

        DragonEntity dragon = atomicDragon.get();
        dragon.beginPlayingEmote(emote);
        PacketDistributor.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), emote, false));
    }

    public static List<DragonEmote> getShownEmotes() {
        //noinspection DataFlowIssue -> player is present
        DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
        List<DragonEmote> emotes = handler.body().value().emotes().value().emotes();
        List<DragonEmote> shownEmotes = new ArrayList<>();

        for (int index = emotePage * PER_PAGE; index < emotes.size(); index++) {
            if (shownEmotes.size() == PER_PAGE) {
                break;
            }

            shownEmotes.add(emotes.get(index));
        }

        return shownEmotes;
    }

    public static int maxPages(final List<DragonEmote> emotes) {
        return (int) Math.ceil((double) emotes.size() / PER_PAGE);
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key keyInputEvent) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        Screen screen = Minecraft.getInstance().screen;
        int keyCode = keyInputEvent.getKey();

        if (keyCode == NO_KEY) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        if (screen instanceof ChatScreen) {
            if (currentlyKeybinding != null) {
                if (keyCode == REMOVE_KEY) {
                    DSEmoteKeybindings.EMOTE_KEYBINDS.remove(currentlyKeybinding);
                } else {
                    DSEmoteKeybindings.EMOTE_KEYBINDS.put(keyCode, currentlyKeybinding);
                }

                currentlyKeybinding = null;
            }
        } else {
            String emote = DSEmoteKeybindings.EMOTE_KEYBINDS.get(keyCode);

            if (emote != null) {
                addEmote(emote);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
