package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.client.skins.DragonSkins;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class DragonSkinReloadHandler {
    @SubscribeEvent
    public static void onCommonSetupSkin(final FMLCommonSetupEvent ignored) {
        DragonSkins.init(true);
    }
}
