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

public record LargePoisonParticleOption(float duration, boolean swirls) implements DurationParticleOption {
    public static final Codec<LargePoisonParticleOption> CODEC = RecordCodecBuilder.create(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(LargePoisonParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(LargePoisonParticleOption::swirls)
    ).apply(codecBuilder, LargePoisonParticleOption::new));

    public static final StreamCodec<ByteBuf, LargePoisonParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, LargePoisonParticleOption::duration,
            ByteBufCodecs.BOOL, LargePoisonParticleOption::swirls,
            LargePoisonParticleOption::new
    );
    public static final ParticleOptions.Deserializer<LargePoisonParticleOption> DESERIALIZER = DurationParticleOption.deserializer(LargePoisonParticleOption::new);

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.LARGE_POISON.get();
    }
}
