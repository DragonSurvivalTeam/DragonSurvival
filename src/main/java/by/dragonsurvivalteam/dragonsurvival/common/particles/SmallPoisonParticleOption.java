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

public record SmallPoisonParticleOption(float duration, boolean swirls) implements ParticleOptions {
    public static final MapCodec<SmallPoisonParticleOption> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(SmallPoisonParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(SmallPoisonParticleOption::swirls)
    ).apply(codecBuilder, SmallPoisonParticleOption::new));

    public static final StreamCodec<ByteBuf, SmallPoisonParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SmallPoisonParticleOption::duration,
            ByteBufCodecs.BOOL, SmallPoisonParticleOption::swirls,
            SmallPoisonParticleOption::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.POISON.value();
    }
}