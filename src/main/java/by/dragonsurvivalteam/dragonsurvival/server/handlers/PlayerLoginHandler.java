package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StopTickingSound;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.SupplyTrigger;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class PlayerLoginHandler {
    // TODO: Do we need to start up any existing ticking sounds when a player starts getting tracked? e.g. moves into render distance while casting.
    // Do we even care enough to account for this edge case?
    @SubscribeEvent
    public static void onTrackingStart(final PlayerEvent.StartTracking event) {
        Player tracker = event.getEntity();
        Entity tracked = event.getTarget();
        syncDragonData(tracker, tracked);
    }

    @SubscribeEvent
    public static void onTrackingEnd(final PlayerEvent.StopTracking event) {
        Player tracker = event.getEntity();
        Entity tracked = event.getTarget();
        stopTickingSounds(tracker, tracked);
    }

    @SubscribeEvent
    public static void onLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        // Remove any existing penalty supplies that may no longer be relevant (due to datapack changes)
        event.getEntity().getExistingData(DSDataAttachments.PENALTY_SUPPLY).ifPresent(data -> {
            DragonStateHandler handler = DragonStateProvider.getData(event.getEntity());

            for (ResourceLocation supplyType : data.getSupplyTypes()) {
                if (handler.species().value().penalties().stream().noneMatch(penalty -> penalty.value().trigger() instanceof SupplyTrigger supplyTrigger && supplyTrigger.supplyType().equals(supplyType))) {
                    data.remove(supplyType);
                }
            }
        });

        syncComplete(event.getEntity());
    }

    @SubscribeEvent
    public static void onRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
           syncComplete(player);
        }
    }

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncComplete(event.getEntity());
    }

    @SubscribeEvent
    public static void startWithDragonChoice(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer) || serverPlayer.isDeadOrDying()) {
            return;
        }

        AltarData data = AltarData.getData(serverPlayer);

        if (data.altarCooldown > 0) {
            data.altarCooldown--;
        }

        if (!ServerConfig.startWithDragonChoice || data.hasUsedAltar || data.isInAltar || serverPlayer.tickCount < Functions.secondsToTicks(5)) {
            return;
        }

        PacketDistributor.sendToPlayer(serverPlayer, OpenDragonAltar.INSTANCE);
        data.isInAltar = true;
    }

    private static void stopTickingSounds(final Entity tracker, final Entity tracked) {
        if (tracker instanceof ServerPlayer trackerPlayer && tracked instanceof ServerPlayer trackedPlayer) {
            MagicData magicDataTracked = MagicData.getData(trackedPlayer);
            DragonAbilityInstance currentlyCasting = magicDataTracked.getCurrentlyCasting();

            if (currentlyCasting != null) {
                PacketDistributor.sendToPlayer(trackerPlayer, new StopTickingSound(currentlyCasting.location().withSuffix(trackedPlayer.getStringUUID())));
            }
        }
    }

    private static void syncDragonData(final Player syncTo, final Entity syncFrom) {
        if (syncTo instanceof ServerPlayer target && syncFrom instanceof ServerPlayer) {
            DragonStateProvider.getOptional(syncFrom).ifPresent(dragonStateHandler -> {
                PacketDistributor.sendToPlayer(target, new SyncComplete.Data(syncFrom.getId(), dragonStateHandler.serializeNBT(syncFrom.registryAccess())));
            });
        }
    }

    public static void syncComplete(final Entity entity) {
        if (entity instanceof ServerPlayer player) {
            DragonStateProvider.getOptional(player).ifPresent(handler -> {
                if (handler.species() != null && handler.body() == null) {
                    // Otherwise players won't be able to join the world
                    handler.setBody(player, DragonBody.random(player.registryAccess()));
                    DragonSurvival.LOGGER.error("Player {} was a dragon but had no dragon body", player);
                }

                SyncComplete.handleDragonSync(player, true);
                PacketDistributor.sendToPlayer(player, new SyncComplete.Data(player.getId(), handler.serializeNBT(player.registryAccess())));
            });

            syncDataAttachments(player);
        }
    }

    private static void syncDataAttachments(final ServerPlayer player) {
        player.getExistingData(DSDataAttachments.MODIFIERS_WITH_DURATION).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.PENALTY_SUPPLY).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.HARVEST_BONUSES).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.EFFECT_MODIFICATIONS).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.OXYGEN_BONUSES).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.BLOCK_VISION).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.SPIN).ifPresent(data -> data.sync(player));
        player.getExistingData(DSDataAttachments.GLOW).ifPresent(data -> data.sync(player));
    }
}
