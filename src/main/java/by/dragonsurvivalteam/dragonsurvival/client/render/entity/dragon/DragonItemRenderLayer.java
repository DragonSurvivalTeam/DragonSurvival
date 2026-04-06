package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DragonItemRenderLayer<R extends LivingEntityRenderState & GeoRenderState> extends BlockAndItemGeoLayer<DragonEntity, Void, R> {
    public DragonItemRenderLayer(final EntityRendererProvider.Context context, final GeoRenderer<DragonEntity, Void, R> renderer) {
        super(context, renderer);
    }

    @Override
    protected List<RenderData> getRelevantBones(final DragonEntity animatable, final @Nullable Void relatedObject, final R renderState, final float partialTick) {
        List<RenderData> relevantBones = new ArrayList<>(2);
        String rightBone = ClientDragonRenderer.renderItemsInMouth ? "RightItem_jaw" : "RightItem";
        String leftBone = ClientDragonRenderer.renderItemsInMouth ? "LeftItem_jaw" : "LeftItem";
        RenderData rightHand = createItemRenderData(rightBone, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, animatable.getMainHandItem(), animatable.getPlayer());
        RenderData leftHand = createItemRenderData(leftBone, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, animatable.getOffhandItem(), animatable.getPlayer());

        if (rightHand != null) {
            relevantBones.add(rightHand);
        }

        if (leftHand != null) {
            relevantBones.add(leftHand);
        }

        return relevantBones;
    }

    @Override
    public void addRenderData(final DragonEntity animatable, final @Nullable Void relatedObject, final R renderState, final float partialTick) {
        List<RenderData> contents = getRelevantBones(animatable, relatedObject, renderState, partialTick);

        if (!contents.isEmpty()) {
            renderState.addGeckolibData(BlockAndItemGeoLayer.CONTENTS, contents);
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
        DragonRenderer.DragonRenderData dragonRenderData = renderState.getGeckolibData(DragonRenderer.DRAGON_RENDER_DATA);

        if (dragonRenderData == null || dragonRenderData.player() == null) {
            return;
        }

        if (!ClientDragonRenderer.renderHeldItem) {
            return;
        }

        if (dragonRenderData.player() == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return;
        }

        poseStack.pushPose();

        if (bone.name().equals("RightItem") || bone.name().equals("RightItem_jaw")) {
            Quaternionf rotation = new Quaternionf();
            rotation.rotateY((float)Math.toRadians(90));
            rotation.rotateX((float)Math.toRadians(60));
            poseStack.mulPose(rotation);
            poseStack.scale(0.75F, 0.75F, 0.75F);
        } else if (bone.name().equals("LeftItem") || bone.name().equals("LeftItem_jaw")) {
            Quaternionf rotation = new Quaternionf();
            rotation.rotateZ((float)Math.toRadians(90));
            rotation.rotateY((float)Math.toRadians(90));
            rotation.rotateX((float)Math.toRadians(-120));
            poseStack.mulPose(rotation);
            poseStack.scale(0.75F, 0.75F, 0.75F);
        }

        super.submitItemStackRender(poseStack, bone, stackState, displayContext, renderState, renderTasks, packedLight);
        poseStack.popPose();
    }

    private @Nullable RenderData createItemRenderData(final String boneName, final ItemDisplayContext displayContext, final ItemStack stack, final Entity owner) {
        if (stack.isEmpty()) {
            return null;
        }

        return RenderData.item(boneName, displayContext, RenderUtil.createRenderStateForItem(stack, this.itemModelResolver, displayContext, owner));
    }
}
