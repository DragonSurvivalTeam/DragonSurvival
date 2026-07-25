package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.GriffinEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class GriffinModel extends GeoModel<GriffinEntity> {
    @Override
    public ResourceLocation getModelResource(GriffinEntity animatable) {
        return new ResourceLocation(MODID, "geo/hunter_griffin.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GriffinEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/hunters/griffins/hunter_griffin_1.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GriffinEntity animatable) {
        return new ResourceLocation(MODID, "animations/hunter_griffin.animation.json");
    }
}
