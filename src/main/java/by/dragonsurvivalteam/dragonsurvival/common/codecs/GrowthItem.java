package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Arrays;

public record GrowthItem(HolderSet<Item> items, int growthInTicks, int maximumUsages) {
    public static final int INFINITE_USAGES = -1;

    public static Codec<GrowthItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(GrowthItem::items),
            Codec.INT.fieldOf("growth_in_ticks").forGetter(GrowthItem::growthInTicks),
            Codec.INT.optionalFieldOf("maximum_usages", INFINITE_USAGES).forGetter(GrowthItem::maximumUsages)
    ).apply(instance, instance.stable(GrowthItem::new)));

    public boolean canBeUsed(final Player player, final Item item) {
        return canBeUsed(DragonStateProvider.getData(player), item);
    }

    public boolean canBeUsed(final DragonStateHandler handler, final Item item) {
        return true; // TODO
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