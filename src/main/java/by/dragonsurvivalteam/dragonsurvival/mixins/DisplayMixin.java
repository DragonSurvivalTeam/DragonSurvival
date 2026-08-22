package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.GlowData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Display.class)
public abstract class DisplayMixin {
    @ModifyReturnValue(method = "getGlowColorOverride", at = @At("RETURN"))
    private int dragonSurvival$getGlowColorOverride(final int glowColorOverride) {
        Entity self = (Entity) (Object) this;
        return self.getExistingData(DSDataAttachments.GLOW).map(GlowData::getColor).orElse(glowColorOverride);
    }
}
