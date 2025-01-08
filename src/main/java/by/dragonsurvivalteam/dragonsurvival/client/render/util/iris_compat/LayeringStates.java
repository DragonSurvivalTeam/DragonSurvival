package by.dragonsurvivalteam.dragonsurvival.client.render.util.iris_compat;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import org.joml.Matrix4fStack;

// TODO :: This is from 1.21.4. When we update, we can remove this.
public class LayeringStates {
    public static final RenderStateShard.LayeringStateShard POLYGON_OFFSET_LAYERING = new RenderStateShard.LayeringStateShard(
            "polygon_offset_layering", () -> {
        RenderSystem.polygonOffset(-1.0F, -10.0F);
        RenderSystem.enablePolygonOffset();
    }, () -> {
        RenderSystem.polygonOffset(0.0F, 0.0F);
        RenderSystem.disablePolygonOffset();
    }
    );

    public static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING = new RenderStateShard.LayeringStateShard("view_offset_z_layering", () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.scale(0.0f);
    }, () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.popMatrix();
    });

    public static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING_FORWARD = new RenderStateShard.LayeringStateShard(
            "view_offset_z_layering_forward", () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.scale(2.0F / 4096.0F);
    }, () -> {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.popMatrix();
    }
    );
}
