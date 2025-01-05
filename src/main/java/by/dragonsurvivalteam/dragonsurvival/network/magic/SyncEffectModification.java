package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.EffectModification;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectModifications;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncEffectModification(int playerId, EffectModification.Instance modifierInstance, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncEffectModification> TYPE = new Type<>(DragonSurvival.res("sync_effect_modification"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncEffectModification> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncEffectModification::playerId,
            ByteBufCodecs.fromCodecWithRegistries(EffectModification.Instance.CODEC), SyncEffectModification::modifierInstance,
            ByteBufCodecs.BOOL, SyncEffectModification::remove,
            SyncEffectModification::new
    );

    public static void handleClient(final SyncEffectModification packet, final IPayloadContext context) {
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
