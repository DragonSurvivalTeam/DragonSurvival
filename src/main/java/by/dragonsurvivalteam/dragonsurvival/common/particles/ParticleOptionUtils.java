package by.dragonsurvivalteam.dragonsurvival.common.particles;

import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;

final class ParticleOptionUtils {
    private ParticleOptionUtils() {
    }

    static <T extends ParticleOptions> ParticleOptions.Deserializer<T> deserializer(
            final StreamCodec<ByteBuf, T> codec,
            final CommandDecoder<T> commandDecoder
    ) {
        return new ParticleOptions.Deserializer<>() {
            @Override
            public T fromCommand(final ParticleType<T> type, final StringReader reader) throws CommandSyntaxException {
                return commandDecoder.decode(reader);
            }

            @Override
            public T fromNetwork(final ParticleType<T> type, final FriendlyByteBuf buffer) {
                return codec.decode(buffer);
            }
        };
    }

    static float readFloat(final StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readFloat();
    }

    static double readDouble(final StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readDouble();
    }

    static boolean readBoolean(final StringReader reader) throws CommandSyntaxException {
        reader.expect(' ');
        return reader.readBoolean();
    }

    static String toString(final ParticleOptions options, final Object... arguments) {
        StringBuilder value = new StringBuilder(BuiltInRegistries.PARTICLE_TYPE.getKey(options.getType()).toString());
        for (Object argument : arguments) {
            value.append(' ').append(argument);
        }
        return value.toString();
    }

    @FunctionalInterface
    interface CommandDecoder<T> {
        T decode(StringReader reader) throws CommandSyntaxException;
    }
}
