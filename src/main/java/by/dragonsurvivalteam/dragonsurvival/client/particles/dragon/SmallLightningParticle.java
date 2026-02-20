package by.dragonsurvivalteam.dragonsurvival.client.particles.dragon;

import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallLightningParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

public class SmallLightningParticle extends DragonParticle {
    protected SmallLightningParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, double duration, boolean swirls, SpriteSet sprite) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, duration, swirls, sprite);
    }

    @Override
    public void remove() {
        level.addParticle(ParticleTypes.WHITE_ASH, x, y, z, 0, 0.01, 0);
        super.remove();
    }

    @Override
    protected int getLightColor(float partialTick) {
        int color = super.getLightColor(partialTick);
        int red = ARGB.red(color);
        return 240 | red << 16;
    }

    public static final class Factory implements ParticleProvider<SmallLightningParticleOption> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {

            spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SmallLightningParticleOption type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            SmallLightningParticle particle = new SmallLightningParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.duration(), type.swirls(), spriteSet);
            particle.setSpriteFromAge(spriteSet);
            return particle;
        }
    }
}