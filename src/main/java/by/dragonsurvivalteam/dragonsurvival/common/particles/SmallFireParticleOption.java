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

public record SmallFireParticleOption(float duration, boolean swirls) implements DurationParticleOption {
    public static final Codec<SmallFireParticleOption> CODEC = RecordCodecBuilder.create(codecBuilder -> codecBuilder.group(
            Codec.FLOAT.fieldOf("duration").forGetter(SmallFireParticleOption::duration),
            Codec.BOOL.fieldOf("swirls").forGetter(SmallFireParticleOption::swirls)
    ).apply(codecBuilder, SmallFireParticleOption::new));

    public static final StreamCodec<ByteBuf, SmallFireParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, SmallFireParticleOption::duration,
            ByteBufCodecs.BOOL, SmallFireParticleOption::swirls,
            SmallFireParticleOption::new
    );
    public static final ParticleOptions.Deserializer<SmallFireParticleOption> DESERIALIZER = DurationParticleOption.deserializer(SmallFireParticleOption::new);

    @Override
    public @NotNull ParticleType<?> getType() {
        return DSParticles.FIRE.get();
    }
}
