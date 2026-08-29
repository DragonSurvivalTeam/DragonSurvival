package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public record SyncExperienceManaConversion(boolean enabled) implements CustomPacketPayload {
    public static final Type<SyncExperienceManaConversion> TYPE = new Type<>(DragonSurvival.res("sync_experience_mana_conversion"));

    public static final StreamCodec<FriendlyByteBuf, SyncExperienceManaConversion> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncExperienceManaConversion::enabled,
            SyncExperienceManaConversion::new
    );

    public static void handleServer(final SyncExperienceManaConversion packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            MagicData magic = MagicData.getData(player);
            magic.setUseExperienceForMana(packet.enabled());
            PacketDistributor.sendToPlayer(player, new SyncMagicData(magic.serializeNBT(player.level().registryAccess())));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
