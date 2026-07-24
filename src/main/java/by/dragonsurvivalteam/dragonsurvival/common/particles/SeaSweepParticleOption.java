package by.dragonsurvivalteam.dragonsurvival.common.particles;

import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record SeaSweepParticleOption(double quadSize) implements ParticleOptions {
    public static final MapCodec<SeaSweepParticleOption> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder.group(
            Codec.DOUBLE.fieldOf("quadSize").forGetter(SeaSweepParticleOption::quadSize)
    ).apply(codecBuilder, SeaSweepParticleOption::new));

    public static final StreamCodec<ByteBuf, SeaSweepParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, SeaSweepParticleOption::quadSize,
            SeaSweepParticleOption::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.SEA_SWEEP.value();
    }
}
