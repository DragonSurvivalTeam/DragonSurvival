package by.dragonsurvivalteam.dragonsurvival.client.models.projectiles;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles.GenericBallRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericBallEntity;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

import java.util.Objects;

public class GenericBallModel extends GeoModel<GenericBallEntity> {
    @Override
    public @NotNull Identifier getModelResource(GeoRenderState renderState) {
        return Objects.requireNonNull(renderState.getGeckolibData(GenericBallRenderer.MODEL_RESOURCE));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Objects.requireNonNull(renderState.getGeckolibData(GenericBallRenderer.TEXTURE_RESOURCE));
    }

    @Override
    public Identifier getAnimationResource(final GenericBallEntity animatable) {
        return animatable.getAnimationResource();
    }
}
