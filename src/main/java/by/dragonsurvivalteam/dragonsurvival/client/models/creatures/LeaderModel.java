package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.LeaderEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class LeaderModel extends GeoModel<LeaderEntity> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_leader");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/hunters/leader.png");
    }

    @Override
    public Identifier getAnimationResource(LeaderEntity animatable) {
        return Identifier.fromNamespaceAndPath(MODID, "hunter_leader");
    }
}
