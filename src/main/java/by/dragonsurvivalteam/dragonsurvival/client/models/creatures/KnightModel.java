package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.KnightEntity;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class KnightModel extends GeoModel<@NotNull KnightEntity> {
    @Override
    public @NotNull Identifier getModelResource(@NotNull GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_knight");
    }

    @Override
    public @NotNull Identifier getTextureResource(@NotNull GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/hunters/knight_on_horse.png");
    }

    @Override
    public @NotNull Identifier getAnimationResource(KnightEntity animatable) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_knight");
    }
}