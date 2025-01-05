package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncModifierWithDuration(int playerId, ModifierWithDuration.Instance modifierInstance, boolean remove) implements CustomPacketPayload {
    public static final Type<SyncModifierWithDuration> TYPE = new Type<>(DragonSurvival.res("sync_modifier_with_duration"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncModifierWithDuration> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncModifierWithDuration::playerId,
            ByteBufCodecs.fromCodecWithRegistries(ModifierWithDuration.Instance.CODEC), SyncModifierWithDuration::modifierInstance,
            ByteBufCodecs.BOOL, SyncModifierWithDuration::remove,
            SyncModifierWithDuration::new
    );

    public static void handleClient(final SyncModifierWithDuration packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                ModifiersWithDuration data = player.getData(DSDataAttachments.MODIFIERS_WITH_DURATION);

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
