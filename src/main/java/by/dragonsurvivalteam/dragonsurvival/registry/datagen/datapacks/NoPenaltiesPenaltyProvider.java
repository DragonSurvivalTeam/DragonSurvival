package by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonPenaltyTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class NoPenaltiesPenaltyProvider extends KeyTagProvider<DragonPenalty> {

    public NoPenaltiesPenaltyProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, DragonPenalty.REGISTRY, lookupProvider, DragonSurvival.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(DSDragonPenaltyTags.CAVE).replace(true);
        tag(DSDragonPenaltyTags.SEA).replace(true);
        tag(DSDragonPenaltyTags.FOREST).replace(true);
    }
}
