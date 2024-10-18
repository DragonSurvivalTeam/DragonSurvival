package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

public class UpgradeAbilityTrigger extends SimpleCriterionTrigger<UpgradeAbilityTrigger.UpgradeAbilityInstance> {
    public void trigger(ServerPlayer player, String ability, int level) {
        this.trigger(player, triggerInstance -> {
            boolean flag = true;
            if (triggerInstance.ability.isPresent()) {
                flag = triggerInstance.ability.get().equals(ability);
            }
            if (triggerInstance.level.isPresent()) {
                flag = flag && triggerInstance.level.get().equals(level);
            }
            return flag;
        });
    }

    @Override
    public Codec<UpgradeAbilityInstance> codec() {
        return UpgradeAbilityInstance.CODEC;
    }

    public record UpgradeAbilityInstance(Optional<ContextAwarePredicate> player, Optional<String> ability, Optional<Integer> level) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<UpgradeAbilityInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UpgradeAbilityInstance::player),
                Codec.STRING.optionalFieldOf("ability").forGetter(UpgradeAbilityInstance::ability),
                Codec.INT.optionalFieldOf("level").forGetter(UpgradeAbilityInstance::level)
        ).apply(instance, UpgradeAbilityInstance::new));
    }
}
