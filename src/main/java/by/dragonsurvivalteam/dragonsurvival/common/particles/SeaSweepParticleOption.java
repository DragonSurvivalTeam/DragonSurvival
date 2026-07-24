package by.dragonsurvivalteam.dragonsurvival.common.particles;

import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record SeaSweepParticleOption(double quadSize) implements ParticleOptions {
    public static final Codec<SeaSweepParticleOption> CODEC = RecordCodecBuilder.create(codecBuilder -> codecBuilder.group(
            Codec.DOUBLE.fieldOf("quadSize").forGetter(SeaSweepParticleOption::quadSize)
    ).apply(codecBuilder, SeaSweepParticleOption::new));

    public static final StreamCodec<ByteBuf, SeaSweepParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SeaSweepParticleOption::quadSize,
            SeaSweepParticleOption::new
    );
    public static final ParticleOptions.Deserializer<SeaSweepParticleOption> DESERIALIZER = ParticleOptionUtils.deserializer(
            STREAM_CODEC,
            reader -> new SeaSweepParticleOption(ParticleOptionUtils.readDouble(reader))
    );

    @Override
    public void writeToNetwork(final FriendlyByteBuf buffer) {
        STREAM_CODEC.encode(buffer, this);
    }

    @Override
    public String writeToString() {
        return ParticleOptionUtils.toString(this, quadSize);
    }

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.SEA_SWEEP.get();
    }
}
