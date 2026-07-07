package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.SpearmanEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class SpearmanModel extends GeoModel<SpearmanEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_spearman");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/hunters/spearman.png");
    }

    @Override
    public Identifier getAnimationResource(SpearmanEntity animatable) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_spearman");
    }
}
