package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncResting;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import by.dragonsurvivalteam.dragonsurvival.util.BlockPosHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers.SLEEP_ON_TREASURE;

@EventBusSubscriber
public class DragonTreasureHandler {
    @SubscribeEvent
    public static void update(final PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        boolean isMining = DragonSurvival.PROXY.isMining(player);
        MovementData movement = MovementData.getData(player);
        movement.dig = isMining;

        TreasureRestData treasureData = TreasureRestData.getData(player);
        boolean wasResting = treasureData.isResting();

        if (treasureData.isResting() && (isMining || movement.isMoving() || player.isCrouching() || !(player.getBlockStateOn().getBlock() instanceof TreasureBlock))) {
            treasureData.setResting(false);
        }

        if (treasureData.isResting() != wasResting && player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncResting(serverPlayer.getId(), treasureData.isResting()));
            serverPlayer.serverLevel().updateSleepingPlayerList();
        }

        if (treasureData.isResting()) {
            treasureData.sleepingTicks++;
            handleResting(player, treasureData);
        }
    }

    /** Handles the healing when resting near treasure */
    private static void handleResting(final Player player, final TreasureRestData treasureData) {
        if (!ServerConfig.treasureHealthRegen || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int treasureNearby = 0;

        for (BlockPos position : BlockPosHelper.betweenClosed(AABB.ofSize(serverPlayer.position(), 16, 9, 16))) {
            BlockState state = serverPlayer.level().getBlockState(position);

            if (state.getBlock() instanceof TreasureBlock) {
                int layers = state.getValue(TreasureBlock.LAYERS);
                treasureNearby += layers;
            }
        }

        treasureNearby = Mth.clamp(treasureNearby, 0, ServerConfig.maxTreasures);
        SLEEP_ON_TREASURE.get().trigger(serverPlayer, treasureNearby);

        int totalTime = ServerConfig.treasureRegenTicks;
        int restTimer = totalTime - ServerConfig.treasureRegenTicksReduce * treasureNearby;

        if (treasureData.restingTicks >= restTimer) {
            treasureData.restingTicks = 0;

            if (player.getHealth() < player.getMaxHealth() + 1) {
                player.heal(1);
            }
        } else {
            treasureData.restingTicks++;
        }
    }

    @SubscribeEvent
    public static void interruptResting(final LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TreasureRestData treasureData = TreasureRestData.getData(serverPlayer);

            if (treasureData.isResting()) {
                treasureData.setResting(false);
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncResting(serverPlayer.getId(), treasureData.isResting()));
                serverPlayer.serverLevel().updateSleepingPlayerList();
            }
        }
    }
}