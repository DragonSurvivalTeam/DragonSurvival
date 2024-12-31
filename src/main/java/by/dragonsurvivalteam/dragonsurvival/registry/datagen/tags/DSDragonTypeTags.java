package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
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

public class DSDragonTypeTags extends TagsProvider<DragonSpecies> {
    public static final TagKey<DragonSpecies> NONE = key("none");
    public static final TagKey<DragonSpecies> ALL = key("all");

    public static final TagKey<DragonSpecies> CAVE = key("cave");
    public static final TagKey<DragonSpecies> FOREST = key("forest");
    public static final TagKey<DragonSpecies> SEA = key("sea");

    public DSDragonTypeTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, DragonSpecies.REGISTRY, provider, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(ALL).add(BuiltInDragonSpecies.CAVE).add(BuiltInDragonSpecies.FOREST).add(BuiltInDragonSpecies.SEA);
        tag(NONE);

        tag(CAVE).add(BuiltInDragonSpecies.CAVE);
        tag(FOREST).add(BuiltInDragonSpecies.FOREST);
        tag(SEA).add(BuiltInDragonSpecies.SEA);
    }

    public static TagKey<DragonSpecies> key(final String path) {
        return TagKey.create(DragonSpecies.REGISTRY, DragonSurvival.res(path));
    }
}
