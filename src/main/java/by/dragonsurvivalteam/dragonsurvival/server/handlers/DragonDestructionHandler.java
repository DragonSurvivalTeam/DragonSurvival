package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.BlockPosHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** See {@link by.dragonsurvivalteam.dragonsurvival.client.handlers.DragonDestructionHandler} for client-specific handling */
@EventBusSubscriber
public class DragonDestructionHandler {
    private static final float SIZE_RATIO = 4;

    private static int crushTickCounter;
    private static boolean isBreakingMultipleBlocks;

    private static void checkAndDestroyCollidingBlocks(final DragonStateHandler data, final PlayerTickEvent event, final AABB boundingBox) {
        MiscCodecs.DestructionData destructionData = data.stage().value().destructionData().orElse(null);

        if (destructionData == null || !destructionData.isBlockDestructionAllowed(data.getSize())) {
            return;
        }

        BlockPosHelper.betweenClosedCeil(boundingBox).forEach(position -> {
            BlockState state = event.getEntity().level().getBlockState(position);

            if (state.isAir() || !state.is(DSBlockTags.LARGE_DRAGON_DESTRUCTIBLE)) {
                return;
            }

            if (event.getEntity().getRandom().nextDouble() > ServerConfig.blockDestructionRemoval) {
                event.getEntity().level().destroyBlock(position, false);
            } else {
                event.getEntity().level().removeBlock(position, false);
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

        int radius = (int) event.getPlayer().getAttributeValue(DSAttributes.BLOCK_BREAK_RADIUS);

        if (radius < 1) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.multiMining == DragonStateHandler.MultiMining.DISABLED) {
            return;
        }

        event.setCanceled(true);
        isBreakingMultipleBlocks = true;
        BlockPos.betweenClosedStream(AABB.ofSize(event.getPos().getCenter(), radius, radius, radius)).forEach(player.gameMode::destroyBlock);
        isBreakingMultipleBlocks = false;
    }

    @SubscribeEvent
    public static void checkAndDestroyCollidingBlocksAndCrushedEntities(final PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.isCrouching()) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon() || data.largeDragonDestruction == DragonStateHandler.LargeDragonDestruction.DISABLED) {
            return;
        }

        MiscCodecs.DestructionData destructionData = data.stage().value().destructionData().orElse(null);

        if (destructionData == null || !destructionData.isDestructionAllowed(data.getSize())) {
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
        if (destructionData == null || !destructionData.isCrushingAllowed(data.getSize())) {
            return;
        }

        if (--crushTickCounter > 0) {
            return;
        }

        // Get only the bounding box of the player's feet (estimate by using only the bottom 1/3rd of the bounding box)
        AABB feetBoundingBox = new AABB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY - (boundingBox.maxY - boundingBox.minY) / 3.0, boundingBox.maxZ);

        for (LivingEntity entity : player.level().getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT, player, feetBoundingBox)) {
            // If the entity being crushed is too big, don't damage it
            if (entity.getBoundingBox().getSize() > boundingBox.getSize() / SIZE_RATIO) {
                continue;
            }

            entity.hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.CRUSHED), player), (float) (data.getSize() * destructionData.crushingDamageScalar()));
            crushTickCounter = ServerConfig.crushingTickDelay;
        }
    }
}
