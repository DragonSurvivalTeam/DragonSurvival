package by.dragonsurvivalteam.dragonsurvival.common.capability;

import net.minecraft.nbt.CompoundTag;

public interface NBTInterface {
    CompoundTag writeNBT();

    void readNBT(CompoundTag base);
}