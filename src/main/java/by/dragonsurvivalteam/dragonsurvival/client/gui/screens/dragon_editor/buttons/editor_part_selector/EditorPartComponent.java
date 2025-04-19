package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.editor_part_selector;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.ScrollableComponent;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader.DefaultPartLoader;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonPart;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditorPartComponent implements ScrollableComponent {
    private static final ResourceLocation DROPDOWN_BUTTON_BACKGROUND = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/part_name_background.png");
    private static final ResourceLocation SMALL_LEFT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_left_arrow_hover.png");
    private static final ResourceLocation SMALL_LEFT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_left_arrow_main.png");
    private static final ResourceLocation SMALL_RIGHT_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_right_arrow_hover.png");
    private static final ResourceLocation SMALL_RIGHT_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/editor/small_right_arrow_main.png");

    private final DragonEditorScreen screen;
    private final HoverButton partButton;
    private final ColorSelectorButton colorSelectorButton;

    private final Map<String, DragonPart> parts;
    private final List<String> sortedPartKeys;
    private final SkinLayer skinLayer;
    private String selectedPart;

    public EditorPartComponent(final DragonEditorScreen screen, int xPos, int yPos, final Map<String, DragonPart> parts, final String partKey, final SkinLayer skinLayer, boolean isLeft, boolean isTop) {
        this.screen = screen;

        this.parts = parts;
        this.sortedPartKeys = parts.keySet().stream().sorted().toList();
        this.selectedPart = partKey;
        this.skinLayer = skinLayer;

        partButton = new HoverButton(xPos, yPos, 110, 19, 149, 22, DROPDOWN_BUTTON_BACKGROUND, DROPDOWN_BUTTON_BACKGROUND, button -> { /* Nothing to do*/ }) {
            @Override
            public boolean isValidClickButton(int button) {
                return button == 1 && skinLayer != SkinLayer.BASE;
            }

            @Override
            public void onClick(double mouseX, double mouseY, int button) {
                setSelectedPartInternal(DefaultPartLoader.NO_PART);
            }
        };

        partButton.setMessage(translatePart(partKey));
        partButton.setTooltip(Tooltip.create(Component.translatable(skinLayer.getTranslatedName())));
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(partButton);

        // Left arrow
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(new HoverButton(xPos - 7, yPos + 2, 10, 16, 10, 16, SMALL_LEFT_ARROW_MAIN, SMALL_LEFT_ARROW_HOVER, button -> previousPart()));
        // Right arrow
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(new HoverButton(xPos + 108, yPos + 2, 10, 16, 10, 16, SMALL_RIGHT_ARROW_MAIN, SMALL_RIGHT_ARROW_HOVER, button -> nextPart()));

        colorSelectorButton = new ColorSelectorButton(screen, skinLayer, isLeft ? xPos - 23 : xPos + 120, yPos + 3, 15, 15, isLeft, isTop);
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(colorSelectorButton);
    }

    public boolean colorSelectorIsToggled() {
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

        if (scrollY < 0) {
            nextPart();
        } else if (scrollY > 0) {
            previousPart();
        }
    }

    private void nextPart() {
        if (sortedPartKeys.isEmpty()) {
            return;
        }

        int currentPart = sortedPartKeys.indexOf(selectedPart);
        if (currentPart == -1) {
            DragonSurvival.LOGGER.error("Part {} not found in sorted part keys. Did you forget to add the default parts to this species' part map?", selectedPart);
        }

        if (currentPart < 0) {
            setSelectedPartInternal(sortedPartKeys.getLast());
        } else if (currentPart - 1 < 0) {
            setSelectedPartInternal(sortedPartKeys.getLast());
        } else {
            setSelectedPartInternal(sortedPartKeys.get(currentPart - 1));
        }
    }

    private void previousPart() {
        if (sortedPartKeys.isEmpty()) {
            return;
        }

        int currentPart = sortedPartKeys.indexOf(selectedPart);
        if (currentPart == -1) {
            DragonSurvival.LOGGER.error("Part {} not found in sorted part keys. Did you forget to add the default parts to this species' part map?", selectedPart);
        }

        if (currentPart < 0) {
            setSelectedPartInternal(sortedPartKeys.getFirst());
        } else if (currentPart + 1 >= sortedPartKeys.size()) {
            setSelectedPartInternal(sortedPartKeys.getFirst());
        } else {
            setSelectedPartInternal(sortedPartKeys.get(currentPart + 1));
        }
    }

    public void setSelectedPart(final String partKey) {
        selectedPart = partKey;
        partButton.setMessage(translatePart(partKey));
    }

    private Component translatePart(final String partKey) {
        Component translation = Component.translatable(Translation.Type.SKIN_PART.wrap(DragonEditorScreen.HANDLER.speciesId().getPath() + "." + partKey.toLowerCase(Locale.ENGLISH)));
        DragonPart part = parts.get(partKey);

        if (part != null) {
            return part.localization().orElse(translation);
        }

        return translation;
    }

    private void setSelectedPartInternal(final String partKey) {
        if (selectedPart.equals(partKey)) {
            return;
        }

        screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(screen.dragonPartSelectAction, new Pair<>(skinLayer, partKey)));
    }
}
