package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;
import javax.annotation.Nullable;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceMixin implements AdditionalEffectData {
    @Unique private static final String dragonSurvival$APPLIER = "applier";

    @Unique private @Nullable Entity dragonSurvival$applier;
    @Unique private @Nullable UUID dragonSurvival$applierUUID;

    @Override
    public void dragonSurvival$setApplier(final Entity applier) {
        this.dragonSurvival$applier = applier;
    }

    @Override
    public @Nullable Entity dragonSurvival$getApplier(final ServerLevel level) {
        if (dragonSurvival$applier == null && dragonSurvival$applierUUID != null) {
            dragonSurvival$applier = level.getEntity(dragonSurvival$applierUUID);
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
}
