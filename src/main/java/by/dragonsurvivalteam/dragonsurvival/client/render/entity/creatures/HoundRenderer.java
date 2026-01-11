package by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.HoundEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.profiling.Profiler;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;

public class HoundRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<HoundEntity, R> {
    // Data tickets
    public static DataTicket<Integer> VARIETY = DataTicket.create("variety", Integer.class);

    public HoundRenderer(EntityRendererProvider.Context renderManager, GeoModel<HoundEntity> model) {
        super(renderManager, model);
    }

    @Override
    public void addRenderData(HoundEntity animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(VARIETY, animatable.getVariety());
    }

    @Override
    public void preRenderPass(@NotNull RenderPassInfo<@NotNull R> renderPassInfo, @NotNull SubmitNodeCollector renderTasks) {
        Profiler.get().push("hound");
        super.preRenderPass(renderPassInfo, renderTasks);
    }

    @Override
    public void postRenderPass(@NotNull RenderPassInfo<@NotNull R> renderPassInfo, @NotNull SubmitNodeCollector renderTasks) {
        super.postRenderPass(renderPassInfo, renderTasks);
        Profiler.get().pop();
    }
}
