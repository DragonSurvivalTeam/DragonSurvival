//package by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures;
//
//import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.KnightEntity;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.math.Axis;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.entity.state.EntityRenderState;
//import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.item.ItemDisplayContext;
//import net.minecraft.world.item.ItemStack;
//import org.jetbrains.annotations.Nullable;
//import com.geckolib.animatable.GeoAnimatable;
//import com.geckolib.cache.model.BakedGeoModel;
//import com.geckolib.renderer.base.GeoRenderState;
//import com.geckolib.renderer.base.GeoRenderer;
//import com.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;
//
//import java.util.List;
//
//public class CustomBlockAndItemGeoLayer<R extends EntityRenderState & GeoRenderState> extends BlockAndItemGeoLayer<Entity, R> {
//    public CustomBlockAndItemGeoLayer(final GeoRenderer<T> renderer) {
//        super(renderer);
//    }
//
//    @Override
//    protected void renderStackForBone(final PoseStack poseStack, final GeoBone bone, final ItemStack stack, final T animatable, final MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
//        poseStack.pushMatrix();
//
//        if (animatable instanceof KnightEntity) {
//            if (bone.getName().equalsIgnoreCase("left_item")) {
//                // Shield
//                poseStack.mulPose(Axis.ZP.rotationDegrees(180)); // Turn shield around (handle towards entity body)
//                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
//                poseStack.translate(0, 0, -1);
//            } else {
//                // Sword
//                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
//            }
//        }
//
//        if (animatable instanceof LivingEntity livingEntity) {
//            Minecraft.getInstance().getItemRenderer().renderStatic(livingEntity, stack, getTransformTypeForStack(bone, stack, animatable), false, poseStack, bufferSource, livingEntity.level(), packedLight, packedOverlay, livingEntity.getId());
//        } else {
//            Minecraft.getInstance().getItemRenderer().renderStatic(stack, getTransformTypeForStack(bone, stack, animatable), packedLight, packedOverlay, poseStack, bufferSource, Minecraft.getInstance().level, (int) this.renderer.getInstanceId(animatable));
//        }
//
//        poseStack.popMatrix();
//    }
//
//    @Override
//    protected ItemDisplayContext getTransformTypeForStack(final GeoBone bone, final ItemStack stack, final T animatable) {
//        if (bone.getName().equalsIgnoreCase("left_item")) {
//            return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
//        } else if (bone.getName().equalsIgnoreCase("right_item")) {
//            return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
//        }
//
//        return ItemDisplayContext.NONE;
//    }
//
//    @Override
//    protected @Nullable ItemStack getStackForBone(final GeoBone bone, final T animatable) {
//        if (bone != null && animatable instanceof LivingEntity livingEntity) {
//            if (bone.getName().equalsIgnoreCase("left_item")) {
//                return livingEntity.getOffhandItem();
//            } else if (bone.getName().equalsIgnoreCase("right_item")) {
//                return livingEntity.getMainHandItem();
//            }
//        }
//
//        return null;
//    }
//
//    @Override
//    protected List<RenderData> getRelevantBones(GeoRenderState renderState, BakedGeoModel model) {
//        return List.of();
//    }
//
//    @Override
//    public void addRenderData(GeoAnimatable animatable, @Nullable Object relatedObject, GeoRenderState renderState, float partialTick) {
//
//    }
//}
