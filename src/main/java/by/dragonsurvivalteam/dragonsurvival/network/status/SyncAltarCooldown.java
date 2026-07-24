package by.dragonsurvivalteam.dragonsurvival.network.status;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

/** Also sets {@link AltarData#hasUsedAltar} to 'true' and {@link AltarData#isInAltar} to 'false' */
public record SyncAltarCooldown(int cooldown) implements CustomPacketPayload {
    public static final Type<SyncAltarCooldown> TYPE = new Type<>(DragonSurvival.res("sync_altar_cooldown"));

    public static final StreamCodec<FriendlyByteBuf, SyncAltarCooldown> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncAltarCooldown::cooldown,
            SyncAltarCooldown::new
    );

    public static void handleServer(final SyncAltarCooldown message, final PayloadContext context) {
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