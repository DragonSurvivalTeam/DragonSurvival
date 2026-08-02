package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.SpearmanEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class SpearmanModel extends GeoModel<SpearmanEntity> {
    @Override
    public ResourceLocation getModelResource(SpearmanEntity object) {
        return new ResourceLocation(MODID, "geo/hunter_spearman.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SpearmanEntity object) {
        return new ResourceLocation(MODID, "textures/entity/hunters/spearman.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SpearmanEntity animatable) {
        return new ResourceLocation(MODID, "animations/hunter_spearman.animation.json");
    }

    @Override
    public void handleAnimations(final SpearmanEntity animatable, long instanceId, final AnimationState<SpearmanEntity> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        MolangParser.INSTANCE.setValue("query.look_angle_x", () -> entityData.headPitch() * Mth.DEG_TO_RAD);
        MolangParser.INSTANCE.setValue("query.look_angle_y", () -> entityData.netHeadYaw() * Mth.DEG_TO_RAD);

        super.handleAnimations(animatable, instanceId, animationState);
    }
}
