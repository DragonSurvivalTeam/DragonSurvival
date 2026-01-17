package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.loader;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DefaultPart;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.datafixers.util.Either;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class DefaultPartLoader extends SimpleJsonResourceReloadListener<DefaultPart> {
    public static final String NO_PART = "none";

    private static final Map<ResourceKey<DragonBody>, Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonStage>, HashMap<SkinLayer, String>>>> PARTY_BY_BODY = new HashMap<>();
    private static final Map<Identifier, Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonStage>, HashMap<SkinLayer, String>>>> PARTY_BY_MODEL = new HashMap<>();

    public DefaultPartLoader() {
        super(DefaultPart.CODEC, new FileToIdConverter("skin/default_parts", ".json"));
    }

    @Override
    protected void apply(final Map<Identifier, DefaultPart> entries, @NonNull final ResourceManager manager, @NonNull final ProfilerFiller profiler) {
        PARTY_BY_BODY.clear();
        PARTY_BY_MODEL.clear();

        entries.forEach((identifier, part) -> {
            if (part.body().isPresent()) {
                PARTY_BY_BODY.computeIfAbsent(part.body().get(), key -> new HashMap<>())
                        .computeIfAbsent(part.species(), key -> new HashMap<>())
                        .computeIfAbsent(part.stage(), key -> new HashMap<>()).putAll(part.parts());
            } else if (part.model().isPresent()) {
                PARTY_BY_MODEL.computeIfAbsent(part.model().get(), key -> new HashMap<>())
                        .computeIfAbsent(part.species(), key -> new HashMap<>())
                        .computeIfAbsent(part.stage(), key -> new HashMap<>()).putAll(part.parts());
            }
        });
    }

    // TODO :: add a call to also query by body in case people want to define things different defaults for them
    //  idea is body is checked first and then the model if no valid entry was found
    //  this means that 'NO_PART' return might need to be replaced by returning null or sth. like that
    public static @Nullable String getDefaultPartKey(final ResourceKey<DragonSpecies> species, final ResourceKey<DragonStage> stage, final Either<ResourceKey<DragonBody>, Identifier> partsKey, final SkinLayer layer) {
        Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonStage>, HashMap<SkinLayer, String>>> entries = partsKey.map(PARTY_BY_BODY::get, PARTY_BY_MODEL::get);

        if (entries == null) {
            return NO_PART;
        }

        Map<ResourceKey<DragonStage>, HashMap<SkinLayer, String>> perSpecies = entries.get(species);

        if (perSpecies == null) {
            return NO_PART;
        }

        HashMap<SkinLayer, String> perStage = perSpecies.get(stage);

        if (perStage == null) {
            return NO_PART;
        }

        return perStage.getOrDefault(layer, NO_PART);
    }
}
