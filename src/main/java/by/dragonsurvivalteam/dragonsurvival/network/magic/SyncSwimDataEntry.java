package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

public record SyncSwimDataEntry(int maxOxygen, Holder<FluidType> fluidType, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncSwimDataEntry> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_swim_data_entry"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSwimDataEntry> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SyncSwimDataEntry::maxOxygen,
        ByteBufCodecs.fromCodecWithRegistries(NeoForgeRegistries.FLUID_TYPES.holderByNameCodec()), SyncSwimDataEntry::fluidType,
        ByteBufCodecs.BOOL, SyncSwimDataEntry::remove,
        SyncSwimDataEntry::new
    );

    public static void handleClient(final SyncSwimDataEntry packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            SwimData data = SwimData.getData(DragonSurvival.PROXY.getLocalPlayer());

            if (packet.remove()) {
                data.removeEntry(packet.fluidType());
            } else {
                data.addEntry(packet.maxOxygen(), packet.fluidType());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
