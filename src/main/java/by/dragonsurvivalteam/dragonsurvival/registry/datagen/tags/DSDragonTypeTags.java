package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSDragonTypeTags extends TagsProvider<DragonType> {
    public static final TagKey<DragonType> NONE = key("none");
    public static final TagKey<DragonType> ALL = key("all");

    public static final TagKey<DragonType> CAVE = key("cave");
    public static final TagKey<DragonType> FOREST = key("forest");
    public static final TagKey<DragonType> SEA = key("sea");

    public DSDragonTypeTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, DragonType.REGISTRY, provider, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(ALL).add(DragonTypes.CAVE).add(DragonTypes.FOREST).add(DragonTypes.SEA);
        tag(NONE);

        tag(CAVE).add(DragonTypes.CAVE);
        tag(FOREST).add(DragonTypes.FOREST);
        tag(SEA).add(DragonTypes.SEA);
    }

    public static TagKey<DragonType> key(final String path) {
        return TagKey.create(DragonType.REGISTRY, DragonSurvival.res(path));
    }
}
