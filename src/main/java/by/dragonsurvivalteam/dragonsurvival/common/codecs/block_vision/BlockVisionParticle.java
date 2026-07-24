package by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

public record BlockVisionParticle(int rate) implements BlockVisionType {
    public static final int DEFAULT_PARTICLE_RATE = 10;

    public static final MapCodec<BlockVisionParticle> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.intRange(1, Integer.MAX_VALUE).optionalFieldOf("rate", DEFAULT_PARTICLE_RATE).forGetter(BlockVisionParticle::rate)
    ).apply(instance, BlockVisionParticle::new));

    @Override
    public MapCodec<? extends BlockVisionType> codec() {
        return CODEC;
    }
}
