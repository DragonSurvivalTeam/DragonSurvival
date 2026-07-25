package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.LeaderEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class LeaderModel extends GeoModel<LeaderEntity> {
    @Override
    public ResourceLocation getModelResource(LeaderEntity object) {
        return new ResourceLocation(MODID, "geo/hunter_leader.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LeaderEntity object) {
        return new ResourceLocation(MODID, "textures/entity/hunters/leader.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LeaderEntity animatable) {
        return new ResourceLocation(MODID, "animations/hunter_leader.animation.json");
    }

    @Override
    public void handleAnimations(final LeaderEntity animatable, long instanceId, final AnimationState<LeaderEntity> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        MolangParser.INSTANCE.setValue("query.look_angle_x", () -> entityData.headPitch() * Mth.DEG_TO_RAD);
        MolangParser.INSTANCE.setValue("query.look_angle_y", () -> entityData.netHeadYaw() * Mth.DEG_TO_RAD);

        super.handleAnimations(animatable, instanceId, animationState);
    }
}
