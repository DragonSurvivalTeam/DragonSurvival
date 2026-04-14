package by.dragonsurvivalteam.dragonsurvival.client.render.item;

import by.dragonsurvivalteam.dragonsurvival.common.items.RotatingKeyItem;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class RotatingKeyRenderer extends GeoItemRenderer<RotatingKeyItem> {
    private static final float KEY_X_OFFSET = Mth.PI;
    private static final float KEY_Y_OFFSET = Mth.HALF_PI;

    public static final DataTicket<Identifier> MODEL = DataTicket.create("model", Identifier.class);
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("texture", Identifier.class);
    private static final DataTicket<Float> ROOT_ROT_X = DataTicket.create("root_rot_x", Float.class);
    private static final DataTicket<Float> ROOT_ROT_Y = DataTicket.create("root_rot_y", Float.class);
    private static final DataTicket<Float> ROOT_ROT_Z = DataTicket.create("root_rot_z", Float.class);

    public RotatingKeyRenderer() {
        super(new RotatingKeyModel());
    }

    @Override
    public void addRenderData(RotatingKeyItem animatable, GeoItemRenderer.RenderData relatedObject, GeoRenderState renderState, float partialTick) {
        renderState.addGeckolibData(MODEL, animatable.model);
        renderState.addGeckolibData(TEXTURE, animatable.texture);

        Vector3fc currentTarget = relatedObject.itemStack().get(DSDataComponents.TARGET_POSITION.get());
        ItemOwner itemOwner = relatedObject.itemOwner();

        if (!RotatingKeyItem.hasTarget(currentTarget) || itemOwner == null) {
            applyOrientation(renderState, Orientation.NONE);
            return;
        }

        applyOrientation(renderState, calculateOrientation(currentTarget, itemOwner, partialTick));
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<GeoRenderState> renderPassInfo, BoneSnapshots snapshots) {
        snapshots.ifPresent("root", root -> root.setRotation(
            renderPassInfo.getOrDefaultGeckolibData(ROOT_ROT_X, 0.0f),
            renderPassInfo.getOrDefaultGeckolibData(ROOT_ROT_Y, 0.0f),
            renderPassInfo.getOrDefaultGeckolibData(ROOT_ROT_Z, 0.0f)
        ));
    }

    private static Orientation calculateOrientation(final Vector3fc target, final ItemOwner itemOwner, final float partialTick) {
        Vec3 origin = getOrigin(itemOwner, partialTick);
        float dx = (float)(target.x() - origin.x);
        float dy = (float)(target.y() - origin.y);
        float dz = (float)(target.z() - origin.z);
        float distanceSquared = dx * dx + dy * dy + dz * dz;

        if (distanceSquared < 1.0E-12f) {
            return Orientation.NONE;
        }

        Vector3f vectorToTarget = new Vector3f(dx, dy, dz).normalize();
        Quaternionf lookAtRotation = new Quaternionf().lookAlong(vectorToTarget, new Vector3f(0.0f, 1.0f, 0.0f));
        Vector3f eulerAngles = lookAtRotation.getEulerAnglesZXY(new Vector3f());
        float ownerYaw = getOwnerYaw(itemOwner, partialTick);

        return new Orientation(
            eulerAngles.x + KEY_X_OFFSET,
            eulerAngles.y - ownerYaw - KEY_Y_OFFSET,
            eulerAngles.z
        );
    }

    private static Vec3 getOrigin(final ItemOwner itemOwner, final float partialTick) {
        LivingEntity livingEntity = itemOwner.asLivingEntity();

        if (livingEntity != null) {
            return livingEntity.getEyePosition(partialTick);
        }

        return itemOwner.position();
    }

    private static float getOwnerYaw(final ItemOwner itemOwner, final float partialTick) {
        LivingEntity livingEntity = itemOwner.asLivingEntity();

        if (livingEntity != null) {
            return livingEntity.getViewYRot(partialTick) * Mth.DEG_TO_RAD;
        }

        return itemOwner.getVisualRotationYInDegrees() * Mth.DEG_TO_RAD;
    }

    private static void applyOrientation(final GeoRenderState renderState, final Orientation orientation) {
        renderState.addGeckolibData(ROOT_ROT_X, orientation.x());
        renderState.addGeckolibData(ROOT_ROT_Y, orientation.y());
        renderState.addGeckolibData(ROOT_ROT_Z, orientation.z());
    }

    private record Orientation(float x, float y, float z) {
        private static final Orientation NONE = new Orientation(0.0f, 0.0f, 0.0f);
    }
}
