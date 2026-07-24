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

public record SmallLightningParticleOption(float duration, boolean swirls) implements ParticleOptions {
    public static final MapCodec<SmallLightningParticleOption> CODEC = RecordCodecBuilder.mapCodec(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(SmallLightningParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(SmallLightningParticleOption::swirls)
    ).apply(codecBuilder, SmallLightningParticleOption::new));

    public static final StreamCodec<ByteBuf, SmallLightningParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SmallLightningParticleOption::duration,
            ByteBufCodecs.BOOL, SmallLightningParticleOption::swirls,
            SmallLightningParticleOption::new
    );

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.LIGHTNING.value();
    }
}