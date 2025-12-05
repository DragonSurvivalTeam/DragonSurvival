package by.dragonsurvivalteam.dragonsurvival.compat.curios;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.util.Tuple;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.client.CuriosClientConfig;
import top.theillusivec4.curios.client.gui.CuriosButton;
import top.theillusivec4.curios.client.gui.CuriosScreen;

import java.lang.reflect.Constructor;

/**
 * Currently, we only have simple GUI compatibility </br>
 * - It would also initially overlap with the claw inventory </br>
 * - It's unclear how mods potentially modify it / how its counterparts handle the screen </br>
 * - We'd have to handle the screen, menu and various packets, etc.
 */
public class CuriosButtonHandler {
    private static final int NO_VALUE = -1;

    public static int previousMouseX;
    public static int previousMouseY;
    // No need to bother with the mouse values, they all get set at the same point
    public static int previousGuiLeft = NO_VALUE;

    private static boolean hasError;
    private static Constructor<?> constructor;

    /** Just a copy of {@link top.theillusivec4.curios.client.gui.GuiEventHandler#onInventoryGuiInit(net.neoforged.neoforge.client.event.ScreenEvent.Init.Post)} */
    public static void handleCurios(final ScreenEvent.Init.Post event) {
        if (hasError || !CuriosClientConfig.CLIENT.enableButton.get()) {
            return;
        }

        if (previousGuiLeft != NO_VALUE && event.getScreen() instanceof CuriosScreen curios) {
            // Retain the previous mouse position (which is reset after switching screens)
            Window window = Minecraft.getInstance().getWindow();
            double difference = (curios.getGuiLeft() - previousGuiLeft) * window.getGuiScale();
            InputConstants.grabOrReleaseMouse(window.getWindow(), GLFW.GLFW_CURSOR_NORMAL, previousMouseX + difference, previousMouseY);
            previousGuiLeft = NO_VALUE;
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
            } catch (Exception exception) {
                DragonSurvival.LOGGER.error("Failed to add the Curios button to the dragon inventory", exception);
                hasError = true;
            }
        }
    }
}
