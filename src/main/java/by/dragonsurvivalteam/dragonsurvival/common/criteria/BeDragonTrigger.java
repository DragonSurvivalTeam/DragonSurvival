package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.DragonPredicate;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSubPredicates;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class BeDragonTrigger extends DragonCriterionTrigger<BeDragonTrigger.Instance> {
    public BeDragonTrigger() {
        super(DragonSurvival.res("be_dragon"));
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> instance.matchesDragon(player));
    }

    @Override
    protected Instance createInstance(
            final JsonObject json,
            final ContextAwarePredicate player,
            final DeserializationContext context
    ) {
        Optional<DragonPredicate> dragon = json.has("dragon")
                ? Optional.of(DSSubPredicates.deserialize(DragonPredicate.CODEC, json.getAsJsonObject("dragon")))
                : Optional.empty();
        return new Instance(json.has("player") ? Optional.of(player) : Optional.empty(), dragon);
    }

    public static class Instance extends DragonCriterionTrigger.Instance {
        private final Optional<DragonPredicate> dragon;

        public Instance(final Optional<ContextAwarePredicate> player, final Optional<DragonPredicate> dragon) {
            super(DragonSurvival.res("be_dragon"), player);
            this.dragon = dragon;
        }

        private boolean matchesDragon(final ServerPlayer player) {
            return dragon.isEmpty() || dragon.get().matches(player, player.serverLevel(), player.position());
        }

        @Override
        public JsonObject serializeToJson(final SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            dragon.ifPresent(predicate -> json.add("dragon", predicate.serializeCustomData()));
            return json;
        }
    }
}
