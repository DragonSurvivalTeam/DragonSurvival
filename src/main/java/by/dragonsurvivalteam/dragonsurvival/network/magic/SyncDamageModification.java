package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DamageModification;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDamageModification(int playerId, DamageModification.Instance damageModification, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncDamageModification> TYPE = new Type<>(DragonSurvival.res("sync_damage_modification"));

    public static final StreamCodec<FriendlyByteBuf, SyncDamageModification> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDamageModification::playerId,
            ByteBufCodecs.fromCodecWithRegistries(DamageModification.Instance.CODEC), SyncDamageModification::damageModification,
            ByteBufCodecs.BOOL, SyncDamageModification::remove,
            SyncDamageModification::new
    );

    public static void handleClient(final SyncDamageModification packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                DamageModifications data = player.getData(DSDataAttachments.DAMAGE_MODIFICATIONS);

                if (packet.remove()) {
                    data.remove(player, packet.damageModification());
                } else {
                    data.add(player, packet.damageModification());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
