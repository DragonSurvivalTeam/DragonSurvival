package by.dragonsurvivalteam.dragonsurvival.network.compat;

import net.minecraft.resources.ResourceLocation;

/**
 * Backport of the typed custom payload identity introduced after 1.20.1.
 */
public interface CustomPacketPayload {
    Type<? extends CustomPacketPayload> type();

    record Type<T extends CustomPacketPayload>(ResourceLocation id) {}
}
