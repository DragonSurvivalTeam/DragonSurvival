package by.dragonsurvivalteam.dragonsurvival.common.particles;

import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.NotNull;

/** Basically just a copy of {@link GlowParticle.GlowSquidProvider} but allows setting a custom color */
public class CustomGlowParticle extends GlowParticle {
    protected CustomGlowParticle(final ClientLevel level, final double x, final double y, final double z, final double color, final double ySpeed, final double ignored, final SpriteSet sprites) {
        super(level, x, y, z, color, ySpeed, ignored, sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(@NotNull final SimpleParticleType type, @NotNull final ClientLevel level, final double x, final double y, final double z, final double color, final double ySpeed, final double ignored) {
            CustomGlowParticle particle = new CustomGlowParticle(level, x, y, z, 0.5 - level.getRandom().nextDouble(), ySpeed, 0.5 - level.getRandom().nextDouble(), sprites);
            int argb = DSColors.withAlpha((int) color, 1);

            float red = FastColor.ARGB32.red(argb) / 255f;
            float green = FastColor.ARGB32.green(argb) / 255f;
            float blue = FastColor.ARGB32.blue(argb) / 255f;
            particle.setColor(red, green, blue);

            particle.yd *= 0.2F;
            particle.xd *= 0.1F;
            particle.zd *= 0.1F;

            particle.setLifetime((int) (8 / (level.getRandom().nextDouble() * 0.8 + 0.2)));
            return particle;
        }
    }
}
