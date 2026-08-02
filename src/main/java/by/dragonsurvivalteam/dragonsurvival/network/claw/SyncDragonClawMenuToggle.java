package by.dragonsurvivalteam.dragonsurvival.network.claw;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record SyncDragonClawMenuToggle(boolean isOpen) implements CustomPacketPayload {
    public static final Type<SyncDragonClawMenuToggle> TYPE = new Type<>(DragonSurvival.res("sync_dragon_claw_menu_toggle"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonClawMenuToggle> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncDragonClawMenuToggle::isOpen,
            SyncDragonClawMenuToggle::new
    );

    public static void handleServer(final SyncDragonClawMenuToggle packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            ClawInventoryData data = ClawInventoryData.getData(context.player());
            data.setMenuOpen(packet.isOpen());

            if (context.player().containerMenu instanceof DragonContainer container) {
                container.update();
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}