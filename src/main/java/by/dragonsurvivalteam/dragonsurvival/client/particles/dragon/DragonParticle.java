package by.dragonsurvivalteam.dragonsurvival.client.particles.dragon;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class DragonParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float spread;
    private final boolean swirls;
    private int swirlTick;

    protected DragonParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, double duration, boolean swirls, SpriteSet sprite) {
        super(pLevel, pX, pY, pZ);
        setSize(1, 1);
        xd = pXSpeed;
        yd = pYSpeed;
        zd = pZSpeed;
        lifetime = (int) duration;
        swirlTick = random.nextInt(120);
        spread = random.nextFloat();
        hasPhysics = false;
        this.swirls = swirls;
        setSpriteFromAge(sprite);
        sprites = sprite;
    }

    @Override
    protected float getU1() {
        return super.getU1() - (super.getU1() - super.getU0()) / 8f;
    }

    @Override

    protected float getV1() {
        return super.getV1() - (super.getV1() - super.getV0()) / 8f;
    }

    @Override
    public void tick() {
        super.tick();

        if (swirls) {
            Vector3f motionVec = new Vector3f((float) xd, (float) yd, (float) zd);
            motionVec.normalize();
            float yaw = (float) Math.atan2(motionVec.x(), motionVec.z());
            float pitch = (float) Math.atan2(motionVec.y(), 1);
            float swirlRadius = 1f * (age / (float) lifetime) * spread;

            Quaternionf quatSpin = new Quaternionf(new AxisAngle4f(swirlTick * 0.2f, motionVec.x(), motionVec.y(), motionVec.z()));
            Quaternionf quatOrient = new Quaternionf().rotateXYZ(pitch, yaw, 0);

            Vector3f vec = new Vector3f(swirlRadius, 0, 0);
            vec = quatSpin.transform(vec);
            vec = quatOrient.transform(vec);

            x += vec.x();
            y += vec.y();
            z += vec.z();
        }

        if (age >= lifetime) {
            remove();
        }
        age++;
        swirlTick++;
        setSpriteFromAge(sprites);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera renderInfo, float partialTicks) {
        float var = (age + partialTicks) / (float) lifetime;
        alpha = (float) (1 - Math.exp(10 * (var - 1)) - Math.pow(2000, -var));
        if (alpha < 0.1) {
            alpha = 0.1f;
        }

        super.render(buffer, renderInfo, partialTicks);
    }
}
