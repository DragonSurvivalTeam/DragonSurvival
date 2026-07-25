package by.dragonsurvivalteam.dragonsurvival.common.particles;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.BlockVisionData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Basically just a copy of {@link GlowParticle.GlowSquidProvider} but allows setting a custom color */
public class CustomGlowParticle extends TextureSheetParticle {
    /** Allows each particle to start at a separate section of the (potential) color range */
    private final double colorOffset;
    private final SpriteSet sprites;
    private List<Integer> colors;

    protected CustomGlowParticle(final ClientLevel level, final double x, final double y, final double z, final double blockId, final double ySpeed, final double colorOffset, final SpriteSet sprites) {
        super(level, x, y, z, 0.5 - level.getRandom().nextDouble(), ySpeed, 0.5 - level.getRandom().nextDouble());
        friction = 0.96F;
        speedUpWhenYMotionIsBlocked = true;
        this.sprites = sprites;
        quadSize *= 0.75F;
        hasPhysics = false;
        setSpriteFromAge(sprites);
        this.colorOffset = colorOffset;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(final float partialTick) {
        float progress = Mth.clamp(((float) age + partialTick) / (float) lifetime, 0.0F, 1.0F);
        int light = super.getLightColor(partialTick);
        int blockLight = light & 255;
        int skyLight = light >> 16 & 255;
        blockLight = Math.min(blockLight + (int) (progress * 15.0F * 16.0F), 240);
        return blockLight | skyLight << 16;
    }

    @Override
    public void tick() {
        super.tick();
        setSpriteFromAge(sprites);

        if (colors != null) {
            int argb = DSColors.withAlpha(Functions.lerpColor(colors, 1, colorOffset), 1);

            float red = FastColor.ARGB32.red(argb) / 255f;
            float green = FastColor.ARGB32.green(argb) / 255f;
            float blue = FastColor.ARGB32.blue(argb) / 255f;
            setColor(red, green, blue);
        }
    }

    public void setColors(final List<Integer> colors) {
        this.colors = colors;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(@NotNull final SimpleParticleType type, @NotNull final ClientLevel level, final double x, final double y, final double z, final double blockId, final double ySpeed, final double colorOffset) {
            CustomGlowParticle particle = new CustomGlowParticle(level, x, y, z, blockId, ySpeed, colorOffset, sprites);

            LocalPlayer player = Minecraft.getInstance().player;
            List<Integer> colors = List.of();

            //noinspection DataFlowIssue -> player is present
            BlockVisionData vision = AttachmentManager.getExistingData(player, DSDataAttachments.BLOCK_VISION).orElse(null);

            if (vision != null) {
                colors = vision.getColors(BuiltInRegistries.BLOCK.byId((int) blockId));
            }

            int argb = Functions.lerpColor(colors);
            float red = FastColor.ARGB32.red(argb) / 255f;
            float green = FastColor.ARGB32.green(argb) / 255f;
            float blue = FastColor.ARGB32.blue(argb) / 255f;
            particle.setColor(red, green, blue);

            particle.yd *= 0.2F;
            particle.xd *= 0.1F;
            particle.zd *= 0.1F;

            particle.setColors(colors);
            particle.setLifetime((int) (8 / (level.getRandom().nextDouble() * 0.8 + 0.2)));
            return particle;
        }
    }
}
