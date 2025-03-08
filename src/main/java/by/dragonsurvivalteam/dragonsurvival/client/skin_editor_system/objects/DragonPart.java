package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

/** Part data read from the 'skin/parts/*.json' files */
public record DragonPart(
        String key,
        Optional<Component> localization,
        ResourceLocation texture,
        List<ResourceKey<DragonSpecies>> applicableSpecies,
        List<ResourceKey<DragonBody>> applicableBodies,
        float averageHue,
        boolean isColorable,
        boolean includeInRandomizer,
        boolean isHueRandomizable
) {
    public static final Codec<DragonPart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("key").forGetter(DragonPart::key),
            ComponentSerialization.CODEC.optionalFieldOf("localization").forGetter(DragonPart::localization),
            ResourceLocation.CODEC.fieldOf("texture").forGetter(DragonPart::texture),
            ResourceKey.codec(DragonSpecies.REGISTRY).listOf().optionalFieldOf("applicable_species", List.of()).forGetter(DragonPart::applicableSpecies),
            ResourceKey.codec(DragonBody.REGISTRY).listOf().optionalFieldOf("applicable_bodies", List.of()).forGetter(DragonPart::applicableBodies),
            Codec.FLOAT.optionalFieldOf("average_hue", 0f).forGetter(DragonPart::averageHue),
            Codec.BOOL.optionalFieldOf("is_colorable", true).forGetter(DragonPart::isColorable),
            Codec.BOOL.optionalFieldOf("include_in_randomizer", true).forGetter(DragonPart::includeInRandomizer),
            Codec.BOOL.optionalFieldOf("is_hue_randomizable", true).forGetter(DragonPart::isHueRandomizable)
    ).apply(instance, DragonPart::new));

    public static DragonPart load(final JsonObject json) {
        return CODEC.decode(JsonOps.INSTANCE, json).resultOrPartial(DragonSurvival.LOGGER::error).map(Pair::getFirst).orElse(null);
    }
}