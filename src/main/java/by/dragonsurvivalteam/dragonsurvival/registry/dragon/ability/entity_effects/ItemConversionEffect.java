package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ItemConversionEffect(List<ItemConversionData> itemConversions, LevelBasedValue probability) implements AbilityEntityEffect {
    public static final MapCodec<ItemConversionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemConversionData.CODEC.listOf().fieldOf("item_conversions").forGetter(ItemConversionEffect::itemConversions),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(ItemConversionEffect::probability)
    ).apply(instance, ItemConversionEffect::new));

    public record ItemConversionData(ItemPredicate predicate, WeightedRandomList<ItemTo> itemsTo) {
        public static final Codec<ItemConversionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemPredicate.CODEC.fieldOf("item_predicate").forGetter(ItemConversionData::predicate),
                SimpleWeightedRandomList.codec(ItemTo.CODEC).fieldOf("items_to").forGetter(ItemConversionData::itemsTo)
        ).apply(instance, ItemConversionData::new));
    }

    public record ItemTo(Holder<Item> item, int weight) implements WeightedEntry {
        public static final Codec<ItemTo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ItemTo::item),
                Codec.INT.fieldOf("weight").forGetter(ItemTo::weight)
        ).apply(instance, ItemTo::new));

        public static ItemTo of(final Item item) {
            return of(item, 1);
        }

        public static ItemTo of(final Item item, int weight) {
            //noinspection deprecation -> ignore
            return new ItemTo(item.builtInRegistryHolder(), weight);
        }

        @Override
        public @NotNull Weight getWeight() {
            return Weight.of(weight);
        }
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        if (!(entity instanceof ItemEntity itemEntity)) {
            return;
        }

        if (dragon.getRandom().nextDouble() < probability().calculate(ability.level())) {
            return;
        }

        if (itemConversions().isEmpty()) {
            return;
        }

        for (ItemConversionData conversion : itemConversions) {
            if (conversion.predicate().test(itemEntity.getItem())) {
                conversion.itemsTo().getRandom(entity.getRandom()).ifPresent(item -> {
                    DSAdvancementTriggers.CONVERT_ITEM_FROM_ABILITY.get().trigger(dragon, itemEntity.getItem().getItemHolder(), item.item());
                    itemEntity.setItem(new ItemStack(item.item(), itemEntity.getItem().getCount()));
                });

                return;
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
