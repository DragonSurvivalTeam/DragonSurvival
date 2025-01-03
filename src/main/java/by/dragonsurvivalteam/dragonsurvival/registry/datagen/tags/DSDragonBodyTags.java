package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
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
        tag(ORDER).add(DragonBodies.center, DragonBodies.east, DragonBodies.north, DragonBodies.south, DragonBodies.west);
    }

    public static List<Holder<DragonBody>> getOrdered(@Nullable final HolderLookup.Provider provider) {
        HolderLookup.RegistryLookup<DragonBody> registry;

        if (provider != null) {
            registry = provider.lookupOrThrow(DragonBody.REGISTRY);
        } else {
            registry = CommonHooks.resolveLookup(DragonBody.REGISTRY);
        }

        List<Holder<DragonBody>> bodies = new ArrayList<>();

        //noinspection DataFlowIssue -> registry is expected to be present
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
