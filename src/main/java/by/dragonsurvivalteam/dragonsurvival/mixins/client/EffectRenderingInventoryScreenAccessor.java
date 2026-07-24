package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EffectRenderingInventoryScreen.class)
public interface EffectRenderingInventoryScreenAccessor {
    @Accessor("EFFECT_BACKGROUND_LARGE_SPRITE")
    static ResourceLocation dragonSurvival$getEffectBackgroundLargeSprite() {
        throw new AssertionError();
    }

    @Accessor("EFFECT_BACKGROUND_SMALL_SPRITE")
    static ResourceLocation dragonSurvival$getEffectBackgroundSmallSprite() {
        throw new AssertionError();
    }

    @Dynamic
    @Accessor("dragonSurvival$areasBlockedByModifierUIForJEI")
    List<Rect2i> dragonSurvival$areasBlockedByModifierUIForJEI();
}
