package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraftforge.client.RenderTypeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Certain items do not support translucent rendering by default */
@Mixin(value = RenderTypeHelper.class, remap = false)
public abstract class RenderTypeHelperMixin {
    @ModifyReturnValue(method = "getFallbackItemRenderType", at = @At("RETURN"))
    private static RenderType dragonSurvival$getTranslucentRenderType(final RenderType renderType, @Local(argsOnly = true) boolean cull) {
        if (HunterHandler.itemTranslucency != HunterHandler.UNMODIFIED && HunterHandler.itemTranslucency != HunterHandler.NON_TRANSPARENT && renderType == Sheets.cutoutBlockSheet()) {
            return RenderTypeHelper.getEntityRenderType(RenderType.translucent(), cull);
        }

        return renderType;
    }
}
