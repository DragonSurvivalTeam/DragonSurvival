package by.dragonsurvivalteam.dragonsurvival.network.syncing;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnKeyPressed;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnKeyReleased;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncKey(String key, boolean isDown) implements CustomPacketPayload {
    public static final Type<SyncKey> TYPE = new Type<>(DragonSurvival.res("sync_key"));

    public static final StreamCodec<ByteBuf, SyncKey> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SyncKey::key,
            ByteBufCodecs.BOOL, SyncKey::isDown,
            SyncKey::new
    );

    public static void handleServer(final SyncKey packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            boolean hasChanged = context.player().getData(DSDataAttachments.PLAYER_DATA).updateKey(packet.key(), packet.isDown());

            if (!hasChanged) {
                return;
            }

            DragonStateHandler handler = DragonStateProvider.getData(context.player());

            if (!handler.isDragon()) {
                return;
            }

            MagicData magic = MagicData.getData(context.player());

            if (packet.isDown()) {
                magic.filterPassiveByTrigger(trigger -> trigger instanceof OnKeyPressed pressed && pressed.test(packet.key()))
                        .forEach(ability -> ability.tick(context.player()));
            } else {
                magic.filterPassiveByTrigger(trigger -> trigger instanceof OnKeyReleased released && released.test(packet.key()))
                        .forEach(ability -> ability.tick(context.player()));
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
