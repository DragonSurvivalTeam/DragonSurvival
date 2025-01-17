package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.AdvancedRandomSpread;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DSStructurePlacementTypes {
    public static final DeferredRegister<StructurePlacementType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.STRUCTURE_PLACEMENT, DragonSurvival.MODID);

    public static final DeferredHolder<StructurePlacementType<?>, StructurePlacementType<AdvancedRandomSpread>> ADVANCED_RANDOM_SPREAD = REGISTRY.register("advanced_random_spread", () -> () -> AdvancedRandomSpread.CODEC);
}
