package by.dragonsurvivalteam.dragonsurvival.client.particles.dragon;

import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallPoisonParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.NotNull;

public class SmallPoisonParticle extends DragonParticle {
    protected SmallPoisonParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, double duration, boolean swirls, SpriteSet sprite) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, duration, swirls, sprite);
    }

    @Override
    public void remove() {
        level.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, 0, 0.01, 0);
        super.remove();
    }

    public static final class Factory implements ParticleProvider<SmallPoisonParticleOption> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SmallPoisonParticleOption type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SmallPoisonParticle particle = new SmallPoisonParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.duration(), type.swirls(), spriteSet);
            particle.setSpriteFromAge(spriteSet);
            return particle;
        }
    }
}