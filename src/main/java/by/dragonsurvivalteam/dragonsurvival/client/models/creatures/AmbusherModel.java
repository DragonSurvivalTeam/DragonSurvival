package by.dragonsurvivalteam.dragonsurvival.client.models.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.AmbusherEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.state.AnimationTest;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.loading.math.MathParser;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class AmbusherModel extends GeoModel<AmbusherEntity> {
    @Override
    public @NotNull Identifier getModelResource(@NotNull GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "geo/hunter_ambusher.geo.json");
    }

    @Override
    public @NotNull Identifier getTextureResource(@NotNull GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(MODID, "textures/entity/hunters/ambusher_on_horse.png");
    }

    @Override
    public @NotNull Identifier getAnimationResource(AmbusherEntity animatable) {
        return Identifier.fromNamespaceAndPath(MODID, "animations/hunter_ambusher.animation.json");
    }

    @Override
    public void applyMolangQueries(final AnimationTest<AmbusherEntity> animationState, double currentTick) {
        super.applyMolangQueries(animationState, currentTick);

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        MathParser.setVariable("query.look_angle_x", () -> entityData.headPitch() * Mth.DEG_TO_RAD);
        MathParser.setVariable("query.look_angle_y", () -> entityData.netHeadYaw() * Mth.DEG_TO_RAD);
    }
}
