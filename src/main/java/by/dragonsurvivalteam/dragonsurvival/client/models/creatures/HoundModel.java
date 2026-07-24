package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.HoundEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class HoundModel extends GeoModel<HoundEntity> {
    @Override
    public ResourceLocation getModelResource(HoundEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "geo/hunter_hound.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HoundEntity animatable) {
        String houndName = switch (animatable.getVariety()) {
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
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/hunters/hounds/" + houndName + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(HoundEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "animations/hunter_hound.animation.json");
    }
}
