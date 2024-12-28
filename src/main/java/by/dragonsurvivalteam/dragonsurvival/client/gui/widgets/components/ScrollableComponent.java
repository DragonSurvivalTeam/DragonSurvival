package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

public interface ScrollableComponent {
    default void update() { /* Nothing to do */ }

    void scroll(double mouseX, double mouseY, double scrollX, double scrollY);
}
