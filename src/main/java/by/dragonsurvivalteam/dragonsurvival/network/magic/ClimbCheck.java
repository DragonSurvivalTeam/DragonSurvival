package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClimbingHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public record ClimbCheck(Set<BlockPos> positions) implements CustomPacketPayload {
    public static final Type<ClimbCheck> TYPE = new Type<>(DragonSurvival.res("climb_check"));

    public static final StreamCodec<FriendlyByteBuf, ClimbCheck> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Codec.list(BlockPos.CODEC)).map(HashSet::new, ArrayList::new), ClimbCheck::positions,
            ClimbCheck::new
    );

    public static void handleServer(final ClimbCheck packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            if (!(player.level() instanceof WorldGenLevel level)) {
                return;
            }

            ClimbableData data = player.getExistingData(DSDataAttachments.CLIMBABLE_DATA).orElse(null);

            if (data != null) {
                data.setTrackedClimbPositions(packet.positions());
            }

            context.reply(new ClimbCheck(ClimbingHandler.filterPositions(data, level, packet.positions())));
        });
    }

    public static void handleClient(final ClimbCheck packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClimbableData data = context.player().getExistingData(DSDataAttachments.CLIMBABLE_DATA).orElse(null);

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
