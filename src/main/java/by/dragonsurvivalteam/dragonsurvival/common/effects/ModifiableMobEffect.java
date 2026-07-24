package by.dragonsurvivalteam.dragonsurvival.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class ModifiableMobEffect extends MobEffect {
    private final boolean incurable;

    public ModifiableMobEffect(final MobEffectCategory type, int color, boolean incurable) {
        super(type, color);
        this.incurable = incurable;
    }

    @Override
    public void fillEffectCures(@NotNull final Set<EffectCure> cures, @NotNull final MobEffectInstance effectInstance) {
        if (incurable) {
            cures.clear();
        } else {
            super.fillEffectCures(cures, effectInstance);
        }
    }
}