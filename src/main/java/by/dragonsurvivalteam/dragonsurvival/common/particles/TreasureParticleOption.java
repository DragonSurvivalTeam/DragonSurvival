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
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public record TreasureParticleOption(float red, float green, float blue, float scale) implements ParticleOptions {
    public static final MapCodec<TreasureParticleOption> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("red").forGetter(TreasureParticleOption::red),
            Codec.FLOAT.fieldOf("green").forGetter(TreasureParticleOption::green),
            Codec.FLOAT.fieldOf("blue").forGetter(TreasureParticleOption::blue),
            Codec.FLOAT.fieldOf("scale").forGetter(TreasureParticleOption::scale)).apply(instance, TreasureParticleOption::new));

    public static final StreamCodec<ByteBuf, TreasureParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, TreasureParticleOption::red,
            ByteBufCodecs.FLOAT, TreasureParticleOption::green,
            ByteBufCodecs.FLOAT, TreasureParticleOption::blue,
            ByteBufCodecs.FLOAT, TreasureParticleOption::scale,
            TreasureParticleOption::new
    );

    public TreasureParticleOption(float red, float green, float blue, float scale) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.scale = Mth.clamp(scale, 0.01F, 4.0F);
    }

    @Override
    public @NotNull ParticleType<TreasureParticleOption> getType() {
        return DSParticles.TREASURE.value();
    }
}