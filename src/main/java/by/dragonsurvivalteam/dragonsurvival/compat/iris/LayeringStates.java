package by.dragonsurvivalteam.dragonsurvival.compat.iris;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderStateShard;

// TODO :: This is from 1.21.4. When we update, we can remove this.
public class LayeringStates {
    public static final RenderStateShard.LayeringStateShard VIEW_OFFSET_Z_LAYERING_FORWARD = new RenderStateShard.LayeringStateShard(
            "view_offset_z_layering_forward", () -> {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.scale(2.0F / 4096.0F, 2.0F / 4096.0F, 2.0F / 4096.0F);
        RenderSystem.applyModelViewMatrix();
    }, () -> {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
    );
}
