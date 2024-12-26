package by.dragonsurvivalteam.dragonsurvival.network.particle;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncBreathParticles(int playerId, float spread, float speedPerSize, int numParticles, ParticleOptions mainParticle, ParticleOptions secondaryParticle) implements CustomPacketPayload {
    public static final Type<SyncBreathParticles> TYPE = new Type<>(DragonSurvival.res("sync_breath_particles"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBreathParticles> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncBreathParticles::playerId,
            ByteBufCodecs.FLOAT, SyncBreathParticles::spread,
            ByteBufCodecs.FLOAT, SyncBreathParticles::speedPerSize,
            ByteBufCodecs.INT, SyncBreathParticles::numParticles,
            ParticleTypes.STREAM_CODEC, SyncBreathParticles::mainParticle,
            ParticleTypes.STREAM_CODEC, SyncBreathParticles::secondaryParticle,
            SyncBreathParticles::new
    );

    public static void handleClient(final SyncBreathParticles packet, final IPayloadContext context) {
        // FIXME :: we should try to remove the need of this client proxy - use 'context.player().level().addParticle(...)' in the packet here itself
        context.enqueueWork(() -> ClientProxy.handleSyncBreathParticles(packet));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
