package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.level.BlockDestructionProgress;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.SortedSet;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccess {
    @Accessor("destructionProgress")
    Long2ObjectMap<SortedSet<BlockDestructionProgress>> dragonSurvival$getDestructionProgress();

    @Accessor("renderBuffers")
    RenderBuffers dragonSurvival$getRenderBuffers();

    @Accessor("level")
    @Nullable ClientLevel dragonSurvival$getLevel();
}
