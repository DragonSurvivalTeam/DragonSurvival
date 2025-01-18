package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record DietEntry(String items, Optional<FoodProperties> properties) {
    // Copied from FoodProperties.java. Trying to AT this didn't work out well.
    public static final float DEFAULT_EAT_SECONDS = 1.6f;

    public static final Codec<DietEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocationWrapper.validatedCodec().fieldOf("items").forGetter(DietEntry::items),
            FoodProperties.DIRECT_CODEC.optionalFieldOf("properties").forGetter(DietEntry::properties)
    ).apply(instance, DietEntry::new));

    public static DietEntry from(final ResourceLocation location) {
        return from(location.toString());
    }

    public static DietEntry from(final String location) {
        return from(location, null);
    }

    public static DietEntry from(final TagKey<Item> tag) {
        return from(tag, null);
    }

    public static DietEntry from(final TagKey<Item> tag, final FoodProperties properties) {
        return from("#" + tag.location(), properties);
    }

    public static DietEntry from(final Item item) {
        return from(item, null);
    }

    public static DietEntry from(final Item item, final FoodProperties properties) {
        //noinspection deprecation,DataFlowIssue -> ignore deprecated / key is present
        return from(item.builtInRegistryHolder().getKey().location(), properties);
    }

    public static DietEntry from(final ResourceLocation location, final FoodProperties properties) {
        return from(location.toString(), properties);
    }

    public static DietEntry from(final String location, final FoodProperties properties) {
        return new DietEntry(location, Optional.ofNullable(properties));
    }

    public static Map<Item, FoodProperties> map(final List<DietEntry> entries) {
        Map<Item, FoodProperties> diet = new HashMap<>();

        for (DietEntry entry : entries) {
            ResourceLocationWrapper.map(entry.items(), BuiltInRegistries.ITEM).forEach(resource -> {
                Item item = BuiltInRegistries.ITEM.get(resource);

                if (item != null) {
                    FoodProperties properties = entry.properties.orElse(item.getDefaultInstance().getFoodProperties(null));

                    if (properties != null) {
                        diet.put(item, properties);
                    }
                }
            });
        }

        return diet;
    }
}
