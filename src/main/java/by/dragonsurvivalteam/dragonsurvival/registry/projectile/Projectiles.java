package by.dragonsurvivalteam.dragonsurvival.registry.projectile;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.SpawnParticles;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileDamageEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileLightningEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectilePotionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileAreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectilePointTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects.ProjectileExplosionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects.ProjectileWorldParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.LightningHandler;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public class Projectiles {
    @Translation(type = Translation.Type.PROJECTILE, comments = "Fireball")
    public static final ResourceKey<ProjectileData> FIREBALL = key("fireball");

    @Translation(type = Translation.Type.PROJECTILE, comments = "Spike")
    public static final ResourceKey<ProjectileData> SPIKE = key("spike");

    @Translation(type = Translation.Type.PROJECTILE, comments = "Ball Lightning")
    public static final ResourceKey<ProjectileData> BALL_LIGHTNING = key("ball_lightning");

    @Translation(type = Translation.Type.PROJECTILE, comments = "Bouncy Ball Lightning")
    public static final ResourceKey<ProjectileData> BOUNCY_BALL_LIGHTNING = key("bouncy_ball_lightning");

    public static void registerProjectiles(final BootstrapContext<ProjectileData> context) {
        context.register(FIREBALL, new ProjectileData(
                FIREBALL.location(),
                Either.left(
                        new ProjectileData.GenericBallData(
                                new ProjectileData.GenericBallResource(new LevelBasedResource(List.of(new LevelBasedResource.TextureEntry(FIREBALL.location(), 1)))),
                                Optional.of(ParticleTypes.LARGE_SMOKE),
                                List.of(new ProjectilePointTarget(new ProjectileTargeting.GeneralData(List.of(
                                        new ProjectileTargeting.ConditionalEffect(new ProjectileExplosionEffect(
                                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.FIREBALL),
                                                LevelBasedValue.perLevel(1),
                                                true,
                                                true,
                                                false
                                        ), Optional.empty())
                                ), 1, 1))),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(32),
                                LevelBasedValue.constant(100)
                        )
                ),
                Optional.of(EntityCondition.isLiving()),
                List.of(),
                List.of(),
                List.of(
                        new ProjectileDamageEffect(
                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.FIREBALL),
                                LevelBasedValue.perLevel(10, 9)
                        )
                ),
                List.of())
        );

        context.register(SPIKE, new ProjectileData(
                SPIKE.location(),
                Either.right(
                        new ProjectileData.GenericArrowData(
                                new LevelBasedResource(List.of(new LevelBasedResource.TextureEntry(SPIKE.location(), 1))),
                                LevelBasedValue.constant(3)
                        )
                ),
                Optional.of(EntityCondition.isLiving()),
                List.of(),
                List.of(),
                List.of(new ProjectileDamageEffect(
                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.SPIKE),
                        LevelBasedValue.perLevel(2)
                )),
                List.of()
        ));

        context.register(BALL_LIGHTNING, new ProjectileData(
                BALL_LIGHTNING.location(),
                Either.left(
                        new ProjectileData.GenericBallData(
                                new ProjectileData.GenericBallResource(new LevelBasedResource(List.of(new LevelBasedResource.TextureEntry(BALL_LIGHTNING.location(), 1)))),
                                Optional.empty(),
                                List.of(new ProjectilePointTarget(new ProjectileTargeting.GeneralData(List.of(
                                        new ProjectileTargeting.ConditionalEffect(new ProjectileExplosionEffect(
                                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.BALL_LIGHTNING),
                                                LevelBasedValue.perLevel(2, 1),
                                                false,
                                                true,
                                                false
                                        ), Optional.empty())
                                ), 1, 1))),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(100),
                                LevelBasedValue.constant(32),
                                LevelBasedValue.constant(100)
                        )
                ),
                Optional.of(EntityCondition.isLiving()),
                List.of(new ProjectileAreaTarget(
                                new ProjectileTargeting.GeneralData(List.of(
                                        new ProjectileTargeting.ConditionalEffect(
                                                new ProjectileDamageEffect(
                                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.BALL_LIGHTNING),
                                                        LevelBasedValue.perLevel(4)
                                                ),
                                                Optional.of(Condition.thisEntity(EntityCondition.isLiving()).build())
                                        ),
                                        new ProjectileTargeting.ConditionalEffect(
                                                new ProjectilePotionEffect(new PotionData(
                                                        HolderSet.direct(DSEffects.CHARGED),
                                                        LevelBasedValue.constant(0),
                                                        LevelBasedValue.constant(Functions.secondsToTicks(5)),
                                                        LevelBasedValue.constant(0.5f)
                                                )),
                                                Optional.of(Condition.thisEntity(EntityCondition.isLiving()).build())
                                        )
                                ), 5, 1),
                                LevelBasedValue.constant(4),
                                Optional.of(new LargeLightningParticleOption(37, false))
                        ),
                        new ProjectileAreaTarget(
                                new ProjectileTargeting.GeneralData(List.of(
                                        new ProjectileTargeting.ConditionalEffect(
                                                new ProjectileLightningEntityEffect(new LightningHandler.Data(true, true, false)),
                                                Optional.of(
                                                        Condition.thisEntity(EntityCondition.isLiving())
                                                                .and(Condition.thisEntity(EntityCondition.isInRain())).build()
                                                )
                                        )
                                ), 10, 0.1),
                                LevelBasedValue.constant(4),
                                Optional.empty()
                        )
                ),
                List.of(),
                List.of(),
                List.of())
        );

        context.register(BOUNCY_BALL_LIGHTNING, new ProjectileData(
                BALL_LIGHTNING.location(),
                Either.left(
                        new ProjectileData.GenericBallData(
                                new ProjectileData.GenericBallResource(new LevelBasedResource(List.of(new LevelBasedResource.TextureEntry(BALL_LIGHTNING.location(), 1)))),
                                Optional.empty(),
                                List.of(new ProjectilePointTarget(new ProjectileTargeting.GeneralData(List.of(
                                        new ProjectileTargeting.ConditionalEffect(new ProjectileExplosionEffect(
                                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.BALL_LIGHTNING),
                                                LevelBasedValue.perLevel(2, 1),
                                                false,
                                                true,
                                                false
                                        ), Optional.empty())
                                ), 1, 1))),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(5),
                                LevelBasedValue.constant(100),
                                LevelBasedValue.constant(32),
                                LevelBasedValue.constant(100)
                        )
                ),
                Optional.of(EntityCondition.isLiving()),
                List.of(new ProjectileAreaTarget(
                                new ProjectileTargeting.GeneralData(List.of(
                                        new ProjectileTargeting.ConditionalEffect(
                                                new ProjectileDamageEffect(
                                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.BALL_LIGHTNING),
                                                        LevelBasedValue.perLevel(4)
                                                ),
                                                Optional.of(Condition.thisEntity(EntityCondition.isLiving()).build())
                                        ),
                                        new ProjectileTargeting.ConditionalEffect(
                                                new ProjectilePotionEffect(new PotionData(
                                                        HolderSet.direct(DSEffects.CHARGED),
                                                        LevelBasedValue.constant(0),
                                                        LevelBasedValue.constant(Functions.secondsToTicks(5)),
                                                        LevelBasedValue.constant(0.5f)
                                                )),
                                                Optional.of(Condition.thisEntity(EntityCondition.isLiving()).build())
                                        )
                                ), 5, 1),
                                LevelBasedValue.constant(4),
                                Optional.of(new LargeLightningParticleOption(37, false))
                        ),
                        new ProjectileAreaTarget(
                                new ProjectileTargeting.GeneralData(List.of(new ProjectileTargeting.ConditionalEffect(
                                        new ProjectileLightningEntityEffect(new LightningHandler.Data(true, true, false)),
                                        Optional.of(
                                                Condition.thisEntity(EntityCondition.isLiving())
                                                        .and(Condition.thisEntity(EntityCondition.isInRain())).build()
                                        )
                                )), 10, 0.1),
                                LevelBasedValue.constant(4),
                                Optional.empty()
                        )
                ),
                List.of(
                        new ProjectilePointTarget(new ProjectileTargeting.GeneralData(List.of(
                                new ProjectileTargeting.ConditionalEffect(new ProjectileWorldParticleEffect(
                                        new SpawnParticles(new SmallLightningParticleOption(37, true), SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.1f)),
                                        LevelBasedValue.constant(10)
                                ), Optional.empty())), 1, 1)
                        )
                ),
                List.of(),
                List.of())
        );
    }

    private static ResourceKey<ProjectileData> key(final String path) {
        return key(DragonSurvival.res(path));
    }

    public static ResourceKey<ProjectileData> key(final ResourceLocation location) {
        return ResourceKey.create(ProjectileData.REGISTRY, location);
    }
}
