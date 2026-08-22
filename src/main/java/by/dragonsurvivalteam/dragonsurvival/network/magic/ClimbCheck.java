package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClimbingHandler;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.WorldGenLevel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record ClimbCheck(Set<BlockPos> positions) implements CustomPacketPayload {
    public static final Type<ClimbCheck> TYPE = new Type<>(DragonSurvival.res("climb_check"));

    public static final StreamCodec<FriendlyByteBuf, ClimbCheck> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BLOCK_POS.apply(ByteBufCodecs.list()).map(HashSet::new, ArrayList::new), ClimbCheck::positions,
            ClimbCheck::new
    );

    public static void handleServer(final ClimbCheck packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (!(player.level() instanceof WorldGenLevel level)) {
                return;
            }

            ClimbableData data = AttachmentManager.getExistingData(player, DSDataAttachments.CLIMBABLE_DATA).orElse(null);

            if (data != null) {
                data.setTrackedClimbPositions(packet.positions());
            }

            context.reply(new ClimbCheck(ClimbingHandler.filterPositions(data, level, player, packet.positions())));
        });
    }

    public static void handleClient(final ClimbCheck packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() == null) {
                return;
            }

            ClimbableData data = AttachmentManager.getExistingData(context.player(), DSDataAttachments.CLIMBABLE_DATA).orElse(null);

            if (data == null) {
                return;
            }

            data.setApprovedClimbPositions(packet.positions());
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
