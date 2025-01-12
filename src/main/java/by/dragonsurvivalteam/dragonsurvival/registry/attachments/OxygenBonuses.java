package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public class OxygenBonuses extends Storage<OxygenBonus.Instance> {
    public float getOxygenBonus(final ResourceKey<FluidType> fluidTypeKey) {
        if (storage == null) {
            return OxygenBonus.NO_BONUS_VALUE;
        }

        float bonus = OxygenBonus.NO_BONUS_VALUE;

        for (OxygenBonus.Instance instance : storage.values()) {
            bonus += instance.getOxygenBonus(fluidTypeKey);
        }

        return bonus;
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
