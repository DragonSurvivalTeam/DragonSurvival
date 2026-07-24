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
    @Translation(comments = "Dragon Species Sorting Order")
    public static final TagKey<DragonSpecies> ORDER = key("order");

    @Translation(comments = "No Species")
    public static final TagKey<DragonSpecies> NONE = key("none");
    @Translation(comments = "All Species")
    public static final TagKey<DragonSpecies> ALL = key("all");
    @Translation(comments = "True Dragons")
    public static final TagKey<DragonSpecies> TRUE_DRAGONS = key("true_dragons");

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
        tag(ORDER).add(BuiltInDragonSpecies.CAVE_DRAGON).add(BuiltInDragonSpecies.FOREST_DRAGON).add(BuiltInDragonSpecies.SEA_DRAGON);

        tag(ALL).add(BuiltInDragonSpecies.CAVE_DRAGON).add(BuiltInDragonSpecies.FOREST_DRAGON).add(BuiltInDragonSpecies.SEA_DRAGON);
        tag(NONE);

        tag(CAVE_DRAGONS).add(BuiltInDragonSpecies.CAVE_DRAGON);
        tag(FOREST_DRAGONS).add(BuiltInDragonSpecies.FOREST_DRAGON);
        tag(SEA_DRAGONS).add(BuiltInDragonSpecies.SEA_DRAGON);

        tag(TRUE_DRAGONS).add(BuiltInDragonSpecies.CAVE_DRAGON).add(BuiltInDragonSpecies.SEA_DRAGON).add(BuiltInDragonSpecies.FOREST_DRAGON);
    }

    public static TagKey<DragonSpecies> key(final String path) {
        return TagKey.create(DragonSpecies.REGISTRY, DragonSurvival.res(path));
    }
}
