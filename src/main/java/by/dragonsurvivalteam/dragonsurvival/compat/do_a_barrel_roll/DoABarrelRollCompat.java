package by.dragonsurvivalteam.dragonsurvival.compat.do_a_barrel_roll;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import nl.enjarai.doabarrelroll.api.RollEntity;
import org.jetbrains.annotations.Nullable;

public final class DoABarrelRollCompat {
    private DoABarrelRollCompat() {}

    private static boolean isDragonGliding(@Nullable final Player player) {
        return player != null && DragonStateProvider.isDragon(player) && ServerFlightHandler.isGliding(player);
    }

    public static boolean shouldEnableDragonFlight(@Nullable final Player player) {
        return ClientFlightHandler.barrelRollCompatibility && ModID.DO_A_BARREL_ROLL.isLoaded() && isDragonGliding(player);
    }

    public static boolean isActive(@Nullable final Player player) {
        return shouldEnableDragonFlight(player) && Loaded.isRolling(player);
    }

    public static float getRollRadians(final Player player, final float partialTick) {
        return Loaded.getRoll(player, partialTick) * Mth.DEG_TO_RAD;
    }

    private static final class Loaded {
        private Loaded() {}

        private static boolean isRolling(final Player player) {
            return player instanceof RollEntity rollingPlayer && rollingPlayer.doABarrelRoll$isRolling();
        }

        private static float getRoll(final Player player, final float partialTick) {
            return ((RollEntity) player).doABarrelRoll$getRoll(partialTick);
        }
    }
}
