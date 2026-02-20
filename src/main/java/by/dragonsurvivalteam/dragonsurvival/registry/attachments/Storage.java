package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Storage<T extends StorageEntry> implements ValueIOSerializable {
    public static final String STORAGE = "storage";

    @Nullable protected Map<Identifier, T> storage;

    public void sync(final ServerPlayer player) {
        TagValueOutput tagValueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
        serialize(tagValueOutput);

        player.getExistingData(type()).ifPresent(data -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncData(player.getId(), NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type()), tagValueOutput.buildResult())));
    }

    public void tick(final Entity storageHolder) {
        if (storage != null) {
            List<Identifier> finished = new ArrayList<>();

            storage.values().forEach(entry -> {
                if (entry.tick(storageHolder)) {
                    finished.add(entry.id());
                }
            });

            finished.forEach(id -> {
                T removed = storage.remove(id);

                if (removed != null) {
                    removed.onRemovalFromStorage(storageHolder);
                }
            });

            if (!finished.isEmpty()) {
                invalidateCache();
            }
        }
    }

    public void add(final Entity storageHolder, final T entry) {
        if (storage == null) {
            storage = new HashMap<>();
        }

        storage.put(entry.id(), entry);
        entry.onAddedToStorage(storageHolder);
        invalidateCache();
    }

    // TODO :: potentially change it so you pass the resource location
    //  and 'onRemovalFromStorage' gets the removed entry from 'storage'
    public void remove(final Entity storageHolder, final T entry) {
        if (storage == null || entry == null) {
            return;
        }

        storage.remove(entry.id());
        entry.onRemovalFromStorage(storageHolder);
        invalidateCache();
    }

    public @Nullable T get(final Identifier id) {
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

    public void clear(final Entity storageHolder) {
        if (storage == null || storageHolder.level().isClientSide()) {
            return;
        }

        List<T> cleared = new ArrayList<>(storage.values());
        storage.clear();
        cleared.forEach(entry -> entry.onRemovalFromStorage(storageHolder));

        invalidateCache();
        storageHolder.removeData(type());
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

    public void invalidateCache() { /* Nothing to do */ }

    /** Always returns 'false' if the storage is empty */
    public boolean isType(final Class<?> type) {
        if (isEmpty()) {
            return false;
        }

        //noinspection DataFlowIssue -> it's not null at this point
        return type.isInstance(storage.values().iterator().next());
    }

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {
        if (storage == null) {
            return;
        }

        ValueOutput entries = valueOutput.child(STORAGE);
        List<T> objects = storage.values().stream().toList();
        for (int i = 0; i < storage.size(); i++) {
            save(entries, objects.get(i), Integer.toString(i));
        }
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        Map<Identifier, T> storage = new HashMap<>();
        ValueInput entries = valueInput.childOrEmpty(STORAGE);

        for (String key : entries.keySet()) {
            T entry = load(entries, key);

            if (entry != null) {
                storage.put(entry.id(), entry);
            }
        }

        this.storage = !storage.isEmpty() ? storage : null;
        invalidateCache();
    }

    public abstract AttachmentType<?> type();

    protected abstract void save(@NotNull ValueOutput valueOutput, final T entry, final String key);

    protected abstract T load(@NotNull ValueInput valueInput, final String key);
}
