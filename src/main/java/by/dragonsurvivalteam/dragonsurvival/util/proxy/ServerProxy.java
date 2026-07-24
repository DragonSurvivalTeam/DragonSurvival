package by.dragonsurvivalteam.dragonsurvival.util.proxy;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class ServerProxy implements Proxy {
    @Override
    public @Nullable RegistryAccess getAccess() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        if (server != null) {
            return server.registryAccess();
        }

        return null;
    }

    @Override
    public boolean isMining(final Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return serverPlayer.gameMode.isDestroyingBlock;
        }

        return false;
    }
}
