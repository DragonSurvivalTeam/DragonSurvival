package by.dragonsurvivalteam.dragonsurvival.common.items;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public class DarkKeyItem extends RotatingKeyItem {
    public DarkKeyItem(Properties properties, Identifier model, Identifier texture, Identifier target) {
        super(properties, model, texture, target);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
}
