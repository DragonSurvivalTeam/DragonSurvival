package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.util.BlockPosHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/** See {@link by.dragonsurvivalteam.dragonsurvival.client.handlers.DragonDestructionHandler} for client-specific handling */
@EventBusSubscriber
public class DragonDestructionHandler {
    private static int crushTickCounter;
    private static boolean isBreakingMultipleBlocks;

    private static void checkAndDestroyCollidingBlocks(final DragonStateHandler data, final TickEvent.PlayerTickEvent event, final AABB boundingBox) {
        MiscCodecs.DestructionData destructionData = data.stage().value().destructionData().orElse(null);

        if (destructionData == null || !destructionData.isBlockDestructionAllowed(data.getGrowth())) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;
        BlockPosHelper.betweenClosedCeil(boundingBox).forEach(position -> {
            if (!destructionData.blockPredicate().test((ServerLevel) player.level(), position)) {
                return;
            }

            if (player.getRandom().nextDouble() > ServerConfig.blockDestructionRemoval) {
                player.level().destroyBlock(position, false);
            } else {
                player.level().removeBlock(position, false);
            }
        });
    }

    @SubscribeEvent
    public static void destroyBlocksInRadius(final BlockEvent.BreakEvent event) {
        if (isBreakingMultipleBlocks) {
            return;
        }

        if (!(event.getPlayer() instanceof ServerPlayer player) || player.isCrouching()) {
            return;
        }

        int radius = (int) event.getPlayer().getAttributeValue(DSAttributes.BLOCK_BREAK_RADIUS.get());

        if (radius < 1) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.multiMining == DragonStateHandler.MultiMining.DISABLED) {
            return;
        }

        event.setCanceled(true);
        isBreakingMultipleBlocks = true;
        float centerSpeed = event.getState().getDestroySpeed(event.getLevel(), event.getPos());

        BlockPos.betweenClosedStream(AABB.ofSize(event.getPos().getCenter(), radius, radius, radius)).forEach(position -> {
            float speed = event.getLevel().getBlockState(position).getDestroySpeed(event.getLevel(), position);

            if (speed != /* Bedrock strength */ -1 && speed <= centerSpeed) {
                player.gameMode.destroyBlock(position);
            }
        });

        isBreakingMultipleBlocks = false;
    }

    @SubscribeEvent
    public static void checkAndDestroyCollidingBlocksAndCrushedEntities(final TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.isCrouching()) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon() || data.largeDragonDestruction == DragonStateHandler.LargeDragonDestruction.DISABLED) {
            return;
        }

        MiscCodecs.DestructionData destructionData = data.stage().value().destructionData().orElse(null);

        if (destructionData == null || !destructionData.isDestructionAllowed(data.getGrowth())) {
            return;
        }

        Vec2 horizontalDeltaMovement = new Vec2((float) player.getDeltaMovement().x, (float) player.getDeltaMovement().z);

        if (horizontalDeltaMovement.length() < 0.05f && Math.abs(player.getDeltaMovement().y) < 0.25f) {
            return;
        }

        AABB boundingBox = player.getBoundingBox();
        AABB blockCollisionBoundingBox = boundingBox.inflate(1.5);

        checkAndDestroyCollidingBlocks(data, event, blockCollisionBoundingBox);
        checkAndDamageCrushedEntities(data, player, blockCollisionBoundingBox);
    }

    private static void checkAndDamageCrushedEntities(DragonStateHandler data, ServerPlayer player, AABB boundingBox) {
        MiscCodecs.DestructionData destructionData = data.stage().value().destructionData().orElse(null);
        if (destructionData == null || !destructionData.isCrushingAllowed(data.getGrowth())) {
            return;
        }

        if (--crushTickCounter > 0) {
            return;
        }

        // Get only the bounding box of the player's feet (estimate by using only the bottom 1/3rd of the bounding box)
        AABB feetBoundingBox = new AABB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY - (boundingBox.maxY - boundingBox.minY) / 3.0, boundingBox.maxZ);

        for (LivingEntity entity : player.level().getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT, player, feetBoundingBox)) {
            // If the entity being crushed is too big, don't damage it
            if (entity.getBoundingBox().getSize() > boundingBox.getSize() * ServerConfig.crushingSizeRatio) {
                continue;
            }

            if (destructionData.entityPredicate().matches(player, entity)) {
                entity.hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.CRUSHED), player), (float) (data.getGrowth() * destructionData.crushingDamageScalar()));
                crushTickCounter = ServerConfig.crushingTickDelay;
            }
        }
    }
}
