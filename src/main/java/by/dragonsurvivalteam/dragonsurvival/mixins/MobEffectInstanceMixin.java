package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements AdditionalEffectData {
    @Unique private static final String dragonSurvival$APPLIER = "applier";

    @Shadow @Final private Holder<MobEffect> effect;
    @Shadow private int duration;

    @Unique @Nullable private Entity dragonSurvival$applier;
    @Unique @Nullable private UUID dragonSurvival$applierUUID;
    // Only temporarily referenced for the magic source effect
    @Unique @Nullable private ThreadLocal<Player> dragonSurvival$entity;

    @Override
    public void dragonSurvival$setApplier(final Entity applier) {
        this.dragonSurvival$applier = applier;
    }

    @Override
    public @Nullable Entity dragonSurvival$getApplier(final ServerLevel level) {
        if (dragonSurvival$applier == null && dragonSurvival$applierUUID != null) {
            dragonSurvival$applier = level.getEntity(dragonSurvival$applierUUID);

            if (dragonSurvival$applier == null) {
                // The level probably only has references to the entities within it
                // But the server can reference them across levels (i.e. dimensions)
                dragonSurvival$applier = level.getServer().getPlayerList().getPlayer(dragonSurvival$applierUUID);
            }
        }

        return this.dragonSurvival$applier;
    }

    @Override
    public void dragonSurvival$setApplierUUID(final UUID applierUUID) {
        this.dragonSurvival$applierUUID = applierUUID;
    }

    @ModifyReturnValue(method = "save", at = @At("RETURN"))
    private Tag dragonSurvival$saveAdditionalData(final Tag tag) {
        if (dragonSurvival$applierUUID != null && tag instanceof CompoundTag compound) {
            compound.putUUID(dragonSurvival$APPLIER, dragonSurvival$applierUUID);
        }

        return tag;
    }

    @ModifyReturnValue(method = "load", at = @At("RETURN"))
    private static MobEffectInstance dragonSurvival$loadAdditionalData(final MobEffectInstance instance, @Local(argsOnly = true) final CompoundTag tag) {
        Tag applierTag = tag.get(dragonSurvival$APPLIER);

        if (applierTag != null) {
            ((AdditionalEffectData) instance).dragonSurvival$setApplierUUID(NbtUtils.loadUUID(applierTag));
        }

        return instance;
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffectInstance;tickDownDuration()I"))
    private void dragonSurvival$storeEntity(final LivingEntity entity, final Runnable onExpirationRunnable, final CallbackInfoReturnable<Boolean> callback) {
        if (entity instanceof Player player && effect.is(DSEffects.SOURCE_OF_MAGIC)) {
            if (dragonSurvival$entity == null) {
                dragonSurvival$entity = new ThreadLocal<>();
            }

            dragonSurvival$entity.set(player);
        }
    }

    @Inject(method = "tickDownDuration", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$retainDuration(final CallbackInfoReturnable<Integer> callback) {
        if (dragonSurvival$entity != null && effect.is(DSEffects.SOURCE_OF_MAGIC)) {
            Player player = dragonSurvival$entity.get();

            if (player != null && DragonStateProvider.getData(player).isOnMagicSource) {
                // Don't tick down the duration of the effects so the duration can be extended properly
                // Without always having to add some buffer duration
                callback.setReturnValue(duration);
            }

            dragonSurvival$entity.remove();
        }
    }
}
