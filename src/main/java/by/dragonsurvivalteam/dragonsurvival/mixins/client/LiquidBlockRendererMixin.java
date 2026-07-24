package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Make the lava / water layer (more) see-through */
@Mixin(LiquidBlockRenderer.class)
public abstract class LiquidBlockRendererMixin {
    @ModifyVariable(method = "tesselate", at = @At(value = "STORE"), ordinal = 0)
    private float dragonSurvival$handleVision(float alpha, @Local(argsOnly = true) FluidState fluid, @Local(ordinal = 0) boolean isLava) {
        if (isLava && VisionHandler.hasLavaVision() || VisionHandler.hasWaterVision() && fluid.is(FluidTags.WATER)) {
            return alpha * 0.35f;
        }

        return alpha;
    }
}
