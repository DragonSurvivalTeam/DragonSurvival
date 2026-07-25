package by.dragonsurvivalteam.dragonsurvival.common.effects;

import net.minecraft.world.effect.MobEffectCategory;

public class TradeEffect extends ModifiableMobEffect {
    public TradeEffect(final MobEffectCategory type, int color) {
        super(type, color, true);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
