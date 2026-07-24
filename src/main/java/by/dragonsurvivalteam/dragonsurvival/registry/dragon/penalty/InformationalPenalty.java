package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

public record InformationalPenalty() implements PenaltyEffect {
    public static final InformationalPenalty INSTANCE = new InformationalPenalty();
    public static final MapCodec<InformationalPenalty> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void apply(ServerPlayer player, Holder<DragonPenalty> penalty) { }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
