package by.dragonsurvivalteam.dragonsurvival.client.render.util.iris_compat;
import net.minecraft.client.renderer.RenderType;

// This is from Iris, Licensed under LGPL-3.0
public interface WrappableRenderType {
    /**
     * Returns the underlying wrapped RenderType. Might return itself if this RenderType doesn't wrap anything.
     */
    RenderType unwrap();
}