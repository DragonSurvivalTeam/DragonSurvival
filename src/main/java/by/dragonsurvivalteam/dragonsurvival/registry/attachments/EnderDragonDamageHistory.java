package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EnderDragonDamageHistory implements ValueIOSerializable {
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
    public void serialize(@NotNull final ValueOutput valueOutput) {
        damageHistory.forEach((uuid, damage) -> valueOutput.putFloat(uuid.toString(), damage));
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        damageHistory.clear();
        valueInput.keySet().forEach(uuid -> damageHistory.put(UUID.fromString(uuid), valueInput.getFloatOr(uuid, 0f)));
    }
}
