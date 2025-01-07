package by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting;

import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncParticleTrail;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public record ProjectileAreaTarget(GeneralData generalData, LevelBasedValue radius, Optional<ParticleOptions> particleTrail) implements ProjectileTargeting {
    public static final MapCodec<ProjectileAreaTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> ProjectileTargeting.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("radius").forGetter(ProjectileAreaTarget::radius))
            .and(ParticleTypes.CODEC.optionalFieldOf("particle_trail").forGetter(ProjectileAreaTarget::particleTrail))
            .apply(instance, ProjectileAreaTarget::new)
    );

    public void apply(final Projectile projectile, final int level) {
        if (generalData.effects().isEmpty()) {
            return;
        }

        if (projectile.level().getGameTime() % generalData.tickRate() != 0 || generalData.chance() < projectile.getRandom().nextDouble()) {
            return;
        }

        double radius = this.radius.calculate(level);
        ServerLevel serverLevel = (ServerLevel) projectile.level();

        BlockPos.betweenClosedStream(AABB.ofSize(projectile.position(), radius * 2, radius * 2, radius * 2)).forEach(position -> {
            boolean appliedEffect = false;

            for (ConditionalEffect effect : generalData.effects()) {
                if (effect.apply(serverLevel, projectile, position, level)) {
                    appliedEffect = true;
                }
            }

            if (appliedEffect) {
                handleParticleTrail(projectile, position, serverLevel);
            }
        });

        serverLevel.getEntities(projectile, AABB.ofSize(projectile.position(), radius * 2, radius * 2, radius * 2)).forEach(entity -> {
            boolean appliedEffect = false;

            for (ConditionalEffect effect : generalData.effects()) {
                if (effect.apply(serverLevel, projectile, entity, level)) {
                    appliedEffect = true;
                }
            }

            if (appliedEffect) {
                handleParticleTrail(projectile, entity, serverLevel);
            }
        });
    }

    private void handleParticleTrail(final Projectile projectile, final BlockPos position, final ServerLevel level) {
        if (particleTrail().isEmpty()) {
            return;
        }

        Vec3 trailMidpoint = position.getCenter().subtract(projectile.position()).scale(0.5).add(projectile.position());
        SyncParticleTrail packet = new SyncParticleTrail(projectile.position().toVector3f(), position.getCenter().toVector3f(), particleTrail().get());
        PacketDistributor.sendToPlayersNear(level, null, trailMidpoint.x, trailMidpoint.y, trailMidpoint.z, 64, packet);
    }

    private void handleParticleTrail(final Projectile projectile, final Entity entity, final ServerLevel level) {
        if (particleTrail().isEmpty()) {
            return;
        }

        Vec3 entityMidpoint = entity.position().add(0, entity.getEyeHeight() / 2, 0);
        Vec3 trailMidpoint = entityMidpoint.subtract(projectile.position()).scale(0.5).add(projectile.position());
        SyncParticleTrail packet = new SyncParticleTrail(projectile.position().toVector3f(), entityMidpoint.toVector3f(), particleTrail().get());
        PacketDistributor.sendToPlayersNear(level, null, trailMidpoint.x, trailMidpoint.y, trailMidpoint.z, 64, packet);
    }

    @Override
    public MutableComponent getDescription(final Player dragon, int level) {
        MutableComponent description = Component.translatable(AreaTarget.AREA_TARGET_ENTITY, AbilityTargeting.EntityTargetingMode.TARGET_ALL.translation(), radius().calculate(level));

        if (generalData.tickRate() > 1) {
            description.append(Component.translatable(LangKey.ABILITY_X_SECONDS, Functions.ticksToSeconds(generalData.tickRate())));
        }

        return description;
    }

    @Override
    public MapCodec<? extends ProjectileTargeting> codec() {
        return CODEC;
    }
}
