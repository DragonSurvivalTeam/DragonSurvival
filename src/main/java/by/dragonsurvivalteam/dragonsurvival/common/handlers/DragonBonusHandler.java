package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncPlayerJump;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.living.LivingEvent;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;

@EventBusSubscriber
public class DragonBonusHandler {
    @SubscribeEvent
    public static void onJump(final LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.getEffect(DSEffects.TRAPPED.get()) != null) {
            Vec3 deltaMovement = entity.getDeltaMovement();
            entity.setDeltaMovement(deltaMovement.x, deltaMovement.y < 0 ? deltaMovement.y : 0, deltaMovement.z);
            entity.setJumping(false);
            return;
        }

        if (!DragonStateProvider.isDragon(entity)) {
            return;
        }

        // Don't consider the player jumping if they have wings spread; otherwise you end up with strange behavior once you finally touch the ground after flying
        if (entity instanceof ServerPlayer serverPlayer) {
            if (!AttachmentManager.getData(serverPlayer, DSDataAttachments.FLIGHT).areWingsSpread) {
                PacketDistributor.sendToPlayersTrackingEntity(serverPlayer, new SyncPlayerJump(entity.getId(), true));
            }
        } else if (entity instanceof Player player) {
            if (!AttachmentManager.getData(player, DSDataAttachments.FLIGHT).areWingsSpread) {
                DragonEntity.DRAGONS_JUMPING.put(player.getId(), true);
            }
        }
    }

    public static void addFireProtectionToDragonDrop(final Entity breaker, final ItemEntity drop) {
        if (breaker == null) {
            return;
        }

        // TODO :: also handle experience? would need a hook in 'CommonHooks#handleBlockDrops' to store some context and then modify the experience orb in 'ExperienceOrb#award'
        // TODO :: remove check for dragon?
        if (breaker.fireImmune() && DragonStateProvider.isDragon(breaker) && breaker.isInLava()) {
            AttachmentManager.getData(drop, DSDataAttachments.ITEM).isFireImmune = true;
        }
    }
}
