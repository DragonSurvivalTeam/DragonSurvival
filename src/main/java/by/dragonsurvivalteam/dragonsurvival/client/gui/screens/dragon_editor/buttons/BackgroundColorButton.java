package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.BackgroundColorSelectorComponent;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class BackgroundColorButton extends ExtendedButton {
    @Translation(comments = "Change the background color")
    private static final String BACKGROUND_COLOR = Translation.Type.GUI.wrap("dragon_editor.background_color");

    private static final ResourceLocation BACKGROUND_COLOR_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/gui_color_main.png");
    private static final ResourceLocation BACKGROUND_COLOR_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/editor/gui_color_hover.png");

    private final DragonEditorScreen screen;
    private BackgroundColorSelectorComponent colorComponent;
    private Renderable renderButton;
    public boolean toggled;

    public BackgroundColorButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler, DragonEditorScreen dragonEditorScreen) {
        super(xPos, yPos, width, height, displayString, handler);
        screen = dragonEditorScreen;
        setTooltip(Tooltip.create(Component.translatable(BACKGROUND_COLOR)));
    }

    public @NotNull List<? extends GuiEventListener> childrenAndSelf() {
        return ImmutableList.of(colorComponent, this, colorComponent.children().getFirst(), colorComponent.children().getLast());
    }

    @Override
    public void onPress() {
        if (!toggled) {
            renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), button -> { /* Nothing to do */ }) {
                @Override
                public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
                    active = visible = false;

                    if (colorComponent != null) {
                        colorComponent.visible = BackgroundColorButton.this.visible;
                        if (colorComponent.visible) {
                            colorComponent.render(guiGraphics, p_230430_2_, p_230430_3_, p_230430_4_);
                        }
                    }
                }
            };

            colorComponent = new BackgroundColorSelectorComponent(this.screen, getX() - 60, getY() - height - 50, 80, 70);
            screen.renderables.add(renderButton);
            colorComponent.children().forEach(listener -> ((ScreenAccessor) screen).dragonSurvival$children().add(listener));
        } else {
            colorComponent.children().forEach(component -> screen.children().removeIf(other -> component == other));
            screen.children().removeIf(listener -> listener == colorComponent);
            screen.renderables.removeIf(renderable -> renderable == renderButton);
        }

        toggled = !toggled;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial) {
        active = !screen.preset.get(screen.stage.getKey()).get().defaultSkin;

        if (toggled && (!visible || !isMouseOver(mouseX, mouseY) && (colorComponent == null || !colorComponent.isMouseOver(mouseX, mouseY)))) {
            toggled = false;
            colorComponent.children().forEach(component -> screen.children().removeIf(other -> component == other));
            screen.children().removeIf(s -> s == colorComponent);
            screen.renderables.removeIf(s -> s == renderButton);
        }

        if (visible) {
            if(isHovered()) {
                guiGraphics.blit(BACKGROUND_COLOR_HOVER, getX(), getY(), 0, 0, width, height, width, height);
            } else {
                guiGraphics.blit(BACKGROUND_COLOR_MAIN, getX(), getY(), 0, 0, width, height, width, height);
            }
        }
    }
}