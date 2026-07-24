package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockRenderDispatcher.class)
public interface BlockRenderDispatcherAccess {
    @Accessor("blockColors")
    BlockColors dragonSurvival$getBlockColors();
}
