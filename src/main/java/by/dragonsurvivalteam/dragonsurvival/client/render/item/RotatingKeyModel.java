package by.dragonsurvivalteam.dragonsurvival.client.render.item;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.items.RotatingKeyItem;
import net.minecraft.resources.Identifier;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

public class RotatingKeyModel extends GeoModel<RotatingKeyItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getGeckolibData(RotatingKeyRenderer.MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getGeckolibData(RotatingKeyRenderer.TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(RotatingKeyItem object) {
        return DragonSurvival.res("animations/key.animation.json");
    }
}