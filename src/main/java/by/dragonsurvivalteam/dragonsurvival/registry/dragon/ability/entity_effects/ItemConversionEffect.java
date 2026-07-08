package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ParticleData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record ItemConversionEffect(List<ItemConversionData> itemConversions, LevelBasedValue probability) implements AbilityEntityEffect {
    public static final MapCodec<ItemConversionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemConversionData.CODEC.listOf().fieldOf("item_conversions").forGetter(ItemConversionEffect::itemConversions),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(ItemConversionEffect::probability)
    ).apply(instance, ItemConversionEffect::new));

    public record ItemConversionData(ItemPredicate predicate, WeightedList<ItemTo> itemsTo) {
        public static final Codec<ItemConversionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemPredicate.CODEC.fieldOf("item_predicate").forGetter(ItemConversionData::predicate),
                WeightedList.codec(ItemTo.CODEC).fieldOf("items_to").forGetter(ItemConversionData::itemsTo)
        ).apply(instance, ItemConversionData::new));
    }

    public record ItemTo(Holder<Item> item, double conversionRate, Optional<ParticleData> particles) {
        public static final MapCodec<ItemTo> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ItemTo::item),
                Codec.DOUBLE.optionalFieldOf("conversion_rate", 1.0).forGetter(ItemTo::conversionRate),
                ParticleData.CODEC.optionalFieldOf("particles").forGetter(ItemTo::particles)
        ).apply(instance, ItemTo::new));

        public static ItemTo of(final Item item) {
            return of(item, 1.0, null);
        }

        public static ItemTo of(final Item item, final double conversionRate) {
            return of(item, conversionRate, null);
        }

        public static ItemTo of(final Item item, final double conversionRate, final ParticleData particles) {
            //noinspection deprecation -> ignore
            return new ItemTo(item.builtInRegistryHolder(), conversionRate, Optional.ofNullable(particles));
        }
    }

    // TODO :: add other options to scale with ability level? e.g. require fewer items or multiply the converted items?
    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ItemEntity itemEntity)) {
            return;
        }

        if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
            return;
        }

        if (itemConversions.isEmpty()) {
            return;
        }

        for (ItemConversionData data : itemConversions) {
            if (data.predicate().test(itemEntity.getItem())) {
                data.itemsTo().getRandom(target.getRandom()).ifPresent(conversion -> {
                    // TODO :: add converted-to amount to the achievement?
                    DSAdvancementTriggers.CONVERT_ITEM_FROM_ABILITY.get().trigger(dragon, itemEntity.getItem().getItem().builtInRegistryHolder(), conversion.item());
                    conversion.particles().ifPresent(particles -> particles.spawn(dragon.level(), itemEntity, ability.level()));

                    int newAmount = (int)(itemEntity.getItem().getCount() * conversion.conversionRate());
                    int maxStackSize = conversion.item().value().getDefaultMaxStackSize();

                    if (newAmount > maxStackSize) {
                        Vec3 position = itemEntity.position();

                        // It's easier to just spawn new ones
                        itemEntity.discard();

                        int newStacks = newAmount / maxStackSize;
                        int remainder = newAmount % maxStackSize;

                        for (int i = 0; i < newStacks; i++) {
                            dragon.level().addFreshEntity(new ItemEntity(dragon.level(), position.x, position.y, position.z, new ItemStack(conversion.item(), maxStackSize)));
                        }

                        if (remainder > 0) {
                            // Example: We now have 137 items and the new stack size is 24 - we spawn 5 full stacks and a remainder stack of 17
                            dragon.level().addFreshEntity(new ItemEntity(dragon.level(), position.x, position.y, position.z, new ItemStack(conversion.item(), remainder)));
                        }
                    } else {
                        itemEntity.setItem(new ItemStack(conversion.item(), newAmount));
                    }
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
