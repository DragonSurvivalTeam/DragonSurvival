package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.EmoteComponent;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEmoteKeybindings;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmote;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber(Dist.CLIENT)
public class DragonEmoteScreen extends Screen {
    @Translation(comments = "Reset all emote keybinds")
    private static final String RESET_ALL_KEYBINDS = Translation.Type.GUI.wrap("emote_screen.reset_all_keybinds");

    @Translation(comments = "Currently binding emote.")
    private static final String CURRENTLY_BINDING = Translation.Type.GUI.wrap("emote_screen.currently_binding");

    @Translation(comments = "Press escape to cancel.")
    private static final String PRESS_ESCAPE_TO_CANCEL = Translation.Type.GUI.wrap("emote_screen.press_escape_to_cancel");

    @Translation(comments = "Stop all emotes")
    private static final String STOP_ALL_EMOTES = Translation.Type.GUI.wrap("emote_screen.stop_all_emotes");

    @Translation(comments = {"■ This is the emotes menu.",
            "- The icon on the right will allow you to §6keybind§r§f and play the emote without entering this menu.",
            "- Some emotions can be combined with each other. For example, §6Blend§r§f.",
            "- Flip pages - §6mouse wheel§r§f."
    })
    private static final String EMOTE_INFO = Translation.Type.GUI.wrap("emote_screen.info");

    @Translation(comments = "■ This is a link to our §6Wiki§r§7 dedicated to making your own emote!§7")
    private static final String WIKI = Translation.Type.GUI.wrap("emote_screen.wiki");

    public static final String EMOTE_WIKI_URL = "https://github.com/DragonSurvivalTeam/DragonSurvival/wiki/9.-How-Help-or-Make-Content#-create-a-new-emotion";

    public static final int NO_KEY = -1;
    public static final int REMOVE_KEY = 256;
    private static final int PER_PAGE = 9;

    private int emotePage = 0;
    public String currentlyKeybinding = null;
    private List<EmoteComponent> emoteComponents = new ArrayList<>();
    private int guiLeft;
    private int guiTop;

    private static final ResourceLocation BACKGROUND_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/emote_main.png");

    private static final ResourceLocation DISCORD_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/discord_hover.png");
    private static final ResourceLocation DISCORD_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/discord_main.png");

    private static final ResourceLocation RESET_ALL_KEYBINDS_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/reset_all_keybinds_hover.png");
    private static final ResourceLocation RESET_ALL_KEYBINDS_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/reset_all_keybinds_main.png");

    private static final ResourceLocation RESET_EMOTES_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/reset_emotes_hover.png");
    private static final ResourceLocation RESET_EMOTES_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/reset_emotes_main.png");

    private static final ResourceLocation WIKI_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/wiki_hover.png");
    private static final ResourceLocation WIKI_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/wiki_main.png");

    private static final ResourceLocation INFO_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/info_hover.png");
    private static final ResourceLocation INFO_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/info_main.png");

    public DragonEmoteScreen() {
        super(Component.empty());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY < 0) {
            if (emotePage < (int) Math.ceil((double) DragonStateProvider.getData(minecraft.player).body().value().emotes().value().emotes().size() / PER_PAGE) - 1) {
                emotePage++;
                reinitializeEmoteComponents();
            }
        } else if (scrollY > 0) {
            if (emotePage > 0) {
                emotePage--;
                reinitializeEmoteComponents();
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
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

        RenderSystem.enableBlend();
        graphics.blit(BACKGROUND_MAIN, startX, startY, 0, 0, 256, 256);
        RenderSystem.disableBlend();

        super.render(graphics, mouseX, mouseY, partialTick);
        int totalPages = (int) Math.ceil((double) DragonStateProvider.getData(minecraft.player).body().value().emotes().value().emotes().size() / PER_PAGE);
        TextRenderUtil.drawCenteredScaledText(graphics, guiLeft + 75, guiTop + 137, 1.0f, (emotePage + 1) + "/" + totalPages, Color.white.getRGB());

        if(currentlyKeybinding != null) {
            TextRenderUtil.drawCenteredScaledText(graphics, guiLeft + 75, guiTop + 151, 1.0f, Component.translatable(CURRENTLY_BINDING).getString(), Color.red.getRGB());
            TextRenderUtil.drawCenteredScaledText(graphics, guiLeft + 75, guiTop + 161, 1.0f, Component.translatable(PRESS_ESCAPE_TO_CANCEL).getString(), Color.red.getRGB());
        }
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

        guiLeft = (width - xSize / 2) - 30;
        guiTop = (height - ySize / 2) / 2 - 20;

        int startX = guiLeft - 15;
        int startY = guiTop + 30;

        TabButton.addTabButtonsToScreen(this, startX + 17, startY - 56, TabButton.Type.EMOTES_TAB);
        reinitializeEmoteComponents();

        HoverButton resetEmotesButton = new HoverButton(startX + 19, startY - 26, 14, 14, 14, 14, RESET_EMOTES_MAIN, RESET_EMOTES_HOVER, button -> {
            //noinspection DataFlowIssue -> player is present
            AtomicReference<DragonEntity> atomicDragon = ClientDragonRenderer.playerDragonHashMap.get(Minecraft.getInstance().player.getId());

            if (atomicDragon == null) {
                return;
            }

            DragonEntity dragon = atomicDragon.get();
            dragon.stopAllEmotes();
        });
        resetEmotesButton.setTooltip(Tooltip.create(Component.translatable(STOP_ALL_EMOTES)));
        addRenderableWidget(resetEmotesButton);

        HoverButton infoButton = new HoverButton(startX + 68, startY - 26, 14, 14, 14, 14, INFO_MAIN, INFO_HOVER, button -> {});
        infoButton.setTooltip(Tooltip.create(Component.translatable(EMOTE_INFO)));
        addRenderableWidget(infoButton);

        HoverButton discordButton = new HoverButton(startX + 83, startY - 26, 14, 14, 14, 14, DISCORD_MAIN, DISCORD_HOVER, ConfirmLinkScreen.confirmLink(this, DragonSurvival.DISCORD_URL));
        discordButton.setTooltip(Tooltip.create(Component.translatable(LangKey.DISCORD)));
        addRenderableWidget(discordButton);

        HoverButton wikiButton = new HoverButton(startX + 98, startY - 26, 14, 14, 14, 14, WIKI_MAIN, WIKI_HOVER, ConfirmLinkScreen.confirmLink(this, EMOTE_WIKI_URL));
        wikiButton.setTooltip(Tooltip.create(Component.translatable(WIKI)));
        addRenderableWidget(wikiButton);

        HoverButton resetAllKeybindsButton = new HoverButton(startX + 150, startY - 26, 14, 14, 14, 14, RESET_ALL_KEYBINDS_MAIN, RESET_ALL_KEYBINDS_HOVER, button -> {
            DSEmoteKeybindings.EMOTE_KEYBINDS.clear();
            reinitializeEmoteComponents();
        });
        resetAllKeybindsButton.setTooltip(Tooltip.create(Component.translatable(RESET_ALL_KEYBINDS)));
        addRenderableWidget(resetAllKeybindsButton);
    }

    private void reinitializeEmoteComponents() {
        for(EmoteComponent emoteComponent : emoteComponents) {
            for(AbstractWidget button : emoteComponent.children()) {
                removeWidget(button);
            }
        }

        emoteComponents.clear();

        for(int i = 0; i < PER_PAGE; i++) {
            //noinspection DataFlowIssue -> player is present
            DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
            List<DragonEmote> emotes = handler.body().value().emotes().value().emotes();
            int emoteIndex = PER_PAGE * emotePage + i;
            if(emoteIndex >= emotes.size()) {
                break;
            }

            emoteComponents.add(new EmoteComponent(this, guiLeft + 10, guiTop + 30 + i * 12, emotes.get(PER_PAGE * emotePage + i)));
        }
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

    // Prevent the screen from closing when pressing escape to cancel a keybind
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(keyCode == 256 && currentlyKeybinding != null) {
            currentlyKeybinding = null;
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @SubscribeEvent
    public static void onKey(InputEvent.Key keyInputEvent) {
        Minecraft instance = Minecraft.getInstance();
        if (instance.player == null || instance.level == null) {
            return;
        }

        Player player = instance.player;
        if (player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        int keyCode = keyInputEvent.getKey();
        if (keyCode == NO_KEY) {
            return;
        }

        if (instance.screen instanceof DragonEmoteScreen emoteScreen) {
            if (emoteScreen.currentlyKeybinding != null) {
                if (keyCode == REMOVE_KEY) {
                    DSEmoteKeybindings.EMOTE_KEYBINDS.remove(emoteScreen.currentlyKeybinding);
                } else {
                    DSEmoteKeybindings.EMOTE_KEYBINDS.put(keyCode, emoteScreen.currentlyKeybinding);
                }

                for(EmoteComponent emoteComponent : emoteScreen.emoteComponents) {
                    emoteComponent.refreshKeybinding();
                }

                emoteScreen.currentlyKeybinding = null;
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
