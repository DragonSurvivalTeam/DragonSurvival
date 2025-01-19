package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSDragonSpeciesTags extends TagsProvider<DragonSpecies> {
    @Translation(comments = "No Dragon Species")
    public static final TagKey<DragonSpecies> NONE = key("none");
    @Translation(comments = "All Dragon Species")
    public static final TagKey<DragonSpecies> ALL = key("all");

    @Translation(comments = "Cave Dragons")
    public static final TagKey<DragonSpecies> CAVE_DRAGONS = key("cave_dragons");
    @Translation(comments = "Forest Dragons")
    public static final TagKey<DragonSpecies> FOREST_DRAGONS = key("forest_dragons");
    @Translation(comments = "Sea Dragons")
    public static final TagKey<DragonSpecies> SEA_DRAGONS = key("sea_dragons");

    public DSDragonSpeciesTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, DragonSpecies.REGISTRY, provider, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(ALL).add(BuiltInDragonSpecies.CAVE).add(BuiltInDragonSpecies.FOREST).add(BuiltInDragonSpecies.SEA);
        tag(NONE);

        tag(CAVE_DRAGONS).add(BuiltInDragonSpecies.CAVE);
        tag(FOREST_DRAGONS).add(BuiltInDragonSpecies.FOREST);
        tag(SEA_DRAGONS).add(BuiltInDragonSpecies.SEA);
    }

    public static TagKey<DragonSpecies> key(final String path) {
        return TagKey.create(DragonSpecies.REGISTRY, DragonSurvival.res(path));
    }
}
