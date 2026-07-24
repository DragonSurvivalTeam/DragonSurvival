package by.dragonsurvivalteam.dragonsurvival.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class TradeEffect extends MobEffect {
    public TradeEffect(final MobEffectCategory type, int color) {
        super(type, color);
    }

    @Override // Makes the effect incurable
    public void fillEffectCures(final Set<EffectCure> cures, @NotNull final MobEffectInstance effectInstance) {
        cures.clear();
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}