package by.dragonsurvivalteam.dragonsurvival.client.render.item;

import by.dragonsurvivalteam.dragonsurvival.common.items.RotatingKeyItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class RotatingKeyRenderer extends GeoItemRenderer<RotatingKeyItem> {
    public RotatingKeyRenderer() {
        super(new RotatingKeyModel());
    }

    @Override
    public void preRender(PoseStack poseStack, RotatingKeyItem animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model,bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        if(animatable.playerHoldingItem == null) {
            return;
        }

        Vector3f target = new Vector3f(animatable.currentTarget);
        Vector3f vectorTo = target.sub(animatable.playerHoldingItem.getEyePosition(partialTick).toVector3f()).normalize();

        Quaternionf lookAtRot = new Quaternionf();
        lookAtRot.lookAlong(vectorTo, new Vector3f(0.0f, 1.0f, 0.0f));
        Vector3f eulerAngles = new Vector3f();
        lookAtRot.getEulerAnglesZXY(eulerAngles);

        eulerAngles.mul(180 / (float) java.lang.Math.PI);

        MathParser.setVariable("query.x_rotation", () -> eulerAngles.x + 180);
        MathParser.setVariable("query.y_rotation", () -> eulerAngles.y - animatable.playerHoldingItem.getYRot() - 90);
        MathParser.setVariable("query.z_rotation", () -> eulerAngles.z);
    }
}