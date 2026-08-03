package by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.KnightEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class CustomBlockAndItemGeoLayer<R extends LivingEntityRenderState & GeoRenderState> extends BlockAndItemGeoLayer<KnightEntity, Void, R> {
    private static final String RIGHT_ITEM_BONE = "right_item";
    private static final DataTicket<Boolean> IS_SHIELD = DataTicket.create("knight_right_hand_shield", Boolean.class);

    public CustomBlockAndItemGeoLayer(final EntityRendererProvider.Context context, final GeoRenderer<KnightEntity, Void, R> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(final KnightEntity animatable, final @Nullable Void relatedObject, final R renderState, final float partialTick) {
        ItemStack stack = animatable.getOffhandItem();

        if (stack.isEmpty()) {
            return List.of();
        }

        renderState.addGeckolibData(IS_SHIELD, stack.getItem() instanceof ShieldItem);

        return List.of(RenderData.item(
            RIGHT_ITEM_BONE,
            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
            RenderUtil.createRenderStateForItem(stack, this.itemModelResolver, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, animatable)
        ));
    }

    @Override
    public void addRenderData(final KnightEntity animatable, final @Nullable Void relatedObject, final R renderState, final float partialTick) {
        List<RenderData> contents = getRelevantBones(animatable, relatedObject, renderState, partialTick);

        if (!contents.isEmpty()) {
            renderState.addGeckolibData(CONTENTS, contents);
        }
    }

    @Override
    protected void submitItemStackRender(
        final PoseStack poseStack,
        final GeoBone bone,
        final ItemStackRenderState stackState,
        final ItemDisplayContext displayContext,
        final R renderState,
        final SubmitNodeCollector renderTasks,
        final int packedLight
    ) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.XN.rotationDegrees(90));
        poseStack.translate(0, 0.125, -0.0625);

        if (renderState.getOrDefaultGeckolibData(IS_SHIELD, false)) {
            poseStack.translate(0, 0.125, -0.25);
        }

        super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, packedLight);
        poseStack.popPose();
    }
}
