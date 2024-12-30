package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.HarvestBonus;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class HarvestBonuses extends Storage<HarvestBonus.Instance> {
    public int getHarvestBonus(final BlockState state) {
        if (storage == null) {
            return HarvestBonus.NO_BONUS_VALUE;
        }

        int bonus = HarvestBonus.NO_BONUS_VALUE;

        for (HarvestBonus.Instance instance : storage.values()) {
            bonus += instance.getHarvestBonus(state);
        }

        return bonus;
    }

    public float getSpeedMultiplier(final BlockState state) {
        if (storage == null) {
            return 1;
        }

        float multiplier = 1;

        for (HarvestBonus.Instance instance : storage.values()) {
            multiplier += instance.getSpeedMultiplier(state);
        }

        return multiplier;
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        // TODO :: could these be relevant for non-living entities?
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            livingEntity.getExistingData(DSDataAttachments.HARVEST_BONUSES).ifPresent(storage -> {
                storage.tick();

                if (storage.isEmpty()) {
                    livingEntity.removeData(DSDataAttachments.HARVEST_BONUSES);
                }
            });
        }
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final HarvestBonus.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected HarvestBonus.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return HarvestBonus.Instance.load(provider, tag);
    }
}
