package by.dragonsurvivalteam.dragonsurvival.common.items;

import net.minecraft.resources.Identifier;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;

public class HunterKeyItem extends RotatingKeyItem {
    public HunterKeyItem(Properties properties, Identifier model, Identifier texture, Identifier target) {
        super(properties, model, texture, target);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
}
