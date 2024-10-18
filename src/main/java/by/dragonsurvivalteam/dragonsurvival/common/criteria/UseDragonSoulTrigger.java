package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class UseDragonSoulTrigger extends SimpleCriterionTrigger<UseDragonSoulTrigger.UseDragonSoulInstance> {
    public void trigger(ServerPlayer player) {
        this.trigger(player, triggerInstance -> true);
    }

    @Override
    public Codec<UseDragonSoulTrigger.UseDragonSoulInstance> codec() {
        return UseDragonSoulTrigger.UseDragonSoulInstance.CODEC;
    }

    public record UseDragonSoulInstance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<UseDragonSoulTrigger.UseDragonSoulInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UseDragonSoulTrigger.UseDragonSoulInstance::player)
        ).apply(instance, UseDragonSoulTrigger.UseDragonSoulInstance::new));
    }
}
