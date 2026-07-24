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

public record LargeFireParticleOption(float duration, boolean swirls) implements ParticleOptions {
    public static final MapCodec<LargeFireParticleOption> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(LargeFireParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(LargeFireParticleOption::swirls)
    ).apply(codecBuilder, LargeFireParticleOption::new));

    public static final StreamCodec<ByteBuf, LargeFireParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, LargeFireParticleOption::duration,
            ByteBufCodecs.BOOL, LargeFireParticleOption::swirls,
            LargeFireParticleOption::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.LARGE_FIRE.value();
    }
}