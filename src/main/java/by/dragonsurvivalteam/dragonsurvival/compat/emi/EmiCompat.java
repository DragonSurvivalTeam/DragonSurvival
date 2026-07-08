package by.dragonsurvivalteam.dragonsurvival.compat.emi;

import by.dragonsurvivalteam.dragonsurvival.compat.ModID;

public class EmiCompat {
    /**
     * Mirrors EMI's own inventory effect visibility logic without taking a direct compile-time dependency on EMI.
     */
    public static boolean hideEffects() {
        if (!ModID.EMI.isLoaded()) {
            return false;
        }

        //        return EmiConfig.effectLocation == EffectLocation.TOP || EmiConfig.effectLocation == EffectLocation.HIDDEN;
        return false;
    }
}
