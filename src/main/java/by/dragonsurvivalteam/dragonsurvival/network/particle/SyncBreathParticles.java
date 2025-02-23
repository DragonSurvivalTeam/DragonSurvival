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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
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

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBreathParticles> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncBreathParticles::playerId,
            ByteBufCodecs.FLOAT, SyncBreathParticles::spread,
            ByteBufCodecs.FLOAT, SyncBreathParticles::speedPerGrowth,
            ByteBufCodecs.INT, SyncBreathParticles::numParticles,
            ParticleTypes.STREAM_CODEC, SyncBreathParticles::mainParticle,
            ParticleTypes.STREAM_CODEC, SyncBreathParticles::secondaryParticle,
            SyncBreathParticles::new
    );

    public static void handleClient(final SyncBreathParticles packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player().level().getEntity(packet.playerId()) instanceof Entity entity)) {
                return;
            }

            double positionOffset = 0.6;
            double speedMultiplier = 20;

            if (entity instanceof Player player) {
                DragonStateHandler handler = DragonStateProvider.getData(player);

                if (handler.isDragon()) {
                    positionOffset = handler.getGrowth() / 30;
                    speedMultiplier = handler.getGrowth();

                    // FIXME :: breath start is weirdly offset too high / too the side?
//                    DragonEntity dragon = ClientDragonRenderer.PLAYER_DRAGON_MAP.get(player.getId());
//                    Vector3d breathPosition = dragon.getAnimatableInstanceCache().getManagerForId(dragon.getId()).getBoneSnapshotCollection().get("BreathSource").getBone().getWorldPosition();
//                    position = new Vec3(breathPosition.x(), breathPosition.y(), breathPosition.z());
                }
            }

            float yaw = (float) Math.toRadians(-entity.getYRot());
            float pitch = (float) Math.toRadians(-entity.getXRot());
            float speed = (float) (packet.speedPerGrowth() * speedMultiplier);

            int scale = entity instanceof Player player && player.getAbilities().flying ? 2 : 1;
            Vec3 position = entity.getEyePosition().add(entity.getLookAngle().scale(scale)).add(0, -0.1 - 0.2 * positionOffset, 0);

            for (int i = 0; i < packet.numParticles(); i++) {
                spawnParticle(packet.secondaryParticle(), entity, position, yaw, pitch, speed, packet.spread());
            }

            for (int i = 0; i < packet.numParticles() / 2; i++) {
                spawnParticle(packet.mainParticle(), entity, position, yaw, pitch, speed, packet.spread());
            }
        });
    }

    private static void spawnParticle(final ParticleOptions particle, final Entity entity, final Vec3 position, final float yaw, final float pitch, final float speed, final float spread) {
        Vec3 velocity = calculateParticleVelocity((float) (yaw + spread * 2 * (entity.getRandom().nextDouble() * 2 - 1) * 2 * Math.PI), (float) (pitch + spread * (entity.getRandom().nextDouble() * 2 - 1) * 2.f * Math.PI), speed);
        velocity = velocity.add(entity.getDeltaMovement());
        entity.level().addParticle(particle, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
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
