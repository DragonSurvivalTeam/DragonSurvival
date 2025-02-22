package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

import java.util.Arrays;

public record GrowthItem(HolderSet<Item> items, int growthInTicks, int maximumUsages) {
    public static final int INFINITE_USAGES = -1;

    public static final Codec<GrowthItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(GrowthItem::items),
            Codec.INT.fieldOf("growth_in_ticks").forGetter(GrowthItem::growthInTicks),
            ExtraCodecs.intRange(INFINITE_USAGES, Integer.MAX_VALUE).optionalFieldOf("maximum_usages",INFINITE_USAGES).forGetter(GrowthItem::maximumUsages)
    ).apply(instance, instance.stable(GrowthItem::new)));

    public boolean canBeUsed(final DragonStateHandler handler, final Item item) {
        //noinspection deprecation -> ignore
        if (items.contains(item.builtInRegistryHolder())) {
            if (maximumUsages == INFINITE_USAGES) {
                return true;
            }

            return handler.getGrowthUses(item) < maximumUsages;
        }

        return false;
    }

    public static GrowthItem create(int growthInTicks, final TagKey<Item> tag) {
        return create(growthInTicks, INFINITE_USAGES, tag);
    }

    public static GrowthItem create(int growthInTicks, int maximumUsages, final TagKey<Item> tag) {
        return new GrowthItem(BuiltInRegistries.ITEM.getOrCreateTag(tag), growthInTicks, maximumUsages);
    }

    public static GrowthItem create(int growthInTicks, final Item... items) {
        return create(growthInTicks, INFINITE_USAGES, items);
    }

    public static GrowthItem create(int growthInTicks, int maximumUsages, final Item... items) {
        return new GrowthItem(HolderSet.direct(Arrays.stream(items).map(BuiltInRegistries.ITEM::wrapAsHolder).toList()), growthInTicks, maximumUsages);
    }
}