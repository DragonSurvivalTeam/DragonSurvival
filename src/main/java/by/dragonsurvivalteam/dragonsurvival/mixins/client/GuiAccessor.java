package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {
    @Accessor("EFFECT_BACKGROUND_SPRITE") // FIXME :: make static
    ResourceLocation dragonSurvival$getEffectBackgroundSprite();
}
