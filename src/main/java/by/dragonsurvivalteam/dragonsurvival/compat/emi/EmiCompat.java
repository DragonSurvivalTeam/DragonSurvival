package by.dragonsurvivalteam.dragonsurvival.compat.emi;

import by.dragonsurvivalteam.dragonsurvival.compat.ModCheck;

public class EmiCompat {
    /**
     * Mirrors EMI's own inventory effect visibility logic without taking a direct compile-time dependency on EMI.
     */
    public static boolean hideEffects() {
        if (!ModCheck.isModLoaded(ModCheck.EMI)) {
            return false;
        }

        //        return EmiConfig.effectLocation == EffectLocation.TOP || EmiConfig.effectLocation == EffectLocation.HIDDEN;
        return false;
    }
}
