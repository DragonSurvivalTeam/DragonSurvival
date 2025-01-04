package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncPlayerJump;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public class DragonBonusHandler {
    @SubscribeEvent
    public static void dragonDamageImmunities(final LivingIncomingDamageEvent event) {
        // TODO :: cave dragon took 'CaveDragonConfig.caveSplashDamage' damage from snowballs without the DSEffects.FIRE effect
        //  how to replicate this in the new system? part of the config that damages the entity from thrown objects like water potions?
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent jumpEvent) {
        final LivingEntity living = jumpEvent.getEntity();

        if(living.getEffect(DSEffects.TRAPPED) != null){
            Vec3 deltaMovement = living.getDeltaMovement();
            living.setDeltaMovement(deltaMovement.x, deltaMovement.y < 0 ? deltaMovement.y : 0, deltaMovement.z);
            living.setJumping(false);
            return;
        }

        DragonStateProvider.getOptional(living).ifPresent(dragonStateHandler -> {
            if (dragonStateHandler.isDragon()) {
                if (living instanceof ServerPlayer) {
                    PacketDistributor.sendToAllPlayers(new SyncPlayerJump.Data(living.getId(), 10));
                }
            }
        });
    }

    @SubscribeEvent
    public static void addFireProtectionToDragonDrops(BlockDropsEvent dropsEvent) {
        if (dropsEvent.getBreaker() == null) return;

        // TODO :: also handle experience? would need a hook in 'CommonHooks#handleBlockDrops' to store some context and then modify the experience orb in 'ExperienceOrb#award'
        if (dropsEvent.getBreaker().fireImmune() && DragonStateProvider.isDragon(dropsEvent.getBreaker())) {
            dropsEvent.getDrops().forEach(drop -> drop.getData(DSDataAttachments.ENTITY_HANDLER).isFireImmune = true);
        }
    }
}