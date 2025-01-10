package by.dragonsurvivalteam.dragonsurvival.network.particle;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
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
        context.enqueueWork(() -> {
            Player player = context.player();
            DragonStateHandler handler = DragonStateProvider.getData(player);

            float yaw = (float) Math.toRadians(-player.getYRot());
            float pitch = (float) Math.toRadians(-player.getXRot());
            float speed = (float) (handler.getSize() * packet.speedPerSize());

            Vec3 eyePos = player.getEyePosition();
            Vec3 lookAngle = player.getLookAngle();
            Vec3 position;

            if (player.getAbilities().flying) {
                Vec3 forward = lookAngle.scale(2.0F);
                position = eyePos.add(forward).add(0F, -0.1 - 0.5F * (handler.getSize() / 30F), 0F);
            } else {
                Vec3 forward = lookAngle.scale(1.0F);
                position = eyePos.add(forward).add(0F, -0.1F - 0.2F * (handler.getSize() / 30F), 0F);
            }

            RandomSource random = player.getRandom();

            for (int i = 0; i < packet.numParticles(); i++) {
                Vec3 velocity = calculateParticleVelocity((float) (yaw + packet.spread() * 2 * (random.nextDouble() * 2 - 1) * 2.f * Math.PI), (float) (pitch + packet.spread() * (random.nextDouble() * 2 - 1) * 2.f * Math.PI), speed);
                velocity = velocity.add(player.getDeltaMovement());
                // Get the dragon model and place the particles at the mouth of the dragon
                player.level().addParticle(packet.secondaryParticle(), position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            }

            for (int i = 0; i <  packet.numParticles() / 2; i++) {
                Vec3 velocity = calculateParticleVelocity((float) (yaw + packet.spread() * 2 * (random.nextDouble() * 2 - 1) * 2.f * Math.PI), (float) (pitch + packet.spread() * (random.nextDouble() * 2 - 1) * 2.f * Math.PI), speed);
                velocity = velocity.add(player.getDeltaMovement());
                player.level().addParticle(packet.mainParticle(), position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
            }
        });
    }

    private static Vec3 calculateParticleVelocity(float yaw, float pitch, float speed) {
        float xVel = (float) (Math.sin(yaw) * Math.cos(pitch) * speed);
        float yVel = (float) Math.sin(pitch) * speed;
        float zVel = (float) (Math.cos(yaw) * Math.cos(pitch) * speed);
        return new Vec3(xVel, yVel, zVel);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
