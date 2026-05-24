package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.FlatCubeBakeState;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.definition.geometry.Geometry;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Geometry.class)
public class GeometryMixin {
    @WrapMethod(method = "bake", remap = false)
    private BakedGeoModel dragonSurvival$bakeDragonSurvivalModel(final Identifier resource, final Operation<BakedGeoModel> original) {
        FlatCubeBakeState.setDragonSurvivalModelBake(
            resource != null && DragonSurvival.MODID.equals(resource.getNamespace())
        );

        try {
            return original.call(resource);
        } finally {
            FlatCubeBakeState.clearDragonSurvivalModelBake();
        }
    }
}
