package by.dragonsurvivalteam.dragonsurvival.common.conditions;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSLootItemConditions;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public record MatchItem(Optional<ItemPredicate> predicate, Slot slot) implements LootItemCondition {
    public static final MapCodec<MatchItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemPredicate.CODEC.optionalFieldOf("predicate").forGetter(MatchItem::predicate),
            Slot.CODEC.fieldOf("slot").forGetter(MatchItem::slot)
    ).apply(instance, MatchItem::new));

    public static LootItemCondition.Builder build(final ItemPredicate predicate, final Slot slot) {
        return () -> new MatchItem(Optional.of(predicate), slot);
    }

    @Override
    public boolean test(final LootContext context) {
        ItemStack stack = context.getParamOrNull(slot.context());
        return stack != null && (this.predicate.isEmpty() || this.predicate.get().test(stack));
    }

    @Override
    public @NotNull LootItemConditionType getType() {
        return DSLootItemConditions.MATCH_ITEM.value();
    }

    @Override
    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(
                Slot.OFFHAND.context(),
                Slot.HEAD.context(),
                Slot.CHEST.context(),
                Slot.LEGS.context(),
                Slot.FEET.context()
        );
    }

    public enum Slot implements StringRepresentable {
        OFFHAND("offhand", entity -> entity instanceof LivingEntity livingEntity ? livingEntity.getOffhandItem() : ItemStack.EMPTY),
        HEAD("head", entity -> entity instanceof LivingEntity livingEntity ? livingEntity.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY),
        CHEST("chest", entity -> entity instanceof LivingEntity livingEntity ? livingEntity.getItemBySlot(EquipmentSlot.CHEST) : ItemStack.EMPTY),
        LEGS("legs", entity -> entity instanceof LivingEntity livingEntity ? livingEntity.getItemBySlot(EquipmentSlot.LEGS) : ItemStack.EMPTY),
        FEET("feet", entity -> entity instanceof LivingEntity livingEntity ? livingEntity.getItemBySlot(EquipmentSlot.FEET) : ItemStack.EMPTY);

        public static final Codec<Slot> CODEC = StringRepresentable.fromEnum(Slot::values);

        private final String name;
        private final LootContextParam<ItemStack> context;
        private final Function<Entity, ItemStack> itemProvider;

        Slot(final String name, final Function<Entity, ItemStack> itemProvider) {
            this.name = name;
            this.context = new LootContextParam<>(DragonSurvival.res(name));
            this.itemProvider = itemProvider;
        }

        public LootContextParam<ItemStack> context() {
            return context;
        }

        public ItemStack getItem(final Entity entity) {
            return itemProvider.apply(entity);
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
