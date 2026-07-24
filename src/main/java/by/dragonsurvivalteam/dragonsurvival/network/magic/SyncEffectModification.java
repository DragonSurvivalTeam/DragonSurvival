package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.EffectModification;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectModifications;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncEffectModification(int playerId, EffectModification.Instance modifierInstance, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncEffectModification> TYPE = new Type<>(DragonSurvival.res("sync_effect_modification"));

    public static final StreamCodec<FriendlyByteBuf, SyncEffectModification> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncEffectModification::playerId,
            ByteBufCodecs.fromCodecWithRegistries(EffectModification.Instance.CODEC), SyncEffectModification::modifierInstance,
            ByteBufCodecs.BOOL, SyncEffectModification::remove,
            SyncEffectModification::new
    );

    public static void handleClient(final SyncEffectModification packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                EffectModifications data = player.getData(DSDataAttachments.EFFECT_MODIFICATIONS);

                if (packet.remove()) {
                    data.remove(player, packet.modifierInstance());
                } else {
                    data.add(player, packet.modifierInstance());
                }
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
