package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public class DragonUtils {
    public static Holder<DragonType> getType(Player entity) {
        return DragonStateProvider.getData(entity).getType();
    }

    public static Holder<DragonType> getType(DragonStateHandler handler) {
        return handler.getType();
    }

    public static Holder<DragonBody> getBody(Player player) {
        return getBody(DragonStateProvider.getData(player));
    }

    public static Holder<DragonBody> getBody(DragonStateHandler handler) {
        return handler.getBody();
    }

    public static boolean isBody(final DragonStateHandler data, final Holder<DragonBody> typeToCheck) {
        if (data == null) {
            return false;
        }

        return isBody(data.getBody(), typeToCheck);
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

    public static boolean isType(final DragonStateHandler handler, final ResourceKey<DragonType> typeToCheck) {
        return isType(handler.getType().getKey(), typeToCheck);
    }

    public static boolean isType(final Entity entity, ResourceKey<DragonType> typeToCheck) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        Holder<DragonType> playerType = DragonStateProvider.getData(player).getType();

        if (playerType == null) {
            return false;
        }

        return isType(playerType.getKey(), typeToCheck);
    }

    public static boolean isType(final Holder<DragonType> first, final Holder<DragonType> second) {
        if (first == second) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.getKey() == second.getKey();
    }

    public static boolean isType(final ResourceKey<DragonType> playerType, final ResourceKey<DragonType> typeToCheck) {
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