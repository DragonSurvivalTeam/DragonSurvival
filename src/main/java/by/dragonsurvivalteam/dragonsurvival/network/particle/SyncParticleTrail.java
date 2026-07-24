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
import org.joml.Vector3f;

public record SyncParticleTrail(Vector3f source, Vector3f target, ParticleOptions trailParticle) implements CustomPacketPayload {
    public static final Type<SyncParticleTrail> TYPE = new Type<>(DragonSurvival.res("sync_particle_trail"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncParticleTrail> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VECTOR3F, SyncParticleTrail::source,
            ByteBufCodecs.VECTOR3F, SyncParticleTrail::target,
            ParticleTypes.STREAM_CODEC, SyncParticleTrail::trailParticle,
            SyncParticleTrail::new
    );

    public static void handleClient(final SyncParticleTrail packet, final IPayloadContext context) {
        context.enqueueWork(() -> ClientProxy.handleSyncParticleTrail(packet));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
