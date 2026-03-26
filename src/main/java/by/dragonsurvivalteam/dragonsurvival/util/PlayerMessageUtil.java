package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerMessageUtil {
    public static void sendSystemMessage(final Player player, final Component message, final boolean overlay) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(message, overlay);
            return;
        }

        player.sendSystemMessage(message);
    }
}
