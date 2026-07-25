package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.Optional;

public class UpgradeAbilityTrigger extends DragonCriterionTrigger<UpgradeAbilityTrigger.UpgradeAbilityInstance> {
    public UpgradeAbilityTrigger() {
        super(DragonSurvival.res("upgrade_ability"));
    }

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
    protected UpgradeAbilityInstance createInstance(
            final JsonObject json,
            final ContextAwarePredicate player,
            final DeserializationContext context
    ) {
        Optional<ResourceKey<DragonAbility>> ability = json.has("ability")
                ? Optional.of(ResourceKey.create(
                        DragonAbility.REGISTRY,
                        new ResourceLocation(GsonHelper.getAsString(json, "ability"))
                ))
                : Optional.empty();
        Optional<Integer> level = json.has("level")
                ? Optional.of(GsonHelper.getAsInt(json, "level"))
                : Optional.empty();
        return new UpgradeAbilityInstance(
                json.has("player") ? Optional.of(player) : Optional.empty(),
                ability,
                level
        );
    }

    public static class UpgradeAbilityInstance extends DragonCriterionTrigger.Instance {
        private final Optional<ResourceKey<DragonAbility>> ability;
        private final Optional<Integer> level;

        public UpgradeAbilityInstance(
                final Optional<ContextAwarePredicate> player,
                final Optional<ResourceKey<DragonAbility>> ability,
                final Optional<Integer> level
        ) {
            super(DragonSurvival.res("upgrade_ability"), player);
            this.ability = ability;
            this.level = level;
        }

        public Optional<ResourceKey<DragonAbility>> ability() {
            return ability;
        }

        public Optional<Integer> level() {
            return level;
        }

        @Override
        public JsonObject serializeToJson(final SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            ability.ifPresent(value -> json.addProperty("ability", value.location().toString()));
            level.ifPresent(value -> json.addProperty("level", value));
            return json;
        }
    }
}
