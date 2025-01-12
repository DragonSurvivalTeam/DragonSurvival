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

public record SmallFireParticleOption(float duration, boolean swirls) implements ParticleOptions {
    public static final MapCodec<SmallFireParticleOption> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(SmallFireParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(SmallFireParticleOption::swirls)
    ).apply(codecBuilder, SmallFireParticleOption::new));

    public static final StreamCodec<ByteBuf, SmallFireParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SmallFireParticleOption::duration,
            ByteBufCodecs.BOOL, SmallFireParticleOption::swirls,
            SmallFireParticleOption::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.FIRE.value();
    }
}