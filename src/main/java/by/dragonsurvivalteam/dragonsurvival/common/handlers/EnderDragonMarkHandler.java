package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EnderDragonDamageHistory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber
public class EnderDragonMarkHandler {

    @SubscribeEvent
    public static void onEnderDragonHealthChanged(LivingDamageEvent.Post event) {
        if(event.getEntity().level().isClientSide()) return;

        if(event.getEntity() instanceof EnderDragon enderDragon) {
            if(event.getSource().getEntity() instanceof Player player) {
                EnderDragonDamageHistory data = EnderDragonDamageHistory.getData(enderDragon);
                data.addDamage(player.getUUID(), event.getNewDamage());
            }

            // If the dragon is healed, reverse progress for all the players
            if(event.getNewDamage() < 0) {
                EnderDragonDamageHistory data = EnderDragonDamageHistory.getData(enderDragon);
                data.addDamageAll(event.getNewDamage());
            }
        }
    }

    @SubscribeEvent
    public static void onEnderDragonDeath(LivingDeathEvent event) {
        if(event.getEntity().level().isClientSide()) return;

        if(event.getEntity() instanceof EnderDragon enderDragon) {
            EnderDragonDamageHistory data = EnderDragonDamageHistory.getData(enderDragon);
            for(Player player : data.getPlayers(event.getEntity().level())) {
                DragonStateHandler handler = DragonStateProvider.getData(player);
                if(handler.isDragon()) {
                    handler.markedByEnderDragon = true;
                }
            }
        }
    }
}
