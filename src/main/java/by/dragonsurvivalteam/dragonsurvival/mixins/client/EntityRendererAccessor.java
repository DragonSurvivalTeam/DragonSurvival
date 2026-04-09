package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor {
    @Invoker("submitNameDisplay")
    void dragonSurvival$submitNameDisplay(
        EntityRenderState renderState,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        CameraRenderState camera,
        int offset
    );
}
