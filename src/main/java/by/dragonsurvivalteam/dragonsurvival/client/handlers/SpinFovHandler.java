package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.client.event.ComputeFovModifierEvent;


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
