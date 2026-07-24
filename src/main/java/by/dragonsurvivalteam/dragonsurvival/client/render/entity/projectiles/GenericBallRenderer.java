package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericBallEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GenericBallRenderer extends GeoEntityRenderer<GenericBallEntity> {
    public GenericBallRenderer(final EntityRendererProvider.Context renderManager, final GeoModel<GenericBallEntity> model) {
        super(renderManager, model);
    }

    @Override
    protected int getBlockLightLevel(@NotNull final GenericBallEntity entity, @NotNull final BlockPos position) {
        return 15;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull final GenericBallEntity entity) {
        ResourceLocation resource = animatable.getTextureResource();
        return ResourceLocation.fromNamespaceAndPath(resource.getNamespace(), "textures/entity/projectiles/" + resource.getPath() + ".png");
    }
}
