package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.UUID;

public interface AdditionalEffectData {
    void dragonSurvival$setApplier(final Entity applier);
    @Nullable Entity dragonSurvival$getApplier(final ServerLevel level);

    void dragonSurvival$setApplierUUID(final UUID applierUUID);
}
