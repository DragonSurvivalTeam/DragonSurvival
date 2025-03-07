package by.dragonsurvivalteam.dragonsurvival.compat.create;

import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import net.minecraft.world.entity.player.Player;

public class CardboardBoxHelper {
    public static boolean testForStealth(final Player player) {
        if (Compat.isModLoaded(Compat.CREATE)) {
            return CardboardArmorHandler.testForStealth(player);
        } else {
            return false;
        }
    }
}
