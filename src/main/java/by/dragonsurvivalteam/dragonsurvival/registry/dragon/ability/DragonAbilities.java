package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Glow;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ParticleData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.SpawnParticles;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationKey;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.AnimationLayer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation.SimpleAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.ItemCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonAbilityTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Animations;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Notification;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.PassiveActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.SimpleActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Sound;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnDeath;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnKeyPressed;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnSelfHit;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnTargetHit;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnTargetKilled;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.ExplodeBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.ParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.RunFunctionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.BlockVisionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.CooldownRecoveryEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.DamageEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ExperienceEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.GlowEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.HealEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ItemConversionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SmeltItemEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.TeleportEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.TargetingMode;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperienceLevelUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperiencePointsUpgrade;
import by.dragonsurvivalteam.dragonsurvival.util.Expression;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    public static final LevelBasedValue INFINITE_DURATION = LevelBasedValue.constant(DurationInstance.INFINITE_DURATION);
    public static final String TEST_PREFIX = "test_";

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);

        // --- Ore Vision (Treasure Shader) --- //

        //noinspection DataFlowIssue -> ChatFormatting entries exist
        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "ore_vision_treasure_shader")), new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(List.of(
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("plant_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(HolderSet.direct(
                                                        Blocks.TALL_GRASS.builtInRegistryHolder(),
                                                        Blocks.FERN.builtInRegistryHolder(),
                                                        Blocks.LILY_PAD.builtInRegistryHolder(),
                                                        Blocks.KELP_PLANT.builtInRegistryHolder()
                                                ))
                                                .range(LevelBasedValue.constant(36))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(
                                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_BLUE),
                                                        TextColor.fromLegacyFormat(ChatFormatting.BLUE),
                                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_AQUA),
                                                        TextColor.fromLegacyFormat(ChatFormatting.AQUA),
                                                        TextColor.fromLegacyFormat(ChatFormatting.GREEN),
                                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN)
                                                ))
                                                .colorShiftRate(0.7)
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("diamond_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_DIAMOND))
                                                .range(LevelBasedValue.constant(36))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(
                                                        TextColor.fromLegacyFormat(ChatFormatting.GOLD),
                                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_PURPLE),
                                                        TextColor.fromLegacyFormat(ChatFormatting.GREEN),
                                                        TextColor.fromLegacyFormat(ChatFormatting.RED),
                                                        TextColor.fromLegacyFormat(ChatFormatting.BLUE)
                                                ))
                                                .colorShiftRate(0.3)
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("lapis_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_LAPIS))
                                                .range(LevelBasedValue.constant(16))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(TextColor.fromLegacyFormat(ChatFormatting.BLUE)))
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("gold_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_GOLD))
                                                .range(LevelBasedValue.constant(22))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(TextColor.fromLegacyFormat(ChatFormatting.GOLD)))
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("redstone_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_REDSTONE))
                                                .range(LevelBasedValue.constant(16))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(TextColor.fromLegacyFormat(ChatFormatting.DARK_RED)))
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("coal_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_COAL))
                                                .range(LevelBasedValue.constant(16))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(TextColor.fromLegacyFormat(ChatFormatting.BLACK)))
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("emerald_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_EMERALD))
                                                .range(LevelBasedValue.constant(26))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(TextColor.fromLegacyFormat(ChatFormatting.GREEN)))
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("quartz_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_QUARTZ))
                                                .range(LevelBasedValue.constant(12))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(
                                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_RED),
                                                        TextColor.fromLegacyFormat(ChatFormatting.GRAY)
                                                ))
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("copper_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_COPPER))
                                                .range(LevelBasedValue.constant(16))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(
                                                        TextColor.fromLegacyFormat(ChatFormatting.GOLD),
                                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_GREEN)
                                                ))
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("netherite_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_NETHERITE_SCRAP))
                                                .range(LevelBasedValue.constant(36))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(
                                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_AQUA),
                                                        TextColor.fromLegacyFormat(ChatFormatting.BLACK),
                                                        TextColor.fromLegacyFormat(ChatFormatting.AQUA),
                                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY)
                                                ))
                                                .colorShiftRate(3)
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("iron_vision_treasure_shader"))
                                                                .hidden().build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(Tags.Blocks.ORES_IRON))
                                                .range(LevelBasedValue.constant(22))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(
                                                        TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY),
                                                        TextColor.fromLegacyFormat(ChatFormatting.GRAY),
                                                        TextColor.fromLegacyFormat(ChatFormatting.WHITE)
                                                ))
                                                .build()
                                ),
                                BlockVisionEffect.single(BlockVision.create(
                                                        DurationInstanceBase.create(DragonSurvival.res("general_ore_vision_treasure_shader"))
                                                                .customIcon(DragonSurvival.res("textures/ability_effect/general_ore_vision.png")).build()
                                                )
                                                .blocks(context.lookup(Registries.BLOCK).getOrThrow(DSBlockTags.GENERAL_ORES))
                                                .range(LevelBasedValue.constant(16))
                                                .displayType(BlockVision.DisplayType.TREASURE_SHADER)
                                                .colors(List.of(TextColor.fromLegacyFormat(ChatFormatting.WHITE)))
                                                .build()
                                )
                        ), TargetingMode.ALLIES_AND_SELF)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Key Pressed --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "on_key_pressed")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.empty(), OnKeyPressed.create("key.keyboard.g")),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.block(List.of(
                                new ExplodeBlockEffect(
                                        LevelBasedValue.constant(1),
                                        LevelBasedValue.constant(3),
                                        false,
                                        context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.EXPLOSION)
                                )
                        ))), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Damage Expression --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "damage_expression")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.empty(), new OnTargetHit(Optional.empty())),
                Optional.of(new ExperiencePointsUpgrade(5, LevelBasedValue.constant(10))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                new DamageEffect(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.GENERIC), LevelBasedValue.perLevel(1), Attributes.ATTACK_DAMAGE, new Expression("amount * scale * 10"), true)
                        ), TargetingMode.ENEMIES), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Cooldown --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "cooldown_recovery")), new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(15)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(5))),
                        Notification.DEFAULT,
                        true,
                        Optional.empty(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MAGIC_ALT, AnimationLayer.BASE).transitionLength(5).build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MAGIC_ALT, AnimationLayer.BASE).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(12f, 24f, 36f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(List.of(
                        new CooldownRecoveryEffect(
                                Optional.of(context.lookup(DragonAbility.REGISTRY).getOrThrow(DSDragonAbilityTags.CAVE)),
                                CooldownRecoveryEffect.ActionType.REDUCE,
                                CooldownRecoveryEffect.AdjustmentType.PERCENT,
                                LevelBasedValue.perLevel(0.1f, 0.05f),
                                LevelBasedValue.constant(1),
                                true
                        )
                ), TargetingMode.ALLIES_AND_SELF)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- experience --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "experience_points_add")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.empty(), new OnTargetKilled(Optional.empty())),
                Optional.of(new ExperiencePointsUpgrade(5, LevelBasedValue.constant(10))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                new ExperienceEffect(ExperienceEffect.ExperienceType.POINTS, LevelBasedValue.perLevel(25, 15), LevelBasedValue.perLevel(0.25f, 0.15f))
                        ), TargetingMode.ALLIES_AND_SELF), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Glow --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "glow")), new DragonAbility(
                PassiveActivation.DEFAULT,
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(GlowEffect.only(
                                Glow.create(DragonSurvival.res("glow_test"), TextColor.fromLegacyFormat(ChatFormatting.AQUA))
                        ), TargetingMode.ITEMS), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Smelt --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "smelt")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.empty(), new OnTargetKilled(Optional.empty())),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                new SmeltItemEffect(Optional.empty(), Optional.empty(), true)
                        ), TargetingMode.ITEMS), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Heal --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "heal")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))), OnDeath.INSTANCE),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(List.of(
                                new HealEffect(LevelBasedValue.constant(0.3f))
                        ), TargetingMode.ALLIES_AND_SELF)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Function --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "function")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(5))), new OnSelfHit(Optional.empty())),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(List.of(
                                new RunFunctionEffect(DragonSurvival.res("test"))
                        ), TargetingMode.ALLIES_AND_SELF)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Teleport --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "teleport")), new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(15)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(5))),
                        Notification.DEFAULT,
                        true,
                        Sound.create().end(SoundEvents.PLAYER_TELEPORT).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MAGIC_ALT, AnimationLayer.BASE).transitionLength(5).build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MAGIC_ALT, AnimationLayer.BASE).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(12f, 24f, 36f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                                List.of(
                                        new TeleportEffect(
                                                TargetDirection.lookingAt(),
                                                LevelBasedValue.perLevel(100, 50)
                                        )), TargetingMode.ALL_EXCEPT_SELF
                        ), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1)),
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(
                                List.of(
                                        new TeleportEffect(
                                                TargetDirection.lookingAt(),
                                                LevelBasedValue.perLevel(100, 50)
                                        ),
                                        new ParticleEffect(
                                                new SpawnParticles(ParticleTypes.PORTAL, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.05f)),
                                                LevelBasedValue.constant(20)
                                        )), TargetingMode.ALL)
                        ), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1)
                        )),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "teleport_directional")), new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(15)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(5))),
                        Notification.DEFAULT,
                        true,
                        Sound.create().end(SoundEvents.PLAYER_TELEPORT).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MAGIC_ALT, AnimationLayer.BASE).transitionLength(5).build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MAGIC_ALT, AnimationLayer.BASE).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(12f, 24f, 36f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                                List.of(
                                        new TeleportEffect(
                                                TargetDirection.of(Direction.UP),
                                                LevelBasedValue.perLevel(10, 5)
                                        ),
                                        new ParticleEffect(
                                                new SpawnParticles(ParticleTypes.PORTAL, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.05f)),
                                                LevelBasedValue.constant(20)
                                        )), TargetingMode.ALL_EXCEPT_SELF
                        ), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Summon --- //

        CompoundTag beeNBT = new CompoundTag();
        beeNBT.putInt("CannotEnterHiveTicks", Integer.MAX_VALUE);

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "summon")), new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(15)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(5))),
                        Notification.DEFAULT,
                        true,
                        Sound.create().end(SoundEvents.EVOKER_PREPARE_SUMMON).optional(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MAGIC_ALT, AnimationLayer.BASE).transitionLength(5).build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MAGIC_ALT, AnimationLayer.BASE).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(12f, 24f, 36f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.block(
                                List.of(
                                        new SummonEntityEffect(
                                                DurationInstanceBase.create(DragonSurvival.res(TEST_PREFIX + "summon")).duration(LevelBasedValue.constant(Functions.secondsToTicks(60))).build(),
                                                Either.right(HolderSet.direct(EntityType.BEE.builtInRegistryHolder())),
                                                LevelBasedValue.constant(6),
                                                List.of(),
                                                Optional.of(beeNBT),
                                                true
                                        ))
                        ), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        // --- Item Conversion --- //

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res(TEST_PREFIX + "convert_items")), new DragonAbility(
                new SimpleActivation(
                        Optional.of(LevelBasedValue.constant(1)),
                        Optional.of(LevelBasedValue.constant(15)),
                        Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(5))),
                        Notification.DEFAULT,
                        true,
                        Optional.empty(),
                        Animations.create()
                                .startAndCharging(SimpleAbilityAnimation.create(AnimationKey.CAST_MAGIC_ALT, AnimationLayer.BASE).transitionLength(5).build())
                                .end(SimpleAbilityAnimation.create(AnimationKey.MAGIC_ALT, AnimationLayer.BASE).build())
                                .optional()
                ),
                Optional.of(new ExperienceLevelUpgrade(3, LevelBasedValue.lookup(List.of(12f, 24f, 36f), LevelBasedValue.perLevel(15)))),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(
                                List.of(
                                        new ItemConversionEffect(
                                                List.of(
                                                        new ItemConversionEffect.ItemConversionData(ItemCondition.is(Items.IRON_INGOT), WeightedRandomList.create(
                                                                ItemConversionEffect.ItemTo.of(
                                                                        Items.GOLD_INGOT,
                                                                        12,
                                                                        1,
                                                                        new ParticleData(
                                                                                new SpawnParticles(ParticleTypes.SOUL, SpawnParticles.inBoundingBox(), SpawnParticles.inBoundingBox(), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), SpawnParticles.fixedVelocity(ConstantFloat.of(0.05f)), ConstantFloat.of(0.05f)),
                                                                                LevelBasedValue.constant(20)
                                                                        )
                                                                )
                                                        ))
                                                ),
                                                LevelBasedValue.constant(1)
                                        )
                                ), TargetingMode.ITEMS
                        ), LevelBasedValue.constant(5)), ActionContainer.TriggerPoint.DEFAULT, LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));
    }

    public static ResourceKey<DragonAbility> key(final ResourceLocation location) {
        return ResourceKey.create(DragonAbility.REGISTRY, location);
    }

    public static ResourceKey<DragonAbility> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
