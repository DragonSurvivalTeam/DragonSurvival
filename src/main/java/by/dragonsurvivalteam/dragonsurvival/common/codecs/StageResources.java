package by.dragonsurvivalteam.dragonsurvival.common.codecs;

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

    /** See {@link net.minecraft.client.renderer.texture.MissingTextureAtlasSprite#MISSING_TEXTURE_LOCATION} */
    private static final ResourceLocation MISSING_TEXTURE = ResourceLocation.withDefaultNamespace("missingno");
    private static final GrowthIcon MISSING = new GrowthIcon(MISSING_TEXTURE, MISSING_TEXTURE);

    public static StageResources.GrowthIcon getGrowthIcon(final Holder<DragonSpecies> species, final ResourceKey<DragonStage> stage) {
        Map<ResourceKey<DragonStage>, StageResource> resources = species.getData(DSDataMaps.STAGE_RESOURCES);

        if (resources == null) {
            return MISSING;
        }

        return Objects.requireNonNullElse(resources.get(stage).growthIcon(), MISSING);
    }

    public static ResourceLocation getHoverGrowthIcon(final Holder<DragonSpecies> species, final ResourceKey<DragonStage> stage) {
        Map<ResourceKey<DragonStage>, StageResource> resources = species.getData(DSDataMaps.STAGE_RESOURCES);

        if (resources == null) {
            return MISSING_TEXTURE;
        }

        return Objects.requireNonNullElse(resources.get(stage).growthIcon().hoverIcon(), MISSING_TEXTURE);
    }

    public static ResourceLocation getDefaultSkin(final Holder<DragonSpecies> species, final ResourceKey<DragonStage> stage, final boolean glowLayer) {
        Map<ResourceKey<DragonStage>, StageResource> resources = species.getData(DSDataMaps.STAGE_RESOURCES);

        if (resources == null) {
            return MISSING_TEXTURE;
        }

        DefaultSkin skin = resources.get(stage).defaultSkin();
        ResourceLocation texture = glowLayer ? skin.glowSkin() : skin.skin();

        return Objects.requireNonNullElse(texture, MISSING_TEXTURE);
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
