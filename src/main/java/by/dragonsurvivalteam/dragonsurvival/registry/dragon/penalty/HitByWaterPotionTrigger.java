package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;

public record HitByWaterPotionTrigger() implements PenaltyTrigger {
    public static final HitByWaterPotionTrigger INSTANCE = new HitByWaterPotionTrigger();
    public static final MapCodec<HitByWaterPotionTrigger> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public MapCodec<? extends PenaltyTrigger> codec() {
        return CODEC;
    }

    @Override
    public boolean hasCustomTrigger() {
        return true;
    }

    @Override
    public boolean matches(ServerPlayer dragon, boolean conditionMatched) {
        return conditionMatched;
    }
}
