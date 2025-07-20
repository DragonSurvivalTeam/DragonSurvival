package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonEmoteScreen;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEmoteKeybindings;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmote;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EmoteComponent {
    @Translation(comments = "Bound to: %s")
    private static final String BOUND_TO = Translation.Type.GUI.wrap("emotes.bound_to");

    @Translation(comments = "Set Keybind")
    private static final String UNBOUND = Translation.Type.GUI.wrap("emotes.unbound");

    private static final ResourceLocation PLAY_OFF = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/play_off.png");
    private static final ResourceLocation PLAY_ON = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/play_on.png");

    private static final ResourceLocation KEYBIND_OFF = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/keybind_off.png");
    private static final ResourceLocation KEYBIND_ON = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/emote/keybind_on.png");

    private final ExtendedButton isPlayingButton;
    private final ExtendedButton keybindingButton;
    private final ExtendedButton emoteButton;

    private final DragonEmote emote;

    public EmoteComponent(final DragonEmoteScreen screen, int xPos, int yPos, DragonEmote emote) {
        this.emote = emote;
        emoteButton = new ExtendedButton(xPos + 10, yPos - 3, 115, 12, Component.empty(), button -> {
            //noinspection DataFlowIssue -> player is present
            DragonEntity dragon = ClientDragonRenderer.getDragon(Minecraft.getInstance().player);

            if (dragon == null) {
                return;
            }

            if (dragon.isPlayingEmote(emote)) {
                dragon.stopEmote(emote);
                PacketDistributor.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), emote, true));
            } else {
                dragon.beginPlayingEmote(emote);
                PacketDistributor.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), emote, false));
            }
        }) {
            @Override
            public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                // Copied from ExtendedButton#renderWidget, we only want to render the text for this one
                final FormattedText buttonText = Minecraft.getInstance().font.ellipsize(this.getMessage(), this.width + 26); // Remove 6 pixels so that the text is always contained within the button's borders
                int color;
                if (this.isHovered()) {
                    color = 0xFFFFA0;
                } else {
                    color = getFGColor();
                }
                TextRenderUtil.drawScaledText(guiGraphics, this.getX(), this.getY() + (float) (this.height - 8) / 2, 0.8f, buttonText.getString(), color);
            }
        };
        emoteButton.setMessage(emote.name());
        isPlayingButton = new ExtendedButton(xPos, yPos - 1, 6, 6, Component.empty(), button -> {
            //noinspection DataFlowIssue -> player is present
            DragonEntity dragon = ClientDragonRenderer.getDragon(Minecraft.getInstance().player);

            if (dragon == null) {
                return;
            }

            if (dragon.isPlayingEmote(emote)) {
                dragon.stopEmote(emote);
                PacketDistributor.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), emote, true));
            } else {
                dragon.beginPlayingEmote(emote);
                PacketDistributor.sendToServer(new SyncEmote(Minecraft.getInstance().player.getId(), emote, false));
            }
        }) {
            @Override
            public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                ResourceLocation texture = DragonSurvival.PROXY.isPlayingEmote(Minecraft.getInstance().player, emote) ? PLAY_ON : PLAY_OFF;
                guiGraphics.blit(texture, getX(), getY(), 0, 0, 6, 6, 14, 14);
            }
        };
        keybindingButton = new ExtendedButton(xPos + 126, yPos - 1, 6, 6, Component.empty(), button -> {
            screen.currentlyKeybinding = emote.key();
        }) {
            @Override
            public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                ResourceLocation texture = emote.key().equals(screen.currentlyKeybinding) ? KEYBIND_ON : KEYBIND_OFF;
                guiGraphics.blit(texture, getX(), getY(), 0, 0, 6, 6, 14, 14);
            }
        };
        refreshKeybinding();

        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(isPlayingButton);
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(keybindingButton);
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(emoteButton);
    }

    public void refreshKeybinding() {
        int keyCode = DSEmoteKeybindings.EMOTE_KEYBINDS.getKey(emote.key());
        if (keyCode != DragonEmoteScreen.NO_KEY) {
            InputConstants.Key input = InputConstants.Type.KEYSYM.getOrCreate(keyCode);
            // Input.getDisplayName() doesn't work correctly for the mouse buttons (M1, M2, ... M5) so handle them manually here
            Component displayName = input.getValue() > 4 ? input.getDisplayName() : Component.literal("M" + (input.getValue() + 1));
            keybindingButton.setTooltip(Tooltip.create(Component.translatable(BOUND_TO, displayName)));
        } else {
            keybindingButton.setTooltip(Tooltip.create(Component.translatable(UNBOUND)));
        }
    }

    public List<AbstractWidget> children() {
        return List.of(isPlayingButton, keybindingButton, emoteButton);
    }
}
