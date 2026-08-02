package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.OxygenBonuses;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record SyncOxygenBonus(int playerId, OxygenBonus.Instance oxygenBonusInstance, boolean isRemoval) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncOxygenBonus> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_oxygen_bonus"));

    public static final StreamCodec<FriendlyByteBuf, SyncOxygenBonus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncOxygenBonus::playerId,
            ByteBufCodecs.fromCodecWithRegistries(OxygenBonus.Instance.CODEC), SyncOxygenBonus::oxygenBonusInstance,
            ByteBufCodecs.BOOL, SyncOxygenBonus::isRemoval,
            SyncOxygenBonus::new
    );

    public static void handleClient(final SyncOxygenBonus packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                OxygenBonuses data = AttachmentManager.getData(player, DSDataAttachments.OXYGEN_BONUSES);

                if (packet.isRemoval()) {
                    data.remove(player, packet.oxygenBonusInstance());
                } else {
                    data.add(player, packet.oxygenBonusInstance());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
