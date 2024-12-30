package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons.DragonBodyButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ArrowButton;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBodyTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;

import java.util.List;

public interface DragonBodyScreen {
    private Screen self() {
        return (Screen) this;
    }

    default void addDragonBodyWidgets() {
        List<Holder<DragonBody>> bodies = DSBodyTags.getOrdered(null);
        List<AbstractWidget> widgets = getDragonBodyWidgets();
        Screen screen = self();

        int buttonWidth = 25;
        int gap = 1;

        boolean cannotFit = bodies.size() > 5;
        int elements = Math.min(5, bodies.size());
        int requiredWidth = elements * buttonWidth + (elements - 1) * gap;

        for (int index = 0; index < 5; index++) {
            // To make sure the buttons are centered if there are less than 5 elements (max. supported by the current GUI)
            int x = (screen.width - requiredWidth - getDragonBodyButtonXOffset()) / 2 + (index * (buttonWidth + gap));
            int y = screen.height / 2 + getDragonBodyButtonYOffset();

            AbstractWidget widget;

            if (cannotFit && /* leftmost element */ index == 0) {
                // The y offset is to align the arrow properly with the body icon
                // The x offset is for the dragon editor to match up with the arrow buttons for the animation (should probably not be done here)
                widget = new ArrowButton(ArrowButton.Type.PREVIOUS, x + 4, y + 3, 15, 15, button -> {
                    if (getDragonBodySelectionOffset() > 0) {
                        setDragonBodyButtonOffset(getDragonBodySelectionOffset() - 1);

                        widgets.forEach(dragonBodyWidget -> ((ScreenAccessor) screen).dragonSurvival$removeWidget(dragonBodyWidget));
                        widgets.clear();
                        addDragonBodyWidgets();
                    }
                });
            } else if (cannotFit && /* rightmost element */ index == 4) {
                // The y offset is to align the arrow properly with the body icon
                // The x offset is for the dragon editor to match up with the arrow buttons for the animation (should probably not be done here)
                widget = new ArrowButton(ArrowButton.Type.NEXT, x + 2, y + 3, 15, 15, button -> {
                    // If there are 5 bodies we can navigate next two times, showing 0 - 2,  1 - 3 and 2 - 4
                    if (getDragonBodySelectionOffset() < bodies.size() - /* shown elements */ 3) {
                        setDragonBodyButtonOffset(getDragonBodySelectionOffset() + 1);

                        widgets.forEach(dragonBodyWidget -> ((ScreenAccessor) screen).dragonSurvival$removeWidget(dragonBodyWidget));
                        widgets.clear();
                        addDragonBodyWidgets();
                    }
                });
            } else {
                // Subtract 1 since index 0 is an arrow button (otherwise we would skip the first body)
                int selectionIndex = index + getDragonBodySelectionOffset() - (cannotFit ? 1 : 0);
                Holder<DragonBody> body = bodies.get(selectionIndex);
                // The x offset is to move the icons closer to the center of the two arrow buttons
                widget = createButton(body, cannotFit ? x - 5 : x, y);
            }

            widgets.add(widget);
            ((ScreenAccessor) screen).dragonSurvival$addRenderableWidget(widget);
        }
    }

    DragonBodyButton createButton(final Holder<DragonBody> dragonBody, int x, int y);
    List<AbstractWidget> getDragonBodyWidgets();

    int getDragonBodyButtonXOffset();
    int getDragonBodyButtonYOffset();

    void setDragonBodyButtonOffset(int offset);
    int getDragonBodySelectionOffset();
}
