package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.AttributeModifierSupplier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class DSModifiers {
    public static void updateAllModifiers(@Nullable final Player player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }

        float healthPercentage = player.getHealth() / player.getMaxHealth();
        DragonStateHandler handler = DragonStateProvider.getData(player);
        updateTypeModifiers(player, handler);
        updateGrowthModifiers(player, handler);
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
            handler.species().value().applyModifiers(player);
        }

        player.setHealth(player.getMaxHealth() * healthPercentage);
    }

    public static void updateGrowthModifiers(@Nullable final Player player, final DragonStateHandler handler) {
        if (player == null) {
            return;
        }

        float healthPercentage = player.getHealth() / player.getMaxHealth();
        AttributeModifierSupplier.removeModifiers(ModifierType.DRAGON_STAGE, player);

        if (handler.isDragon()) {
            handler.stage().value().applyModifiers(player, handler.getGrowth() - handler.stage().value().growthRange().min());
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
            handler.body().value().applyModifiers(player);
        }

        player.setHealth(player.getMaxHealth() * healthPercentage);
    }
}