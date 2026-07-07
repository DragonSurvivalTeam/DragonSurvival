package by.dragonsurvivalteam.dragonsurvival.common.items;

import com.geckolib.animatable.SingletonGeoAnimatable;
import net.minecraft.resources.Identifier;

public class HunterKeyItem extends RotatingKeyItem {
    public HunterKeyItem(Properties properties, Identifier model, Identifier texture, Identifier target) {
        super(properties, model, texture, target);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
}
