package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;

public abstract class Storage<T extends StorageEntry> implements INBTSerializable<CompoundTag> {
    public static final String STORAGE = "storage";

    @Nullable protected Map<ResourceLocation, T> storage;

    public void sync(final ServerPlayer player) {
        player.getExistingData(type()).ifPresent(data -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncData(player.getId(), NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type()), serializeNBT(player.registryAccess()))));
    }

    public void tick(final Entity storageHolder) {
        if (storage != null) {
            Set<ResourceLocation> finished = new HashSet<>();

            storage.values().forEach(entry -> {
                if (entry.tick()) {
                    finished.add(entry.id());
                }
            });

            finished.forEach(id -> {
                T removed = storage.remove(id);

                if (removed != null) {
                    removed.onRemovalFromStorage(storageHolder);
                }
            });
        }
    }

    public void add(final Entity storageHolder, final T entry) {
        if (storage == null) {
            storage = new HashMap<>();
        }

        storage.put(entry.id(), entry);
        entry.onAddedToStorage(storageHolder);
    }

    public void remove(final Entity storageHolder, final T entry) {
        if (storage == null || entry == null) {
            return;
        }

        storage.remove(entry.id());
        entry.onRemovalFromStorage(storageHolder);
    }

    public @Nullable T get(final ResourceLocation id) {
        if (storage == null) {
            return null;
        }

        return storage.get(id);
    }

    public Collection<T> all() {
        if (storage == null) {
            return List.of();
        }

        return storage.values();
    }

    public int size() {
        if (storage == null) {
            return 0;
        }

        return storage.size();
    }

    public boolean isEmpty() {
        if (storage == null) {
            return true;
        }

        return storage.isEmpty();
    }

    /** Always returns 'false' if the storage is empty */
    public boolean isType(final Class<?> type) {
        if (isEmpty()) {
            return false;
        }

        //noinspection DataFlowIssue -> it's not null at this point
        return type.isAssignableFrom(storage.values().iterator().next().getClass());
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        if (storage == null) {
            return tag;
        }

        ListTag entries = new ListTag();
        storage.values().forEach(entry -> entries.add(save(provider, entry)));
        tag.put(STORAGE, entries);

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        Map<ResourceLocation, T> storage = new HashMap<>();
        ListTag entries = tag.getList(STORAGE, ListTag.TAG_COMPOUND);

        for (int i = 0; i < entries.size(); i++) {
            T entry = load(provider, entries.getCompound(i));

            if (entry != null) {
                storage.put(entry.id(), entry);
            }
        }

        this.storage = !storage.isEmpty() ? storage : null;
    }

    public abstract AttachmentType<?> type();

    protected abstract Tag save(@NotNull final HolderLookup.Provider provider, final T entry);
    protected abstract T load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag);
}
