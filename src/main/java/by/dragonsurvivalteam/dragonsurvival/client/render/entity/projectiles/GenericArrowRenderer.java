package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GenericArrowRenderer extends ArrowRenderer<GenericArrowEntity> {
    public GenericArrowRenderer(final EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull final GenericArrowEntity arrow) {
        ResourceLocation resource = arrow.getResource();
        return ResourceLocation.fromNamespaceAndPath(resource.getNamespace(), "textures/entity/projectiles/" + resource.getPath() + ".png");
    }
}
