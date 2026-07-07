package by.dragonsurvivalteam.dragonsurvival.common.items;

import com.geckolib.animatable.SingletonGeoAnimatable;
import net.minecraft.resources.Identifier;

public class LightKeyItem extends RotatingKeyItem {
    public LightKeyItem(Properties properties, Identifier model, Identifier texture, Identifier target) {
        super(properties, model, texture, target);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
}
