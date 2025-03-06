package by.dragonsurvivalteam.dragonsurvival.compat.bettercombat;

import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class BetterCombat {
    public static Player CURRENT_PLAYER;

    public static boolean isAttacking(@Nullable final Player player) {
        return player instanceof AttackAnimationAccess access && access.dragonSurvival$hasActiveAnimation();
    }
}
