package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import net.minecraft.core.Holder;
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
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class OxygenBonuses extends Storage<OxygenBonus.Instance> {
    public float getBonus(final ResourceKey<FluidType> fluidKey) {
        Holder.Reference<FluidType> fluid = NeoForgeRegistries.FLUID_TYPES.getHolderOrThrow(fluidKey);
        float bonus = OxygenBonus.NONE;

        for (OxygenBonus.Instance instance : all()) {
            if (instance.getOxygenBonus(fluid) == SwimData.UNLIMITED_OXYGEN) {
                return SwimData.UNLIMITED_OXYGEN;
            }

            bonus += instance.getOxygenBonus(fluid);
        }

        return bonus;
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            player.getExistingData(DSDataAttachments.OXYGEN_BONUSES).ifPresent(storage -> {
                storage.tick(player);

                if (storage.isEmpty()) {
                    player.removeData(DSDataAttachments.OXYGEN_BONUSES);
                }
            });
        }
    }

    @Override
    protected Tag save(HolderLookup.@NotNull Provider provider, OxygenBonus.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected OxygenBonus.Instance load(HolderLookup.@NotNull Provider provider, CompoundTag tag) {
        return OxygenBonus.Instance.load(provider, tag);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.OXYGEN_BONUSES.get();
    }
}
