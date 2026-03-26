package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Make the lava / water layer (more) see-through */
@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
//    @ModifyVariable(method = "tesselate", at = @At(value = "STORE"), name = "alpha", argsOnly = true)
//    private float dragonSurvival$handleVision(float alpha, @Local(argsOnly = true) FluidState fluid, @Local(ordinal = 0, argsOnly = true) boolean isLava) {
//        if (isLava && VisionHandler.hasLavaVision() || VisionHandler.hasWaterVision() && fluid.is(FluidTags.WATER)) {
//            return alpha * 0.35f;
//        }
//
//        return alpha;
//    }
}
