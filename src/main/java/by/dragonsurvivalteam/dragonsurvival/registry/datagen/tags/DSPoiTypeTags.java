package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTrades;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.tags.PoiTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DSPoiTypeTags extends PoiTypeTagsProvider {
    public DSPoiTypeTags(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pProvider, DragonSurvivalMod.MODID, existingFileHelper);
    }

    @Override
    @SuppressWarnings("DataFlowIssue") // resource key won't be null
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(DSTrades.DRAGON_RIDER_POI.getKey());
    }
}
