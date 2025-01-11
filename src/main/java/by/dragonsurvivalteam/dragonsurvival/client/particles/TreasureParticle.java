package by.dragonsurvivalteam.dragonsurvival.client.particles;

import by.dragonsurvivalteam.dragonsurvival.common.particles.TreasureParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class TreasureParticle extends TextureSheetParticle {
    public TreasureParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, TreasureParticleOption data, SpriteSet spriteSet) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        rCol = data.red();
        gCol = data.green();
        bCol = data.blue();
        quadSize *= 0.75F * data.scale();
        int i = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
        lifetime = (int) Math.max((float) i * data.scale(), 1.0F);
        pickSprite(spriteSet);
    }

    @Override
    public float getQuadSize(float p_217561_1_) {
        return quadSize * Mth.clamp(((float) age + p_217561_1_) / (float) lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        if (age++ >= lifetime) {
            remove();
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    protected int getLightColor(float partialTick) {
        int color = super.getLightColor(partialTick);
        int red = FastColor.ARGB32.red(color);
        return 240 | red << 16;
    }

    public static class Factory implements ParticleProvider<TreasureParticleOption> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(@NotNull TreasureParticleOption type, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new TreasureParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, type, sprites);
        }
    }
}