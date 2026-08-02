package by.dragonsurvivalteam.dragonsurvival.network.sound;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record StartTickingSound(int playerId, SoundEvent soundEvent, ResourceLocation id) implements CustomPacketPayload {
    public static final Type<StartTickingSound> TYPE = new CustomPacketPayload.Type<>(DragonSurvival.res("start_ticking_sound"));

    public static final StreamCodec<FriendlyByteBuf, StartTickingSound> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StartTickingSound::playerId,
            ByteBufCodecs.SOUND_EVENT, StartTickingSound::soundEvent,
            ByteBufCodecs.RESOURCE_LOCATION, StartTickingSound::id,
            StartTickingSound::new
    );

    public static void handleClient(final StartTickingSound packet, final PayloadContext context) {
        context.enqueueWork(() -> {
            Entity entity = context.player().level().getEntity(packet.playerId());

            if (!(entity instanceof Player player)) {
                return;
            }

            DragonSurvival.PROXY.queueTickingSound(packet.id(), packet.soundEvent(), SoundSource.PLAYERS, player);
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
