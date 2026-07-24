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
import org.joml.Vector3f;

public record SyncParticleTrail(Vector3f source, Vector3f target, ParticleOptions trailParticle) implements CustomPacketPayload {
    public static final Type<SyncParticleTrail> TYPE = new Type<>(DragonSurvival.res("sync_particle_trail"));

    public static final StreamCodec<FriendlyByteBuf, SyncParticleTrail> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, SyncParticleTrail::source,
            ByteBufCodecs.VECTOR3F, SyncParticleTrail::target,
            ByteBufCodecs.PARTICLE_OPTIONS, SyncParticleTrail::trailParticle,
            SyncParticleTrail::new
    );

    public static void handleClient(final SyncParticleTrail packet, final PayloadContext context) {
        context.enqueueWork(() -> ClientProxy.handleSyncParticleTrail(packet));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
