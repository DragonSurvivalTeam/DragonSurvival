package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

// No need to store the resting state if someone re-logs they shouldn't be considered sleeping
public class TreasureRestData implements INBTSerializable<CompoundTag> {
    public static final int TICKS_TO_SLEEP = Functions.secondsToTicks(5);

    public int restingTicks;
    public int sleepingTicks;
    public int nearbyTreasure;

    private boolean isResting;

    public boolean canSleep() {
        return isResting && restingTicks >= TICKS_TO_SLEEP;
    }

    public boolean isResting() {
        return isResting;
    }

    public void setResting(final boolean isResting) {
        this.isResting = isResting;

        if (!isResting) {
            restingTicks = 0;
            sleepingTicks = 0;
            nearbyTreasure = 0;
        }
    }

    public static TreasureRestData getData(final Player player) {
        return player.getData(DSDataAttachments.TREASURE_REST);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) { /* Nothing to do */ }
}
