package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.SpawnParticles;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record ProjectileEntityParticleEffect(SpawnParticles particleData, LevelBasedValue particleCount) implements ProjectileEntityEffect {
    public static final MapCodec<ProjectileEntityParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpawnParticles.CODEC.fieldOf("particle_data").forGetter(ProjectileEntityParticleEffect::particleData),
            LevelBasedValue.CODEC.fieldOf("particle_count").forGetter(ProjectileEntityParticleEffect::particleCount)
    ).apply(instance, ProjectileEntityParticleEffect::new));

    @Override
    public void apply(final Projectile projectile, final Entity target, final int level) {
        particleData.apply((ServerLevel) projectile.level(), target, (int) particleCount.calculate(level));
    }

    @Override
    public MapCodec<? extends ProjectileEntityEffect> codec() {
        return CODEC;
    }
}
