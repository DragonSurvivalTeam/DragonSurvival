package by.dragonsurvivalteam.dragonsurvival.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class BeaconParticle extends TextureSheetParticle {
    private final double fallSpeed;

    public BeaconParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd);
        gravity = 0.9f;
        fallSpeed = 0.02;
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
        BeaconParticle beaconParticle = new BeaconParticle(level, x, y, z, xd, yd, zd);
        beaconParticle.pickSprite(spriteSet);
        return beaconParticle;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class FireFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public FireFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return BeaconParticle.createParticle(level, x, y, z, xd, yd, zd, spriteSet);
        }
    }

    public static class MagicFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public MagicFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return BeaconParticle.createParticle(level, x, y, z, xd, yd, zd, spriteSet);
        }
    }

    public static class PeaceFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public PeaceFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
            return BeaconParticle.createParticle(level, x, y, z, xd, yd, zd, spriteSet);
        }
    }
}