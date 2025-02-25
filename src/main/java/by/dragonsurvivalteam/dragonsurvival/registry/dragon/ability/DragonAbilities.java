package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.PassiveActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnDeath;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.OnTargetKilled;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.HealEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.SmeltItemEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AreaTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.TargetingMode;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    public static final LevelBasedValue INFINITE_DURATION = LevelBasedValue.constant(DurationInstance.INFINITE_DURATION);

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res("test_smelt")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.empty(), new OnTargetKilled(Optional.empty())),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new AreaTarget(AbilityTargeting.entity(List.of(
                                new SmeltItemEffect(Optional.empty(), Optional.empty(), true)
                        ), TargetingMode.ITEMS), LevelBasedValue.constant(5)), LevelBasedValue.constant(1))
                ),
                true,
                new LevelBasedResource(List.of(new LevelBasedResource.Entry(DragonSurvival.res("test"), 0)))
        ));

        context.register(ResourceKey.create(DragonAbility.REGISTRY, DragonSurvival.res("test_heal")), new DragonAbility(
                new PassiveActivation(Optional.empty(), Optional.of(LevelBasedValue.constant(Functions.secondsToTicks(30))), OnDeath.INSTANCE),
                Optional.empty(),
                Optional.empty(),
                List.of(
                        new ActionContainer(new SelfTarget(AbilityTargeting.entity(List.of(
                                new HealEffect(LevelBasedValue.constant(0.3f))
                        ), TargetingMode.ALLIES_AND_SELF)), LevelBasedValue.constant(1))
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
