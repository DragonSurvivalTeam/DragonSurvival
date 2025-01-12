package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.editor_part_selector;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonPart;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ColorSelectorButton extends ExtendedButton {
    public final SkinLayer layer;

    private final DragonEditorScreen screen;
    private HueSelectorComponent hueComponent;
    private Renderable renderButton;

    private final int xSize;
    public boolean toggled;
    private final boolean opensRight;
    private final boolean opensUp;

    private static final ResourceLocation BUTTON_HUE_UNCHANGED = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/hue_unchanged.png");
    private static final ResourceLocation BUTTON_HUE_CHANGED = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/hue_changed.png");

    public ColorSelectorButton(DragonEditorScreen screen, SkinLayer layer, int x, int y, int xSize, int ySize, boolean opensRight, boolean opensUp) {
        super(x, y, xSize, ySize, Component.empty(), action -> { /* Nothing to do */ });
        this.xSize = xSize;
        this.screen = screen;
        this.layer = layer;
        this.opensRight = opensRight;
        this.opensUp = opensUp;
        visible = true;
    }

    public HueSelectorComponent getHueComponent() {
        return hueComponent;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        active = !screen.preset.get(Objects.requireNonNull(screen.dragonStage.getKey())).get().defaultSkin;

        DragonPart part = DragonEditorHandler.getDragonPart(layer, screen.preset.get(screen.dragonStage.getKey()).get().layerSettings.get(layer).get().partKey, DragonEditorScreen.HANDLER.speciesKey());
        visible = part != null && !Objects.equals(part.key(), DefaultPartLoader.NO_PART) &&  part.isColorable();

        if (visible) {
            LayerSettings layerSettings = screen.preset.get(Objects.requireNonNull(screen.dragonStage.getKey())).get().layerSettings.get(layer).get();
            if(layerSettings.modifiedColor || layerSettings.glowing) {
                guiGraphics.blit(BUTTON_HUE_CHANGED, getX(), getY(), 0, 0, width, height, width, height);
            } else {
                guiGraphics.blit(BUTTON_HUE_UNCHANGED, getX(), getY(), 0, 0, width, height, width, height);
            }
        }

        if (toggled && (!visible || !isMouseOver(mouseX, mouseY) && (hueComponent == null || !hueComponent.isMouseOver(mouseX, mouseY)))) {
            toggled = false;
            screen.children().removeIf(s -> s == hueComponent);
            screen.renderables.removeIf(s -> s == renderButton);
        }
    }

    @Override
    public @NotNull Component getMessage() {
        return Component.empty();
    }

    @Override
    public void onPress() {
        if (!toggled) {
            DragonPart part = DragonEditorHandler.getDragonPart(layer, screen.preset.get(screen.dragonStage.getKey()).get().layerSettings.get(layer).get().partKey, DragonEditorScreen.HANDLER.speciesKey());

            if (part == null) {
                return;
            }

            renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), action -> { /* Nothing to do */ }) {
                @Override
                public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
                    active = visible = false;

                    if (hueComponent != null) {
                        hueComponent.visible = ColorSelectorButton.this.visible;

                        if (hueComponent.visible) {
                            hueComponent.render(guiGraphics, p_230430_2_, p_230430_3_, p_230430_4_);
                        }
                    }
                }
            };

            int xOffset;
            if(opensRight) {
                xOffset = getX() + xSize;
            } else {
                xOffset = getX() - 120;
            }

            int yOffset;
            if(opensUp) {
                yOffset = 0;
            } else {
                yOffset = -60;
            }

            hueComponent = new HueSelectorComponent(this.screen, xOffset, getY() + yOffset, 120, 90, layer);
            ((ScreenAccessor) screen).dragonSurvival$children().addFirst(hueComponent);
            ((ScreenAccessor) screen).dragonSurvival$children().add(hueComponent);
            screen.renderables.add(renderButton);
        } else {
            screen.children().removeIf(s -> s == hueComponent);
            screen.renderables.removeIf(s -> s == renderButton);
        }

        toggled = !toggled;
    }
}