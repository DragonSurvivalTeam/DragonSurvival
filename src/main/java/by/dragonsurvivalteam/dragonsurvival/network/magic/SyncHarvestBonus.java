package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.HarvestBonus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HarvestBonuses;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncHarvestBonus(int playerId, HarvestBonus.Instance harvestBonusInstance, boolean remove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncHarvestBonus> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_harvest_bonus"));

    public static final StreamCodec<FriendlyByteBuf, SyncHarvestBonus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncHarvestBonus::playerId,
            ByteBufCodecs.fromCodecWithRegistries(HarvestBonus.Instance.CODEC), SyncHarvestBonus::harvestBonusInstance,
            ByteBufCodecs.BOOL, SyncHarvestBonus::remove,
            SyncHarvestBonus::new
    );

    public static void handleClient(final SyncHarvestBonus packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                HarvestBonuses data = AttachmentManager.getData(player, DSDataAttachments.HARVEST_BONUSES);

                if (packet.remove()) {
                    data.remove(player, packet.harvestBonusInstance());
                } else {
                    data.add(player, packet.harvestBonusInstance());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
