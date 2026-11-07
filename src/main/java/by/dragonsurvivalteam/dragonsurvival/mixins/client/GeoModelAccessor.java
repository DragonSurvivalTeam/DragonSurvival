package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.model.GeoModel;

@Mixin(value = GeoModel.class, remap = false)
public interface GeoModelAccessor {
    @Accessor("lastRenderedInstance")
    void dragonSurvival$setLastRenderedInstance(long instanceId);
}
