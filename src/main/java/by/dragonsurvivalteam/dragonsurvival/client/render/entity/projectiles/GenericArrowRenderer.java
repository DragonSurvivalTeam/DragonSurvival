//package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;
//
//import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericArrowEntity;
//import net.minecraft.client.renderer.entity.ArrowRenderer;
//import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.resources.Identifier;
//import org.jetbrains.annotations.NotNull;
//
//public class GenericArrowRenderer extends ArrowRenderer<GenericArrowEntity> {
//    public GenericArrowRenderer(final EntityRendererProvider.Context context) {
//        super(context);
//    }
//
//    @Override
//    public @NotNull Identifier getTextureLocation(@NotNull final GenericArrowEntity arrow) {
//        Identifier resource = arrow.getResource();
//        return Identifier.fromNamespaceAndPath(resource.getNamespace(), "textures/entity/projectiles/" + resource.getPath() + ".png");
//    }
//}
