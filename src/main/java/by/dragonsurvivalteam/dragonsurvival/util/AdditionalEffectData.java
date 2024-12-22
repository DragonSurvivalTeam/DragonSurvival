package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.UUID;
import javax.annotation.Nullable;

public interface AdditionalEffectData {
    void dragonSurvival$setApplier(final Entity applier);
    @Nullable Entity dragonSurvival$getApplier(final ServerLevel level);

    void dragonSurvival$setApplierUUID(final UUID applierUUID);
}
