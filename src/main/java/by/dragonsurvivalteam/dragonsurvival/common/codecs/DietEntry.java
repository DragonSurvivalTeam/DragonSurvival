package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record DietEntry(List<String> items, FoodProperties properties) {
    public static final Codec<DietEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocationWrapper.validatedCodec().listOf().fieldOf("items").forGetter(DietEntry::items),
            FoodProperties.DIRECT_CODEC.fieldOf("properties").forGetter(DietEntry::properties)
    ).apply(instance, DietEntry::new));

    public static DietEntry from(final ResourceLocation location, final FoodProperties properties) {
        return from(location.toString(), properties);
    }

    // TODO :: add option to keep the properties empty? and if that is the case the items default values will be used?
    public static DietEntry from(final String location, final FoodProperties properties) {
        return new DietEntry(List.of(location), properties);
    }

    public static Map<Item, FoodProperties> map(final List<DietEntry> entries) {
        Map<Item, FoodProperties> diet = new HashMap<>();
        entries.forEach(entry -> entry.items().forEach(item -> ResourceLocationWrapper.map(item, BuiltInRegistries.ITEM).forEach(key -> diet.put(BuiltInRegistries.ITEM.get(key), entry.properties()))));
        return diet;
    }
}
