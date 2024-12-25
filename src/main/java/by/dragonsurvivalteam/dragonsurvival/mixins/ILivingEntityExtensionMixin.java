package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ILivingEntityExtension.class)
public interface ILivingEntityExtensionMixin {
    /** Allow cave dragons to be considered as swimming when in lava (this enables properly sinking in lava when pressing shift e.g.) */
    @Inject(method = "canSwimInFluidType", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$enableSwimming(final CallbackInfoReturnable<Boolean> callback, @Local(argsOnly = true) final FluidType fluid) {
        if ((Object) this instanceof Player player) {
            if (SwimData.getData(player).canSwimIn(fluid)) {
                callback.setReturnValue(true);
            }
        }
    }
}
