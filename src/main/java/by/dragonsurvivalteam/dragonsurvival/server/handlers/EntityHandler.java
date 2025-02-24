package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber
public class EntityHandler {
    @SubscribeEvent
    public static void attachAvoidDragonGoal(final EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Animal animal && !animal.getType().is(DSEntityTypeTags.ANIMAL_AVOID_BLACKLIST)) {
            animal.goalSelector.addGoal(5, new AvoidEntityGoal<>(animal, Player.class, entity -> {
                if (!ServerConfig.dragonsAreScary) {
                    return false;
                }
                
                if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
                    return false;
                }

                if (entity.hasEffect(DSEffects.ANIMAL_PEACE) || HunterData.hasMaxHunterStacks(entity)) {
                    return false;
                }

                return DragonStateProvider.isDragon(entity);
            }, 20, 1.3F, 1.5F, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test));
        }
    }
}
