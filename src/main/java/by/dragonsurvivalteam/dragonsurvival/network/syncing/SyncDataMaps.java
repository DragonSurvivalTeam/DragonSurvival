package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public record SyncDataMaps(CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncDataMaps> TYPE = new Type<>(DragonSurvival.res("sync_data_maps"));
    public static final StreamCodec<ByteBuf, SyncDataMaps> STREAM_CODEC =
            ByteBufCodecs.COMPOUND_TAG.map(SyncDataMaps::new, SyncDataMaps::data);

    public static void handleClient(final SyncDataMaps packet, final PayloadContext context) {
        context.enqueueWork(() -> DSDataMaps.applySynced(
                packet.data(), context.player().level().registryAccess()
        ));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
