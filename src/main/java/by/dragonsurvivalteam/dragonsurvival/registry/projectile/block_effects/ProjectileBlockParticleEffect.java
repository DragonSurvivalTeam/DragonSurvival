package by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.SpawnParticles;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record ProjectileBlockParticleEffect(SpawnParticles particleData, LevelBasedValue particleCount) implements ProjectileBlockEffect {
    public static final MapCodec<ProjectileBlockParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpawnParticles.CODEC.fieldOf("particle_data").forGetter(ProjectileBlockParticleEffect::particleData),
            LevelBasedValue.CODEC.fieldOf("particle_count").forGetter(ProjectileBlockParticleEffect::particleCount)
    ).apply(instance, ProjectileBlockParticleEffect::new));

    @Override
    public void apply(final Projectile projectile, final BlockPos position, final int level) {
        particleData.apply((ServerLevel) projectile.level(), position, (int) particleCount.calculate(level));
    }

    @Override
    public MapCodec<? extends ProjectileBlockEffect> codec() {
        return CODEC;
    }
}
