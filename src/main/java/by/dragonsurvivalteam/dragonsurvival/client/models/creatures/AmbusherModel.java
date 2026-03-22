package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.AmbusherEntity;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class AmbusherModel extends GeoModel<@NotNull AmbusherEntity> {
    @Override
    public @NotNull Identifier getModelResource(@NotNull GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "models/hunter_ambusher");
    }

    @Override
    public @NotNull Identifier getTextureResource(@NotNull GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/hunters/ambusher_on_horse.png");
    }

    @Override
    public @NotNull Identifier getAnimationResource(AmbusherEntity animatable) {
        return Identifier.fromNamespaceAndPath(MODID, "animations/hunter_ambusher");
    }
}
