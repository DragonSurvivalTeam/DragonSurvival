package by.dragonsurvivalteam.dragonsurvival.client.models.projectiles;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericBallEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GenericBallModel extends GeoModel<GenericBallEntity> {
    @Override
    public ResourceLocation getModelResource(final GenericBallEntity animatable) {
        ResourceLocation resource = animatable.getModelResource();
        ResourceLocation path = new ResourceLocation(resource.getNamespace(), "geo/projectiles/" + resource.getPath() + ".geo.json");
        try {
            getBakedModel(path);
        } catch (Exception e) {
            DragonSurvival.LOGGER.error("Model not found for projectile: {}", path);
            return new ResourceLocation(resource.getNamespace(), "geo/projectiles/generic_ball.geo.json");
        }
        return new ResourceLocation(resource.getNamespace(), "geo/projectiles/" + resource.getPath() + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(final GenericBallEntity animatable) {
        ResourceLocation resource = animatable.getTextureResource();
        return new ResourceLocation(resource.getNamespace(), "textures/entity/projectiles/" + resource.getPath() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(final GenericBallEntity animatable) {
        ResourceLocation resource = animatable.getAnimationResource();
        return new ResourceLocation(resource.getNamespace(), "animations/projectiles/" + resource.getPath() + ".animation.json");
    }
}
