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

public record LargeSunParticleOption(float duration, boolean swirls) implements ParticleOptions {
    public static final MapCodec<LargeSunParticleOption> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(LargeSunParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(LargeSunParticleOption::swirls)
    ).apply(codecBuilder, LargeSunParticleOption::new));

    public static final StreamCodec<ByteBuf, LargeSunParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, LargeSunParticleOption::duration,
            ByteBufCodecs.BOOL, LargeSunParticleOption::swirls,
            LargeSunParticleOption::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.LARGE_SUN.value();
    }
}