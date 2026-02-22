package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DSPoiTypeTags extends PoiTypeTagsProvider {
    public DSPoiTypeTags(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider) {
        super(pOutput, pProvider, DragonSurvival.MODID);
    }

    @Override
    @SuppressWarnings("DataFlowIssue") // resource key won't be null
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(DSTrades.DRAGON_RIDER_POI.getKey());
    }
}
