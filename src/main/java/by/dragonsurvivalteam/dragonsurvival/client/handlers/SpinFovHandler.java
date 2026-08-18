package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.compat.do_a_barrel_roll.DoABarrelRollCompat;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;


@EventBusSubscriber(Dist.CLIENT)
public class SpinFovHandler {
    @SubscribeEvent
    public static void onFovEvent(final ComputeFovModifierEvent event) {
        if (!ClientFlightHandler.spinCameraEffect || DoABarrelRollCompat.isActive(Minecraft.getInstance().player)) {
            return;
        }

        float spinFovMultiplier = SpinFlightPresentation.getFovMultiplier(ClientFlightHandler.spinCameraFovStrength);

        if (spinFovMultiplier != 1.0F) {
            event.setNewFovModifier(event.getNewFovModifier() * spinFovMultiplier);
        }
    }
}
