package by.dragonsurvivalteam.dragonsurvival.compat.create;

import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.mixins.create.PlayerSkyhookRendererAccessor;

import java.util.UUID;

public class SkyhookRendererHelper {
    public static boolean isPlayerRidingSkyhook(UUID uuid) {
        if (Compat.isModLoaded(Compat.CREATE)) {
            return PlayerSkyhookRendererAccessor.dragonSurvival$getHangingPlayers().contains(uuid);
        } else {
            return false;
        }
    }
}
