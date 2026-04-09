package by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision;

import com.mojang.serialization.MapCodec;

public record BlockVisionOutline() implements BlockVisionType {
    public static final BlockVisionOutline INSTANCE = new BlockVisionOutline();
    public static final MapCodec<BlockVisionOutline> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends BlockVisionType> codec() {
        return CODEC;
    }
}
