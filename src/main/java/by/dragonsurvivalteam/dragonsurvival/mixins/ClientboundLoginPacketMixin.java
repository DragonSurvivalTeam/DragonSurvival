package by.dragonsurvivalteam.dragonsurvival.mixins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(ClientboundLoginPacket.class)
public class ClientboundLoginPacketMixin {
    @Unique private static final RegistryAccess.Frozen dragonSurvival$builtInRegistries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

    @Redirect(
            method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readWithCodec(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;)Ljava/lang/Object;")
    )
    private static <T> T dragonSurvival$readRegistriesWithContext(final FriendlyByteBuf buffer, final DynamicOps<Tag> ops, final Codec<T> codec) {
        CompoundTag encoded = buffer.readAnySizeNbt();
        // Cross-registry holder codecs need every target registry to exist before the first element is decoded.
        Map<ResourceKey<? extends Registry<?>>, WritableRegistry<?>> registries = dragonSurvival$createRegistries(encoded);
        RegistryOps<Tag> registryOps = RegistryOps.create(ops, dragonSurvival$decodingLookup(ops, registries));

        @SuppressWarnings("unchecked")
        DataResult<RegistryAccess> result = ((Codec<RegistryAccess>) codec).parse(registryOps, encoded);
        RegistryAccess decoded = result.getOrThrow(false, message -> {
            throw new DecoderException("Failed to decode: " + message + " " + encoded);
        });

        registries.values().forEach(registry -> dragonSurvival$copyRegistry(decoded, registry));

        @SuppressWarnings("unchecked")
        T access = (T) new RegistryAccess.ImmutableRegistryAccess(List.copyOf(registries.values()));
        return access;
    }

    @Redirect(
            method = "write",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeWithCodec(Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/lang/Object;)V")
    )
    private <T> void dragonSurvival$writeRegistriesWithContext(final FriendlyByteBuf buffer, final DynamicOps<Tag> ops, final Codec<T> codec, final T value) {
        if (value instanceof RegistryAccess access) {
            RegistryOps<Tag> registryOps = RegistryOps.create(ops, dragonSurvival$encodingLookup(ops, access));
            //noinspection deprecation
            buffer.writeWithCodec(registryOps, codec, value);
        } else {
            //noinspection deprecation
            buffer.writeWithCodec(ops, codec, value);
        }
    }

    private static Map<ResourceKey<? extends Registry<?>>, WritableRegistry<?>> dragonSurvival$createRegistries(final CompoundTag encoded) {
        Map<ResourceKey<? extends Registry<?>>, WritableRegistry<?>> registries = new LinkedHashMap<>();

        for (String name : encoded.getAllKeys()) {
            ResourceLocation location = ResourceLocation.tryParse(name);
            if (location == null) {
                throw new DecoderException("Invalid registry key in login packet: " + name);
            }

            ResourceKey<? extends Registry<Object>> key = ResourceKey.createRegistryKey(location);
            registries.put(key, new MappedRegistry<>(key, Lifecycle.experimental()));
        }

        return registries;
    }

    private static RegistryOps.RegistryInfoLookup dragonSurvival$encodingLookup(final DynamicOps<Tag> ops, final RegistryAccess access) {
        RegistryOps.RegistryInfoLookup parent = dragonSurvival$lookupProvider(ops);

        return new RegistryOps.RegistryInfoLookup() {
            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(final ResourceKey<? extends Registry<? extends T>> key) {
                return access.registry(key)
                        .map(registry -> new RegistryOps.RegistryInfo<>(registry.asLookup(), registry.asTagAddingLookup(), registry.registryLifecycle()))
                        .or(() -> parent.lookup(key));
            }
        };
    }

    private static RegistryOps.RegistryInfoLookup dragonSurvival$decodingLookup(final DynamicOps<Tag> ops, final Map<ResourceKey<? extends Registry<?>>, WritableRegistry<?>> registries) {
        RegistryOps.RegistryInfoLookup parent = dragonSurvival$lookupProvider(ops);

        return new RegistryOps.RegistryInfoLookup() {
            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(final ResourceKey<? extends Registry<? extends T>> key) {
                @SuppressWarnings("unchecked")
                WritableRegistry<T> registry = (WritableRegistry<T>) registries.get(key);

                if (registry != null) {
                    return Optional.of(new RegistryOps.RegistryInfo<>(registry.asLookup(), registry.createRegistrationLookup(), registry.registryLifecycle()));
                }

                // Built-in tags arrive after the login packet, so permit named sets to be created empty for now.
                return dragonSurvival$builtInRegistries.registry(key)
                        .map(builtIn -> new RegistryOps.RegistryInfo<>(builtIn.asLookup(), builtIn.asTagAddingLookup(), builtIn.registryLifecycle()))
                        .or(() -> parent.lookup(key));
            }
        };
    }

    private static RegistryOps.RegistryInfoLookup dragonSurvival$lookupProvider(final DynamicOps<Tag> ops) {
        if (ops instanceof RegistryOps<Tag> registryOps) {
            return ((RegistryOpsAccess) (Object) registryOps).dragonSurvival$getLookupProvider();
        }

        return new RegistryOps.RegistryInfoLookup() {
            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(final ResourceKey<? extends Registry<? extends T>> key) {
                return Optional.empty();
            }
        };
    }

    private static <T> void dragonSurvival$copyRegistry(final RegistryAccess decoded, final WritableRegistry<T> target) {
        Registry<T> source = decoded.registryOrThrow(target.key());

        for (Map.Entry<ResourceKey<T>, T> entry : source.entrySet()) {
            T value = entry.getValue();
            target.registerMapping(source.getId(value), entry.getKey(), value, source.lifecycle(value));
        }
    }
}
