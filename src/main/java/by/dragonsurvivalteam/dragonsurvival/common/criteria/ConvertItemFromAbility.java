package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;

import java.util.Optional;

public class ConvertItemFromAbility extends DragonCriterionTrigger<ConvertItemFromAbility.TriggerInstance> {
    public ConvertItemFromAbility() {
        super(DragonSurvival.res("convert_item_from_ability"));
    }

    public void trigger(final ServerPlayer player, final Holder<Item> itemFrom, final Holder<Item> itemTo) {
        this.trigger(player, instance -> instance.itemFrom.equals(itemFrom) && instance.itemTo.equals(itemTo));
    }

    @Override
    protected TriggerInstance createInstance(
            final JsonObject json,
            final ContextAwarePredicate player,
            final DeserializationContext context
    ) {
        return new TriggerInstance(
                json.has("player") ? Optional.of(player) : Optional.empty(),
                item(json, "item_from"),
                item(json, "item_to")
        );
    }

    private static Holder<Item> item(final JsonObject json, final String key) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, key));
        return BuiltInRegistries.ITEM.getHolder(ResourceKey.create(BuiltInRegistries.ITEM.key(), id))
                .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + id));
    }

    public static class TriggerInstance extends DragonCriterionTrigger.Instance {
        private final Holder<Item> itemFrom;
        private final Holder<Item> itemTo;

        public TriggerInstance(
                final Optional<ContextAwarePredicate> player,
                final Holder<Item> itemFrom,
                final Holder<Item> itemTo
        ) {
            super(DragonSurvival.res("convert_item_from_ability"), player);
            this.itemFrom = itemFrom;
            this.itemTo = itemTo;
        }

        public Holder<Item> itemFrom() {
            return itemFrom;
        }

        public Holder<Item> itemTo() {
            return itemTo;
        }

        @Override
        public JsonObject serializeToJson(final SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("item_from", itemFrom.unwrapKey().orElseThrow().location().toString());
            json.addProperty("item_to", itemTo.unwrapKey().orElseThrow().location().toString());
            return json;
        }
    }
}
