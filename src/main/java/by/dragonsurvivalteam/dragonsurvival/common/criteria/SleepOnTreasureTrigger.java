package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SleepOnTreasureTrigger extends SimpleCriterionTrigger<SleepOnTreasureTrigger.SleepOnTreasureInstance> {
    public void trigger(ServerPlayer player, int count) {
        this.trigger(player, triggerInstance -> triggerInstance.nearbyTreasureAmount.map(integer -> integer <= count).orElse(true));
    }

    @Override
    public @NotNull Codec<SleepOnTreasureInstance> codec() {
        return SleepOnTreasureInstance.CODEC;
    }

    public record SleepOnTreasureInstance(Optional<ContextAwarePredicate> player, Optional<Integer> nearbyTreasureAmount) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<SleepOnTreasureInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SleepOnTreasureInstance::player),
                Codec.INT.optionalFieldOf("nearby_treasure_amount").forGetter(SleepOnTreasureInstance::nearbyTreasureAmount)
        ).apply(instance, SleepOnTreasureInstance::new));
    }
}