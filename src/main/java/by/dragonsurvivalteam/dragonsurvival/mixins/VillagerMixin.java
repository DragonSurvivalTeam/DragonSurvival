package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.HunterOmenHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerMixin {
    @Unique private int dragonSurvival$pillagedTimer;

    @Inject(method = "startTrading", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$preventTradingWithMarkedPlayers(final Player player, final CallbackInfo callback) {
        if (player.hasEffect(DSEffects.HUNTER_OMEN)) {
            if (dragonSurvival$pillagedTimer == 0) {
                Villager villager = (Villager) (Object) this;
                // To level up trades for players which are stealing
                villager.setVillagerXp(villager.getVillagerXp() + ServerConfig.pillageXPGain);

                if (villager.shouldIncreaseLevel()) {
                    villager.updateMerchantTimer = 40;
                    villager.increaseProfessionLevelOnUpdate = true;
                }

                setLastHurtByMob(player); // To increase the prices when players are stealing
                HunterOmenHandler.generateVillagerLoot(villager, player.level(), null, false).forEach(player.getInventory()::add);
                villager.makeSound(getHurtSound(null));
                player.level().broadcastEntityEvent(villager, EntityEvent.VILLAGER_ANGRY);
                dragonSurvival$pillagedTimer = Functions.secondsToTicks(600);
            } else {
                setUnhappy();
            }

            callback.cancel();
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void dragonSurvival$readPillagedTimer(final CompoundTag tag, final CallbackInfo callback) {
        dragonSurvival$pillagedTimer = tag.getInt(DragonSurvival.MODID + ".pillaged_timer");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void dragonSurvival$savePillagedTimer(final CompoundTag tag, final CallbackInfo callback) {
        tag.putInt(DragonSurvival.MODID + ".pillaged_timer", dragonSurvival$pillagedTimer);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void dragonSurvival$tickPillagedTimer(CallbackInfo ci) {
        if (dragonSurvival$pillagedTimer > 0) {
            dragonSurvival$pillagedTimer--;
        }
    }

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void dragonSurvival$triggerSweatEvent(final CallbackInfo callback) {
        Villager villager = (Villager) (Object) this;

        if (!villager.isNoAi() && HunterOmenHandler.isNearbyPlayerWithHunterOmen(8, villager.level(), villager)) {
            villager.level().broadcastEntityEvent(villager, EntityEvent.VILLAGER_SWEAT);
        }
    }

    @Shadow public abstract void setLastHurtByMob(@Nullable LivingEntity pLivingBase);
    @Shadow protected abstract SoundEvent getHurtSound(DamageSource pDamageSource);
    @Shadow protected abstract void setUnhappy();
}
