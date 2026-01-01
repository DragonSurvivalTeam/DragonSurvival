package by.dragonsurvivalteam.dragonsurvival.client.models.projectiles;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericBallEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GenericBallModel extends GeoModel<GenericBallEntity> {
    @Override
    public Identifier getModelResource(final GenericBallEntity animatable) {
        Identifier resource = animatable.getModelResource();
        Identifier path = Identifier.fromNamespaceAndPath(resource.getNamespace(), "geo/projectiles/" + resource.getPath() + ".geo.json");
        try {
            getBakedModel(path);
        } catch (Exception e) {
            DragonSurvival.LOGGER.error("Model not found for projectile: {}", path);
            return Identifier.fromNamespaceAndPath(resource.getNamespace(), "geo/projectiles/generic_ball.geo.json");
        }
        return Identifier.fromNamespaceAndPath(resource.getNamespace(), "geo/projectiles/" + resource.getPath() + ".geo.json");
    }

    @Override
    public Identifier getTextureResource(final GenericBallEntity animatable) {
        Identifier resource = animatable.getTextureResource();
        return Identifier.fromNamespaceAndPath(resource.getNamespace(), "textures/entity/projectiles/" + resource.getPath() + ".png");
    }

    @Override
    public Identifier getAnimationResource(final GenericBallEntity animatable) {
        Identifier resource = animatable.getAnimationResource();
        return Identifier.fromNamespaceAndPath(resource.getNamespace(), "animations/projectiles/" + resource.getPath() + ".animation.json");
    }
}
