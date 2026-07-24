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

public record LargePoisonParticleOption(float duration, boolean swirls) implements ParticleOptions {
    public static final MapCodec<LargePoisonParticleOption> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(LargePoisonParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(LargePoisonParticleOption::swirls)
    ).apply(codecBuilder, LargePoisonParticleOption::new));

    public static final StreamCodec<ByteBuf, LargePoisonParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, LargePoisonParticleOption::duration,
            ByteBufCodecs.BOOL, LargePoisonParticleOption::swirls,
            LargePoisonParticleOption::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.LARGE_POISON.value();
    }
}