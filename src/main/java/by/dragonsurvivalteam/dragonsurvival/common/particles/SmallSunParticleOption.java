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

public record SmallSunParticleOption(float duration, boolean swirls) implements ParticleOptions {
    public static final MapCodec<SmallSunParticleOption> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(SmallSunParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(SmallSunParticleOption::swirls)
    ).apply(codecBuilder, SmallSunParticleOption::new));

    public static final StreamCodec<ByteBuf, SmallSunParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SmallSunParticleOption::duration,
            ByteBufCodecs.BOOL, SmallSunParticleOption::swirls,
            SmallSunParticleOption::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.SUN.value();
    }
}