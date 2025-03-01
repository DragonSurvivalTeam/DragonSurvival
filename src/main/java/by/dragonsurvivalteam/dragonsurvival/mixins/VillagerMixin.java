package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.HunterOmenHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin extends AbstractVillager {
    public VillagerMixin(final EntityType<? extends AbstractVillager> entityType, final Level level) {
        super(entityType, level);
    }

    @Inject(method = "startTrading", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$preventTradingWithMarkedPlayers(final Player player, final CallbackInfo callback) {
        if (player.hasEffect(DSEffects.HUNTER_OMEN)) {
            EntityStateHandler handler = getData(DSDataAttachments.ENTITY_HANDLER);

            if (handler.pillageCooldown == 0) {
                Villager villager = (Villager) (Object) this;
                // To level up trades for players which are stealing
                villager.setVillagerXp(villager.getVillagerXp() + ServerConfig.pillageXPGain);

                if (villager.shouldIncreaseLevel()) {
                    villager.updateMerchantTimer = 40;
                    villager.increaseProfessionLevelOnUpdate = true;
                }

                setLastHurtByMob(player); // To increase the prices when players are stealing
                HunterOmenHandler.generateVillagerLoot(villager, player.level(), null, false).forEach(player.getInventory()::add);
                villager.makeSound(getHurtSound(damageSources().generic()));
                player.level().broadcastEntityEvent(villager, EntityEvent.VILLAGER_ANGRY);

                if (player instanceof ServerPlayer serverPlayer) {
                    DSAdvancementTriggers.STEAL_FROM_VILLAGER.get().trigger(serverPlayer);
                }

                handler.setPillageCooldown();
                handler.sync(this, null);
            } else {
                setUnhappy();
            }

            callback.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void dragonSurvival$tickPillagedTimer(final CallbackInfo callback) {
        getExistingData(DSDataAttachments.ENTITY_HANDLER).ifPresent(handler -> handler.pillageCooldown = Math.max(0, handler.pillageCooldown - 1));
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void dragonSurvival$triggerSweatEvent(final CallbackInfo callback) {
        Villager villager = (Villager) (Object) this;

        if (villager.isNoAi()) {
            return;
        }

        villager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).flatMap(entities -> entities.findClosest(player -> HunterOmenHandler.avoidPlayer(villager, player))).ifPresent(player -> {
            if (villager.getRandom().nextInt(100) < villager.distanceToSqr(player)) {
                // Trigger the action less often if the player is further away
                return;
            }

            villager.level().broadcastEntityEvent(villager, EntityEvent.VILLAGER_SWEAT);
        });
    }

    @Shadow public abstract void setLastHurtByMob(@Nullable final LivingEntity entity);
    @Shadow protected abstract SoundEvent getHurtSound(@NotNull final DamageSource damageSource);
    @Shadow protected abstract void setUnhappy();
}
