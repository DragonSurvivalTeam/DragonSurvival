package by.dragonsurvivalteam.dragonsurvival.client.particles.dragon;

import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallSunParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class SmallSunParticle extends DragonParticle {
    protected SmallSunParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, double duration, boolean swirls, SpriteSet sprite) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, duration, swirls, sprite);
    }

    @Override
    public void remove() {
        level.addParticle(ParticleTypes.END_ROD, x, y, z, 0, 0.1, 0);
        super.remove();
    }

    @Override // Clamp brightness between 10 and 15, depending on the particle age
    protected int getLightColor(float partialTick) {
        float progress = (age + partialTick) / lifetime;
        progress = Mth.clamp(progress, 0, 1);
        return (int) (0xF0 - (progress * (0xF0 - 0xA0)));
    }

    public static final class Factory implements ParticleProvider<SmallSunParticleOption> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SmallSunParticleOption type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SmallSunParticle particle = new SmallSunParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.duration(), type.swirls(), spriteSet);
            particle.setSpriteFromAge(spriteSet);
            return particle;
        }
    }
}