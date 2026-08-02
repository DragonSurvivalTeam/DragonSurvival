package by.dragonsurvivalteam.dragonsurvival.network.claw;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record SyncDragonClawsMenu(int playerId, boolean isOpen, CompoundTag data) implements CustomPacketPayload {
    public static final Type<SyncDragonClawsMenu> TYPE = new Type<>(DragonSurvival.res("sync_dragon_claws_menu"));

    public static final StreamCodec<FriendlyByteBuf, SyncDragonClawsMenu> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncDragonClawsMenu::playerId,
            ByteBufCodecs.BOOL, SyncDragonClawsMenu::isOpen,
            ByteBufCodecs.COMPOUND_TAG, SyncDragonClawsMenu::data,
            SyncDragonClawsMenu::new
    );

    public static void handleClient(final SyncDragonClawsMenu packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().level().getEntity(packet.playerId()) instanceof Player player) {
                ClawInventoryData data = ClawInventoryData.getData(player);
                data.setMenuOpen(packet.isOpen());
                data.deserializeNBT(player.level().registryAccess(), packet.data());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}