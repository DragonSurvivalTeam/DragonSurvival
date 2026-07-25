package by.dragonsurvivalteam.dragonsurvival.common.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ModifiableMobEffect extends MobEffect {
    private final boolean incurable;

    public ModifiableMobEffect(final MobEffectCategory type, int color, boolean incurable) {
        super(type, color);
        this.incurable = incurable;
    }

    public boolean isIncurable() {
        return incurable;
    }

    @SubscribeEvent
    public static void clearCurativeItems(final MobEffectEvent.Added event) {
        MobEffectInstance instance = event.getEffectInstance();
        if (instance.getEffect() instanceof ModifiableMobEffect effect && effect.isIncurable()) {
            instance.setCurativeItems(java.util.List.of());
        }
    }
}
