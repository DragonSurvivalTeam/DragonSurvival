package by.dragonsurvivalteam.dragonsurvival.mixins.sodium;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer", remap = false)
public abstract class DefaultFluidRendererMixin {
    private static final float VISION_ALPHA = 0.35F;

    @ModifyExpressionValue(
        method = "updateQuad",
        at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/util/ColorARGB;toABGR(I)I", remap = false),
        remap = false
    )
    private int dragonSurvival$applyVisionAlpha(final int color, @Local(argsOnly = true) final FluidState fluidState) {
        if ((!VisionHandler.hasWaterVision() || !fluidState.is(FluidTags.WATER)) && (!VisionHandler.hasLavaVision() || !fluidState.is(FluidTags.LAVA))) {
            return color;
        }

        return ARGB.color(ARGB.as8BitChannel(VISION_ALPHA), ARGB.transparent(color));
    }
}
