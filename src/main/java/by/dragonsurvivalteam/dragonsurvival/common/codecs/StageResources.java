package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Objects;

public class StageResources {
    public static final Codec<Map<ResourceKey<DragonStage>, StageResource>> CODEC = Codec.unboundedMap(
            ResourceKey.codec(DragonStage.REGISTRY), StageResource.CODEC
    );

    private static final GrowthIcon MISSING = new GrowthIcon(DragonSurvival.MISSING_TEXTURE, DragonSurvival.MISSING_TEXTURE);

    public static StageResources.GrowthIcon getGrowthIcon(final Holder<DragonSpecies> species, final ResourceKey<DragonStage> stage) {
        Map<ResourceKey<DragonStage>, StageResource> resources = species.getData(DSDataMaps.STAGE_RESOURCES);

        if (resources == null) {
            DragonSurvival.LOGGER.error("Stage resources data map is missing for species [{}]. Define a data map in dragonsurvival/data_maps/dragonsurvival/dragon_species/stage_resources.json.", species.getKey());
            return MISSING;
        }

        StageResource stageResource = resources.get(stage);

        if (stageResource == null) {
            DragonSurvival.LOGGER.error("Stage resources data map is missing a resource for stage [{}] for species [{}].", stage, species.getKey());
            return MISSING;
        }

        return Objects.requireNonNullElse(stageResource.growthIcon(), MISSING);
    }

    public static ResourceLocation getDefaultSkin(final Holder<DragonSpecies> species, final ResourceKey<DragonStage> stage, final boolean glowLayer) {
        Map<ResourceKey<DragonStage>, StageResource> resources = species.getData(DSDataMaps.STAGE_RESOURCES);

        if (resources == null) {
            DragonSurvival.LOGGER.error("Stage resources data map is missing for species [{}]. Define a data map in dragonsurvival/data_maps/dragonsurvival/dragon_species/stage_resources.json.", species.getKey());
            return DragonSurvival.MISSING_TEXTURE;
        }

        StageResource stageResource = resources.get(stage);

        if (stageResource == null) {
            DragonSurvival.LOGGER.error("Stage resources data map is missing a resource for stage [{}] for species [{}].", stage, species.getKey());
            return DragonSurvival.MISSING_TEXTURE;
        }

        DefaultSkin skin = stageResource.defaultSkin();
        ResourceLocation texture = glowLayer ? skin.glowSkin() : skin.skin();

        return Objects.requireNonNullElse(texture, DragonSurvival.MISSING_TEXTURE);
    }

    public record StageResource(GrowthIcon growthIcon, DefaultSkin defaultSkin) {
        public static final Codec<StageResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GrowthIcon.CODEC.fieldOf("growth_icon").forGetter(StageResource::growthIcon),
                DefaultSkin.CODEC.fieldOf("default_skin").forGetter(StageResource::defaultSkin)
        ).apply(instance, StageResource::new));
    }

    public record GrowthIcon(ResourceLocation hoverIcon, ResourceLocation icon) {
        public static final Codec<GrowthIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("hover_icon").forGetter(GrowthIcon::hoverIcon),
                ResourceLocation.CODEC.fieldOf("icon").forGetter(GrowthIcon::icon)
        ).apply(instance, GrowthIcon::new));
    }

    public record DefaultSkin(ResourceLocation skin, ResourceLocation glowSkin) {
        public static final Codec<DefaultSkin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("skin").forGetter(DefaultSkin::skin),
                ResourceLocation.CODEC.fieldOf("glow_skin").forGetter(DefaultSkin::glowSkin)
        ).apply(instance, DefaultSkin::new));
    }
}
