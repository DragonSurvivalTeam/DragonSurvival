package by.dragonsurvivalteam.dragonsurvival.client.particles;

import by.dragonsurvivalteam.dragonsurvival.common.particles.SeaSweepParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

public class SeaSweepParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    public SeaSweepParticle(ClientLevel level, double x, double y, double z, double quadSize, SpriteSet spriteSet) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D, spriteSet.first());
        sprites = spriteSet;
        lifetime = 4;
        float f = random.nextFloat() * 0.6F + 0.4F;
        rCol = f;
        gCol = f;
        bCol = f;
        this.quadSize = 1.0F - (float) quadSize * 0.5F;
        setSpriteFromAge(spriteSet);
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        if (age++ >= lifetime) {
            remove();
        } else {
            setSpriteFromAge(sprites);
        }
    }

    @Override
    protected @NotNull Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public int getLightCoords(float pPartialTicks) {
        return 15728880;
    }

    public static class Factory implements ParticleProvider<SeaSweepParticleOption> {
        private final SpriteSet sprites;

        public Factory(SpriteSet spriteSet) {
            sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SeaSweepParticleOption type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new SeaSweepParticle(level, x, y, z, type.quadSize(), sprites);
        }
    }
}
