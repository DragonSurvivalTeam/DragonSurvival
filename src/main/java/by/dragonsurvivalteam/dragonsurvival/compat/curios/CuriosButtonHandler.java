package by.dragonsurvivalteam.dragonsurvival.compat.curios;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ScreenEvent;
import org.lwjgl.glfw.GLFW;
import top.theillusivec4.curios.client.gui.CuriosScreen;

import java.lang.reflect.Constructor;

public class CuriosButtonHandler {
    private static final int NO_VALUE = -1;
    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("curios", "textures/gui/inventory.png");

    public static int previousMouseX;
    public static int previousMouseY;
    public static int previousGuiLeft = NO_VALUE;

    private static boolean hasError;
    private static Constructor<?> constructor;

    public static void handleCurios(final ScreenEvent.Init.Post event) {
        if (hasError) {
            return;
        }

        if (previousGuiLeft != NO_VALUE && event.getScreen() instanceof CuriosScreen curios) {
            Window window = Minecraft.getInstance().getWindow();
            double difference = (curios.getGuiLeft() - previousGuiLeft) * window.getGuiScale();
            InputConstants.grabOrReleaseMouse(window.getWindow(), GLFW.GLFW_CURSOR_NORMAL, previousMouseX + difference, previousMouseY);
            previousGuiLeft = NO_VALUE;
            return;
        }

        if (event.getScreen() instanceof DragonInventoryScreen screen) {
            Tuple<Integer, Integer> offsets = CuriosScreen.getButtonOffset(false);
            int size = 10;
            int yOffset = 81;

            try {
                if (constructor == null) {
                    Class<?> button = Class.forName("top.theillusivec4.curios.client.gui.CuriosButton");
                    constructor = button.getDeclaredConstructor(AbstractContainerScreen.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, ResourceLocation.class);
                    constructor.setAccessible(true);
                }

                event.addListener((GuiEventListener) constructor.newInstance(
                        screen,
                        screen.getGuiLeft() + offsets.getA() - 2,
                        screen.getGuiTop() + offsets.getB() + yOffset,
                        size,
                        size,
                        64,
                        0,
                        size,
                        INVENTORY_TEXTURE
                ));
            } catch (ReflectiveOperationException exception) {
                DragonSurvival.LOGGER.error("Failed to add the Curios button to the dragon inventory", exception);
                hasError = true;
            }
        }
    }
}
