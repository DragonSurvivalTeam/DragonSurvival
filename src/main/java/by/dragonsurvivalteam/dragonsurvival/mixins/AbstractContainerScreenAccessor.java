package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("topPos")
    int getTopPos();

    @Accessor("leftPos")
    int getLeftPos();

    @Accessor("imageWidth")
    int getImageWidth();
}
