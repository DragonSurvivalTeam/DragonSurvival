package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class EntityStateHandler implements INBTSerializable<CompoundTag> {
    // To handle the burn effect damage
    public Vec3 lastPos;
    // Amount of times the last chain attack has chained
    public int chainCount;

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(CHAIN_COUNT, chainCount);

        if (lastPos != null) {
            tag.put(LAST_POSITION, Functions.newDoubleList(lastPos.x, lastPos.y, lastPos.z));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag tag) {
        chainCount = tag.getInt(CHAIN_COUNT);

        if (tag.contains(LAST_POSITION)) {
            ListTag list = tag.getList(LAST_POSITION, ListTag.TAG_DOUBLE);
            lastPos = new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }
    }

    public static final String LAST_POSITION = "last_position";
    public static final String CHAIN_COUNT = "chain_count";
}
