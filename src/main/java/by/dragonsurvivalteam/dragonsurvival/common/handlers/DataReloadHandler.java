package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber
public class DataReloadHandler {
    public static long lastReload;

    @SubscribeEvent
    public static void handleDatapackReload(final TagsUpdatedEvent event) {
        lastReload = System.currentTimeMillis();
        // Putting a try catch here; in some strange edge cases
        // (trying to use things like https://modrinth.com/mod/be-quiet-negotiator
        // to have a multiserver architecture) you'll end up throwing errors during this
        // stage that you actually would want to ignore.
        try {
            DragonStage.update(event.getRegistryAccess());
            DragonSpecies.validate(event.getRegistryAccess());
            DragonAbility.validate(event.getRegistryAccess());
        } catch (Exception e) {
            DragonSurvival.LOGGER.error("An error was thrown while trying to reload datapacks: {}", e.toString());
        }
    }
}
