package by.dragonsurvivalteam.dragonsurvival.compat.curios;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Tuple;
import net.neoforged.neoforge.client.event.ScreenEvent;
import top.theillusivec4.curios.client.CuriosClientConfig;
import top.theillusivec4.curios.client.gui.CuriosButton;
import top.theillusivec4.curios.client.gui.CuriosScreen;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CuriosButtonHandler {
    private static boolean hasError;
    private static Constructor<?> constructor;

    /** Just a copy of {@link top.theillusivec4.curios.client.gui.GuiEventHandler#onInventoryGuiInit(net.neoforged.neoforge.client.event.ScreenEvent.Init.Post)} */
    public static void onInventoryGuiInit(final ScreenEvent.Init.Post event) {
        if (hasError || !CuriosClientConfig.CLIENT.enableButton.get()) {
            return;
        }

        if (event.getScreen() instanceof DragonInventoryScreen screen) {
            Tuple<Integer, Integer> offsets = CuriosScreen.getButtonOffset(false);
            int x = offsets.getA();
            int y = offsets.getB();
            int size = 10;
            int yOffset = 81;

            try {
                if (constructor == null) {
                    Class<?> button = Class.forName("top.theillusivec4.curios.client.gui.CuriosButton");
                    constructor = button.getDeclaredConstructor(AbstractContainerScreen.class, int.class, int.class, int.class, int.class, WidgetSprites.class);
                    constructor.setAccessible(true);
                }

                event.addListener((GuiEventListener) constructor.newInstance(screen, screen.getGuiLeft() + x - 2, screen.getGuiTop() + y + yOffset, size, size, CuriosButton.BIG));
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException exception) {
                DragonSurvival.LOGGER.error("Failed to add the Curios button to the dragon inventory", exception);
                hasError = true;
            }
        }
    }
}
