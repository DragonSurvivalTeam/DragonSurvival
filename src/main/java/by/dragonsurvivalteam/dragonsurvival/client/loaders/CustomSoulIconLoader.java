package by.dragonsurvivalteam.dragonsurvival.client.loaders;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(Dist.CLIENT)
public class CustomSoulIconLoader {
    private static final FileToIdConverter SOUL_ICON_FILES = FileToIdConverter.json("custom_soul_icons");
    private static final Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonStage>, Identifier>> ICONS = new HashMap<>();

    record CustomSoulIcon(ResourceKey<DragonSpecies> species, Optional<ResourceKey<DragonStage>> stage, Identifier model) {
        public static final Codec<CustomSoulIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(DragonSpecies.REGISTRY).fieldOf("species").forGetter(CustomSoulIcon::species),
                ResourceKey.codec(DragonStage.REGISTRY).optionalFieldOf("stage").forGetter(CustomSoulIcon::stage),
                Identifier.CODEC.fieldOf("model").forGetter(CustomSoulIcon::model)
        ).apply(instance, CustomSoulIcon::new));
    }

    public static void reload(final ResourceManager manager) {
        Map<Identifier, CustomSoulIcon> resources = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(manager, SOUL_ICON_FILES, com.mojang.serialization.JsonOps.INSTANCE, CustomSoulIcon.CODEC, resources);
        apply(resources);
    }

    public static void reloadFromGameResources() {
        reload(Minecraft.getInstance().getResourceManager());
    }

    private static void apply(final Map<Identifier, CustomSoulIcon> resources) {
        ICONS.clear();
        resources.values().forEach(result -> {
            ResourceKey<DragonStage> stage = result.stage().orElse(null);
            ICONS.computeIfAbsent(result.species(), key -> new HashMap<>()).put(stage, result.model());
        });
    }

    public static @Nullable Identifier getIcon(final ResourceKey<DragonSpecies> species, @Nullable final ResourceKey<DragonStage> stage) {
        Map<ResourceKey<DragonStage>, Identifier> resources = ICONS.get(species);

        if (resources == null) {
            return null;
        }

        Identifier resource = resources.get(stage);

        if (resource == null) {
            // Check if a generic one exists (i.e. no stage specified)
            resource = resources.get(null);
        }

        return resource;
    }

    public static Collection<Identifier> getModels() {
        Collection<Identifier> models = new HashSet<>();
        ICONS.values().forEach(entries -> models.addAll(entries.values()));
        return models;
    }

    @SubscribeEvent
    public static void registerIcons(final ModelEvent.RegisterStandalone event) {
        reloadFromGameResources();
    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener<CustomSoulIcon> {
        public ReloadListener() {
            super(CustomSoulIcon.CODEC, SOUL_ICON_FILES);
        }

        @Override
        protected void apply(final Map<Identifier, CustomSoulIcon> entries, @NonNull final ResourceManager manager, @NonNull final ProfilerFiller profiler) {
            CustomSoulIconLoader.apply(entries);
        }
    }
}
