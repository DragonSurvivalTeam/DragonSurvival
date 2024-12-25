package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EntityStateHandler implements INBTSerializable<CompoundTag> {
    public UUID owner;
    // To handle the burn effect damage
    public Vec3 lastPos;
    // Amount of times the last chain attack has chained
    public int chainCount;
    // Currently only used for item entities
    public boolean isFireImmune;

    @Override
    public CompoundTag serializeNBT(@NotNull HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(CHAIN_COUNT, chainCount);
        tag.putBoolean(IS_FIRE_IMMUNE, isFireImmune);

        if (owner != null) {
            tag.putUUID(OWNER, owner);
        }

        if (lastPos != null) {
            tag.put(LAST_POSITION, Functions.newDoubleList(lastPos.x, lastPos.y, lastPos.z));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull HolderLookup.Provider provider, CompoundTag tag) {
        chainCount = tag.getInt(CHAIN_COUNT);
        isFireImmune = tag.getBoolean(IS_FIRE_IMMUNE);

        if (tag.contains(OWNER)) {
            owner = tag.getUUID(OWNER);
        }

        if (tag.contains(LAST_POSITION)) {
            ListTag list = tag.getList(LAST_POSITION, ListTag.TAG_DOUBLE);
            lastPos = new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }
    }

    public static final String OWNER = "owner";
    public static final String LAST_POSITION = "last_position";
    public static final String CHAIN_COUNT = "chain_count";
    public static final String IS_FIRE_IMMUNE = "is_fire_immune";
}
