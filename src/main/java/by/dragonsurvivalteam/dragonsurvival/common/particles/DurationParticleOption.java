package by.dragonsurvivalteam.dragonsurvival.common.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.BiFunction;

interface DurationParticleOption extends ParticleOptions {
    float duration();

    boolean swirls();

    @Override
    default void writeToNetwork(final FriendlyByteBuf buffer) {
        buffer.writeFloat(duration());
        buffer.writeBoolean(swirls());
    }

    @Override
    default String writeToString() {
        return ParticleOptionUtils.toString(this, duration(), swirls());
    }

    static <T extends DurationParticleOption> ParticleOptions.Deserializer<T> deserializer(final BiFunction<Float, Boolean, T> factory) {
        return new ParticleOptions.Deserializer<>() {
            @Override
            public T fromCommand(final ParticleType<T> type, final StringReader reader) throws CommandSyntaxException {
                return factory.apply(ParticleOptionUtils.readFloat(reader), ParticleOptionUtils.readBoolean(reader));
            }

            @Override
            public T fromNetwork(final ParticleType<T> type, final FriendlyByteBuf buffer) {
                return factory.apply(buffer.readFloat(), buffer.readBoolean());
            }
        };
    }
}
