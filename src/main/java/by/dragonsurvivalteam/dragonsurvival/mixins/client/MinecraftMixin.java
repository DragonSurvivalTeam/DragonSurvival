package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/** Enable the features of the Fabulous graphics mode */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow static Minecraft instance;

    @ModifyReturnValue(method = "shouldEntityAppearGlowing", at = @At("RETURN"))
    boolean dragonSurvival$handleGlowEffect(final boolean shouldAppearGlowing, @Local(argsOnly = true) final Entity entity) {
        if (shouldAppearGlowing) {
            return true;
        }

        return entity.getExistingData(DSDataAttachments.GLOW).map(glow -> glow.getColor() != GlowData.NO_COLOR).orElse(false);
    }

    @ModifyReturnValue(method = "useShaderTransparency", at = @At("RETURN"))
    private static boolean dragonSurvival$enableTranslucencyFix(boolean isEnabled) {
        if (Compat.isModLoaded(Compat.IRIS)) {
            // Iris doesn't properly work with Fabulous! mode
            // (the translucency sorting feature) (it causes particles to be invisible)
            return isEnabled;
        }

        return isEnabled || (/* Unsure why this check exists in vanilla */ !instance.gameRenderer.isPanoramicMode() && HunterHandler.FIX_TRANSLUCENCY);
    }
}
