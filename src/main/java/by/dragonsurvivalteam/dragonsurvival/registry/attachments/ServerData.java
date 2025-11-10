package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/** Only exists so that the IDs can properly be cleaned up if the block entity is removed */
public class ServerData implements INBTSerializable<CompoundTag> {
    private final Map<Integer, Boolean> usedFakeClientPlayers = new HashMap<>();

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putIntArray(USED_FAKE_CLIENT_PLAYERS, usedFakeClientPlayers.keySet().stream().mapToInt(Integer::intValue).toArray());
        return null;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag nbt) {
        usedFakeClientPlayers.clear();

        for (int id : nbt.getIntArray(USED_FAKE_CLIENT_PLAYERS)) {
            usedFakeClientPlayers.put(id, true);
        }
    }

    public static int getNextID(final @NotNull Level level) {
        ServerData data = level.getData(DSDataAttachments.SERVER_DATA);

        // 0 and 1 are reserved for the dragon altar, editor, smithing screen, etc.
        int id = 2;

        while (data.usedFakeClientPlayers.containsKey(id)) {
            id++;
        }

        data.usedFakeClientPlayers.put(id, true);
        level.setData(DSDataAttachments.SERVER_DATA, data);

        return id;
    }

    public static void remove(final Level level, final int fakePlayerIndex) {
        ServerData data = level.getData(DSDataAttachments.SERVER_DATA);
        data.usedFakeClientPlayers.remove(fakePlayerIndex);
        level.setData(DSDataAttachments.SERVER_DATA, data);
    }

    private static final String USED_FAKE_CLIENT_PLAYERS = "used_fake_client_players";
}
