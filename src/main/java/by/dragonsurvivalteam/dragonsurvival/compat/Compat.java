package by.dragonsurvivalteam.dragonsurvival.compat;

import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.world.entity.player.Player;
import net.xolt.freecam.Freecam;

public class Compat {
    /**
     * Generic in case compatibility for other mods will be added <br>
     * (Which have the ability to swap the player's model)
     */
    public static boolean hasModelSwap(final Player player) {
        if (ModCheck.isModLoaded(ModCheck.CREATE)) {
            return CardboardArmorHandler.testForStealth(player);
        } else {
            return false;
        }
    }

    /** In case a mod needs the neck + head displayed in first person */
    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public static boolean displayNeck() {
        if (ModCheck.isModLoaded(ModCheck.IRIS) && IrisApi.getInstance().isRenderingShadowPass()) {
            return true;
        }

        if (ModCheck.isModLoaded(ModCheck.FREECAM) && Freecam.isEnabled()) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public static boolean isShaderActive() {
        if (ModCheck.isModLoaded(ModCheck.IRIS) && IrisApi.getInstance().isShaderPackInUse()) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public static boolean isRenderingShadows() {
        if (ModCheck.isModLoaded(ModCheck.IRIS) && IrisApi.getInstance().isRenderingShadowPass()) {
            return true;
        }

        return false;
    }
}
