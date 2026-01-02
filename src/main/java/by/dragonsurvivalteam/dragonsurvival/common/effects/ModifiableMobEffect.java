package by.dragonsurvivalteam.dragonsurvival.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

// FIXME :: Curing logic has been completely removed, we need a new way to handle incurable effects
public class ModifiableMobEffect extends MobEffect {
    public ModifiableMobEffect(final MobEffectCategory type, int color, boolean incurable) {
        super(type, color);
    }
}