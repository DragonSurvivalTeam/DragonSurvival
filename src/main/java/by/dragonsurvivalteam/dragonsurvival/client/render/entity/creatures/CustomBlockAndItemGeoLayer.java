package by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.KnightEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;

public class CustomBlockAndItemGeoLayer<T extends GeoAnimatable> extends BlockAndItemGeoLayer<T> {
    public CustomBlockAndItemGeoLayer(final GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    protected void renderStackForBone(final PoseStack poseStack, final GeoBone bone, final ItemStack stack, final T animatable, final MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        if (animatable instanceof KnightEntity) {
            if (bone.getName().equalsIgnoreCase("left_item")) {
                // Shield
                poseStack.mulPose(Axis.ZP.rotationDegrees(180)); // Turn shield around (handle towards entity body)
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.translate(0, 0, -1);
            } else {
                // Sword
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            }
        }

        if (animatable instanceof LivingEntity livingEntity) {
            Minecraft.getInstance().getItemRenderer().renderStatic(livingEntity, stack, getTransformTypeForStack(bone, stack, animatable), false, poseStack, bufferSource, livingEntity.level(), packedLight, packedOverlay, livingEntity.getId());
        } else {
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, getTransformTypeForStack(bone, stack, animatable), packedLight, packedOverlay, poseStack, bufferSource, Minecraft.getInstance().level, (int) this.renderer.getInstanceId(animatable));
        }

        poseStack.popPose();
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(final GeoBone bone, final ItemStack stack, final T animatable) {
        if (bone.getName().equalsIgnoreCase("left_item")) {
            return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        } else if (bone.getName().equalsIgnoreCase("right_item")) {
            return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        }

        return ItemDisplayContext.NONE;
    }

    @Nullable @Override
    protected ItemStack getStackForBone(final GeoBone bone, final T animatable) {
        if (bone != null && animatable instanceof LivingEntity livingEntity) {
            if (bone.getName().equalsIgnoreCase("left_item")) {
                return livingEntity.getOffhandItem();
            } else if (bone.getName().equalsIgnoreCase("right_item")) {
                return livingEntity.getMainHandItem();
            }
        }

        return null;
    }
}
