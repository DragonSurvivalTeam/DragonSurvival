package by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks;

import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ItemUpgrade;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;

import java.util.List;
import java.util.Optional;

public class StartWithWingsDataPack {

    public static void register(final BootstrapContext<DragonAbility> context) {
        Holder<DragonAbility> caveWings = context.lookup(DragonAbility.REGISTRY).getOrThrow(CaveDragonAbilities.CAVE_WINGS);
        context.register(caveWings.getKey(), new DragonAbility(
                caveWings.value().activation(),
                Optional.of(new ItemUpgrade(List.of(HolderSet.direct(DSItems.WING_GRANT_ITEM), HolderSet.direct(DSItems.SPIN_GRANT_ITEM)), HolderSet.empty())),
                caveWings.value().usageBlocked(),
                caveWings.value().actions(),
                caveWings.value().icon()
        ));
    }
}
