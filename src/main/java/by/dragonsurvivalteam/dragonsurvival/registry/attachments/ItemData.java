package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class ItemData implements INBTSerializable<CompoundTag> {
    public boolean isFireImmune;

    public double smeltingTime;
    public double smeltingProgress;

    private double previousSmeltingProgress;
    private int noSmeltingChange;

    @SubscribeEvent // Resets progress after inactivity and spawns the smoke particles
    public static void handleSmelting(final EntityTickEvent.Post event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.ITEM).ifPresent(data -> {
            if (data.smeltingTime == 0) {
                return;
            }

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

            if (event.getEntity().tickCount % 10 == 0) {
                int amount = (int) Math.max(1, 10 * (data.smeltingProgress / data.smeltingTime));
                serverLevel.sendParticles(ParticleTypes.SMOKE, event.getEntity().getX(), event.getEntity().getY() + 0.5, event.getEntity().getZ(), amount, 0, 0, 0, 0);
            }
        });
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(IS_FIRE_IMMUNE, isFireImmune);
        tag.putDouble(SMELTING_TIME, smeltingTime);
        tag.putDouble(SMELTING_PROGRESS, smeltingProgress);
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        isFireImmune = tag.getBoolean(IS_FIRE_IMMUNE);
        smeltingTime = tag.getDouble(SMELTING_TIME);
        smeltingProgress = tag.getDouble(SMELTING_PROGRESS);
    }

    public static final String IS_FIRE_IMMUNE = "is_fire_immune";
    public static final String SMELTING_TIME = "smelting_time";
    public static final String SMELTING_PROGRESS = "smelting_progress";
}
