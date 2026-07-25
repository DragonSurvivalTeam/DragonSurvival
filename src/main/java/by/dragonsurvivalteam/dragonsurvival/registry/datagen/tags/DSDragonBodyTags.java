package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DSDragonBodyTags extends TagsProvider<DragonBody> {
    public static final TagKey<DragonBody> ORDER = key("order");

    public DSDragonBodyTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, DragonBody.REGISTRY, provider, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(ORDER).add(DragonBodies.CENTER, DragonBodies.EAST, DragonBodies.NORTH, DragonBodies.SOUTH, DragonBodies.WEST);
    }

    public static List<Holder<DragonBody>> getOrdered(@Nullable final HolderLookup.Provider provider) {
        HolderLookup.Provider actualProvider = provider != null ? provider : DragonSurvival.PROXY.getAccess();

        if (actualProvider == null) {
            throw new IllegalStateException("Registry context is not available for " + DragonBody.REGISTRY.location());
        }

        HolderLookup.RegistryLookup<DragonBody> registry = actualProvider.lookupOrThrow(DragonBody.REGISTRY);
        List<Holder<DragonBody>> bodies = new ArrayList<>();

        registry.get(ORDER).ifPresent(set -> set.forEach(bodies::add));

        registry.listElements().forEach(body -> {
            if (!bodies.contains(body)) {
                bodies.add(body);
            }
        });

        return bodies;
    }

    private static TagKey<DragonBody> key(final String path) {
        return TagKey.create(DragonBody.REGISTRY, DragonSurvival.res(path));
    }
}
