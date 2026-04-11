package by.dragonsurvivalteam.dragonsurvival.client.render.item;

import by.dragonsurvivalteam.dragonsurvival.common.items.RotatingKeyItem;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.math.MathParser;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.base.GeoRenderState;

public class RotatingKeyRenderer extends GeoItemRenderer<RotatingKeyItem> {

    // Data tickets
    public static DataTicket<Identifier> MODEL = DataTicket.create("model", Identifier.class);
    public static DataTicket<Identifier> TEXTURE = DataTicket.create("texture", Identifier.class);

    public RotatingKeyRenderer() {
        super(new RotatingKeyModel());
    }

    @Override
    public void setMolangQueryValues(RotatingKeyItem animatable, GeoItemRenderer.RenderData relatedObject, GeoRenderState renderState, float partialTick) {
        super.setMolangQueryValues(animatable, relatedObject, renderState, partialTick);

        Vector3f target = new Vector3f(animatable.currentTarget);
        Vector3f vectorTo = target.sub(animatable.playerHoldingItem.getEyePosition(partialTick).toVector3f()).normalize();

        Quaternionf lookAtRot = new Quaternionf();
        lookAtRot.lookAlong(vectorTo, Direction.UP.step());
        Vector3f eulerAngles = new Vector3f();
        lookAtRot.getEulerAnglesZXY(eulerAngles);

        eulerAngles.mul(180 / (float) Math.PI);

        MathParser.setVariable("query.x_rotation", state -> eulerAngles.x + 180);
        MathParser.setVariable("query.y_rotation", state -> eulerAngles.y - animatable.playerHoldingItem.getYRot() - 90);
        MathParser.setVariable("query.z_rotation", state -> eulerAngles.z);
    }

    @Override
    public void addRenderData(RotatingKeyItem animatable, GeoItemRenderer.RenderData relatedObject, GeoRenderState renderState, float partialTick) {
        renderState.addGeckolibData(MODEL, animatable.model);
        renderState.addGeckolibData(TEXTURE, animatable.texture);
    }
}