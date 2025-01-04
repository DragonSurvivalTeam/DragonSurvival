package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DamageModification;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncDamageModification(int playerId, DamageModification.Instance damageModification, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncDamageModification> TYPE = new Type<>(DragonSurvival.res("sync_damage_modification"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDamageModification> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDamageModification::playerId,
            ByteBufCodecs.fromCodecWithRegistries(DamageModification.Instance.CODEC), SyncDamageModification::damageModification,
            ByteBufCodecs.BOOL, SyncDamageModification::remove,
            SyncDamageModification::new
    );

    public static void handleClient(final SyncDamageModification packet, final IPayloadContext context) {
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
