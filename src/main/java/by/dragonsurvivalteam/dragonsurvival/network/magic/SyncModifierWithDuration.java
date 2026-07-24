package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncModifierWithDuration(int playerId, ModifierWithDuration.Instance modifierInstance, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncModifierWithDuration> TYPE = new Type<>(DragonSurvival.res("sync_modifier_with_duration"));

    public static final StreamCodec<FriendlyByteBuf, SyncModifierWithDuration> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncModifierWithDuration::playerId,
            ByteBufCodecs.fromCodecWithRegistries(ModifierWithDuration.Instance.CODEC), SyncModifierWithDuration::modifierInstance,
            ByteBufCodecs.BOOL, SyncModifierWithDuration::remove,
            SyncModifierWithDuration::new
    );

    public static void handleClient(final SyncModifierWithDuration packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                ModifiersWithDuration data = player.getData(DSDataAttachments.MODIFIERS_WITH_DURATION);

                if (packet.remove()) {
                    // The stored server and client ids (used to remove the effects) may be different
                    // Therefor we retrieve the actual client instance and remove that not the encoded server instance
                    data.remove(player, data.get(packet.modifierInstance().id()));
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
