package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ConvertItemFromAbility extends SimpleCriterionTrigger<ConvertItemFromAbility.TriggerInstance> {
    public void trigger(final ServerPlayer player, final Holder<Item> itemFrom, final Holder<Item> itemTo) {
        this.trigger(player, instance -> instance.itemFrom.equals(itemFrom) && instance.itemTo.equals(itemTo));
    }

    @Override
    public @NotNull Codec<ConvertItemFromAbility.TriggerInstance> codec() {
        return ConvertItemFromAbility.TriggerInstance.CODEC;
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Holder<Item> itemFrom, Holder<Item> itemTo) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ConvertItemFromAbility.TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ConvertItemFromAbility.TriggerInstance::player),
                ItemStack.ITEM_NON_AIR_CODEC.fieldOf("item_from").forGetter(ConvertItemFromAbility.TriggerInstance::itemFrom),
                ItemStack.ITEM_NON_AIR_CODEC.fieldOf("item_to").forGetter(ConvertItemFromAbility.TriggerInstance::itemTo)
        ).apply(instance, ConvertItemFromAbility.TriggerInstance::new));
    }
}
