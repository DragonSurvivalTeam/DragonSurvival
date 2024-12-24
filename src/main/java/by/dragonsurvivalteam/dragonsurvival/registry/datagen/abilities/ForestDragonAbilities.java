package by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade.Upgrade;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade.ValueBasedUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ProjectileEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public class ForestDragonAbilities {
    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Shoot out sharp §cdarts§r, which fly a large distance to pierce your target. Less effective underwater.")
    @Translation(type = Translation.Type.ABILITY, comments = "Spike")
    public static final ResourceKey<DragonAbility> SPIKE = DragonAbilities.key("spike");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        registerActiveAbilities(context);
    }

    private static void registerActiveAbilities(final BootstrapContext<DragonAbility> context) {
        context.register(SPIKE, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_SIMPLE,
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.empty(),
                        Optional.empty(),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(3))),
                        Optional.of(new Activation.Sound(
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(SoundEvents.ARROW_SHOOT)
                        )),
                        Optional.empty()
                ),
                Optional.of(
                        new Upgrade(
                                Either.left(
                                        new ValueBasedUpgrade(
                                                ValueBasedUpgrade.Type.PASSIVE_LEVEL,
                                                4,
                                                LevelBasedValue.lookup(List.of(0f, 20f, 30f, 40f), LevelBasedValue.perLevel(15))
                                        )
                                )
                        )
                ),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(Either.right(
                        new AbilityTargeting.EntityTargeting(
                                Optional.of(List.of(Condition.living())),
                                List.of(new ProjectileEffect(
                                        context.lookup(ProjectileData.REGISTRY).getOrThrow(Projectiles.SPIKE),
                                        TargetDirection.lookingAt(),
                                        LevelBasedValue.perLevel(1),
                                        LevelBasedValue.constant(1.5f),
                                        LevelBasedValue.constant(1)
                                )),
                                AbilityTargeting.EntityTargetingMode.TARGET_ALL
                        )
                ), false), LevelBasedValue.constant(1))),
                new LevelBasedResource(
                        List.of(
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_0"), 0),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_1"), 1),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_2"), 2),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_3"), 3),
                                new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/spike_4"), 4)
                        )
                )
        ));
    }
}
