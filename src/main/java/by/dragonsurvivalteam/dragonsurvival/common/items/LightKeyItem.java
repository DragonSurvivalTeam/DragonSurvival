package by.dragonsurvivalteam.dragonsurvival.common.items;

import net.minecraft.resources.Identifier;
import com.geckolib.animatable.SingletonGeoAnimatable;

public class LightKeyItem extends RotatingKeyItem {
    public LightKeyItem(Properties properties, Identifier model, Identifier texture, Identifier target) {
        super(properties, model, texture, target);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
}
