package by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.AmbusherEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import com.geckolib.constant.DataTickets;
import com.geckolib.loading.math.MathParser;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

public class AmbusherRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<AmbusherEntity, R> {
    public AmbusherRenderer(EntityRendererProvider.Context renderManager, GeoModel<AmbusherEntity> model) {
        super(renderManager, model);
    }

    @Override
    public void preRenderPass(@NotNull RenderPassInfo<@NotNull R> renderPassInfo, @NotNull SubmitNodeCollector renderTasks) {
        Profiler.get().push("ambusher");
        super.preRenderPass(renderPassInfo, renderTasks);
    }

    @Override
    public void postRenderPass(@NotNull RenderPassInfo<@NotNull R> renderPassInfo, @NotNull SubmitNodeCollector renderTasks) {
        super.postRenderPass(renderPassInfo, renderTasks);
        Profiler.get().pop();
    }

    @Override
    public void setMolangQueryValues(AmbusherEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        super.setMolangQueryValues(animatable, relatedObject, renderState, partialTick);

        float entityPitch = renderState.getGeckolibData(DataTickets.ENTITY_PITCH);
        float entityYaw = renderState.getGeckolibData(DataTickets.ENTITY_YAW);
        MathParser.setVariable("query.look_angle_x", controllerState -> entityPitch * Mth.DEG_TO_RAD);
        MathParser.setVariable("query.look_angle_y", controllerState -> entityYaw * Mth.DEG_TO_RAD);
    }
}
