package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class DragonUtils {
    public static Holder<DragonSpecies> getType(Player entity) {
        return DragonStateProvider.getData(entity).species();
    }

    public static Holder<DragonSpecies> getType(DragonStateHandler handler) {
        return handler.species();
    }

    public static Holder<DragonBody> getBody(Player player) {
        return getBody(DragonStateProvider.getData(player));
    }

    public static Holder<DragonBody> getBody(DragonStateHandler handler) {
        return handler.body();
    }

    public static boolean isBody(final DragonStateHandler data, final Holder<DragonBody> typeToCheck) {
        if (data == null) {
            return false;
        }

        return isBody(data.body(), typeToCheck);
    }

    public static boolean isBody(final Holder<DragonBody> playerBody, final Holder<DragonBody> typeToCheck) {
        if (playerBody == typeToCheck) {
            return true;
        }

        if (playerBody == null || typeToCheck == null) {
            return false;
        }

        return playerBody.is(typeToCheck);
    }

    public static boolean isSpecies(final DragonStateHandler handler, final ResourceKey<DragonSpecies> typeToCheck) {
        return isSpecies(handler.speciesKey(), typeToCheck);
    }

    public static boolean isSpecies(final Entity entity, ResourceKey<DragonSpecies> typeToCheck) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        Holder<DragonSpecies> playerType = DragonStateProvider.getData(player).species();

        if (playerType == null) {
            return false;
        }

        return isSpecies(playerType.getKey(), typeToCheck);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore
    public static boolean isSpecies(final Holder<DragonSpecies> first, final Holder<DragonSpecies> second) {
        if (first == second) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.getKey() == second.getKey();
    }

    public static boolean isSpecies(final ResourceKey<DragonSpecies> playerType, final ResourceKey<DragonSpecies> typeToCheck) {
        if (playerType == typeToCheck) {
            return true;
        }

        if (playerType == null || typeToCheck == null) {
            return false;
        }

        return playerType.equals(typeToCheck);
    }

    public static boolean isNearbyDragonPlayerToEntity(double detectionRadius, Level level, Entity entity) {
        List<Player> players = level.getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(detectionRadius));

        for (Player player : players) {
            if (DragonStateProvider.isDragon(player)) {
                return true;
            }
        }
        return false;
    }
}