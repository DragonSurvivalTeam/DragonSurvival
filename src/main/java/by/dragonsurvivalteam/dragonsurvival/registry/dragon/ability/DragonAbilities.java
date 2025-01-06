package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.*;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.EffectModificationEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ModifierEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.SelfTarget;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;

public class DragonAbilities {
    public static final ResourceLocation GOOD_MANA_CONDITION = DragonSurvival.res("good_mana_condition");

    // FIXME :: test
    @Translation(type = Translation.Type.ABILITY_DESCRIPTION, comments = "■ Exists for testing purposes")
    @Translation(type = Translation.Type.ABILITY, comments = "Test Ability")
    public static final ResourceKey<DragonAbility> TEST = DragonAbilities.key("test");

    public static void registerAbilities(final BootstrapContext<DragonAbility> context) {
        CaveDragonAbilities.registerAbilities(context);
        ForestDragonAbilities.registerAbilities(context);
        SeaDragonAbilities.registerAbilities(context);

        context.register(TEST, new DragonAbility(
                Activation.passive(ManaCost.reserved(LevelBasedValue.constant(5))),
                Optional.empty(),
                Optional.of(Condition.thisEntity(EntityCondition.isInSunlight(8)).invert().build()),
                List.of(new ActionContainer(new SelfTarget(AbilityTargeting.entity(List.of(
                        new EffectModificationEffect(List.of(
                                new EffectModification(
                                        DragonSurvival.res("test"),
                                        HolderSet.direct(DSEffects.FIRE, MobEffects.MOVEMENT_SPEED),
                                        new Modification(Modification.ModificationType.MULTIPLICATIVE, LevelBasedValue.perLevel(1.5f)),
                                        new Modification(Modification.ModificationType.ADDITIVE, LevelBasedValue.perLevel(2)),
                                        LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)),
                                new EffectModification(
                                        DragonSurvival.res("test.two"),
                                        HolderSet.direct(MobEffects.HUNGER, MobEffects.POISON),
                                        new Modification(Modification.ModificationType.ADDITIVE, LevelBasedValue.perLevel(-Functions.secondsToTicks(10))),
                                        new Modification(Modification.ModificationType.MULTIPLICATIVE, LevelBasedValue.perLevel(0.2f, -0.05f)),
                                        LevelBasedValue.constant(DurationInstance.INFINITE_DURATION))
                        )),
                        new ModifierEffect(List.of(new ModifierWithDuration(
                                DragonSurvival.res("test"),
                                DragonSurvival.res("test"),
                                List.of(
                                        Modifier.constant(Attributes.MOVEMENT_SPEED, 0.015f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                                        Modifier.per(Attributes.MOVEMENT_SPEED, -0.00012f, AttributeModifier.Operation.ADD_VALUE),
                                        Modifier.constant(DSAttributes.MANA, 5, AttributeModifier.Operation.ADD_VALUE)
                                ),
                                LevelBasedValue.constant(DurationInstance.INFINITE_DURATION),
                                false
                        )))
                ), AbilityTargeting.EntityTargetingMode.TARGET_ALLIES), true), LevelBasedValue.constant(1))),
                true,
                new LevelBasedResource(List.of(
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/test_0"), 0),
                        new LevelBasedResource.TextureEntry(DragonSurvival.res("abilities/test_1"), 1)
                ))
        ));
    }

    public static ResourceKey<DragonAbility> key(final ResourceLocation location) {
        return ResourceKey.create(DragonAbility.REGISTRY, location);
    }

    public static ResourceKey<DragonAbility> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
