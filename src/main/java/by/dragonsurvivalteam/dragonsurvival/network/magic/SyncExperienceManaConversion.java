package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncExperienceManaConversion(boolean enabled) implements CustomPacketPayload {
    public static final Type<SyncExperienceManaConversion> TYPE = new Type<>(DragonSurvival.res("sync_experience_mana_conversion"));

    public static final StreamCodec<FriendlyByteBuf, SyncExperienceManaConversion> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncExperienceManaConversion::enabled,
            SyncExperienceManaConversion::new
    );

    public static void handleServer(final SyncExperienceManaConversion packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            MagicData magic = MagicData.getData(player);
            magic.setUseExperienceForMana(packet.enabled());
            TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
            magic.serialize(valueOutput);
            PacketDistributor.sendToPlayer(player, new SyncMagicData(valueOutput.buildResult()));
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
