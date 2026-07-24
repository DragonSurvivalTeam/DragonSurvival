package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.SpearmanEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class SpearmanModel extends GeoModel<SpearmanEntity> {
    @Override
    public ResourceLocation getModelResource(SpearmanEntity object) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "geo/hunter_spearman.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpearmanEntity object) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/hunters/spearman.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpearmanEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "animations/hunter_spearman.animation.json");
    }

    @Override
    public void applyMolangQueries(final AnimationState<SpearmanEntity> animationState, double currentTick) {
        super.applyMolangQueries(animationState, currentTick);

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        MathParser.setVariable("query.look_angle_x", () -> entityData.headPitch() * Mth.DEG_TO_RAD);
        MathParser.setVariable("query.look_angle_y", () -> entityData.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}
