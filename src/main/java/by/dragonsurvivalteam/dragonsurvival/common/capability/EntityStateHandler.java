package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntityStateHandler implements INBTSerializable<CompoundTag> {
    // To handle the burn effect damage
    public Vec3 lastPos;
    // Amount of times the last chain attack has chained
    public int chainCount;
    // Currently only used for item entities
    public boolean isFireImmune;

    private UUID summonOwner;

    public @Nullable Entity getSummonOwner(final Level level) {
        if (summonOwner != null && level instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(summonOwner);
        }

        return null;
    }

    public void setSummonOwner(@Nullable final Entity entity) {
        if (entity == null) {
            summonOwner = null;
        } else {
            summonOwner = entity.getUUID();
        }
    }

    public boolean isOwner(final Entity entity) {
        return summonOwner != null && summonOwner.equals(entity.getUUID());
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(CHAIN_COUNT, chainCount);
        tag.putBoolean(IS_FIRE_IMMUNE, isFireImmune);

        if (summonOwner != null) {
            tag.putUUID(SUMMON_OWNER, summonOwner);
        }

        if (lastPos != null) {
            tag.put(LAST_POSITION, Functions.newDoubleList(lastPos.x, lastPos.y, lastPos.z));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, CompoundTag tag) {
        chainCount = tag.getInt(CHAIN_COUNT);
        isFireImmune = tag.getBoolean(IS_FIRE_IMMUNE);

        if (tag.contains(SUMMON_OWNER)) {
            summonOwner = tag.getUUID(SUMMON_OWNER);
        }

        if (tag.contains(LAST_POSITION)) {
            ListTag list = tag.getList(LAST_POSITION, ListTag.TAG_DOUBLE);
            lastPos = new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }
    }

    public static final String SUMMON_OWNER = "summon_owner";
    public static final String LAST_POSITION = "last_position";
    public static final String CHAIN_COUNT = "chain_count";
    public static final String IS_FIRE_IMMUNE = "is_fire_immune";
}
