package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.neoforged.neoforge.client.RenderTypeHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Certain items do not support translucent rendering by default */
@Mixin(RenderTypeHelper.class)
public abstract class RenderTypeHelperMixin {
    @ModifyReturnValue(method = "getFallbackItemRenderType", at = @At("RETURN"))
    private static RenderType dragonSurvival$getTranslucentRenderType(final RenderType renderType, @Local(argsOnly = true) boolean cull) {
        if (HunterHandler.itemTranslucency != -1  && HunterHandler.itemTranslucency != 1 && renderType == Sheets.cutoutBlockSheet()) {
            return getEntityRenderType(RenderType.translucent(), cull);
        }

        return renderType;
    }

    @Shadow
    public static RenderType getEntityRenderType(final RenderType renderType, boolean cull) {
        throw new AssertionError();
    }
}
