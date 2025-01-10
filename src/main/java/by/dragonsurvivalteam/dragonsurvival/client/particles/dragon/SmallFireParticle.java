package by.dragonsurvivalteam.dragonsurvival.client.particles.dragon;

import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallFireParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

public class SmallFireParticle extends DragonParticle {
    protected SmallFireParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, double duration, boolean swirls, SpriteSet sprite) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, duration, swirls, sprite);
    }

    @Override
    public void remove() {
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0.01, 0);
        super.remove();
    }

    @Override
    protected int getLightColor(float partialTick) {
        int color = super.getLightColor(partialTick);
        int red = FastColor.ARGB32.red(color);
        return 240 | red << 16;
    }

    public static final class Factory implements ParticleProvider<SmallFireParticleOption> {
        private final SpriteSet spriteSet;

        public Factory(SpriteSet sprite) {
            spriteSet = sprite;
        }

        @Override
        public Particle createParticle(SmallFireParticleOption type, @NotNull ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            SmallFireParticle particle = new SmallFireParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type.duration(), type.swirls(), spriteSet);
            particle.setSpriteFromAge(spriteSet);
            return particle;
        }
    }
}