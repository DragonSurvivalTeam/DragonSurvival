package by.dragonsurvivalteam.dragonsurvival.compat.emi;

import by.dragonsurvivalteam.dragonsurvival.compat.ModCheck;
import dev.emi.emi.config.EffectLocation;
import dev.emi.emi.config.EmiConfig;

public class EmiCompat {
    /**
     * Copies the logic of <a href="https://github.com/emilyploszaj/emi/blob/533b5d025723b1627b8afb11804295740d61d20e/xplat/src/main/java/dev/emi/emi/mixin/AbstractInventoryScreenMixin.java#L66-L73">EMI</a> <br> <br>
     * Since that only modifies the first `.size()` call we could run into crashes in {@link by.dragonsurvivalteam.dragonsurvival.mixins.client.EffectRenderingInventoryScreenMixin#dragonSurvival$adjustRenderedEffectsSize(int)} <br> <br>
     * See <a href="https://github.com/DragonSurvivalTeam/DragonSurvival/issues/839">this</a> issue
     */
    public static boolean hideEffects() {
        if (!ModCheck.isModLoaded(ModCheck.EMI)) {
            return false;
        }

        return EmiConfig.effectLocation == EffectLocation.TOP || EmiConfig.effectLocation == EffectLocation.HIDDEN;
    }
}
