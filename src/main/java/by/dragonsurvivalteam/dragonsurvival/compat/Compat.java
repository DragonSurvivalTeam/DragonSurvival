package by.dragonsurvivalteam.dragonsurvival.compat;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.world.entity.player.Player;

public class Compat {
    /**
     * Generic in case compatibility for other mods will be added <br>
     * (Which have the ability to swap the player's model)
     */
    public static boolean hasModelSwapOrDoesNotUseModel(final Player player) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        if (handler.isDragon()) {
            return handler.body().value().noDragonModelRendering();
        }

        return false;
    }

    /** In case a mod needs the neck + head displayed in first person */
    public static boolean displayNeck() {
        return false;
    }

    public static boolean isShaderActive() {
        return false;
    }

    public static boolean isRenderingShadows() {
        return false;
    }
}
