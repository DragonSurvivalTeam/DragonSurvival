package by.dragonsurvivalteam.dragonsurvival.compat.appleskin;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import squeek.appleskin.api.event.FoodValuesEvent;

@SuppressWarnings("unused")
@EventBusSubscriber
public class AppleSkinHandler {
    @SubscribeEvent
    public static void appleSkinFoodValuesEvent(FoodValuesEvent event) {
        DragonStateHandler handler = DragonStateProvider.getData(event.player);
        if (handler.isDragon()) {
            event.modifiedFoodProperties = DragonFoodHandler.getDragonFoodProperties(handler.species(), event.itemStack, event.modifiedFoodProperties);
        }
    }
}
