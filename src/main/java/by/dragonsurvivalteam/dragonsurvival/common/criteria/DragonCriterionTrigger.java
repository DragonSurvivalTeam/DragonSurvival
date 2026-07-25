package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

abstract class DragonCriterionTrigger<T extends AbstractCriterionTriggerInstance>
        extends SimpleCriterionTrigger<T> {
    private final ResourceLocation id;

    protected DragonCriterionTrigger(final ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public Criterion createCriterion(final T instance) {
        return new Criterion(instance);
    }

    abstract static class Instance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> player;

        protected Instance(
                final ResourceLocation id,
                final Optional<ContextAwarePredicate> player
        ) {
            super(id, player.orElse(ContextAwarePredicate.ANY));
            this.player = player;
        }

        public Optional<ContextAwarePredicate> player() {
            return player;
        }

        @Override
        public JsonObject serializeToJson(final SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            if (player.isEmpty()) {
                json.remove("player");
            }
            return json;
        }
    }
}
