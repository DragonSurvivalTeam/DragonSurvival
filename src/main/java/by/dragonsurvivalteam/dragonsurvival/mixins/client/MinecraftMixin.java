package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Enable the features of the Fabulous graphics mode */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow static Minecraft instance;

    @ModifyReturnValue(method = "useShaderTransparency", at = @At("RETURN"))
    private static boolean dragonSurvival$enableTranslucencyFix(boolean isEnabled) {
        return isEnabled || (/* Unsure why this check exists in vanilla */ !instance.gameRenderer.isPanoramicMode() && HunterHandler.FIX_TRANSLUCENCY);
    }
}
