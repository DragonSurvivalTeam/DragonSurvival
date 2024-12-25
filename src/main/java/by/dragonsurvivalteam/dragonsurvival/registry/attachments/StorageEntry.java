package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public interface StorageEntry {
    default void onAddedToStorage(final Entity storageHolder) { /* Nothing to do */ }
    default void onRemovalFromStorage(final Entity storageHolder) { /* Nothing to do */ }

    boolean tick();
    ResourceLocation id();
}
