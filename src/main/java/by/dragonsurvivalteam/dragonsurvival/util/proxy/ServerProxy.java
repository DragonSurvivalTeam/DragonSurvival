package by.dragonsurvivalteam.dragonsurvival.util.proxy;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
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
}
