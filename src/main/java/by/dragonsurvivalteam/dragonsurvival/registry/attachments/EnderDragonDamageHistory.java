package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderDragonDamageHistory implements INBTSerializable<CompoundTag> {
    private final Map<UUID, Float> damageHistory = new HashMap<>();

    public static EnderDragonDamageHistory getData(EnderDragon dragon) {
        return dragon.getData(DSDataAttachments.ENDER_DRAGON_DAMAGE_HISTORY);
    }

    public void addDamage(UUID uuid, float damage) {
        damageHistory.computeIfPresent(uuid, (key, value) -> value + damage);
        damageHistory.putIfAbsent(uuid, damage);
        damageHistory.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    public void addDamageAll(float damage) {
        damageHistory.replaceAll((uuid, value) -> value + damage);
        damageHistory.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    public float getDamage(UUID uuid) {
        return damageHistory.getOrDefault(uuid, 0f);
    }

    public List<Player> getPlayers(Level level) {
        return damageHistory.keySet().stream().map(level::getPlayerByUUID).toList();
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag nbt = new CompoundTag();
        damageHistory.forEach((uuid, damage) -> nbt.putFloat(uuid.toString(), damage));
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        damageHistory.clear();
        nbt.getAllKeys().forEach(uuid -> damageHistory.put(UUID.fromString(uuid), nbt.getFloat(uuid)));
    }
}
