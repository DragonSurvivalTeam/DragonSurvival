package by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;

import java.util.Optional;

public class UnlockWingsDatapack {
    public static void register(final BootstrapContext<DragonAbility> context, final HolderLookup.RegistryLookup<DragonAbility> registryLookup) {
        handleWings(context, registryLookup.getOrThrow(CaveDragonAbilities.CAVE_WINGS));
        handleWings(context, registryLookup.getOrThrow(ForestDragonAbilities.FOREST_WINGS));
        handleWings(context, registryLookup.getOrThrow(SeaDragonAbilities.SEA_WINGS));
    }

    private static void handleWings(final BootstrapContext<DragonAbility> context, final Holder<DragonAbility> wings) {
        //noinspection DataFlowIssue -> key is present
        context.register(wings.getKey(), new DragonAbility(
                wings.value().activation(),
                Optional.empty(),
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.hasEffect(DSEffects.TRAPPED)),
                        Condition.thisEntity(EntityCondition.hasEffect(DSEffects.BROKEN_WINGS))
                ).build()),
                wings.value().actions(),
                wings.value().canBeManuallyDisabled(),
                wings.value().icon()
        ));
    }
}
