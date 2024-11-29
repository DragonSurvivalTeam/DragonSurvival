package by.dragonsurvivalteam.dragonsurvival.registry.projectile;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.RandomPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.WeatherPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileDamageEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileLightningEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileMobEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileAreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectilePointTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects.ProjectileExplosionEffect;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.LightningHandler;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

    public static void registerProjectiles(final BootstrapContext<ProjectileData> context) {
        context.register(FIREBALL, new ProjectileData(
                FIREBALL.location(),
                Either.left(
                        new ProjectileData.GenericBallData(
                                new ProjectileData.GenericBallResource(new LevelBasedResource(List.of(new LevelBasedResource.TextureEntry(FIREBALL.location(), 1)))),
                                Optional.of(ParticleTypes.LARGE_SMOKE),
                                List.of(new ProjectilePointTarget(
                                        new ProjectileTargeting.WorldTargeting(
                                                Optional.empty(),
                                                Optional.empty(),
                                                Optional.empty(),
                                                List.of(new ProjectileExplosionEffect(
                                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.FIREBALL),
                                                        LevelBasedValue.perLevel(1),
                                                        true,
                                                        true,
                                                        false
                                                )),
                                                1)
                                        )
                                ),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(0),
                                LevelBasedValue.constant(32),
                                LevelBasedValue.constant(100)
                        )
                ),
                Optional.empty(),
                List.of(),
                List.of(),
                List.of(
                        new ProjectileDamageEffect(
                                context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.FIREBALL),
                                LevelBasedValue.perLevel(5)
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
                Optional.empty(),
                List.of(),
                List.of(),
                List.of(new ProjectileDamageEffect(
                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.FOREST_DRAGON_SPIKE),
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
                                List.of(new ProjectilePointTarget(
                                        new ProjectileTargeting.WorldTargeting(
                                                Optional.empty(),
                                                Optional.empty(),
                                                Optional.empty(),
                                                List.of(new ProjectileExplosionEffect(
                                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.DRAGON_BALL_LIGHTNING),
                                                        LevelBasedValue.perLevel(2, 1),
                                                        false,
                                                        true,
                                                        false
                                                )),
                                                1)
                                        )
                                ),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(1f),
                                LevelBasedValue.constant(100),
                                LevelBasedValue.constant(32),
                                LevelBasedValue.constant(100)
                        )
                ),
                Optional.empty(),
                List.of(new ProjectileAreaTarget(
                            Either.right(
                                new ProjectileTargeting.EntityTargeting(
                                    Optional.empty(),
                                    Optional.empty(),
                                    Optional.empty(),
                                    List.of(
                                            new ProjectileDamageEffect(
                                                    context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.DRAGON_BALL_LIGHTNING),
                                                    LevelBasedValue.perLevel(4)
                                            ),
                                            new ProjectileMobEffect(
                                                    HolderSet.direct(context.lookup(Registries.MOB_EFFECT).getOrThrow(DSEffects.CHARGED.getKey())),
                                                    LevelBasedValue.constant(0),
                                                    LevelBasedValue.constant(5),
                                                    LevelBasedValue.constant(0.5f)
                                            )
                                    ),
                                    5
                                )
                            ),
                            LevelBasedValue.constant(4),
                            Optional.of(new LargeLightningParticleOption(37, false))
                        ),
                        new ProjectileAreaTarget(
                                Either.right(
                                        new ProjectileTargeting.EntityTargeting(
                                                Optional.of(EntityPredicate.Builder.entity().located(LocationPredicate.Builder.location().setCanSeeSky(true)).build()),
                                                Optional.of(new WeatherPredicate(
                                                        Optional.empty(),
                                                        Optional.of(true)
                                                )),
                                                Optional.of(new RandomPredicate(
                                                        LevelBasedValue.constant(0.3f)
                                                )),
                                                List.of(
                                                        new ProjectileLightningEntityEffect(
                                                                new LightningHandler.Data(
                                                                        true,
                                                                        true,
                                                                        false
                                                                )
                                                        )
                                                ),
                                                5
                                        )
                                ),
                                LevelBasedValue.constant(4),
                                Optional.empty()
                        )
                ),
                List.of(),
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