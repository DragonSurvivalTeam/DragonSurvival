package by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade.Upgrade;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade.ValueBasedUpgrade;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargePoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallPoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BlockConversionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.BonemealEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.*;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.DragonBreathTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.Projectiles;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.Optional;

public class ForestDragonAbilities {
    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Shoot out sharp §cdarts§r, which fly a large distance to pierce your target. Less effective underwater.")
    @Translation(type = Translation.Type.ABILITY, comments = "Spike")
    public static final ResourceKey<DragonAbility> SPIKE = DragonAbilities.key("spike");

    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = {
            "■ Elemental breath: a toxic gas that creates a §c«Drain»§r area of effect, which is deadly for creatures, but helps plants grow faster.\n",
            "■ Range depends on the age of the dragon. Cannot be used while affected by §c«Stress»§r.",
    })
    @Translation(type = Translation.Type.ABILITY, comments = "Poison Breath")
    public static final ResourceKey<DragonAbility> POISON_BREATH = DragonAbilities.key("poison_breath");

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

        context.register(POISON_BREATH, new DragonAbility(
                new Activation(
                        Activation.Type.ACTIVE_CHANNELED,
                        Optional.empty(),
                        Optional.of(ManaCost.ticking(LevelBasedValue.constant(0.025f))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(1))),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(2))),
                        Optional.of(new Activation.Sound(
                                Optional.of(DSSounds.FOREST_BREATH_START.get()),
                                Optional.empty(),
                                Optional.of(DSSounds.FOREST_BREATH_LOOP.get()),
                                Optional.of(DSSounds.FOREST_BREATH_END.get())
                        )),
                        Optional.of(new Activation.Animations(
                                Optional.empty(),
                                Optional.of(new SimpleAbilityAnimation("breath", AnimationLayer.BREATH, 5, false, false)),
                                Optional.empty()
                        ))
                ),
                Optional.of(new Upgrade(Either.left(new ValueBasedUpgrade(ValueBasedUpgrade.Type.PASSIVE_LEVEL, 4, LevelBasedValue.lookup(List.of(0f, 10f, 30f, 50f), LevelBasedValue.perLevel(15)))))),
                Optional.of(EntityPredicate.Builder.entity().effects(MobEffectsPredicate.Builder.effects().and(DSEffects.STRESS)).build()),
                List.of(new ActionContainer(new DragonBreathTarget(Either.right(
                                new AbilityTargeting.EntityTargeting(
                                        Optional.of(List.of(Condition.living())),
                                        List.of(
                                                new DamageEffect(
                                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.POISON_BREATH),
                                                        LevelBasedValue.perLevel(2)
                                                ),
                                                new PotionEffect(
                                                        HolderSet.direct(DSEffects.DRAIN),
                                                        LevelBasedValue.constant(0),
                                                        LevelBasedValue.constant(Functions.secondsToTicks(10)),
                                                        LevelBasedValue.constant(0.3f)
                                                ),
                                                new ItemConversionEffect(
                                                        List.of(
                                                                new ItemConversionEffect.ItemConversionData(
                                                                        HolderSet.direct(context.lookup(Registries.ITEM).getOrThrow(Items.POTATO.builtInRegistryHolder().key())),
                                                                        List.of(
                                                                                new ItemConversionEffect.ItemTo(
                                                                                        context.lookup(Registries.ITEM).getOrThrow(Items.POISONOUS_POTATO.builtInRegistryHolder().key()),
                                                                                        1f
                                                                                )
                                                                        )
                                                                )
                                                        ),
                                                        LevelBasedValue.constant(0.5f)
                                                )
                                        ),
                                        AbilityTargeting.EntityTargetingMode.TARGET_ENEMIES
                                )
                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new DragonBreathTarget(Either.left(
                                new AbilityTargeting.BlockTargeting(
                                        Optional.empty(),
                                        List.of(new BonemealEffect(
                                                LevelBasedValue.constant(2),
                                                LevelBasedValue.perLevel(0.01f)
                                                ),
                                                new BlockConversionEffect(
                                                        List.of(
                                                                new BlockConversionEffect.BlockConversionData(
                                                                        HolderSet.direct(
                                                                                context.lookup(Registries.BLOCK).getOrThrow(Blocks.DIRT.builtInRegistryHolder().key()),
                                                                                context.lookup(Registries.BLOCK).getOrThrow(Blocks.COARSE_DIRT.builtInRegistryHolder().key())
                                                                        ),
                                                                        List.of(
                                                                                new BlockConversionEffect.BlockTo(
                                                                                        context.lookup(Registries.BLOCK).getOrThrow(Blocks.GRASS_BLOCK.builtInRegistryHolder().key()),
                                                                                        25f
                                                                                ),
                                                                                new BlockConversionEffect.BlockTo(
                                                                                        context.lookup(Registries.BLOCK).getOrThrow(Blocks.PODZOL.builtInRegistryHolder().key()),
                                                                                        5f
                                                                                ),
                                                                                new BlockConversionEffect.BlockTo(
                                                                                        context.lookup(Registries.BLOCK).getOrThrow(Blocks.MYCELIUM.builtInRegistryHolder().key()),
                                                                                        1f
                                                                                ),
                                                                                new BlockConversionEffect.BlockTo(
                                                                                        context.lookup(Registries.BLOCK).getOrThrow(Blocks.COARSE_DIRT.builtInRegistryHolder().key()),
                                                                                        3f
                                                                                )
                                                                        ))
                                                                ), LevelBasedValue.constant(0.2f))
                                                        )
                                                )
                                        ), LevelBasedValue.constant(1)), LevelBasedValue.constant(10)),
                        new ActionContainer(new SelfTarget(Either.right(
                                new AbilityTargeting.EntityTargeting(
                                        Optional.empty(),
                                        List.of(new BreathParticlesEffect(
                                                0.4f,
                                                0.02f,
                                                new SmallPoisonParticleOption(37, true),
                                                new LargePoisonParticleOption(37, false)
                                        )),
                                        AbilityTargeting.EntityTargetingMode.TARGET_ALL
                                )
                        ), false), LevelBasedValue.constant(1))),
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_1"), 1),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_2"), 2),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_3"), 3),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/forest/poisonous_breath_4"), 4)
                ))
        ));
    }
}
