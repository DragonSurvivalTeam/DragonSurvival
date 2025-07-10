package by.dragonsurvivalteam.dragonsurvival.common.items;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public class LightKeyItem extends RotatingKeyItem {
    public LightKeyItem(Properties properties, ResourceLocation model, ResourceLocation texture, ResourceLocation target) {
        super(properties, model, texture, target);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
}
