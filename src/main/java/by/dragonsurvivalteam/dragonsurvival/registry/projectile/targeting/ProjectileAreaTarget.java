package by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting;

import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncParticleTrail;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public record ProjectileAreaTarget(Either<Either<ProjectileTargeting.BlockTargeting, ProjectileTargeting.EntityTargeting>, ProjectileTargeting.WorldTargeting> target, LevelBasedValue radius, Optional<ParticleOptions> particleTrail) implements ProjectileTargeting {
    public static final MapCodec<ProjectileAreaTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.either(Codec.either(BlockTargeting.CODEC, EntityTargeting.CODEC), WorldTargeting.CODEC).fieldOf("target").forGetter(ProjectileAreaTarget::target),
            LevelBasedValue.CODEC.fieldOf("radius").forGetter(ProjectileAreaTarget::radius),
            ParticleTypes.CODEC.optionalFieldOf("particle_trail").forGetter(ProjectileAreaTarget::particleTrail)
    ).apply(instance, ProjectileAreaTarget::new));

    public void apply(final Projectile projectile, final int projectileLevel) {
        if(target.right().isPresent()) {
            throw new IllegalStateException("Area target must be a block or entity target");
        }

        double radius = radius().calculate(projectileLevel);
        ServerLevel level = (ServerLevel) projectile.level();
        Vec3 position = projectile.position();

        target().ifLeft(blockOrEntityTarget -> {
            blockOrEntityTarget.ifLeft(blockTarget -> {
                if (level.getGameTime() % blockTarget.tickRate() == 0) {
                    BlockPos.betweenClosedStream(AABB.ofSize(position, radius * 2, radius * 2, radius * 2)).forEach(blockPos -> {
                        if (blockTarget.targetConditions().isEmpty() || blockTarget.targetConditions().get().matches(level, blockPos)
                                && blockTarget.weatherConditions().isEmpty() || blockTarget.weatherConditions().get().matches(level, position)
                                && blockTarget.randomCondition().isEmpty() || blockTarget.randomCondition().get().matches(level, projectileLevel)) {
                            blockTarget.effects().forEach(effect -> effect.apply(projectile, blockPos, projectileLevel));
                            if (particleTrail().isPresent()) {
                                Vec3 trailMidpoint = blockPos.getCenter().subtract(position).scale(0.5).add(position);
                                PacketDistributor.sendToPlayersNear(
                                        level,
                                        null,
                                        trailMidpoint.x,
                                        trailMidpoint.y,
                                        trailMidpoint.z,
                                        64,
                                        new SyncParticleTrail(position.toVector3f(), blockPos.getCenter().toVector3f(), particleTrail().get()));
                            }
                        }
                    });
                }
            }).ifRight(entityTarget -> {
                        if (level.getGameTime() % entityTarget.tickRate() == 0) {
                            // TODO :: use Entity.class (would affect items etc.)?
                            level.getEntities(EntityTypeTest.forClass(LivingEntity.class), AABB.ofSize(position, radius * 2, radius * 2, radius * 2),
                                    entity -> entityTarget.targetConditions().isEmpty() || entityTarget.targetConditions().get().matches(level, position, entity)
                                            && entityTarget.randomCondition().isEmpty() || entityTarget.randomCondition().get().matches(level, projectileLevel)
                            ).forEach(entity -> {
                                entityTarget.effects().forEach(effect -> effect.apply(projectile, entity, projectileLevel));
                                if (particleTrail().isPresent()) {
                                    Vec3 trailMidpoint = entity.position().subtract(position).scale(0.5).add(position);
                                    PacketDistributor.sendToPlayersNear(
                                            level,
                                            null,
                                            trailMidpoint.x,
                                            trailMidpoint.y,
                                            trailMidpoint.z,
                                            64,
                                            new SyncParticleTrail(position.toVector3f(), entity.position().toVector3f(), particleTrail().get()));
                                }
                            });
                        }
                    }
            );
        });
    }

    @Override
    public MutableComponent getDescription(final Player dragon, int level) {
        MutableComponent description = Component.translatable(LangKey.ABILITY_TO_TARGET_AREA, AbilityTargeting.EntityTargetingMode.TARGET_ALL.translation(), radius().calculate(level));

        if (tickRate() > 1) {
            description.append(Component.translatable(LangKey.ABILITY_X_SECONDS, tickRate() / 20f));
        }

        return description;
    }

    @Override
    public MapCodec<? extends ProjectileTargeting> codec() {
        return CODEC;
    }
}
