package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

public class ResourceHelper {
    public static ResourceLocation getKey(Block object) {
        return BuiltInRegistries.BLOCK.getKey(object);
    }

    public static ResourceLocation getKey(Item object) {
        return BuiltInRegistries.ITEM.getKey(object);
    }

    public static ResourceLocation getKey(EntityType<?> object) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(object);
    }

    public static ResourceLocation getKey(Entity object) {
        return getKey(object.getType());
    }

    public static ResourceLocation getKey(SoundEvent event) {
        return BuiltInRegistries.SOUND_EVENT.getKey(event);
    }

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

    public static @Nullable <T> ResourceKey<T> parseKey(@Nullable final HolderLookup.Provider provider, final ResourceKey<Registry<T>> registryKey, final CompoundTag tag, final String key) {
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

    public static <T> String getNameLowercase(final Holder<T> holder) {
        String rawPath = holder.getKey().location().getPath();
        // Get the last part of the path
        return rawPath.substring(rawPath.lastIndexOf("/") + 1).toLowerCase();
    }
}
