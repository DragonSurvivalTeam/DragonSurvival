package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

// FIXME
public record ItemConversionEffect(List<ItemConversionData> itemConversions, LevelBasedValue probability) implements AbilityEntityEffect {
    public static final MapCodec<ItemConversionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemConversionData.CODEC.listOf().fieldOf("item_conversions").forGetter(ItemConversionEffect::itemConversions),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(ItemConversionEffect::probability)
    ).apply(instance, ItemConversionEffect::new));

    public record ItemConversionData(ItemPredicate predicate/*, WeightedRandomList<ItemTo> itemsTo*/) {
        public static final Codec<ItemConversionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemPredicate.CODEC.fieldOf("item_predicate").forGetter(ItemConversionData::predicate)
                //SimpleWeightedRandomList.codec(ItemTo.CODEC).fieldOf("items_to").forGetter(ItemConversionData::itemsTo)
        ).apply(instance, ItemConversionData::new));
    }

//    public record ItemTo(Holder<Item> item, int conversionRate, int weight, Optional<ParticleData> particles) implements WeightedEntry {
//        public static final Codec<ItemTo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
//                BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ItemTo::item),
//                ExtraCodecs.intRange(1, 64).optionalFieldOf("conversion_rate", 1).forGetter(ItemTo::conversionRate),
//                Codec.INT.fieldOf("weight").forGetter(ItemTo::weight),
//                ParticleData.CODEC.optionalFieldOf("particles").forGetter(ItemTo::particles)
//        ).apply(instance, ItemTo::new));
//
//        public static ItemTo of(final Item item) {
//            return of(item, 1);
//        }
//
//        public static ItemTo of(final Item item, int weight) {
//            return of(item, 1, weight, null);
//        }
//
//        public static ItemTo of(final Item item, int conversionRate, int weight, final ParticleData particles) {
//            //noinspection deprecation -> ignore
//            return new ItemTo(item.builtInRegistryHolder(), conversionRate, weight, Optional.ofNullable(particles));
//        }
//
//        @Override
//        public @NotNull Weight getWeight() {
//            return Weight.of(weight);
//        }
//    }

    // TODO :: add other options to scale with ability level? e.g. require fewer items or multiply the converted items?
    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
//        if (!(target instanceof ItemEntity itemEntity)) {
//            return;
//        }
//
//        if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
//            return;
//        }
//
//        if (itemConversions.isEmpty()) {
//            return;
//        }
//
//        for (ItemConversionData data : itemConversions) {
//            if (data.predicate().test(itemEntity.getItem())) {
//                data.itemsTo().getRandom(target.getRandom()).ifPresent(conversion -> {
//                    // TODO :: add converted-to amount to the achievement?
//                    DSAdvancementTriggers.CONVERT_ITEM_FROM_ABILITY.get().trigger(dragon, itemEntity.getItem().getItemHolder(), conversion.item());
//                    conversion.particles.ifPresent(particles -> particles.spawn(dragon.serverLevel(), itemEntity, ability.level()));
//
//                    int newAmount = itemEntity.getItem().getCount() * conversion.conversionRate();
//                    int maxStackSize = conversion.item().value().getDefaultMaxStackSize();
//
//                    if (newAmount > maxStackSize) {
//                        Vec3 position = itemEntity.position();
//
//                        // It's easier to just spawn new ones
//                        itemEntity.discard();
//
//                        int newStacks = newAmount / maxStackSize;
//                        int remainder = newAmount % maxStackSize;
//
//                        for (int i = 0; i < newStacks; i++) {
//                            dragon.level().addFreshEntity(new ItemEntity(dragon.serverLevel(), position.x, position.y, position.z, new ItemStack(conversion.item(), maxStackSize)));
//                        }
//
//                        if (remainder > 0) {
//                            // Example: We now have 137 items and the new stack size is 24 - we spawn 5 full stacks and a remainder stack of 17
//                            dragon.level().addFreshEntity(new ItemEntity(dragon.serverLevel(), position.x, position.y, position.z, new ItemStack(conversion.item(), remainder)));
//                        }
//                    } else {
//                        itemEntity.setItem(new ItemStack(conversion.item(), newAmount));
//                    }
//                });
//
//                return;
//            }
//        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
