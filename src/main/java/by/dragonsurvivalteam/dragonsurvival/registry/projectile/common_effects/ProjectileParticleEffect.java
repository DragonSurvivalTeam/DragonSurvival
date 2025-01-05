package by.dragonsurvivalteam.dragonsurvival.registry.projectile.common_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.SpawnParticles;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects.ProjectileWorldEffect;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public record ProjectileParticleEffect(SpawnParticles particleData, LevelBasedValue particleCount) implements ProjectileBlockEffect, ProjectileEntityEffect, ProjectileWorldEffect {
    public static final MapCodec<ProjectileParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpawnParticles.CODEC.fieldOf("particle_data").forGetter(ProjectileParticleEffect::particleData),
            LevelBasedValue.CODEC.fieldOf("particle_count").forGetter(ProjectileParticleEffect::particleCount)
    ).apply(instance, ProjectileParticleEffect::new));

    @Override
    public void apply(Projectile projectile, BlockPos position, int projectileLevel) {
        particleData.apply((ServerLevel) projectile.level(), position, (int)particleCount.calculate(projectileLevel));
    }

    @Override
    public MapCodec<? extends ProjectileBlockEffect> blockCodec() {
        return CODEC;
    }

    @Override
    public void apply(Projectile projectile, Entity target, int projectileLevel) {
        particleData.apply((ServerLevel) projectile.level(), target, (int)particleCount.calculate(projectileLevel));
    }

    @Override
    public MapCodec<? extends ProjectileEntityEffect> entityCodec() {
        return CODEC;
    }

    @Override
    public void apply(Projectile projectile, int level) {
        particleData.apply((ServerLevel) projectile.level(), projectile, (int)particleCount.calculate(level));
    }

    @Override
    public MapCodec<? extends ProjectileWorldEffect> worldCodec() {
        return CODEC;
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final int level) {
        return List.of();
    }
}
