package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class UseDragonSoulTrigger extends DragonCriterionTrigger<UseDragonSoulTrigger.UseDragonSoulInstance> {
    public UseDragonSoulTrigger() {
        super(DragonSurvival.res("use_dragon_soul"));
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, triggerInstance -> true);
    }

    @Override
    protected UseDragonSoulInstance createInstance(
            final JsonObject json,
            final ContextAwarePredicate player,
            final DeserializationContext context
    ) {
        return new UseDragonSoulInstance(
                json.has("player") ? Optional.of(player) : Optional.empty()
        );
    }

    public static class UseDragonSoulInstance extends DragonCriterionTrigger.Instance {
        public UseDragonSoulInstance(final Optional<ContextAwarePredicate> player) {
            super(DragonSurvival.res("use_dragon_soul"), player);
        }
    }
}
