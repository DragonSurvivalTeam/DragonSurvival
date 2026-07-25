package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.Optional;

public class SleepOnTreasureTrigger extends DragonCriterionTrigger<SleepOnTreasureTrigger.SleepOnTreasureInstance> {
    public SleepOnTreasureTrigger() {
        super(DragonSurvival.res("sleep_on_treasure"));
    }

    public void trigger(ServerPlayer player, int count) {
        this.trigger(player, triggerInstance -> triggerInstance.nearbyTreasureAmount.map(integer -> integer <= count).orElse(true));
    }

    @Override
    protected SleepOnTreasureInstance createInstance(
            final JsonObject json,
            final ContextAwarePredicate player,
            final DeserializationContext context
    ) {
        return new SleepOnTreasureInstance(
                json.has("player") ? Optional.of(player) : Optional.empty(),
                json.has("nearby_treasure_amount")
                        ? Optional.of(GsonHelper.getAsInt(json, "nearby_treasure_amount"))
                        : Optional.empty()
        );
    }

    public static class SleepOnTreasureInstance extends DragonCriterionTrigger.Instance {
        private final Optional<Integer> nearbyTreasureAmount;

        public SleepOnTreasureInstance(
                final Optional<ContextAwarePredicate> player,
                final Optional<Integer> nearbyTreasureAmount
        ) {
            super(DragonSurvival.res("sleep_on_treasure"), player);
            this.nearbyTreasureAmount = nearbyTreasureAmount;
        }

        public Optional<Integer> nearbyTreasureAmount() {
            return nearbyTreasureAmount;
        }

        @Override
        public JsonObject serializeToJson(final SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            nearbyTreasureAmount.ifPresent(value ->
                    json.addProperty("nearby_treasure_amount", value)
            );
            return json;
        }
    }
}
