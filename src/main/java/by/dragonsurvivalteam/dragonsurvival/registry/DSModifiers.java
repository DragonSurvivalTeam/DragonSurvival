package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.AttributeModifierSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class DSModifiers {
    private static final String SYNCED_MODIFIER_NAME = "Unknown synced attribute modifier";
    private static final double SYNCED_MODIFIER_EPSILON = 1.0E-4;

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

    /** 1.20.1 does not transmit attribute modifier names, which are used to replace dragon stage modifiers on the client. */
    public static void restoreSyncedGrowthModifierNames(@Nullable final Player player, final DragonStateHandler handler) {
        if (player == null || !player.level().isClientSide() || !handler.isDragon()) {
            return;
        }

        double level = handler.getGrowth() - handler.stage().value().growthRange().min();

        for (Modifier modifier : handler.stage().value().modifiers()) {
            AttributeInstance instance = player.getAttribute(modifier.attribute().value());
            if (instance == null) {
                continue;
            }

            AttributeModifier syncedModifier = instance.getModifiers().stream()
                    .filter(candidate -> SYNCED_MODIFIER_NAME.equals(candidate.getName()))
                    .filter(candidate -> candidate.getOperation() == modifier.operation().legacy())
                    .min(Comparator.comparingDouble(candidate -> Math.abs(candidate.getAmount() - modifier.calculate(level))))
                    .filter(candidate -> Math.abs(candidate.getAmount() - modifier.calculate(level)) <= SYNCED_MODIFIER_EPSILON)
                    .orElse(null);

            if (syncedModifier != null) {
                instance.removeModifier(syncedModifier);
                instance.addTransientModifier(modifier.getModifier(syncedModifier.getId(), ModifierType.DRAGON_STAGE.path(), level));
            }
        }
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
