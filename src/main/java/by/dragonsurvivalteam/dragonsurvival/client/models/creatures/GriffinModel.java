package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.GriffinEntity;
import net.minecraft.resources.Identifier;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class GriffinModel extends GeoModel<GriffinEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_griffin");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/hunters/griffins/hunter_griffin_1.png");
    }

    @Override
    public Identifier getAnimationResource(GriffinEntity animatable) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_griffin");
    }
}
