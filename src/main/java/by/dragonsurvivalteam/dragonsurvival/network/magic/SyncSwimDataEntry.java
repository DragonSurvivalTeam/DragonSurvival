package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraftforge.fluids.FluidType;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public record SyncSwimDataEntry(int maxOxygen, Holder<FluidType> fluidType, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncSwimDataEntry> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_swim_data_entry"));

    public static final StreamCodec<FriendlyByteBuf, SyncSwimDataEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncSwimDataEntry::maxOxygen,
            ByteBufCodecs.fromCodecWithRegistries(MiscCodecs.forgeRegistryHolderCodec(ForgeRegistries.FLUID_TYPES)), SyncSwimDataEntry::fluidType,
            ByteBufCodecs.BOOL, SyncSwimDataEntry::remove,
            SyncSwimDataEntry::new
    );

    public static void handleClient(final SyncSwimDataEntry packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            SwimData data = SwimData.getData(context.player());

            if (packet.remove()) {
                data.remove(packet.fluidType());
            } else {
                data.add(packet.maxOxygen(), packet.fluidType());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
