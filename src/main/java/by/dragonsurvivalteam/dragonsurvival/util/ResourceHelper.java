package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

public class ResourceHelper {
    public static <T> Optional<Holder.Reference<T>> get(@Nullable final HolderLookup.Provider provider, final ResourceKey<T> key) {
        HolderLookup.RegistryLookup<T> registry;

        if (provider == null) {
            registry = CommonHooks.resolveLookup(key.registryKey());
        } else {
            registry = provider.lookupOrThrow(key.registryKey());
        }

        return Objects.requireNonNull(registry).get(key);
    }

    public static <T> List<ResourceKey<T>> keys(@Nullable final HolderLookup.Provider provider, ResourceKey<Registry<T>> registryKey) {
        HolderLookup.RegistryLookup<T> registry;

        if (provider == null) {
            registry = CommonHooks.resolveLookup(registryKey);
        } else {
            registry = provider.lookupOrThrow(registryKey);
        }

        //noinspection DataFlowIssue -> registry is expected to be present
        return registry.listElementIds().toList();
    }

    /**
     * Returns either the key stored in the tag or 'null' if: <br>
     * - The key is not present in the tag <br>
     * - Or the read value is not present in the registry <br>
     * - Or no registry access is available
     */
    public static @Nullable <T> ResourceKey<T> decodeKey(@Nullable final HolderLookup.Provider provider, final ResourceKey<Registry<T>> registryKey, final CompoundTag tag, final String key) {
        if (!tag.contains(key)) {
            return null;
        }

        HolderLookup.Provider actualProvider = provider != null ? provider : DragonSurvival.PROXY.getAccess();

        if (actualProvider == null) {
            DragonSurvival.LOGGER.error("Context was not available to deserialize the value of [{}] from the tag [{}]", key, tag);
            return null;
        }

        return ResourceKey.codec(registryKey).decode(actualProvider.createSerializationContext(NbtOps.INSTANCE), tag.get(key)).mapOrElse(Pair::getFirst, error -> {
            DragonSurvival.LOGGER.error(error.message());
            return null;
        });
    }

    /**
     * Returns either the tag (normally {@link StringTag}) or 'null' if: <br>
     * - The key is null <br>
     * - Or the key is not present in the registry <br>
     * - Or no registry access is available
     */
    public static @Nullable <T> Tag encodeKey(@Nullable final HolderLookup.Provider provider, final ResourceKey<T> key) {
        if (key == null) {
            return null;
        }

        HolderLookup.Provider actualProvider = provider != null ? provider : DragonSurvival.PROXY.getAccess();

        if (actualProvider == null) {
            DragonSurvival.LOGGER.error("Context was not available to serialize the value [{}]", key);
            return null;
        }

        return ResourceKey.codec(key.registryKey()).encodeStart(actualProvider.createSerializationContext(NbtOps.INSTANCE), key).mapOrElse(Function.identity(),error -> {
            DragonSurvival.LOGGER.error(error.message());
            return null;
        });
    }
}
