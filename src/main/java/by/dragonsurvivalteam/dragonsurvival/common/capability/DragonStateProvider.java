package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DragonStateProvider implements ICapabilityProvider {
    private final LazyOptional<DragonStateHandler> capability;

    public DragonStateProvider(final Player player) {
        capability = LazyOptional.of(() -> getData(player));
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> requested, @Nullable Direction side) {
        return requested == Capabilities.DRAGON_CAPABILITY ? capability.cast() : LazyOptional.empty();
    }

    public void invalidate() {
        capability.invalidate();
    }

    public static @NotNull DragonStateHandler getData(@NotNull final Player player) {
        DragonStateHandler fakeData = getFakePlayerHandler(player);

        if (fakeData != null) {
            return fakeData;
        }

        return AttachmentManager.getData(player, DSDataAttachments.DRAGON_HANDLER);
    }

    public static Optional<DragonStateHandler> getOptional(@Nullable final Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }

        DragonStateHandler fakeData = getFakePlayerHandler(entity);
        if (fakeData != null) {
            return Optional.of(fakeData);
        }

        return entity.getCapability(Capabilities.DRAGON_CAPABILITY).resolve();
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
