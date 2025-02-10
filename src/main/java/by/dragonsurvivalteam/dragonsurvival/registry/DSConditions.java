package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.RegisteredCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class DSConditions {
    public static final DeferredRegister<MapCodec<? extends ICondition>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, DragonSurvival.MODID);
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<RegisteredCondition<?>>> REGISTERED = REGISTRY.register("registered", () -> RegisteredCondition.CODEC);

    public static final RegisteredCondition<DragonSpecies> CAVE_DRAGON_LOADED = new RegisteredCondition<>(BuiltInDragonSpecies.CAVE_DRAGON);
    public static final RegisteredCondition<DragonSpecies> FOREST_DRAGON_LOADED = new RegisteredCondition<>(BuiltInDragonSpecies.FOREST_DRAGON);
    public static final RegisteredCondition<DragonSpecies> SEA_DRAGON_LOADED = new RegisteredCondition<>(BuiltInDragonSpecies.SEA_DRAGON);
}
