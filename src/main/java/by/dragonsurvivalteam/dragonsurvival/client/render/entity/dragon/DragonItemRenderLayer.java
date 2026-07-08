
package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.compat.bettercombat.BetterCombat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class DragonItemRenderLayer extends BlockAndItemGeoLayer<DragonEntity> {
    private final List<RenderInfo> renderInfos = new ArrayList<>();

    public DragonItemRenderLayer(GeoRenderer<DragonEntity> renderer, BiFunction<GeoBone, DragonEntity, ItemStack> stackForBone, BiFunction<GeoBone, DragonEntity, BlockState> blockForBone) {
        super(renderer, stackForBone, blockForBone);
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, DragonEntity animatable) {
        if (bone.getName().equals("RightItem")) {
            return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        }
        if (bone.getName().equals("LeftItem")) {
            return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        }

        return ItemDisplayContext.GROUND;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, DragonEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        if (BetterCombat.isAttacking(animatable.getPlayer())) {
            return;
        }

        if (ClientDragonRenderer.renderHeldItem && (animatable.getPlayer() != Minecraft.getInstance().player || !Minecraft.getInstance().options.getCameraType().isFirstPerson())) {
            poseStack.pushPose();

            if (bone.getName().equals("RightItem")) {
                Quaternionf rotation = new Quaternionf();
                rotation.rotateY((float) Math.toRadians(90));
                rotation.rotateX((float) Math.toRadians(60));
                poseStack.rotateAround(rotation, 0, 0, 0);
                poseStack.scale(0.75F, 0.75F, 0.75F);
            } else if (bone.getName().equals("LeftItem")) {
                Quaternionf rotation = new Quaternionf();
                rotation.rotateZ((float) Math.toRadians(90));
                rotation.rotateY((float) Math.toRadians(90));
                rotation.rotateX((float) Math.toRadians(-120d));
                poseStack.rotateAround(rotation, 0, 0, 0);
                poseStack.scale(0.75F, 0.75F, 0.75F);
            }

            renderInfos.add(new RenderInfo(stack, getTransformTypeForStack(bone, stack, animatable), new Matrix4f(poseStack.last().pose()), new Matrix3f(poseStack.last().normal())));
            poseStack.popPose();
        }
    }

    @Override
    public void render(PoseStack poseStack, DragonEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        for (RenderInfo renderInfo : renderInfos) {
            poseStack.pushPose();
            poseStack.last().pose().set(renderInfo.poseMatrix());
            poseStack.last().normal().set(renderInfo.normalMatrix());
            Minecraft.getInstance().getItemRenderer().renderStatic(animatable.getPlayer(), renderInfo.stack(), renderInfo.displayContext(), false, poseStack, bufferSource, animatable.level(), packedLight, packedOverlay, (int) renderer.getInstanceId(animatable));
            poseStack.popPose();
        }

        renderInfos.clear();
    }
}
