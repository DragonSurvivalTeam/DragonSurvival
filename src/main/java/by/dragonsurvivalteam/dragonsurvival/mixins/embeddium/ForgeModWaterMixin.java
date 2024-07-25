package by.dragonsurvivalteam.dragonsurvival.mixins.embeddium;

import by.dragonsurvivalteam.dragonsurvival.client.VisionHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Modify water alpha */
@Mixin(targets = "net.minecraftforge.common.ForgeMod$2$1", remap = false)
public abstract class ForgeModWaterMixin implements IClientFluidTypeExtensions {
    @ModifyReturnValue(method = "getTintColor(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;)I", at = @At("RETURN"))
    private int modifyColor(int color) {
        if (VisionHandler.hasWaterVision()) {
            // 0x5A is 90 which is roughly the result of 255 * 0.35
            return (color /* Remove alpha */ & 0x00FFFFFF) /* Add custom alpha */ | 0x5A000000;
        }

        return color;
    }
}
