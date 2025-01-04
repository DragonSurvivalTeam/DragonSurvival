package by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks;

import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ItemUpgrade;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;

import java.util.List;
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
                Optional.of(new ItemUpgrade(List.of(HolderSet.empty(), HolderSet.direct(DSItems.SPIN_GRANT_ITEM)), HolderSet.empty())),
                wings.value().usageBlocked(),
                wings.value().actions(),
                wings.value().icon()
        ));
    }
}
