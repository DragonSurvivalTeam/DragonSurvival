package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

// FIXME :: We need to make this into a generic "convert" trigger tied to dragon abilities and make an instance of this trigger for the forest dragon potato conversion
public class ConvertPotatoTrigger extends SimpleCriterionTrigger<ConvertPotatoTrigger.TriggerInstance> {
    public void trigger(final ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    @Override
    public @NotNull Codec<ConvertPotatoTrigger.TriggerInstance> codec() {
        return ConvertPotatoTrigger.TriggerInstance.CODEC;
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ConvertPotatoTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ConvertPotatoTrigger.TriggerInstance::player)
        ).apply(instance, ConvertPotatoTrigger.TriggerInstance::new));
    }
}
