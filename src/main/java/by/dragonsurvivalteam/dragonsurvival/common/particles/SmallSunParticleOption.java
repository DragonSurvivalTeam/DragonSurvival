package by.dragonsurvivalteam.dragonsurvival.common.particles;

import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import org.jetbrains.annotations.NotNull;

public record SmallSunParticleOption(float duration, boolean swirls) implements DurationParticleOption {
    public static final Codec<SmallSunParticleOption> CODEC = RecordCodecBuilder.create(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(SmallSunParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(SmallSunParticleOption::swirls)
    ).apply(codecBuilder, SmallSunParticleOption::new));

    public static final StreamCodec<ByteBuf, SmallSunParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SmallSunParticleOption::duration,
            ByteBufCodecs.BOOL, SmallSunParticleOption::swirls,
            SmallSunParticleOption::new
    );
    public static final ParticleOptions.Deserializer<SmallSunParticleOption> DESERIALIZER = DurationParticleOption.deserializer(SmallSunParticleOption::new);

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.SUN.get();
    }
}
