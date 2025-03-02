package by.dragonsurvivalteam.dragonsurvival.compat.create;

import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import net.minecraft.world.entity.Entity;

public class CardboardBoxHelper {
    public static boolean testForStealth(final Entity entity) {
        if (Compat.isModLoaded(Compat.CREATE)) {
            return CardboardArmorHandler.testForStealth(entity);
        } else {
            return false;
        }
    }
}
