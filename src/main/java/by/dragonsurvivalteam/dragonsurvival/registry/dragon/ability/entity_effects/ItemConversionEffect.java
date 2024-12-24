package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;

public record ItemConversionEffect(List<ItemConversionData> itemConversions, LevelBasedValue probability) implements AbilityEntityEffect {
    public static final MapCodec<ItemConversionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemConversionData.CODEC.listOf().fieldOf("item_conversions").forGetter(ItemConversionEffect::itemConversions),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(ItemConversionEffect::probability)
    ).apply(instance, ItemConversionEffect::new));


    public record ItemConversionData(HolderSet<Item> itemsFrom, List<ItemTo> itemsTo) {
        public static final Codec<ItemConversionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items_from").forGetter(ItemConversionData::itemsFrom),
                ItemTo.CODEC.listOf().fieldOf("items_to").forGetter(ItemConversionData::itemsTo)
        ).apply(instance, ItemConversionData::new));
    }

    public record ItemTo(Holder<Item> itemTo, double chance) {
        public static final Codec<ItemTo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("block").forGetter(ItemTo::itemTo),
                Codec.DOUBLE.fieldOf("chance").forGetter(ItemTo::chance)
        ).apply(instance, ItemTo::new));
    }


    // FIXME :: This doesn't work and I don't know why. For some reason we aren't getting item entities in the area when targetting. Even using the getEntities mode that should get all entities doesn't seem to work.
    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, Entity entity) {
        if(!(entity instanceof ItemEntity itemEntity)) {
            return;
        }

        if (dragon.getRandom().nextDouble() < probability().calculate(ability.level())) {
            return;
        }

        if (itemConversions().isEmpty()) {
            return;
        }

        for(ItemConversionData data : itemConversions) {
            if (data.itemsFrom().contains(itemEntity.getItem().getItemHolder())) {
                double chance = dragon.getRandom().nextDouble();
                double sumOfOdds = 0;
                for (ItemTo itemTo : data.itemsTo) {
                    sumOfOdds += itemTo.chance();
                }

                chance *= sumOfOdds;
                for (ItemTo itemTo : data.itemsTo) {
                    chance -= itemTo.chance();
                    if (chance <= 0) {
                        itemEntity.setItem(new ItemStack(itemTo.itemTo(), itemEntity.getItem().getCount()));
                        return;
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
