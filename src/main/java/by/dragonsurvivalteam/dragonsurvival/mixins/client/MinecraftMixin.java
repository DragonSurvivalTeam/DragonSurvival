package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

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
}
