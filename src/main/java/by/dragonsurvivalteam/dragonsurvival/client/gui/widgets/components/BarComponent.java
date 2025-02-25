package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverButton;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BarComponent implements ScrollableComponent {
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final HoverButton leftArrow;
    private final HoverButton rightArrow;

    private final int displayAmount;
    private final int spacing;
    private final int xPos;
    private final int yPos;

    private int scrollAmount;

    public BarComponent(final Screen screen, int xPos, int yPos, int displayAmount, final List<? extends AbstractWidget> widgets, int spacing, int arrowLeftX, int arrowRightX, int arrowY, int arrowWidth, int arrowHeight, ResourceLocation leftArrowHover, ResourceLocation leftArrowMain, ResourceLocation rightArrowHover, ResourceLocation rightArrowMain) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.displayAmount = displayAmount;
        this.spacing = spacing;
        this.widgets.addAll(widgets);

        for (AbstractWidget widget : widgets) {
            ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(widget);
        }

        leftArrow = new HoverButton(xPos + arrowLeftX, yPos + arrowY, arrowWidth, arrowHeight, arrowWidth, arrowHeight, leftArrowMain, leftArrowHover, button -> scroll(false));
        rightArrow = new HoverButton(xPos + arrowRightX, yPos + arrowY, arrowWidth, arrowHeight, arrowWidth, arrowHeight, rightArrowMain, rightArrowHover, button -> scroll(true));
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(leftArrow);
        ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(rightArrow);

        leftArrow.visible = false;
        rightArrow.visible = widgets.size() > displayAmount;

        forceSetButtonPositions();
    }

    private boolean isHoveringOverWidget(double mouseX, double mouseY) {
        for (AbstractWidget widget : widgets) {
            if (widget.isMouseOver(mouseX, mouseY)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (Screen.hasShiftDown() || !isHoveringOverWidget(mouseX, mouseY)) {
            return;
        }

        scroll(scrollY < 0);
    }

    private void scroll(boolean isNext) {
        if (widgets.size() <= displayAmount) {
            return;
        }

        if (isNext && displayAmount + scrollAmount < widgets.size()) {
            scrollAmount++;

            if (leftArrow != null) {
                leftArrow.visible = true;
            }

            if (rightArrow != null && displayAmount + scrollAmount == widgets.size()) {
                rightArrow.visible = false;
            }
        } else if (!isNext && scrollAmount > 0) {
            scrollAmount--;

            if (rightArrow != null) {
                rightArrow.visible = true;
            }

            if (leftArrow != null && scrollAmount == 0) {
                leftArrow.visible = false;
            }
        }

        forceSetButtonPositions();
    }

    private boolean isVisibleElement(int index) {
        return index >= scrollAmount && index < displayAmount + scrollAmount;
    }

    private void forceSetButtonPositions() {
        for (int index = 0; index < widgets.size(); index++) {
            AbstractWidget widget = widgets.get(index);
            widget.visible = isVisibleElement(index);

            if (widget.visible) {
                if (widgets.size() < displayAmount) {
                    int elementWidth = widget.getWidth() + spacing;

                    int totalWidth = widgets.size() * elementWidth;
                    int centeredX = xPos + (displayAmount * elementWidth - totalWidth) / 2;

                    widget.setX(centeredX + index * elementWidth + spacing / 2);
                } else {
                    widget.setX(xPos + (index - scrollAmount) * (widget.getWidth() + spacing));
                }

                widget.setY(yPos);
            }
        }
    }

    /** Needed due to the 'show ui' button of the dragon editor (which overwrites the visibility) */
    public boolean isHidden(final AbstractWidget widget) {
        if (widget == leftArrow && scrollAmount == 0) {
            return true;
        }

        if (widget == rightArrow && (widgets.size() < displayAmount || displayAmount + scrollAmount == widgets.size())) {
            return true;
        }

        int index = widgets.indexOf(widget);

        if (index == -1) {
            return false;
        }

        return !isVisibleElement(index);
    }
}
