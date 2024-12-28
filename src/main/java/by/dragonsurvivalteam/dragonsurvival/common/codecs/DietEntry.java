package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.*;

public record DietEntry(List<String> items, Optional<FoodProperties> properties) {
    public static final Codec<DietEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocationWrapper.validatedCodec().listOf().fieldOf("items").forGetter(DietEntry::items),
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
        return new DietEntry(List.of(location), Optional.ofNullable(properties));
    }

    public static Map<Item, FoodProperties> map(final List<DietEntry> entries) {
        Map<Item, FoodProperties> diet = new HashMap<>();

        for (DietEntry entry : entries) {
            for (String location : entry.items()) {
                Set<ResourceKey<Item>> keys = ResourceLocationWrapper.map(location, BuiltInRegistries.ITEM);

                keys.forEach(key -> {
                    Item item = BuiltInRegistries.ITEM.get(key);
                    //noinspection DataFlowIssue -> item is present
                    FoodProperties properties = entry.properties.orElse(item.getDefaultInstance().getFoodProperties(null));

                    if (properties != null) {
                        diet.put(item, properties);
                    }
                });
            }
        }

        return diet;
    }
}
