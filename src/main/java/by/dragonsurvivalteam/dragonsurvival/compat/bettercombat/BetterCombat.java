package by.dragonsurvivalteam.dragonsurvival.compat.bettercombat;

import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import net.bettercombat.client.animation.AttackAnimationSubStack;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class BetterCombat {
    public static Player CURRENT_PLAYER;

    private static Field attackAnimationField;
    private static boolean resolvedAttackAnimationField;

    public static boolean isAttacking(@Nullable final Player player) {
        if (player == null || !ModID.BETTER_COMBAT.isLoaded()) {
            return false;
        }

        if (!resolvedAttackAnimationField) {
            resolveAttackAnimationField(player.getClass());
        }

        if (attackAnimationField == null) {
            return false;
        }

        try {
            AttackAnimationSubStack animation = (AttackAnimationSubStack) attackAnimationField.get(player);
            return animation.base.getAnimation() != null && animation.base.getAnimation().isActive();
        } catch (IllegalAccessException ignored) {
            return false;
        }
    }

    private static void resolveAttackAnimationField(final Class<?> playerClass) {
        resolvedAttackAnimationField = true;
        Class<?> type = playerClass;

        while (type != null) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() == AttackAnimationSubStack.class) {
                    field.setAccessible(true);
                    attackAnimationField = field;
                    return;
                }
            }

            type = type.getSuperclass();
        }
    }
}
