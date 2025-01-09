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
    private int centerIndex = 0;
    private final int numberOfElementsToDisplay;
    private final int xPos;
    private final int yPos;
    private final int elementSpacing;

    public BarComponent(Screen parentScreen, int xPos, int yPos, int numberOfElementsToDisplay, List<AbstractWidget> widgets, int elementSpacing, int arrowLeftX, int arrowRightX, int arrowY, int arrowWidth, int arrowHeight, int arrowTextureWidth, int arrowTextureHeight, ResourceLocation leftArrowHover, ResourceLocation leftArrowMain, ResourceLocation rightArrowHover, ResourceLocation rightArrowMain, boolean replaceButtonsWithArrowsWhenOversize) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.elementSpacing = elementSpacing;
        if(replaceButtonsWithArrowsWhenOversize) {
            if (widgets.size() > numberOfElementsToDisplay) {
                this.numberOfElementsToDisplay = numberOfElementsToDisplay - 2;
            } else {
                this.numberOfElementsToDisplay = numberOfElementsToDisplay;
            }
        } else {
            this.numberOfElementsToDisplay = numberOfElementsToDisplay;
        }

        if (widgets.size() > 3) {
            int oddOffset = widgets.size() % 2 == 0 ? 0 : 1;
            centerIndex = widgets.size() / 2 - 1 + oddOffset;
        } else if (widgets.size() == 3) {
            centerIndex = 1;
        } else if (widgets.size() > 1) {
            centerIndex = 0;
        }

        this.widgets.addAll(widgets);

        for (AbstractWidget widget : widgets) {
            ((ScreenAccessor) parentScreen).dragonSurvival$addRenderableWidget(widget);
        }

        if (widgets.size() > this.numberOfElementsToDisplay) {
            leftArrow = new HoverButton(xPos + arrowLeftX, yPos + arrowY, arrowWidth, arrowHeight, arrowTextureWidth, arrowTextureHeight, leftArrowMain, leftArrowHover, button -> rotate(false));
            rightArrow = new HoverButton(xPos + arrowRightX, yPos + arrowY, arrowWidth, arrowHeight, arrowTextureWidth, arrowTextureHeight, rightArrowMain, rightArrowHover, button -> rotate(true));
            ((ScreenAccessor) parentScreen).dragonSurvival$addRenderableWidget(leftArrow);
            ((ScreenAccessor) parentScreen).dragonSurvival$addRenderableWidget(rightArrow);

            if (centerIndex - getNumberOfElementsLeftOfCenter() <= 0) {
                leftArrow.visible = false;
            }

            if (centerIndex + getNumberOfElementsRightOfCenter() >= widgets.size() - 1) {
                rightArrow.visible = false;
            }
        } else {
            leftArrow = null;
            rightArrow = null;
        }

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
        if (!isHoveringOverWidget(mouseX, mouseY)) {
            return;
        }

        rotate(scrollY > 0);
    }

    private void rotate(boolean right) {
        if (widgets.size() <= numberOfElementsToDisplay) {
            return;
        }

        if (!right) {
            if (centerIndex - getNumberOfElementsLeftOfCenter() > 0) {
                centerIndex--;

                if (rightArrow != null) {
                    rightArrow.visible = true;
                }

                if (leftArrow != null && centerIndex - getNumberOfElementsLeftOfCenter() <= 0) {
                    leftArrow.visible = false;
                }
            }
        } else if (centerIndex + getNumberOfElementsRightOfCenter() < widgets.size() - 1) {
            centerIndex++;

            if (leftArrow != null) {
                leftArrow.visible = true;
            }

            if (rightArrow != null && centerIndex + getNumberOfElementsRightOfCenter() >= widgets.size() - 1) {
                rightArrow.visible = false;
            }
        }

        forceSetButtonPositions();
    }

    private boolean isAnIndexBeingDisplayed(int index) {
        return index >= centerIndex - getNumberOfElementsLeftOfCenter() && index <= centerIndex + getNumberOfElementsRightOfCenter();
    }

    private int getNumberOfElementsLeftOfCenter() {
        int evenOffset = numberOfElementsToDisplay % 2 == 0 ? 1 : 0;
        return numberOfElementsToDisplay / 2 - evenOffset;
    }

    private int getNumberOfElementsRightOfCenter() {
        return numberOfElementsToDisplay / 2;
    }

    private void forceSetButtonPositions() {
        for (int i = 0; i < widgets.size(); i++) {
            if (!isAnIndexBeingDisplayed(i)) {
                widgets.get(i).visible = false;
            } else {
                int centerAlignment = i - centerIndex + 1;
                widgets.get(i).setX(xPos + centerAlignment * elementSpacing);
                widgets.get(i).setY(yPos);
                widgets.get(i).visible = true;
            }
        }
    }

    public List<AbstractWidget> currentlyHiddenWidgets() {
        List<AbstractWidget> hiddenWidgets = new ArrayList<>();
        for (int i = 0; i < widgets.size(); i++) {
            if (!isAnIndexBeingDisplayed(i)) {
                hiddenWidgets.add(widgets.get(i));
            }
        }

        if (rightArrow != null && centerIndex + getNumberOfElementsRightOfCenter() >= widgets.size() - 1) {
            hiddenWidgets.add(rightArrow);
        }

        if (leftArrow != null && centerIndex - getNumberOfElementsLeftOfCenter() <= 0) {
            hiddenWidgets.add(leftArrow);
        }

        return hiddenWidgets;
    }
}
