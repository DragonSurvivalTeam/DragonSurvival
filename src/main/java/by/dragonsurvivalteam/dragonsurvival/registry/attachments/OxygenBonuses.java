package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.attachment.AttachmentType;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.tick.EntityTickEvent;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class OxygenBonuses extends Storage<OxygenBonus.Instance> {
    public float getBonus(final ResourceKey<FluidType> fluidKey) {
        Holder<FluidType> fluid = fluidKey != null ? ForgeRegistries.FLUID_TYPES.getHolderOrThrow(fluidKey) : ForgeMod.EMPTY_TYPE;
        float bonus = OxygenBonus.NONE;

        float instanceBonus;
        for (OxygenBonus.Instance instance : all()) {
            instanceBonus = instance.getOxygenBonus(fluid);
            if (instanceBonus == SwimData.UNLIMITED_OXYGEN) {
                return SwimData.UNLIMITED_OXYGEN;
            }

            bonus += instanceBonus;
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
