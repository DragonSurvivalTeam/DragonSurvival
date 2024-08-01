package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DragonSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT )
public class DragonSkinReloadHandle {
    @SubscribeEvent
    public static void onReloadEvent(AddReloadListenerEvent reloadEvent) {
        DragonSkins.init(true);
    }
}
