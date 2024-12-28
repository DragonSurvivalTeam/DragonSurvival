package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.AbilityButton;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AbilityColumnsComponent implements ScrollableComponent {
    private static final int ELEMENTS_PER_COLUMN = 4;
    private static final int NUM_COLUMN_PHASES = 4;

    private static final int LEFT_SIDE = 0;
    private static final int MIDDLE = 1;
    private static final int RIGHT_SIDE = 2;
    private static final int BEHIND = 3;

    private final ArrayList<ArrayList<AbilityButton>> columns = new ArrayList<>();
    // 0 = left side, 1 = middle, 2 = right side, 3 = behind (values to lerp to as the column fades behind)
    private final Vec3[][] buttonPositions = new Vec3[NUM_COLUMN_PHASES][ELEMENTS_PER_COLUMN];
    private final ButtonTemplateState[] buttonTemplateStates = new ButtonTemplateState[NUM_COLUMN_PHASES];

    // Elements of the BEHIND column will hide themselves once they reach this lerp amount
    // Since the MIDDLE column might only contain 2 elements compared to 4 of the BEHIND column - we don't want to show any entries of the BEHIND column
    @SuppressWarnings("FieldCanBeLocal")
    private final float TRANSITION_BACK_VISUAL_CUTOFF = 0.7f;

    private int currentColumn = 0;
    private int nextColumn = 0;
    // How long until we just fully hide the button in position 3 (behind the front icon) so that it doesn't show itself when there is no icon in the center

    private record ButtonTemplateState(float scale, float alpha, boolean interactable, boolean visible) { /* Nothing to do */ }

    public AbilityColumnsComponent(DragonAbilityScreen parentScreen, int xPos, int yPos, int verticalSpacing, int sideColumnSpacing, float sideColumnScale, float sideColumnOpacity, List<DragonAbilityInstance> abilities) {
        // Set all the button positions to be what the center column would be; this is because the buttons are only interactable when they are in the center column
        for (int i = 0; i < abilities.size(); i++) {
            int column = i / ELEMENTS_PER_COLUMN;
            int row = i % ELEMENTS_PER_COLUMN;
            int y = yPos + row * verticalSpacing;
            if (columns.size() <= column) {
                columns.add(new ArrayList<>());
            }

            columns.get(column).add(((ScreenAccessor) parentScreen).dragonSurvival$addRenderableWidget(new AbilityButton(xPos, y, abilities.get(i), parentScreen, sideColumnScale)));
        }

        // Calculate the positions of the buttons for all columns
        for (int i = 0; i < NUM_COLUMN_PHASES; i++) {
            int xPosNew;

            if (i == LEFT_SIDE) {
                xPosNew = xPos - sideColumnSpacing;
            } else if (i == MIDDLE) {
                xPosNew = xPos;
            } else if (i == RIGHT_SIDE) {
                xPosNew = xPos + sideColumnSpacing;
            } else {
                xPosNew = xPos;
            }

            int zPosDefault = i == MIDDLE ? 0 : -100;

            for (int j = 0; j < ELEMENTS_PER_COLUMN; j++) {
                buttonPositions[i][j] = new Vec3(xPos - xPosNew, 0, zPosDefault);
            }

            float scale = i == MIDDLE ? 1 : (i == BEHIND ? sideColumnScale / 2 : sideColumnScale);
            float alpha = i == MIDDLE ? 1 : sideColumnOpacity;

            boolean isInteractable = i == MIDDLE;
            boolean isVisible = i != BEHIND;

            buttonTemplateStates[i] = new ButtonTemplateState(scale, alpha, isInteractable, isVisible);
        }

        // Set the initial positions of the buttons
        forceSetButtonPositions();
    }

    @Override
    public void update() {
        if (currentColumn == nextColumn) {
            return;
        }

        // Lerp the positions of the buttons
        for (int i = 0; i < columns.size(); i++) {
            for (int j = 0; j < columns.get(i).size(); j++) {
                AbilityButton button = columns.get(i).get(j);
                int nextColumnPhase = convertIndexToColumnPhase(i, this.nextColumn);

                Vec3 currentOffset = button.getOffset();
                Vec3 nextOffset = buttonPositions[nextColumnPhase][j];
                float deltaTick = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
                float lerpRate = Math.min(1, deltaTick);
                // Modify the rate of the lerp to be faster if we are closer to the target values
                Vec3 newOffset = new Vec3(
                        Mth.lerp(lerpRate, currentOffset.x(), nextOffset.x()),
                        Mth.lerp(lerpRate, currentOffset.y(), nextOffset.y()),
                        Mth.lerp(lerpRate, currentOffset.z(), nextOffset.z())
                );
                button.setOffset(newOffset);

                int currentColumnPhase = convertIndexToColumnPhase(i, currentColumn);
                float lerpProgress = (float) (1 - Math.abs((nextOffset.x() - currentOffset.x()) / (nextOffset.x() - buttonPositions[currentColumnPhase][j].x())));

                if (lerpProgress > TRANSITION_BACK_VISUAL_CUTOFF && nextColumnPhase == BEHIND) {
                    button.setVisible(false);
                }

                ButtonTemplateState nextButtonTemplateState = buttonTemplateStates[nextColumnPhase];
                float currentScale = button.getScale();
                button.setScale(Mth.lerp(lerpRate, currentScale, nextButtonTemplateState.scale));
                float currentAlpha = button.getAlpha();
                button.setAlpha(Mth.lerp(lerpRate, currentAlpha, nextButtonTemplateState.alpha));
            }
        }

        // Once the lerp is complete, update the current column
        // This is done by comparing the x position of the middle column with the next (which the lerp is being applied to)
        if (Math.abs(buttonPositions[MIDDLE][0].x() - columns.get(nextColumn).getFirst().getOffset().x()) < 0.1) {
            currentColumn = nextColumn;
            forceSetButtonPositions();
        }
    }

    @Override
    public void scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!isHoveringOverButton(mouseX, mouseY)) {
            return;
        }

        if (scrollY > 0) {
            rotateRight();
        } else if (scrollY < 0) {
            rotateLeft();
        }
    }

    private void forceSetButtonPositions() {
        for (int i = 0; i < columns.size(); i++) {
            for (int j = 0; j < columns.get(i).size(); j++) {
                int columnPhase = convertIndexToColumnPhase(i, currentColumn);
                AbilityButton button = columns.get(i).get(j);
                ButtonTemplateState buttonTemplateState = buttonTemplateStates[columnPhase];
                button.setOffset(buttonPositions[columnPhase][j]);
                button.setScale(buttonTemplateState.scale);
                button.setAlpha(buttonTemplateState.alpha);
                button.setInteractable(buttonTemplateState.interactable);
                button.setVisible(buttonTemplateState.visible);
            }
        }
    }

    private void rotateRight() {
        if (nextColumn != currentColumn || columns.size() == 1) {
            return;
        }

        nextColumn = (nextColumn + 1) % columns.size();
        for (ArrayList<AbilityButton> column : columns) {
            for (AbilityButton abilityButton : column) {
                abilityButton.setInteractable(false);
                abilityButton.setVisible(true);
            }
        }
    }

    private void rotateLeft() {
        if (nextColumn != currentColumn || columns.size() == 1) {
            return;
        }

        nextColumn = (nextColumn - 1 + columns.size()) % columns.size();
        for (ArrayList<AbilityButton> column : columns) {
            for (AbilityButton abilityButton : column) {
                abilityButton.setInteractable(false);
                abilityButton.setVisible(true);
            }
        }
    }

    private boolean isHoveringOverButton(double mouseX, double mouseY) {
        for (ArrayList<AbilityButton> column : columns) {
            for (AbilityButton abilityButton : column) {
                if (abilityButton.isMouseOver(mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return false;
    }

    private int wrapInt(int value, int min, int max) {
        if (value < min) {
            return max - (min - value) + 1;
        } else if (value > max) {
            return min + (value - max) - 1;
        } else {
            return value;
        }
    }

    private int convertIndexToColumnPhase(int index, int column) {
        int signedDistanceFromCenter = column - index;

        if (columns.size() == 1) {
            return MIDDLE;
        } else {
            return wrapInt(signedDistanceFromCenter + 1, 0, columns.size() - 1);
        }
    }
}
