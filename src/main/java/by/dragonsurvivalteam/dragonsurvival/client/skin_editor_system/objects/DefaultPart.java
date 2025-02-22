package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.SkinLayer;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.Optional;

public record DefaultPart(
        ResourceKey<DragonSpecies> species,
        ResourceKey<DragonStage> stage,
        Optional<ResourceKey<DragonBody>> body,
        Optional<ResourceLocation> model,
        Map<SkinLayer, String> parts
) {
    public static final Codec<DefaultPart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(DragonSpecies.REGISTRY).fieldOf("species").forGetter(DefaultPart::species),
            ResourceKey.codec(DragonStage.REGISTRY).fieldOf("stage").forGetter(DefaultPart::stage),
            ResourceKey.codec(DragonBody.REGISTRY).optionalFieldOf("body").forGetter(DefaultPart::body),
            ResourceLocation.CODEC.optionalFieldOf("model").forGetter(DefaultPart::model),
            Codec.unboundedMap(SkinLayer.CODEC, Codec.STRING).fieldOf("parts").forGetter(DefaultPart::parts)
    ).apply(instance, DefaultPart::new));
}
