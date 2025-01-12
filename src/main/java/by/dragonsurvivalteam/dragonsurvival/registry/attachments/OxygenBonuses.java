package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class OxygenBonuses extends Storage<OxygenBonus.Instance> {
    public float getOxygenBonus(final ResourceKey<FluidType> fluidTypeKey) {
        if (storage == null) {
            return OxygenBonus.NO_BONUS_VALUE;
        }

        float bonus = OxygenBonus.NO_BONUS_VALUE;

        for (OxygenBonus.Instance instance : storage.values()) {
            if(instance.getOxygenBonus(fluidTypeKey) == OxygenBonus.INFINITE_VALUE) {
                return OxygenBonus.INFINITE_VALUE;
            }
            bonus += instance.getOxygenBonus(fluidTypeKey);
        }

        return bonus;
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            player.getExistingData(DSDataAttachments.OXYGEN_BONUSES).ifPresent(storage -> {
                storage.tick(event.getEntity());

                if (storage.isEmpty()) {
                    player.removeData(DSDataAttachments.OXYGEN_BONUSES);
                }
            });
        }
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.OXYGEN_BONUSES.get();
    }

    @Override
    protected Tag save(HolderLookup.@NotNull Provider provider, OxygenBonus.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected OxygenBonus.Instance load(HolderLookup.@NotNull Provider provider, CompoundTag tag) {
        return OxygenBonus.Instance.load(provider, tag);
    }
}
