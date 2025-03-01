package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class StealFromVillagerTrigger extends SimpleCriterionTrigger<by.dragonsurvivalteam.dragonsurvival.common.criteria.StealFromVillagerTrigger.Instance> {
    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    @Override
    public @NotNull Codec<by.dragonsurvivalteam.dragonsurvival.common.criteria.StealFromVillagerTrigger.Instance> codec() {
        return by.dragonsurvivalteam.dragonsurvival.common.criteria.StealFromVillagerTrigger.Instance.CODEC;
    }

    public record Instance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<by.dragonsurvivalteam.dragonsurvival.common.criteria.StealFromVillagerTrigger.Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(by.dragonsurvivalteam.dragonsurvival.common.criteria.StealFromVillagerTrigger.Instance::player)
        ).apply(instance, by.dragonsurvivalteam.dragonsurvival.common.criteria.StealFromVillagerTrigger.Instance::new));
    }
}