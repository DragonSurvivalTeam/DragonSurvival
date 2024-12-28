package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.AttributeModifierSupplier;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class DSModifiers {
    public static void updateAllModifiers(@Nullable final Player player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        float healthPercentage = player.getHealth() / player.getMaxHealth();
        DragonStateHandler handler = DragonStateProvider.getData(player);
        updateTypeModifiers(player, handler);
        updateSizeModifiers(player, handler);
        updateBodyModifiers(player, handler);
        player.setHealth(player.getMaxHealth() * healthPercentage);
    }

    public static void clearModifiers(@Nullable final Player player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        float healthPercentage = player.getHealth() / player.getMaxHealth();
        AttributeModifierSupplier.removeModifiers(ModifierType.DRAGON_TYPE, player);
        AttributeModifierSupplier.removeModifiers(ModifierType.DRAGON_STAGE, player);
        AttributeModifierSupplier.removeModifiers(ModifierType.DRAGON_BODY, player);
        player.setHealth(player.getMaxHealth() * healthPercentage);
    }

    public static void updateTypeModifiers(@Nullable final Player player, final DragonStateHandler handler) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        float healthPercentage = player.getHealth() / player.getMaxHealth();
        AttributeModifierSupplier.removeModifiers(ModifierType.DRAGON_TYPE, player);

        if (handler.isDragon()) {
            handler.getType().value().applyModifiers(player, handler.getDragonType(), /* Type has nothing to scale */ 1);
        }

        player.setHealth(player.getMaxHealth() * healthPercentage);
    }

    public static void updateSizeModifiers(@Nullable final Player player, final DragonStateHandler handler) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        float healthPercentage = player.getHealth() / player.getMaxHealth();
        AttributeModifierSupplier.removeModifiers(ModifierType.DRAGON_STAGE, player);

        if (handler.isDragon()) {
            handler.getStage().value().applyModifiers(player, handler.getDragonType(), handler.getSize());
        }

        player.setHealth(player.getMaxHealth() * healthPercentage);
    }

    public static void updateBodyModifiers(@Nullable final Player player, final DragonStateHandler handler) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        float healthPercentage = player.getHealth() / player.getMaxHealth();
        AttributeModifierSupplier.removeModifiers(ModifierType.DRAGON_BODY, player);

        if (handler.isDragon()) {
            handler.getBody().value().applyModifiers(player, handler.getDragonType(), /* Body has nothing to scale */ 1);
        }
        player.setHealth(player.getMaxHealth() * healthPercentage);
    }
}