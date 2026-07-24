package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber // Failsafe in case the data gets out of sync
public class ServerPlayerStatusSync {
    // TODO :: is there any point in syncing once every 10 minutes?
    private static final int SYNC_RATE = Functions.secondsToTicks(600);

    @SubscribeEvent
    public static void onServerTick(final PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        Player player = event.getEntity();

        if (!player.isAddedToLevel() || !player.isAlive()) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (data.isDragon() && player.tickCount >= data.lastSync + SYNC_RATE) {
            data.lastSync = player.tickCount;
            PacketDistributor.sendToPlayersTrackingEntity(player, new SyncComplete(player.getId(), data.serializeNBT(player.registryAccess())));
        }
    }
}