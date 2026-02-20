package by.dragonsurvivalteam.dragonsurvival.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class BeaconParticle extends SingleQuadParticle {
    private final double fallSpeed;

    public BeaconParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet sprite) {
        super(level, x, y, z, xd, yd, zd, sprite.first());
        gravity = 0.9f;
        fallSpeed = 0.02;
        setSpriteFromAge(sprite);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        } else {
            yd += fallSpeed;
            move(0, yd, 0);
            if (y == yo) {
                xd *= 1.1D;
                zd *= 1.1D;
            }
            yd *= 0.7F;
            if (onGround) {
                xd *= 0.96F;
                zd *= 0.96F;
            }
        }
    }

    public static BeaconParticle createParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, SpriteSet spriteSet) {
        return new BeaconParticle(level, x, y, z, xd, yd, zd, spriteSet);
    }

    @Override
    protected @NotNull Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    public static class FireFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public FireFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return BeaconParticle.createParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }

    public static class MagicFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public MagicFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return BeaconParticle.createParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }

    public static class PeaceFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public PeaceFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return BeaconParticle.createParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
        }
    }
}