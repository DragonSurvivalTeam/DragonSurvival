package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.HarvestBonus;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.OxygenBonus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HarvestBonuses;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.OxygenBonuses;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncOxygenBonus(int playerId, OxygenBonus.Instance oxygenBonusInstance, boolean remove) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncOxygenBonus> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("sync_oxygen_bonus"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncOxygenBonus> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncOxygenBonus::playerId,
            ByteBufCodecs.fromCodecWithRegistries(OxygenBonus.Instance.CODEC), SyncOxygenBonus::oxygenBonusInstance,
            ByteBufCodecs.BOOL, SyncOxygenBonus::remove,
            SyncOxygenBonus::new
    );

    public static void handleClient(final SyncOxygenBonus packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                OxygenBonuses data = player.getData(DSDataAttachments.OXYGEN_BONUSES);

                if (packet.remove()) {
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
