package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;


@EventBusSubscriber(Dist.CLIENT)
public class SpinFovHandler {
    @SubscribeEvent
    public static void onFovEvent(final ComputeFovModifierEvent event) {
        if (!ClientFlightHandler.spinCameraEffect) {
            return;
        }

        float spinFovMultiplier = SpinFlightPresentation.getFovMultiplier(ClientFlightHandler.spinCameraFovStrength);

        if (spinFovMultiplier != 1.0F) {
            event.setNewFovModifier(event.getNewFovModifier() * spinFovMultiplier);
        }
    }
}
