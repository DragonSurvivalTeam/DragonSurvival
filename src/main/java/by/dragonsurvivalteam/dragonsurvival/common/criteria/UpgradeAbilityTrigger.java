package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UpgradeAbilityTrigger extends SimpleCriterionTrigger<UpgradeAbilityTrigger.UpgradeAbilityInstance> {
    public void trigger(ServerPlayer player, ResourceKey<DragonAbility> ability, int level) {
        this.trigger(player, instance -> {
            boolean flag = true;

            if (instance.ability().isPresent()) {
                flag = instance.ability().get().equals(ability);
            }

            if (instance.level().isPresent()) {
                flag = flag && instance.level().get().equals(level);
            }

            return flag;
        });
    }

    @Override
    public @NotNull Codec<UpgradeAbilityInstance> codec() {
        return UpgradeAbilityInstance.CODEC;
    }

    public record UpgradeAbilityInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ResourceKey<DragonAbility>> ability,
            Optional<Integer> level
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<UpgradeAbilityInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UpgradeAbilityInstance::player),
                ResourceKey.codec(DragonAbility.REGISTRY).optionalFieldOf("ability").forGetter(UpgradeAbilityInstance::ability),
                Codec.INT.optionalFieldOf("level").forGetter(UpgradeAbilityInstance::level)
        ).apply(instance, UpgradeAbilityInstance::new));
    }
}
