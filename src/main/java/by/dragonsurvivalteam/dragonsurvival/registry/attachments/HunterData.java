package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;

public class HunterData implements ValueIOSerializable {
    /** Only needs to be updated on effect removal (server -> client) */
    private int hunterStacks;
    /** Translucent rendering in the inventory screen leads to issues (invisible model) */
    private boolean transparencyDisabled;

    public static boolean hasTransparency(final LivingEntity entity) {
        HunterData data = entity.getExistingData(DSDataAttachments.HUNTER).orElse(null);

        if (data == null) {
            return false;
        }

        return data.hasTransparency();
    }

    public static boolean hasMaxHunterStacks(final LivingEntity entity) {
        HunterData data = entity.getExistingData(DSDataAttachments.HUNTER).orElse(null);

        if (data == null) {
            return false;
        }

        return data.hasMaxHunterStacks();
    }

    public boolean hasTransparency() {
        if (transparencyDisabled) {
            return false;
        }

        return hasHunterStacks();
    }

    public void modifyHunterStacks(int modification) {
        hunterStacks = Math.clamp(hunterStacks + modification, 0, HunterHandler.getMaxStacks());
    }

    public boolean hasMaxHunterStacks() {
        return hunterStacks == HunterHandler.getMaxStacks();
    }

    public boolean hasHunterStacks() {
        return hunterStacks > 0;
    }

    public void clearHunterStacks() {
        hunterStacks = 0;
    }

    public int getHunterStacks() {
        return hunterStacks;
    }

    public void disableTransparency() {
        transparencyDisabled = true;
    }

    public void enableTransparency() {
        transparencyDisabled = false;
    }

    // None of this data actually needs to be saved

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {}

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {}
}
