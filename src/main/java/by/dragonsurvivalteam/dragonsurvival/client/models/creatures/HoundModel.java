package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures.HoundRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.HoundEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class HoundModel extends GeoModel<HoundEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_hound");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        String houndName = switch (renderState.getGeckolibData(HoundRenderer.VARIETY)) {
            case 0 -> "hound_1";
            case 1 -> "hound_2";
            case 2 -> "hound_3";
            case 3 -> "hound_4";
            case 4 -> "hound_5";
            case 5 -> "hound_6";
            case 6 -> "hound_7";
            case 7 -> "hound_8";
            default -> "hound_hector";
        };
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/hunters/hounds/" + houndName + ".png");
    }

    @Override
    public Identifier getAnimationResource(HoundEntity animatable) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_hound");
    }
}
