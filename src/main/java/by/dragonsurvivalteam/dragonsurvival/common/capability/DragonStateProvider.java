package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DragonStateProvider implements ICapabilityProvider<Player, Void, DragonStateHandler> {
    @Override
    public @Nullable DragonStateHandler getCapability(@NotNull Player player, @Nullable Void context) {
        return getData(player);
    }

    public static @NotNull DragonStateHandler getData(@NotNull final Player player) {
        DragonStateHandler fakeData = getFakePlayerHandler(player);

        if (fakeData != null) {
            return fakeData;
        }

        return player.getData(DSDataAttachments.DRAGON_HANDLER);
    }

    public static Optional<DragonStateHandler> getOptional(@Nullable final Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(entity.getCapability(Capabilities.DRAGON_CAPABILITY));
    }

    public static boolean isDragon(@Nullable Entity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        return DragonStateProvider.getData(player).isDragon();
    }

    private static DragonStateHandler getFakePlayerHandler(@NotNull Entity entity) {
        if (!entity.level().isClientSide()) {
            return null;
        }

        if (entity instanceof FakeClientPlayer fakeClientPlayer) {
            if (fakeClientPlayer.handler != null) {
                return fakeClientPlayer.handler;
            }
        }

        return null;
    }
}