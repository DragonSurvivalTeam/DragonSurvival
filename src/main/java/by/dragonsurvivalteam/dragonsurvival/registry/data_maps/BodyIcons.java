package by.dragonsurvivalteam.dragonsurvival.registry.data_maps;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class BodyIcons {
    public static final Codec<Map<ResourceKey<DragonSpecies>, ResourceLocation>> CODEC = Codec.unboundedMap(
            ResourceKey.codec(DragonSpecies.REGISTRY), ResourceLocation.CODEC
    );

    public static ResourceLocation getIcon(final Holder<DragonBody> body, final ResourceKey<DragonSpecies> species) {
        Map<ResourceKey<DragonSpecies>, ResourceLocation> data = body.getData(DSDataMaps.BODY_ICONS);

        if (data == null) {
            return body.value().defaultIcon().orElse(DragonSurvival.MISSING_TEXTURE);
        }

        return data.getOrDefault(species, body.value().defaultIcon().orElse(DragonSurvival.MISSING_TEXTURE));
    }
}
