package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot.AddTableLootExtendedLootModifier;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot.DragonHeartLootModifier;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot.DragonOreLootModifier;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class DSLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, DragonSurvival.MODID);

    static {
        REGISTRY.register("dragon_ore", DragonOreLootModifier.CODEC);
        REGISTRY.register("dragon_heart", DragonHeartLootModifier.CODEC);
        REGISTRY.register("add_table_loot_extended", () -> AddTableLootExtendedLootModifier.CODEC);
    }
}
