package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record SpawnParticles(
        ParticleOptions particle,
        PositionSource horizontalPosition,
        PositionSource verticalPosition,
        VelocitySource horizontalVelocity,
        VelocitySource verticalVelocity,
        FloatProvider speed
) {
    public static final Codec<SpawnParticles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ParticleTypes.CODEC.fieldOf("particle").forGetter(SpawnParticles::particle),
            PositionSource.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticles::horizontalPosition),
            PositionSource.CODEC.fieldOf("vertical_position").forGetter(SpawnParticles::verticalPosition),
            VelocitySource.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticles::horizontalVelocity),
            VelocitySource.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticles::verticalVelocity),
            FloatProvider.CODEC.optionalFieldOf("speed", ConstantFloat.ZERO).forGetter(SpawnParticles::speed)
    ).apply(instance, SpawnParticles::new));

    public static PositionSource offsetFromEntityPosition(float offset) {
        return new PositionSource(PositionSourceType.ENTITY_POSITION, offset, 1.0F);
    }

    public static PositionSource inBoundingBox() {
        return new PositionSource(PositionSourceType.BOUNDING_BOX, 0.0F, 1.0F);
    }

    public static VelocitySource movementScaled(float movementScale) {
        return new VelocitySource(movementScale, ConstantFloat.ZERO);
    }

    public static VelocitySource fixedVelocity(FloatProvider velocity) {
        return new VelocitySource(0.0F, velocity);
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
        RandomSource randomsource = entity.level().random;
        Vec3 origin = entity.position();
        Vec3 vec3 = entity.getDeltaMovement();
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

    public record PositionSource(PositionSourceType type, float offset, float scale) {
        public static final MapCodec<PositionSource> CODEC = RecordCodecBuilder.<PositionSource>mapCodec(instance -> instance.group(
                PositionSourceType.CODEC.fieldOf("type").forGetter(PositionSource::type),
                Codec.FLOAT.optionalFieldOf("offset", 0.0F).forGetter(PositionSource::offset),
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", 1.0F).forGetter(PositionSource::scale)
        ).apply(instance, PositionSource::new)).flatXmap(PositionSource::validate, PositionSource::validate);

        private static DataResult<PositionSource> validate(final PositionSource source) {
            if (source.type() == PositionSourceType.ENTITY_POSITION && source.scale() != 1.0F) {
                return DataResult.error(() -> "Cannot scale an entity position coordinate source");
            }
            return DataResult.success(source);
        }

        public double getCoordinate(final double entityPosition, final double boundingBoxCenter, final float size, final RandomSource random) {
            return type.getCoordinate(entityPosition, boundingBoxCenter, size * scale, random) + offset;
        }
    }

    public enum PositionSourceType implements StringRepresentable {
        ENTITY_POSITION("entity_position") {
            @Override
            double getCoordinate(final double entityPosition, final double boundingBoxCenter, final float size, final RandomSource random) {
                return entityPosition;
            }
        },
        BOUNDING_BOX("in_bounding_box") {
            @Override
            double getCoordinate(final double entityPosition, final double boundingBoxCenter, final float size, final RandomSource random) {
                return boundingBoxCenter + (random.nextDouble() - 0.5D) * size;
            }
        };

        public static final Codec<PositionSourceType> CODEC = StringRepresentable.fromEnum(PositionSourceType::values);

        private final String id;

        PositionSourceType(final String id) {
            this.id = id;
        }

        abstract double getCoordinate(double entityPosition, double boundingBoxCenter, float size, RandomSource random);

        @Override
        public String getSerializedName() {
            return id;
        }
    }

    public record VelocitySource(float movementScale, FloatProvider base) {
        public static final MapCodec<VelocitySource> CODEC = RecordCodecBuilder.<VelocitySource>mapCodec(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("movement_scale", 0.0F).forGetter(VelocitySource::movementScale),
                FloatProvider.CODEC.optionalFieldOf("base", ConstantFloat.ZERO).forGetter(VelocitySource::base)
        ).apply(instance, VelocitySource::new));

        public double getVelocity(final double movement, final RandomSource random) {
            return movement * movementScale + base.sample(random);
        }
    }
}
