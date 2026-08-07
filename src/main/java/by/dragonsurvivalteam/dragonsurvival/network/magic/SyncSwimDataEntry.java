package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SwimEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncSwimDataEntry(SwimEffect.Entry entry, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncSwimDataEntry> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_swim_data_entry"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSwimDataEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(SwimEffect.Entry.CODEC), SyncSwimDataEntry::entry,
            ByteBufCodecs.BOOL, SyncSwimDataEntry::remove,
            SyncSwimDataEntry::new
    );

    public static void handleClient(final SyncSwimDataEntry packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            SwimData data = SwimData.getData(context.player());

            if (packet.remove()) {
                data.remove(packet.entry().fluidType());
            } else {
                data.add(packet.entry());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
