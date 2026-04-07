package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(EffectsInInventory.class)
public interface EffectsInInventoryAccessor {
    @Accessor("EFFECT_BACKGROUND_SPRITE")
    static Identifier dragonSurvival$getEffectBackgroundSprite() {
        throw new AssertionError();
    }

    @Dynamic
    @Accessor("dragonSurvival$areasBlockedByModifierUIForJEI")
    List<Rect2i> dragonSurvival$areasBlockedByModifierUIForJEI();
}
