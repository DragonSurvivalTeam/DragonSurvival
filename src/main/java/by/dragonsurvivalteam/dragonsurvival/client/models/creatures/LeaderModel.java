package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.LeaderEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class LeaderModel extends GeoModel<LeaderEntity> {
    @Override
    public ResourceLocation getModelResource(LeaderEntity object) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "geo/hunter_leader.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LeaderEntity object) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/hunters/leader.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LeaderEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "animations/hunter_leader.animation.json");
    }

    @Override
    public void applyMolangQueries(final AnimationState<LeaderEntity> animationState, double currentTick) {
        super.applyMolangQueries(animationState, currentTick);

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        MathParser.setVariable("query.look_angle_x", () -> entityData.headPitch() * Mth.DEG_TO_RAD);
        MathParser.setVariable("query.look_angle_y", () -> entityData.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}
