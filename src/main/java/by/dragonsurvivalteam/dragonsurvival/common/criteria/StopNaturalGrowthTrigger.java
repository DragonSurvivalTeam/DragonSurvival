package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class StopNaturalGrowthTrigger extends DragonCriterionTrigger<StopNaturalGrowthTrigger.Instance> {
    public StopNaturalGrowthTrigger() {
        super(DragonSurvival.res("stop_natural_growth"));
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    @Override
    protected Instance createInstance(
            final JsonObject json,
            final ContextAwarePredicate player,
            final DeserializationContext context
    ) {
        return new Instance(json.has("player") ? Optional.of(player) : Optional.empty());
    }

    public static class Instance extends DragonCriterionTrigger.Instance {
        public Instance(final Optional<ContextAwarePredicate> player) {
            super(DragonSurvival.res("stop_natural_growth"), player);
        }
    }
}
