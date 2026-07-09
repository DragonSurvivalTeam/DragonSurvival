package by.dragonsurvivalteam.dragonsurvival.compat.curios;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import net.minecraft.util.Tuple;
import net.neoforged.neoforge.client.event.ScreenEvent;
import top.theillusivec4.curios.client.screen.CuriosScreen;
import top.theillusivec4.curios.client.screen.button.CuriosButton;
import top.theillusivec4.curios.config.CuriosClientConfig;

public class CuriosButtonHandler {
    private CuriosButtonHandler() { }

    public static void handleCurios(final ScreenEvent.Init.Post event) {
        if (!CuriosClientConfig.CLIENT.enableButton.get()) {
            return;
        }

        if (event.getScreen() instanceof DragonInventoryScreen screen) {
            Tuple<Integer, Integer> offsets = CuriosScreen.getButtonOffset(false);
            event.addListener(new CuriosButton(
                    screen,
                    screen.getLeftPos() + offsets.getA() - 2,
                    screen.getTopPos() + offsets.getB() + 81,
                    10,
                    10,
                    CuriosButton.BIG
            ));
        }
    }
}
