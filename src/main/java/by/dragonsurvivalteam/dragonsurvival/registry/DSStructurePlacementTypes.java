package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.AdvancedRandomSpread;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DSStructurePlacementTypes {
    public static final DeferredRegister<StructurePlacementType<?>> REGISTRY = DeferredRegister.create(Registries.STRUCTURE_PLACEMENT, DragonSurvival.MODID);

    public static final RegistryObject<StructurePlacementType<AdvancedRandomSpread>> ADVANCED_RANDOM_SPREAD = REGISTRY.register("advanced_random_spread", () -> () -> AdvancedRandomSpread.CODEC.codec());
}
