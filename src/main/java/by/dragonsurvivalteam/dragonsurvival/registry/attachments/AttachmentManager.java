package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import by.dragonsurvivalteam.dragonsurvival.common.compat.attachments.AttachmentType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = DragonSurvival.MODID)
public final class AttachmentManager {
    private static final String ENTITY_TAG = "DragonSurvivalAttachments";
    private static final String LEVEL_DATA = "dragonsurvival_attachments";
    private static final Map<Object, Map<AttachmentType<?>, Object>> FALLBACK_STORAGE = new WeakHashMap<>();

    private AttachmentManager() {}

    public static <T> T getData(final Object holder, final Supplier<? extends AttachmentType<T>> type) {
        return getData(holder, type.get());
    }

    public static <T> T getData(final Object holder, final AttachmentType<T> type) {
        Map<AttachmentType<?>, Object> attachments = attachments(holder);
        markDirty(holder);
        //noinspection unchecked
        return (T) attachments.computeIfAbsent(type, ignored -> type.create());
    }

    public static <T> Optional<T> getExistingData(final Object holder, final Supplier<? extends AttachmentType<T>> type) {
        return getExistingData(holder, type.get());
    }

    public static <T> Optional<T> getExistingData(final Object holder, final AttachmentType<T> type) {
        //noinspection unchecked
        return Optional.ofNullable((T) attachments(holder).get(type));
    }

    public static <T> T setData(final Object holder, final Supplier<? extends AttachmentType<T>> type, final T value) {
        return setData(holder, type.get(), value);
    }

    public static <T> T setData(final Object holder, final AttachmentType<T> type, final T value) {
        attachments(holder).put(type, value);
        markDirty(holder);
        return value;
    }

    public static boolean removeData(final Object holder, final Supplier<? extends AttachmentType<?>> type) {
        return removeData(holder, type.get());
    }

    public static boolean removeData(final Object holder, final AttachmentType<?> type) {
        boolean removed = attachments(holder).remove(type) != null;
        if (removed) {
            markDirty(holder);
        }
        return removed;
    }

    public static void writeEntityAttachments(final Entity entity, final CompoundTag entityTag) {
        CompoundTag attachments = serialize(attachments(entity), entity.level().registryAccess());
        if (!attachments.isEmpty()) {
            entityTag.put(ENTITY_TAG, attachments);
        }
    }

    public static void readEntityAttachments(final Entity entity, final CompoundTag entityTag) {
        if (entityTag.contains(ENTITY_TAG, Tag.TAG_COMPOUND)) {
            deserialize(attachments(entity), entityTag.getCompound(ENTITY_TAG), entity.level().registryAccess());
        }
    }

    @SubscribeEvent
    public static void copyPlayerAttachments(final PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player replacement = event.getEntity();
        HolderLookup.Provider provider = replacement.level().registryAccess();

        attachments(original).forEach((type, value) -> {
            if (type.isSerializable() && (!event.isWasDeath() || type.copyOnDeath())) {
                copy(type, value, attachments(replacement), provider);
            }
        });
    }

    private static Map<AttachmentType<?>, Object> attachments(final Object holder) {
        if (holder instanceof ServerLevel level) {
            return levelData(level).dragonSurvival$getAttachments();
        }
        if (holder instanceof AttachmentStorage storage) {
            return storage.dragonSurvival$getAttachments();
        }

        synchronized (FALLBACK_STORAGE) {
            return FALLBACK_STORAGE.computeIfAbsent(holder, ignored -> new IdentityHashMap<>());
        }
    }

    private static void markDirty(final Object holder) {
        if (holder instanceof ServerLevel level) {
            levelData(level).setDirty();
        }
    }

    private static LevelAttachmentData levelData(final ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                tag -> new LevelAttachmentData(level.registryAccess(), tag),
                () -> new LevelAttachmentData(level.registryAccess()),
                LEVEL_DATA
        );
    }

    private static CompoundTag serialize(final Map<AttachmentType<?>, Object> values, final HolderLookup.Provider provider) {
        CompoundTag result = new CompoundTag();
        values.forEach((type, value) -> {
            ResourceLocation id = DSDataAttachments.ATTACHMENT_TYPES.get().getKey(type);
            if (id != null && type.isSerializable()) {
                putSerialized(result, id, type, value, provider);
            }
        });
        return result;
    }

    private static void deserialize(final Map<AttachmentType<?>, Object> values, final CompoundTag tag, final HolderLookup.Provider provider) {
        values.clear();
        tag.getAllKeys().forEach(key -> {
            AttachmentType<?> type = DSDataAttachments.ATTACHMENT_TYPES.get().getValue(new ResourceLocation(key));
            if (type != null && type.isSerializable()) {
                values.put(type, type.deserialize(tag.get(key), provider));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> void putSerialized(final CompoundTag target, final ResourceLocation id, final AttachmentType<T> type, final Object value, final HolderLookup.Provider provider) {
        Tag serialized = type.serialize((T) value, provider);
        if (serialized != null) {
            target.put(id.toString(), serialized);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void copy(final AttachmentType<T> type, final Object value, final Map<AttachmentType<?>, Object> target, final HolderLookup.Provider provider) {
        Tag serialized = type.serialize((T) value, provider);
        if (serialized != null) {
            Object existing = target.get(type);
            if (existing == null) {
                target.put(type, type.deserialize(serialized, provider));
            } else {
                // Forge's capability bridge caches the attachment instance in a LazyOptional.
                // Keep that instance valid when the player is cloned during respawn.
                type.deserialize((T) existing, serialized, provider);
            }
        }
    }

    private static final class LevelAttachmentData extends SavedData implements AttachmentStorage {
        private final HolderLookup.Provider provider;
        private final Map<AttachmentType<?>, Object> attachments = new IdentityHashMap<>();

        private LevelAttachmentData(final HolderLookup.Provider provider) {
            this.provider = provider;
        }

        private LevelAttachmentData(final HolderLookup.Provider provider, final CompoundTag tag) {
            this(provider);
            deserialize(attachments, tag, provider);
        }

        @Override
        public Map<AttachmentType<?>, Object> dragonSurvival$getAttachments() {
            return attachments;
        }

        @Override
        public CompoundTag save(final CompoundTag tag) {
            return serialize(attachments, provider);
        }
    }
}
