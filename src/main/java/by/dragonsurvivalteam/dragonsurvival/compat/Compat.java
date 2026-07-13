package by.dragonsurvivalteam.dragonsurvival.compat;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.world.entity.player.Player;
import net.xolt.freecam.Freecam;

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
    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public static boolean displayNeck() {
        if (ModID.IRIS.isLoaded() && IrisApi.getInstance().isRenderingShadowPass()) {
            return true;
        }

        if (ModID.FREECAM.isLoaded() && Freecam.isEnabled()) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public static boolean isShaderActive() {
        if (ModID.IRIS.isLoaded() && IrisApi.getInstance().isShaderPackInUse()) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public static boolean isRenderingShadows() {
        if (ModID.IRIS.isLoaded() && IrisApi.getInstance().isRenderingShadowPass()) {
            return true;
        }

        return false;
    }
}
