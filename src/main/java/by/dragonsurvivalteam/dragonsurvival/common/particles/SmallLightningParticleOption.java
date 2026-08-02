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

public record SmallLightningParticleOption(float duration, boolean swirls) implements DurationParticleOption {
    public static final Codec<SmallLightningParticleOption> CODEC = RecordCodecBuilder.create(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(SmallLightningParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(SmallLightningParticleOption::swirls)
    ).apply(codecBuilder, SmallLightningParticleOption::new));

    public static final StreamCodec<ByteBuf, SmallLightningParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SmallLightningParticleOption::duration,
            ByteBufCodecs.BOOL, SmallLightningParticleOption::swirls,
            SmallLightningParticleOption::new
    );
    public static final ParticleOptions.Deserializer<SmallLightningParticleOption> DESERIALIZER = DurationParticleOption.deserializer(SmallLightningParticleOption::new);

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.LIGHTNING.get();
    }
}
