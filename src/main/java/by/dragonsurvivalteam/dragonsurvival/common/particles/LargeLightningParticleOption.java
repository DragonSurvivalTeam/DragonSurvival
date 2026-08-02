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

public record LargeLightningParticleOption(float duration, boolean swirls) implements DurationParticleOption {
    public static final Codec<LargeLightningParticleOption> CODEC = RecordCodecBuilder.create(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(LargeLightningParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(LargeLightningParticleOption::swirls)
    ).apply(codecBuilder, LargeLightningParticleOption::new));

    public static final StreamCodec<ByteBuf, LargeLightningParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, LargeLightningParticleOption::duration,
            ByteBufCodecs.BOOL, LargeLightningParticleOption::swirls,
            LargeLightningParticleOption::new
    );
    public static final ParticleOptions.Deserializer<LargeLightningParticleOption> DESERIALIZER = DurationParticleOption.deserializer(LargeLightningParticleOption::new);

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.LARGE_LIGHTNING.get();
    }
}
