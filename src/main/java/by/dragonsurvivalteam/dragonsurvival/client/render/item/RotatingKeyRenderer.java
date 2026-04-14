package by.dragonsurvivalteam.dragonsurvival.client.render.item;

import by.dragonsurvivalteam.dragonsurvival.common.items.RotatingKeyItem;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.math.MathParser;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class RotatingKeyRenderer extends GeoItemRenderer<RotatingKeyItem> {
    private static final float RAD_TO_DEG = 180.0f / (float)Math.PI;
    private static final float MIN_DIRECTION_LENGTH_SQUARED = 1.0E-12f;
    private static final Vector3f WORLD_UP = new Vector3f(0.0f, 1.0f, 0.0f);
    private static final String QUERY_X_ROTATION = "query.x_rotation";
    private static final String QUERY_Y_ROTATION = "query.y_rotation";
    private static final String QUERY_Z_ROTATION = "query.z_rotation";

    public static final DataTicket<Identifier> MODEL = DataTicket.create("model", Identifier.class);
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("texture", Identifier.class);
    public static final DataTicket<Boolean> HAS_TARGET = DataTicket.create("has_target", Boolean.class);
    private static final DataTicket<Float> QUERY_ROTATION_X = DataTicket.create("query_rotation_x", Float.class);
    private static final DataTicket<Float> QUERY_ROTATION_Y = DataTicket.create("query_rotation_y", Float.class);
    private static final DataTicket<Float> QUERY_ROTATION_Z = DataTicket.create("query_rotation_z", Float.class);

    public RotatingKeyRenderer() {
        super(new RotatingKeyModel());
    }

    @Override
    public void addRenderData(RotatingKeyItem animatable, GeoItemRenderer.RenderData relatedObject, GeoRenderState renderState, float partialTick) {
        renderState.addGeckolibData(MODEL, animatable.model);
        renderState.addGeckolibData(TEXTURE, animatable.texture);

        Vector3fc currentTarget = relatedObject.itemStack().get(DSDataComponents.TARGET_POSITION.get());
        ItemOwner itemOwner = relatedObject.itemOwner();
        boolean hasTarget = RotatingKeyItem.hasTarget(currentTarget);
        Orientation orientation = !hasTarget || itemOwner == null
            ? Orientation.NONE
            : calculateOrientation(currentTarget, itemOwner, partialTick);

        renderState.addGeckolibData(HAS_TARGET, hasTarget);
        renderState.addGeckolibData(QUERY_ROTATION_X, orientation.x());
        renderState.addGeckolibData(QUERY_ROTATION_Y, orientation.y());
        renderState.addGeckolibData(QUERY_ROTATION_Z, orientation.z());
    }

    @Override
    public void setMolangQueryValues(RotatingKeyItem animatable, GeoItemRenderer.RenderData relatedObject, GeoRenderState renderState, float partialTick) {
        super.setMolangQueryValues(animatable, relatedObject, renderState, partialTick);

        MathParser.setVariable(QUERY_X_ROTATION, state -> state.renderState().getOrDefaultGeckolibData(QUERY_ROTATION_X, 0.0f));
        MathParser.setVariable(QUERY_Y_ROTATION, state -> state.renderState().getOrDefaultGeckolibData(QUERY_ROTATION_Y, 0.0f));
        MathParser.setVariable(QUERY_Z_ROTATION, state -> state.renderState().getOrDefaultGeckolibData(QUERY_ROTATION_Z, 0.0f));
    }

    private static Orientation calculateOrientation(final Vector3fc target, final ItemOwner itemOwner, final float partialTick) {
        Vector3f vectorToTarget = getDirectionToTarget(target, itemOwner, partialTick);

        if (vectorToTarget == null) {
            return Orientation.NONE;
        }

        Quaternionf lookAtRotation = new Quaternionf().lookAlong(vectorToTarget, WORLD_UP);
        Vector3f eulerAngles = lookAtRotation.getEulerAnglesZXY(new Vector3f()).mul(RAD_TO_DEG);

        return new Orientation(
            eulerAngles.x + 180.0f,
            eulerAngles.y - getOwnerYawDegrees(itemOwner) - 90.0f,
            eulerAngles.z
        );
    }

    private static Vector3f getDirectionToTarget(final Vector3fc target, final ItemOwner itemOwner, final float partialTick) {
        Vec3 origin = getOrigin(itemOwner, partialTick);
        Vector3f vectorToTarget = new Vector3f(
            (float)(target.x() - origin.x),
            (float)(target.y() - origin.y),
            (float)(target.z() - origin.z)
        );

        if (vectorToTarget.lengthSquared() < MIN_DIRECTION_LENGTH_SQUARED) {
            return null;
        }

        return vectorToTarget.normalize();
    }

    private static Vec3 getOrigin(final ItemOwner itemOwner, final float partialTick) {
        LivingEntity livingEntity = itemOwner.asLivingEntity();

        if (livingEntity != null) {
            return livingEntity.getEyePosition(partialTick);
        }

        return itemOwner.position();
    }

    private static float getOwnerYawDegrees(final ItemOwner itemOwner) {
        LivingEntity livingEntity = itemOwner.asLivingEntity();

        if (livingEntity != null) {
            return livingEntity.getYRot();
        }

        return itemOwner.getVisualRotationYInDegrees();
    }

    private record Orientation(float x, float y, float z) {
        private static final Orientation NONE = new Orientation(0.0f, 0.0f, 0.0f);
    }
}
