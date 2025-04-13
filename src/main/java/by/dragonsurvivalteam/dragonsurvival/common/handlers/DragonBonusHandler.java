package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncPlayerJump;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class DragonBonusHandler {
    @SubscribeEvent
    public static void onJump(final LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.getEffect(DSEffects.TRAPPED) != null) {
            Vec3 deltaMovement = entity.getDeltaMovement();
            entity.setDeltaMovement(deltaMovement.x, deltaMovement.y < 0 ? deltaMovement.y : 0, deltaMovement.z);
            entity.setJumping(false);
            return;
        }

        if (!DragonStateProvider.isDragon(entity)) {
            return;
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayersTrackingEntity(serverPlayer, new SyncPlayerJump(entity.getId(), 18));
        } else if (entity instanceof Player player) {
            DragonEntity.DRAGON_JUMP_TICKS.put(player.getId(), 18);
        }
    }

    @SubscribeEvent
    public static void addFireProtectionToDragonDrops(final BlockDropsEvent event) {
        if (event.getBreaker() == null) {
            return;
        }

        // TODO :: also handle experience? would need a hook in 'CommonHooks#handleBlockDrops' to store some context and then modify the experience orb in 'ExperienceOrb#award'
        // TODO :: remove check for dragon?
        if (event.getBreaker().fireImmune() && DragonStateProvider.isDragon(event.getBreaker()) && event.getBreaker().isInLava()) {
            event.getDrops().forEach(drop -> drop.getData(DSDataAttachments.ITEM).isFireImmune = true);
        }
    }
}