package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.EffectConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncResting;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.BlockPosHelper;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers.SLEEP_ON_TREASURE;

@EventBusSubscriber
public class DragonTreasureHandler {
    @Translation(key = "treasure_health_regeneration", type = Translation.Type.CONFIGURATION, comments = "Sleeping on treasure blocks will regenerate health if enabled")
    @ConfigOption(side = ConfigSide.SERVER, category = "treasure", key = "treasure_health_regeneration")
    public static Boolean IS_REGENERATION_HEALTH = true;

    @ConfigRange(min = 1, max = /* 1 hour */ 72_000)
    @Translation(key = "treasure_health_regeneration_rate", type = Translation.Type.CONFIGURATION, comments = "The time in ticks (20 ticks = 1 second) it takes to recover 1 health while sleeping on treasure")
    @ConfigOption(side = ConfigSide.SERVER, category = "treasure", key = "treasure_health_regeneration_rate")
    public static Integer REGENERATION_RATE = Functions.secondsToTicks(14);

    @ConfigRange(min = 1, max = /* 1 hour */ 72_000)
    @Translation(key = "nearby_treasure_rate_reduction", type = Translation.Type.CONFIGURATION, comments = {
            "The amount of ticks (20 ticks = 1 second) each nearby treasure reduces the health regeneration time by",
            "(i.e. it increases the rate of regeneration)"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "treasure", key = "nearby_treasure_rate_reduction")
    public static Integer TIME_REDUCTION = 1;

    @ConfigRange(min = 1, max = /* 16 x 9 x 16 hardcoded radius */ 2304)
    @Translation(key = "max_treasure_for_rate_reduction", type = Translation.Type.CONFIGURATION, comments = {
            "The maximum amount of additional treasure that can affect the health regeneration reduction",
            "Only treasure within a 16 x 9 x 16 radius is considered"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "treasure", key = "max_treasure_for_rate_reduction")
    public static Integer MAX_TREASURES = 240;

    @Translation(key = "effects_on_sleep", type = Translation.Type.CONFIGURATION, comments = {
            "Effects that are granted when skipping a night when sleeping on treasures",
            "Format: resource/tag;duration;amplifier;duration_multiplier;amplifier_multiplier",
            "The resource can also be defined using regular expressions (for both namespace and path)",
            "the multipliers are applied per nearby treasure ('max_treasure_for_rate_reduction' is used as limit)",
            "(amplifier is calculated with +1, the +1 is subtracted for the final result)"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "treasure", key = "effects_on_sleep")
    public static List<EffectConfig> EFFECTS_ON_SLEEP = List.of(EffectConfig.create(MobEffects.REGENERATION, 200, 0, 0.5, 0.01));

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

    /** Handles the healing when resting near treasure */
    private static void handleResting(final Player player, final TreasureRestData treasureData) {
        if (!IS_REGENERATION_HEALTH || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        int nearbyTreasure = 0;

        for (BlockPos position : BlockPosHelper.betweenClosed(AABB.ofSize(serverPlayer.position(), 16, 9, 16))) {
            BlockState state = serverPlayer.level().getBlockState(position);

            if (state.getBlock() instanceof TreasureBlock) {
                int layers = state.getValue(TreasureBlock.LAYERS);
                nearbyTreasure += layers;
            }
        }

        nearbyTreasure = Mth.clamp(nearbyTreasure, 0, MAX_TREASURES);
        treasureData.nearbyTreasure = nearbyTreasure;

        SLEEP_ON_TREASURE.get().trigger(serverPlayer, nearbyTreasure);

        int totalTime = REGENERATION_RATE;
        int restTimer = totalTime - TIME_REDUCTION * nearbyTreasure;

        if (treasureData.restingTicks >= restTimer) {
            treasureData.restingTicks = 0;

            if (player.getHealth() < player.getMaxHealth() + 1) {
                player.heal(1);
            }
        } else {
            treasureData.restingTicks++;
        }
    }
}