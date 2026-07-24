package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Allow lava to be rendered translucent */
@Mixin(ItemBlockRenderTypes.class)
public abstract class ItemBlockRenderTypesMixin {
    @ModifyReturnValue(method = "getRenderLayer", at = @At(value = "RETURN"))
    private static RenderType dragonSurvival$handleLavaVision(RenderType renderType, @Local(argsOnly = true) FluidState fluidState) {
        if (VisionHandler.hasLavaVision() && fluidState.is(FluidTags.LAVA)) {
            return RenderType.translucent();
        }

        return renderType;
    }
}
