package by.dragonsurvivalteam.dragonsurvival.compat.create;

import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.mixins.create.PlayerSkyhookRendererAccessor;

import java.util.Set;
import java.util.UUID;

public class SkyhookRendererHelper {
    public static boolean isPlayerRidingSkyhook(UUID uuid) {
        if (Compat.isModLoaded(Compat.CREATE)) {
            Set<UUID> hangingPlayers = PlayerSkyhookRendererAccessor.dragonSurvival$getHangingPlayers();
            return hangingPlayers != null && hangingPlayers.contains(uuid);
        } else {
            return false;
        }
    }
}
