package by.dragonsurvivalteam.dragonsurvival.registry.datagen.compat;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.ModCompat;
import by.dragonsurvivalteam.dragonsurvival.compat.ModCheck;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.BlockLootTableSubProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// TODO :: Can probably have a generic parent where we only set the compat id here and check that within the parent
public class SilentGemsDatapack extends BlockLootSubProvider {
    public SilentGemsDatapack(HolderLookup.Provider provider) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        DSBlocks.REGISTRY.getEntries().forEach((key) -> {
            if (key.get() instanceof ModCompat compat && ModCheck.SILENTGEMS.equals(compat.getCompatId())) {
                add(key.get(), BlockLootTableSubProvider.createTreasureBlockLoot(key.get()));
            }
        });
    }

    @Override
    protected @NotNull Iterable<Block> getKnownBlocks() {
        return DSBlocks.REGISTRY.getEntries().stream().map(DeferredHolder::get)
                .filter(block -> block instanceof ModCompat compat && ModCheck.SILENTGEMS.equals(compat.getCompatId()))
                .collect(Collectors.toList());
    }

    public static class Provider extends LootTableProvider {
        public Provider(final PackOutput output, final Set<ResourceKey<LootTable>> requiredTables, final List<SubProviderEntry> subProviders, final CompletableFuture<HolderLookup.Provider> registries) {
            super(output, requiredTables, subProviders, registries);
        }

        @Override
        public final @NotNull String getName() {
            return "Compatibility Loot Tables: Silent Gems";
        }
    }
}
