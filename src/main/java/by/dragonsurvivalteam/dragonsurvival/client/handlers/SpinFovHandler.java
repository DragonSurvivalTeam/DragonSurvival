package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.compat.do_a_barrel_roll.DoABarrelRollCompat;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;


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
