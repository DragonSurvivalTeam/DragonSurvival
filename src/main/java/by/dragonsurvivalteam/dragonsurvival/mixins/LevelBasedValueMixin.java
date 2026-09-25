package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelBasedValue.Lookup.class)
public abstract class LevelBasedValueMixin {
    @Shadow @Final private LevelBasedValue fallback;

    /**
     * For the dragon ability info we want to display the data even when the ability is not leveled yet </br>
     * But to avoid having to do multiple checks / adjustments for said level, we just add a guard in here </br>
     * We don't always do it, so datapacks etc. are still informed of invalid setups and issues
     */
    @Inject(method = "calculate", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$skipLevelZero(final int level, final CallbackInfoReturnable<Float> callback) {
        if (level == 0 && DragonSurvival.PROXY.shouldGuardLevelBasedLookup()) {
            callback.setReturnValue(fallback.calculate(level));
        }
    }
}
