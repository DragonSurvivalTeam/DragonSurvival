package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.editor_part_selector;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.ScrollableComponent;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class EditorPartComponent implements ScrollableComponent {
    private static final ResourceLocation DROPDOWN_BUTTON_BACKGROUND = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/part_name_background.png");
    private static final ResourceLocation SMALL_LEFT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_left_arrow_hover.png");
    private static final ResourceLocation SMALL_LEFT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_left_arrow_main.png");
    private static final ResourceLocation SMALL_RIGHT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_right_arrow_hover.png");
    private static final ResourceLocation SMALL_RIGHT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_right_arrow_main.png");

    private final DragonEditorScreen screen;
    private final HoverButton partButton;
    private final ColorSelectorButton colorSelectorButton;

    private final SkinLayer skinLayer;
    private String selectedPart;

    public EditorPartComponent(final DragonEditorScreen screen, int xPos, int yPos, final String partKey, final SkinLayer skinLayer, boolean isLeft, boolean isTop) {
        this.screen = screen;
        this.selectedPart = partKey;
        this.skinLayer = skinLayer;

        partButton = new HoverButton(xPos, yPos, 110, 19, 149, 22, DROPDOWN_BUTTON_BACKGROUND, DROPDOWN_BUTTON_BACKGROUND, button -> { /* Nothing to do*/ }){
            @Override
            public boolean isValidClickButton(int button) {
                return button == 1 && skinLayer != SkinLayer.BASE;
            }

            @Override
            public void onClick(double mouseX, double mouseY, int button) {
                setSelectedPartInternal(DefaultPartLoader.NO_PART);
            }
        };
        partButton.setMessage(Component.translatable(DragonEditorScreen.partToTranslation(partKey)));
        partButton.setTooltip(Tooltip.create(Component.translatable(skinLayer.getTranslatedName())));
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(partButton);

        // Left arrow
        ((ScreenAccessor)screen).dragonSurvival$addRenderableWidget(new HoverButton(xPos - 7, yPos + 2, 9, 16, 20, 20, SMALL_LEFT_ARROW_MAIN, SMALL_LEFT_ARROW_HOVER, button -> {
            List<String> partsFromLayer = screen.getPartsFromLayer(skinLayer);
            int currentPart = partsFromLayer.indexOf(this.selectedPart);

            if (currentPart < 0) {
                setSelectedPartInternal(partsFromLayer.getLast());
            } else {
                if (currentPart - 1 < 0) {
                    if (skinLayer != SkinLayer.BASE) {
                        setSelectedPartInternal(DefaultPartLoader.NO_PART);
                    } else {
                        setSelectedPartInternal(partsFromLayer.getLast());
                    }
                } else {
                    setSelectedPartInternal(partsFromLayer.get(currentPart - 1));
                }
            }
        }));

        // Right arrow
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(new HoverButton(xPos + 108, yPos + 2, 9, 16, 20, 20, SMALL_RIGHT_ARROW_MAIN, SMALL_RIGHT_ARROW_HOVER, button -> {
            List<String> partsFromLayer = screen.getPartsFromLayer(skinLayer);
            int currentPart = partsFromLayer.indexOf(this.selectedPart);

            if (currentPart < 0) {
                setSelectedPartInternal(partsFromLayer.getFirst());
            } else {
                if (currentPart + 1 >= partsFromLayer.size()) {
                    if (skinLayer != SkinLayer.BASE) {
                        setSelectedPartInternal(DefaultPartLoader.NO_PART);
                    } else {
                        setSelectedPartInternal(partsFromLayer.getFirst());
                    }
                } else {
                    setSelectedPartInternal(partsFromLayer.get(currentPart + 1));
                }
            }
        }));

        colorSelectorButton = new ColorSelectorButton(screen, skinLayer, isLeft ? xPos - 23 : xPos + 120, yPos + 3, 15, 15, isLeft, isTop);
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(colorSelectorButton);
    }

    public boolean colorSelectorIsToggled () {
        return colorSelectorButton.toggled;
    }

    public ColorSelectorButton getColorSelectorButton() {
        return colorSelectorButton;
    }

    @Override
    public void scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!partButton.isMouseOver(mouseX, mouseY)) {
            return;
        }

        List<String> parts = screen.getPartsFromLayer(skinLayer);
        int currentPart = parts.indexOf(selectedPart);

        if (scrollY < 0) {
            if (currentPart < 0) {
                setSelectedPartInternal(parts.getLast());
            } else if (currentPart - 1 < 0) {
                if (skinLayer != SkinLayer.BASE) {
                    setSelectedPartInternal(DefaultPartLoader.NO_PART);
                } else {
                    setSelectedPartInternal(parts.getLast());
                }
            } else {
                setSelectedPartInternal(parts.get(currentPart - 1));
            }
        } else if (scrollY > 0) {
            if (currentPart < 0) {
                setSelectedPartInternal(parts.getFirst());
            } else if (currentPart + 1 >= parts.size()) {
                if (skinLayer != SkinLayer.BASE) {
                    setSelectedPartInternal(DefaultPartLoader.NO_PART);
                } else {
                    setSelectedPartInternal(parts.getFirst());
                }
            } else {
                setSelectedPartInternal(parts.get(currentPart + 1));
            }
        }
    }

    public void setSelectedPart(final String partKey) {
        selectedPart = partKey;
        partButton.setMessage(Component.translatable(DragonEditorScreen.partToTranslation(partKey)));
    }

    private void setSelectedPartInternal(final String partKey) {
        if (selectedPart.equals(partKey)) {
            return;
        }

        screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(screen.dragonPartSelectAction, new Pair<>(skinLayer, partKey)));
    }
}
