package by.dragonsurvivalteam.dragonsurvival.network.codec;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryOps;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stream codecs used by the 1.21.1 codebase, implemented with 1.20.1 buffers.
 */
public interface ByteBufCodecs {
    StreamCodec<ByteBuf, Boolean> BOOL = codec(ByteBuf::readBoolean, ByteBuf::writeBoolean);
    StreamCodec<ByteBuf, Integer> INT = codec(ByteBuf::readInt, ByteBuf::writeInt);
    StreamCodec<ByteBuf, Float> FLOAT = codec(ByteBuf::readFloat, ByteBuf::writeFloat);
    StreamCodec<ByteBuf, Double> DOUBLE = codec(ByteBuf::readDouble, ByteBuf::writeDouble);
    StreamCodec<ByteBuf, Integer> VAR_INT = codec(ByteBufCodecs::readVarInt, ByteBufCodecs::writeVarInt);
    StreamCodec<ByteBuf, String> STRING_UTF8 = codec(
            buffer -> friendly(buffer).readUtf(),
            (buffer, value) -> friendly(buffer).writeUtf(value)
    );
    StreamCodec<ByteBuf, CompoundTag> COMPOUND_TAG = codec(
            buffer -> friendly(buffer).readNbt(),
            (buffer, value) -> friendly(buffer).writeNbt(value)
    );
    StreamCodec<ByteBuf, Tag> TAG = new StreamCodec<>() {
        private static final String VALUE_KEY = "value";

        @Override
        public Tag decode(final ByteBuf buffer) {
            CompoundTag wrapper = friendly(buffer).readNbt();
            if (wrapper == null || !wrapper.contains(VALUE_KEY)) {
                throw new DecoderException("Missing NBT payload");
            }
            return wrapper.get(VALUE_KEY);
        }

        @Override
        public void encode(final ByteBuf buffer, final Tag value) {
            CompoundTag wrapper = new CompoundTag();
            wrapper.put(VALUE_KEY, value.copy());
            friendly(buffer).writeNbt(wrapper);
        }
    };
    StreamCodec<ByteBuf, Vector3f> VECTOR3F = new StreamCodec<>() {
        @Override
        public Vector3f decode(final ByteBuf buffer) {
            return new Vector3f(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
        }

        @Override
        public void encode(final ByteBuf buffer, final Vector3f value) {
            buffer.writeFloat(value.x());
            buffer.writeFloat(value.y());
            buffer.writeFloat(value.z());
        }
    };
    StreamCodec<ByteBuf, ResourceLocation> RESOURCE_LOCATION = STRING_UTF8.map(ResourceLocation::new, ResourceLocation::toString);
    StreamCodec<ByteBuf, BlockPos> BLOCK_POS = new StreamCodec<>() {
        @Override
        public BlockPos decode(final ByteBuf buffer) {
            return friendly(buffer).readBlockPos();
        }

        @Override
        public void encode(final ByteBuf buffer, final BlockPos value) {
            friendly(buffer).writeBlockPos(value);
        }
    };
    StreamCodec<FriendlyByteBuf, ParticleOptions> PARTICLE_OPTIONS = new StreamCodec<>() {
        @Override
        public ParticleOptions decode(final FriendlyByteBuf buffer) {
            ParticleType<?> type = buffer.readById(BuiltInRegistries.PARTICLE_TYPE);
            if (type == null) {
                throw new DecoderException("Unknown particle type");
            }
            return decodeParticle(type, buffer);
        }

        @Override
        public void encode(final FriendlyByteBuf buffer, final ParticleOptions value) {
            buffer.writeId(BuiltInRegistries.PARTICLE_TYPE, value.getType());
            value.writeToNetwork(buffer);
        }
    };
    StreamCodec<FriendlyByteBuf, MobEffectInstance> MOB_EFFECT_INSTANCE = codec(buffer -> {
        CompoundTag tag = buffer.readNbt();
        if (tag == null) {
            throw new DecoderException("Missing mob effect instance");
        }
        MobEffectInstance effect = MobEffectInstance.load(tag);
        if (effect == null) {
            throw new DecoderException("Unknown mob effect instance");
        }
        return effect;
    }, (buffer, value) -> buffer.writeNbt(value.save(new CompoundTag())));
    StreamCodec<FriendlyByteBuf, SoundEvent> SOUND_EVENT = codec(
            SoundEvent::readFromNetwork,
            (buffer, value) -> value.writeToNetwork(buffer)
    );

    static <B extends ByteBuf, V> StreamCodec<B, V> codec(final Decoder<B, V> decoder, final Encoder<B, V> encoder) {
        return new StreamCodec<>() {
            @Override
            public V decode(final B buffer) {
                return decoder.decode(buffer);
            }

            @Override
            public void encode(final B buffer, final V value) {
                encoder.encode(buffer, value);
            }
        };
    }

    static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumCodec(final Class<E> enumType) {
        E[] values = enumType.getEnumConstants();
        return VAR_INT.map(index -> {
            if (index < 0 || index >= values.length) {
                throw new DecoderException("Unknown " + enumType.getSimpleName() + " ordinal " + index);
            }
            return values[index];
        }, Enum::ordinal);
    }

    static <T> StreamCodec<ByteBuf, T> fromCodec(final Codec<T> codec) {
        return new StreamCodec<>() {
            @Override
            public T decode(final ByteBuf buffer) {
                return codec.parse(NbtOps.INSTANCE, TAG.decode(buffer)).getOrThrow(false, message -> {
                    throw new DecoderException("Failed to decode NBT payload: " + message);
                });
            }

            @Override
            public void encode(final ByteBuf buffer, final T value) {
                Tag encoded = codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, message -> {
                    throw new EncoderException("Failed to encode NBT payload: " + message);
                });
                TAG.encode(buffer, encoded);
            }
        };
    }

    static <T> StreamCodec<FriendlyByteBuf, T> fromCodecWithRegistries(final Codec<T> codec) {
        return new StreamCodec<>() {
            @Override
            public T decode(final FriendlyByteBuf buffer) {
                RegistryAccess access = requireRegistryAccess();
                return codec.parse(RegistryOps.create(NbtOps.INSTANCE, access), TAG.decode(buffer)).getOrThrow(false, message -> {
                    throw new DecoderException("Failed to decode registry NBT payload: " + message);
                });
            }

            @Override
            public void encode(final FriendlyByteBuf buffer, final T value) {
                RegistryAccess access = requireRegistryAccess();
                Tag encoded = codec.encodeStart(RegistryOps.create(NbtOps.INSTANCE, access), value).getOrThrow(false, message -> {
                    throw new EncoderException("Failed to encode registry NBT payload: " + message);
                });
                TAG.encode(buffer, encoded);
            }
        };
    }

    static <B extends ByteBuf, V> StreamCodec<B, Optional<V>> optional(final StreamCodec<B, V> codec) {
        return new StreamCodec<>() {
            @Override
            public Optional<V> decode(final B buffer) {
                return buffer.readBoolean() ? Optional.of(codec.decode(buffer)) : Optional.empty();
            }

            @Override
            public void encode(final B buffer, final Optional<V> value) {
                buffer.writeBoolean(value.isPresent());
                value.ifPresent(entry -> codec.encode(buffer, entry));
            }
        };
    }

    static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, List<V>> list() {
        return codec -> new StreamCodec<>() {
            @Override
            public List<V> decode(final B buffer) {
                int size = readVarInt(buffer);
                if (size < 0 || size > 65_536) {
                    throw new DecoderException("Invalid collection size " + size);
                }
                List<V> values = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    values.add(codec.decode(buffer));
                }
                return values;
            }

            @Override
            public void encode(final B buffer, final List<V> values) {
                if (values.size() > 65_536) {
                    throw new EncoderException("Collection is too large: " + values.size());
                }
                writeVarInt(buffer, values.size());
                values.forEach(value -> codec.encode(buffer, value));
            }
        };
    }

    static <T> StreamCodec<FriendlyByteBuf, T> registry(final ResourceKey<? extends Registry<T>> registryKey) {
        return new StreamCodec<>() {
            @Override
            public T decode(final FriendlyByteBuf buffer) {
                ResourceLocation location = RESOURCE_LOCATION.decode(buffer);
                T value = resolveRegistry(registryKey).get(location);
                if (value == null) {
                    throw new DecoderException("Unknown value " + location + " in registry " + registryKey.location());
                }
                return value;
            }

            @Override
            public void encode(final FriendlyByteBuf buffer, final T value) {
                ResourceLocation location = resolveRegistry(registryKey).getKey(value);
                if (location == null) {
                    throw new EncoderException("Unregistered value in registry " + registryKey.location());
                }
                RESOURCE_LOCATION.encode(buffer, location);
            }
        };
    }

    static <T> StreamCodec<FriendlyByteBuf, Holder<T>> holderRegistry(final ResourceKey<? extends Registry<T>> registryKey) {
        return new StreamCodec<>() {
            @Override
            public Holder<T> decode(final FriendlyByteBuf buffer) {
                ResourceLocation location = RESOURCE_LOCATION.decode(buffer);
                return resolveHolder(registryKey, location);
            }

            @Override
            public void encode(final FriendlyByteBuf buffer, final Holder<T> value) {
                ResourceLocation location = value.unwrapKey()
                        .map(ResourceKey::location)
                        .orElseThrow(() -> new EncoderException("Cannot encode a direct holder for " + registryKey.location()));
                RESOURCE_LOCATION.encode(buffer, location);
            }
        };
    }

    static <T> StreamCodec<FriendlyByteBuf, HolderSet<T>> holderSet(final ResourceKey<? extends Registry<T>> registryKey) {
        StreamCodec<FriendlyByteBuf, Holder<T>> holderCodec = holderRegistry(registryKey);
        return holderCodec.apply(ByteBufCodecs.list()).map(HolderSet::direct, value -> value.stream().toList());
    }

    static <T> StreamCodec<ByteBuf, ResourceKey<T>> resourceKey(final ResourceKey<? extends Registry<T>> registryKey) {
        return RESOURCE_LOCATION.map(location -> ResourceKey.create(registryKey, location), ResourceKey::location);
    }

    private static FriendlyByteBuf friendly(final ByteBuf buffer) {
        return buffer instanceof FriendlyByteBuf friendly ? friendly : new FriendlyByteBuf(buffer);
    }

    private static RegistryAccess requireRegistryAccess() {
        RegistryAccess access = DragonSurvival.PROXY == null ? null : DragonSurvival.PROXY.getAccess();
        if (access == null) {
            throw new IllegalStateException("Registry access is unavailable while encoding or decoding network data");
        }
        return access;
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<T> resolveRegistry(final ResourceKey<? extends Registry<T>> registryKey) {
        Registry<?> builtIn = BuiltInRegistries.REGISTRY.get(registryKey.location());
        if (builtIn != null) {
            return (Registry<T>) builtIn;
        }

        RegistryAccess access = requireRegistryAccess();
        return access.registryOrThrow(registryKey);
    }

    private static <T> Holder<T> resolveHolder(final ResourceKey<? extends Registry<T>> registryKey, final ResourceLocation location) {
        IForgeRegistry<T> forgeRegistry = RegistryManager.ACTIVE.getRegistry(registryKey.location());
        if (forgeRegistry != null) {
            return forgeRegistry.getHolder(location)
                    .orElseThrow(() -> new DecoderException("Unknown value " + location + " in registry " + registryKey.location()));
        }

        Registry<T> registry = resolveRegistry(registryKey);
        return registry.getHolder(ResourceKey.create(registryKey, location))
                .orElseThrow(() -> new DecoderException("Unknown value " + location + " in registry " + registryKey.location()));
    }

    @SuppressWarnings("unchecked")
    private static <T extends ParticleOptions> T decodeParticle(final ParticleType<?> type, final FriendlyByteBuf buffer) {
        ParticleType<T> typed = (ParticleType<T>) type;
        return typed.getDeserializer().fromNetwork(typed, buffer);
    }

    private static int readVarInt(final ByteBuf buffer) {
        int value = 0;
        int position = 0;
        byte current;

        do {
            current = buffer.readByte();
            value |= (current & 0x7F) << position;
            position += 7;
            if (position >= 35) {
                throw new DecoderException("VarInt is too big");
            }
        } while ((current & 0x80) != 0);

        return value;
    }

    private static void writeVarInt(final ByteBuf buffer, int value) {
        while ((value & ~0x7F) != 0) {
            buffer.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buffer.writeByte(value);
    }

    @FunctionalInterface
    interface Decoder<B, V> {
        V decode(B buffer);
    }

    @FunctionalInterface
    interface Encoder<B, V> {
        void encode(B buffer, V value);
    }
}
