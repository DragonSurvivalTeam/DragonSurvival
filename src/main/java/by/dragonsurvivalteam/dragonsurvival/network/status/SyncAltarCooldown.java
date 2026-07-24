package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/** Also sets {@link AltarData#hasUsedAltar} to 'true' and {@link AltarData#isInAltar} to 'false' */
public record SyncAltarCooldown(int cooldown) implements CustomPacketPayload {
    public static final Type<SyncAltarCooldown> TYPE = new Type<>(DragonSurvival.res("sync_altar_cooldown"));

    public static final StreamCodec<FriendlyByteBuf, SyncAltarCooldown> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncAltarCooldown::cooldown,
            SyncAltarCooldown::new
    );

    public static void handleServer(final SyncAltarCooldown message, final IPayloadContext context) {
        context.enqueueWork(() -> {
            AltarData data = AltarData.getData(context.player());
            data.altarCooldown = message.cooldown();
            data.hasUsedAltar = true;
            data.isInAltar = false;
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}