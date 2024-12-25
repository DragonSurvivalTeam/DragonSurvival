package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

public class SummonedEntities extends Storage<SummonEntityEffect.Instance> {
    @SubscribeEvent
    public static void tickModifiers(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(data -> {
                data.tick();

                if (data.isEmpty()) {
                    livingEntity.removeData(DSDataAttachments.SUMMONED_ENTITIES);
                }
            });
        }
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final SummonEntityEffect.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected SummonEntityEffect.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return SummonEntityEffect.Instance.load(provider, tag);
    }
}
