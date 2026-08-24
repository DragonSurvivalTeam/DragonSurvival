package by.dragonsurvivalteam.dragonsurvival.network.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record SyncClimbFlag(int entityId, ClimbingType climbingType) implements CustomPacketPayload {
    public static final Type<SyncClimbFlag> TYPE = new Type<>(DragonSurvival.res("sync_climb_flag"));

    public static final StreamCodec<FriendlyByteBuf, SyncClimbFlag> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncClimbFlag::entityId,
            ByteBufCodecs.fromCodec(ClimbingType.CODEC), SyncClimbFlag::climbingType,
            SyncClimbFlag::new
    );

    public static void handleClient(final SyncClimbFlag packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();

            if (player != null && player.level().getEntity(packet.entityId()) instanceof LivingEntity entity) {
                ClimbableData data = AttachmentManager.getData(entity, DSDataAttachments.CLIMBABLE_DATA);
                data.setClimbingType(packet.climbingType());
            }
        });
    }

    public enum ClimbingType implements StringRepresentable {
        WALL("wall"),
        CEILING("ceiling"),
        NONE("none");

        public static final Codec<ClimbingType> CODEC = StringRepresentable.fromEnum(ClimbingType::values);

        private final String name;

        ClimbingType(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
