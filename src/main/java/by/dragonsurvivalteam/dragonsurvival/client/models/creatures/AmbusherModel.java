package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.AmbusherEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.molang.MolangParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class AmbusherModel extends GeoModel<AmbusherEntity> {
    @Override
    public ResourceLocation getModelResource(AmbusherEntity animatable) {
        return new ResourceLocation(MODID, "geo/hunter_ambusher.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AmbusherEntity animatable) {
        return new ResourceLocation(MODID, "textures/entity/hunters/ambusher_on_horse.png");
    }

    @Override
    public ResourceLocation getAnimationResource(AmbusherEntity animatable) {
        return new ResourceLocation(MODID, "animations/hunter_ambusher.animation.json");
    }

    @Override
    public void handleAnimations(final AmbusherEntity animatable, long instanceId, final AnimationState<AmbusherEntity> animationState) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        MolangParser.INSTANCE.setValue("query.look_angle_x", () -> entityData.headPitch() * Mth.DEG_TO_RAD);
        MolangParser.INSTANCE.setValue("query.look_angle_y", () -> entityData.netHeadYaw() * Mth.DEG_TO_RAD);

        super.handleAnimations(animatable, instanceId, animationState);
    }
}
