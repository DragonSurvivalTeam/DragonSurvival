package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class ItemData implements INBTSerializable<CompoundTag> {
    public boolean isFireImmune;

    public double smeltingProgress;
    public double previousSmeltingProgress;

    public int noSmeltingChange;

    @SubscribeEvent
    public static void resetSmeltingProgress(final EntityTickEvent.Post event) {
        event.getEntity().getExistingData(DSDataAttachments.ITEM).ifPresent(data -> {
            if (data.smeltingProgress == data.previousSmeltingProgress) {
                data.noSmeltingChange++;

                if (data.noSmeltingChange == Functions.ticksToSeconds(3)) {
                    data.smeltingProgress = 0;
                    data.noSmeltingChange = 0;
                }
            } else {
                data.noSmeltingChange = 0;
            }

            data.previousSmeltingProgress = data.smeltingProgress;
        });
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(IS_FIRE_IMMUNE, isFireImmune);
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        isFireImmune = tag.getBoolean(IS_FIRE_IMMUNE);
    }

    public static final String IS_FIRE_IMMUNE = "is_fire_immune";
}
