package by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.CaveDragon;

import by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.DragonParticle;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeFireParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.NotNull;

public class LargeFireParticle extends DragonParticle {
    protected LargeFireParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, double duration, boolean swirls, SpriteSet sprite) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, duration, swirls, sprite);
    }

    public static final class Factory implements ParticleProvider<LargeFireParticleOption> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            spriteSet = sprite;
        }

        @Override
        public @NotNull Particle createParticle(@NotNull LargeFireParticleOption type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            LargeFireParticle particle = new LargeFireParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.duration(), type.swirls(), spriteSet);
            particle.setSpriteFromAge(spriteSet);
            return particle;
        }
    }
}