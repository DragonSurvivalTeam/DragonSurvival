package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.network.status.SyncAltarState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class AltarData implements ValueIOSerializable {
    public static final String ALTAR_COOLDOWN = "altar_cooldown";
    public static final String HAS_USED_ALTAR = "has_used_altar";

    public int altarCooldown;
    public boolean hasUsedAltar;
    public boolean isInAltar;

    public void sync(final ServerPlayer player) {
        TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
        serialize(valueOutput);
        PacketDistributor.sendToPlayer(player, new SyncAltarState(valueOutput.buildResult()));
    }

    public static AltarData getData(final Player player) {
        return player.getData(DSDataAttachments.ALTAR);
    }

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {
        valueOutput.putInt(ALTAR_COOLDOWN, altarCooldown);
        valueOutput.putBoolean(HAS_USED_ALTAR, hasUsedAltar);
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        altarCooldown = valueInput.getInt(ALTAR_COOLDOWN).orElseThrow();
        hasUsedAltar = valueInput.getBooleanOr(HAS_USED_ALTAR, false);
    }
}
