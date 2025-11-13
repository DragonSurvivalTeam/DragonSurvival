package by.dragonsurvivalteam.dragonsurvival.client.loaders;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(Dist.CLIENT)
public class CustomSoulIconLoader {
    private static final Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonStage>, ResourceLocation>> ICONS = new HashMap<>();

    record CustomSoulIcon(ResourceKey<DragonSpecies> species, Optional<ResourceKey<DragonStage>> stage, ResourceLocation model) {
        public static final Codec<CustomSoulIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(DragonSpecies.REGISTRY).fieldOf("species").forGetter(CustomSoulIcon::species),
                ResourceKey.codec(DragonStage.REGISTRY).optionalFieldOf("stage").forGetter(CustomSoulIcon::stage),
                ResourceLocation.CODEC.fieldOf("model").forGetter(CustomSoulIcon::model)
        ).apply(instance, CustomSoulIcon::new));
    }

    // Not using a normal reload listener since it would run async and therefore potentially complete after the registration has happened
    public static void reload(final ResourceManager manager) {
        ICONS.clear();

        Map<ResourceLocation, JsonElement> resources = new HashMap<>();
        SimpleJsonResourceReloadListener.scanDirectory(manager, "custom_soul_icons", new Gson(), resources);

        resources.forEach((location, element) -> CustomSoulIcon.CODEC.decode(JsonOps.INSTANCE, element)
                .ifError(DragonSurvival.LOGGER::error).map(Pair::getFirst)
                .ifSuccess(result -> {
                    ResourceKey<DragonStage> stage = result.stage().orElse(null);
                    ICONS.computeIfAbsent(result.species(), key -> new HashMap<>()).put(stage, result.model());
                }));
    }

    public static @Nullable ResourceLocation getIcon(final ResourceKey<DragonSpecies> species, @Nullable final ResourceKey<DragonStage> stage) {
        Map<ResourceKey<DragonStage>, ResourceLocation> resources = ICONS.get(species);

        if (resources == null) {
            return null;
        }

        ResourceLocation resource = resources.get(stage);

        if (resource == null) {
            // Check if a generic one exists (i.e. no stage specified)
            resource = resources.get(null);
        }

        return resource;
    }

    @SubscribeEvent
    public static void registerIcons(final ModelEvent.RegisterAdditional event) {
        ICONS.values().forEach(maps -> maps.values().forEach(resource -> event.register(ModelResourceLocation.standalone(resource))));
    }
}
