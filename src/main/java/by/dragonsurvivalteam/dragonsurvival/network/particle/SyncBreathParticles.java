package by.dragonsurvivalteam.dragonsurvival.network.particle;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import by.dragonsurvivalteam.dragonsurvival.network.codec.ByteBufCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.codec.StreamCodec;
import by.dragonsurvivalteam.dragonsurvival.network.compat.CustomPacketPayload;
import by.dragonsurvivalteam.dragonsurvival.network.compat.PayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncBreathParticles(
        int playerId,
        float spread,
        float speedPerGrowth,
        int numParticles,
        ParticleOptions mainParticle,
        ParticleOptions secondaryParticle
) implements CustomPacketPayload {
    public static final Type<SyncBreathParticles> TYPE = new Type<>(DragonSurvival.res("sync_breath_particles"));

    public static final StreamCodec<FriendlyByteBuf, SyncBreathParticles> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncBreathParticles::playerId,
            ByteBufCodecs.FLOAT, SyncBreathParticles::spread,
            ByteBufCodecs.FLOAT, SyncBreathParticles::speedPerGrowth,
            ByteBufCodecs.INT, SyncBreathParticles::numParticles,
            ByteBufCodecs.PARTICLE_OPTIONS, SyncBreathParticles::mainParticle,
            ByteBufCodecs.PARTICLE_OPTIONS, SyncBreathParticles::secondaryParticle,
            SyncBreathParticles::new
    );

    public static void handleClient(final SyncBreathParticles packet, final PayloadContext context) {
        context.enqueueWork(() -> ClientProxy.handleBreathParticles(packet, context.player()));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
