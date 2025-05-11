package by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record ProjectileAreaCloudEffect(PotionData potion, LevelBasedValue duration, LevelBasedValue probability, Optional<LevelBasedValue> delay, Optional<LevelBasedValue> radius, ParticleOptions particle) implements ProjectileBlockEffect {
    public static final MapCodec<ProjectileAreaCloudEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PotionData.CODEC.fieldOf("potion").forGetter(ProjectileAreaCloudEffect::potion),
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(ProjectileAreaCloudEffect::duration),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(ProjectileAreaCloudEffect::probability),
            LevelBasedValue.CODEC.optionalFieldOf("delay").forGetter(ProjectileAreaCloudEffect::delay),
            LevelBasedValue.CODEC.optionalFieldOf("radius").forGetter(ProjectileAreaCloudEffect::radius),
            ParticleTypes.CODEC.fieldOf("particle").forGetter(ProjectileAreaCloudEffect::particle)
    ).apply(instance, ProjectileAreaCloudEffect::new));

    @Override
    public void apply(Projectile projectile, BlockPos target, int level) {
        if (projectile.level().random.nextDouble() < probability.calculate(level)) {
            AreaEffectCloud cloud = new AreaEffectCloud(projectile.level(), target.getX(), target.getY(), target.getZ());
            if (projectile.getOwner() instanceof ServerPlayer serverPlayer) {
                cloud.setPotionContents(potion.toPotionContents(serverPlayer, level));
            } else {
                cloud.setPotionContents(potion.toPotionContents(null, level));
            }
            cloud.setDuration((int) duration.calculate(level));
            cloud.setParticle(particle);
            if (projectile.getOwner() instanceof LivingEntity living) {
                cloud.setOwner(living);
            }
            cloud.setWaitTime((int) delay.orElse(LevelBasedValue.constant(0)).calculate(level));
            cloud.setRadius((int) radius.orElse(LevelBasedValue.constant(1)).calculate(level));

            projectile.level().addFreshEntity(cloud);
        }
    }

    @Override
    public MapCodec<? extends ProjectileBlockEffect> codec() {
        return CODEC;
    }
}
