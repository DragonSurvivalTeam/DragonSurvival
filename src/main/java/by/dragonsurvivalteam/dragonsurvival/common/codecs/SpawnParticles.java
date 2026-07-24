package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;
import net.minecraft.world.phys.Vec3;

public record SpawnParticles(
        ParticleOptions particle,
        SpawnParticlesEffect.PositionSource horizontalPosition,
        SpawnParticlesEffect.PositionSource verticalPosition,
        SpawnParticlesEffect.VelocitySource horizontalVelocity,
        SpawnParticlesEffect.VelocitySource verticalVelocity,
        FloatProvider speed
) {
    public static final Codec<SpawnParticles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(SpawnParticles::particle),
            SpawnParticlesEffect.PositionSource.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticles::horizontalPosition),
            SpawnParticlesEffect.PositionSource.CODEC.fieldOf("vertical_position").forGetter(SpawnParticles::verticalPosition),
            SpawnParticlesEffect.VelocitySource.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticles::horizontalVelocity),
            SpawnParticlesEffect.VelocitySource.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticles::verticalVelocity),
            FloatProvider.CODEC.optionalFieldOf("speed", ConstantFloat.ZERO).forGetter(SpawnParticles::speed)
    ).apply(instance, SpawnParticles::new));

    public static SpawnParticlesEffect.PositionSource offsetFromEntityPosition(float offset) {
        return new SpawnParticlesEffect.PositionSource(SpawnParticlesEffect.PositionSourceType.ENTITY_POSITION, offset, 1.0F);
    }

    public static SpawnParticlesEffect.PositionSource inBoundingBox() {
        return new SpawnParticlesEffect.PositionSource(SpawnParticlesEffect.PositionSourceType.BOUNDING_BOX, 0.0F, 1.0F);
    }

    public static SpawnParticlesEffect.VelocitySource movementScaled(float movementScale) {
        return new SpawnParticlesEffect.VelocitySource(movementScale, ConstantFloat.ZERO);
    }

    public static SpawnParticlesEffect.VelocitySource fixedVelocity(FloatProvider velocity) {
        return new SpawnParticlesEffect.VelocitySource(0.0F, velocity);
    }

    public void apply(ServerLevel level, BlockPos blockPos, int count) {
        for (int i = 0; i < count; ++i) {
            level.sendParticles(
                    this.particle,
                    blockPos.getX() + this.horizontalPosition.getCoordinate(0.5D, 0.5D, 1.0F, level.random),
                    blockPos.getY() + this.verticalPosition.getCoordinate(0.5D, 0.5D, 1.0F, level.random),
                    blockPos.getZ() + this.horizontalPosition.getCoordinate(0.5D, 0.5D, 1.0F, level.random),
                    1,
                    this.horizontalVelocity.getVelocity(0.0D, level.random),
                    this.verticalVelocity.getVelocity(0.0D, level.random),
                    this.horizontalVelocity.getVelocity(0.0D, level.random),
                    this.speed.sample(level.random)
            );
        }
    }

    public void apply(ServerLevel level, Entity entity, int count) {
        RandomSource randomsource = entity.getRandom();
        Vec3 origin = entity.position();
        Vec3 vec3 = entity.getKnownMovement();
        float width = entity.getBbWidth();
        float height = entity.getBbHeight();
        for (int i = 0; i < count; ++i) {
            level.sendParticles(
                    this.particle,
                    this.horizontalPosition.getCoordinate(origin.x(), origin.x(), width, randomsource),
                    this.verticalPosition.getCoordinate(origin.y(), origin.y() + (double) (height / 2.0F), height, randomsource),
                    this.horizontalPosition.getCoordinate(origin.z(), origin.z(), width, randomsource),
                    1,
                    this.horizontalVelocity.getVelocity(vec3.x(), randomsource),
                    this.verticalVelocity.getVelocity(vec3.y(), randomsource),
                    this.horizontalVelocity.getVelocity(vec3.z(), randomsource),
                    this.speed.sample(randomsource)
            );
        }
    }
}
