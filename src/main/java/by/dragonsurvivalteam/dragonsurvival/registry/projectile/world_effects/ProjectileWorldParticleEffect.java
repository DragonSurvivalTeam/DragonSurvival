package by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.SpawnParticles;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record ProjectileWorldParticleEffect(SpawnParticles particleData, LevelBasedValue particleCount) implements ProjectileWorldEffect {
    public static final MapCodec<ProjectileWorldParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpawnParticles.CODEC.fieldOf("particle_data").forGetter(ProjectileWorldParticleEffect::particleData),
            LevelBasedValue.CODEC.fieldOf("particle_count").forGetter(ProjectileWorldParticleEffect::particleCount)
    ).apply(instance, ProjectileWorldParticleEffect::new));

    @Override
    public void apply(final Projectile projectile, final Void target, final int level) {
        particleData.apply((ServerLevel) projectile.level(), projectile, (int) particleCount.calculate(level));
    }

    @Override
    public MapCodec<? extends ProjectileWorldEffect> codec() {
        return CODEC;
    }
}
