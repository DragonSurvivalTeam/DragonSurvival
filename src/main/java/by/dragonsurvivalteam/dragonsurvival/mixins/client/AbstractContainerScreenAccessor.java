package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("topPos")
    int dragonSurvival$getTopPos();

    @Accessor("leftPos")
    int dragonSurvival$getLeftPos();

    @Accessor("imageWidth")
    int dragonSurvival$imageWidth();
}
