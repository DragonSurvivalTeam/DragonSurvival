package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.VillagerTradeTags;
import net.minecraft.world.item.trading.VillagerTrade;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DSVillagerTradeTags extends KeyTagProvider<VillagerTrade> {
    public DSVillagerTradeTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.VILLAGER_TRADE, lookupProvider, DragonSurvival.MODID);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(VillagerTradeTags.CARTOGRAPHER_LEVEL_2).add(DSTrades.CARTOGRAPHER_DRAGON_HUNTER_MAP);
    }
}
