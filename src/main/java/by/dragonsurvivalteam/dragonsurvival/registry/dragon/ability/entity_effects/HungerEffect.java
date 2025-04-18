package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record HungerEffect(
    Optional<LevelBasedValue> hunger,
    Optional<LevelBasedValue> saturation,
    Optional<LevelBasedValue> maxSaturation,
    Optional<LevelBasedValue> conversionRate
) implements AbilityEntityEffect {
    @Translation(comments = "Adds %s food.")
    public static final String ADD_FOOD = Translation.Type.GUI.wrap("hunger.food");

    @Translation(comments = "Adds %s exhaustion.")
    public static final String ADD_EXHAUSTION = Translation.Type.GUI.wrap("hunger.exhaustion");

    @Translation(comments = "Adds %s saturation.")
    public static final String SATURATION = Translation.Type.GUI.wrap("hunger.saturation");

    @Translation(comments = "Converts %s excess hunger to saturation.")
    public static final String CONVERSION_RATE = Translation.Type.GUI.wrap("hunger.conversion_rate");

    @Translation(comments = "You cannot have more than %s saturation from this ability.")
    public static final String MAX_SATURATION = Translation.Type.GUI.wrap("hunger.max_saturation");

    public static final MapCodec<HungerEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.optionalFieldOf("hunger_gain").forGetter(HungerEffect::hunger),
            LevelBasedValue.CODEC.optionalFieldOf("saturation_gain").forGetter(HungerEffect::saturation),
            LevelBasedValue.CODEC.optionalFieldOf("maximum_saturation").forGetter(HungerEffect::maxSaturation),
            LevelBasedValue.CODEC.optionalFieldOf("conversion_rate").forGetter(HungerEffect::conversionRate)
    ).apply(instance, HungerEffect::new));
    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, Entity target) {
        if (target instanceof ServerPlayer player) {
            FoodData foodData = player.getFoodData();

            float hungerValue = hunger.map(hun -> hun.calculate(ability.level())).orElse(0F);
            if (hungerValue > 0) {
                // Currently hardcoded to 20.0, there is no way to get the player's max hunger
                float leftover = foodData.getFoodLevel() + hungerValue - 20.0f;
                foodData.eat((int) hungerValue, 0);

                if (conversionRate.isPresent() && leftover > 0) {
                    fillSaturationToCap(foodData, ability, foodData.getSaturationLevel() + (leftover * conversionRate.get().calculate(ability.level())));
                }
            } else if (hungerValue < 0) {
                // Exhaustion is 4 points per hunger depleted
                foodData.addExhaustion(hungerValue * 4);
            }
            fillSaturationToCap(foodData, ability, saturation.map(sat -> sat.calculate(ability.level())).orElse(0F));
        }
    }

    private void fillSaturationToCap(FoodData foodData, final DragonAbilityInstance ability, final float saturation) {
        if (maxSaturation.isPresent() && saturation > 0) {
            float cap = maxSaturation.get().calculate(ability.level());
            if (foodData.getSaturationLevel() < cap) {
                foodData.setSaturation(Math.min(cap, foodData.getSaturationLevel() + saturation));
            }
        } else {
            foodData.setSaturation(foodData.getSaturationLevel() + saturation);
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        MutableComponent description = Component.empty();

        hunger.ifPresent(value -> {
            float food = value.calculate(ability.level());
            if (food > 0) {
                description.append(Component.translatable(ADD_FOOD, food).append("\n"));
            } else if (food < 0) {
                description.append(Component.translatable(ADD_EXHAUSTION, food * -4).append("\n"));
            }
        });

        conversionRate.ifPresent(value -> description.append(Component.translatable(CONVERSION_RATE, value.calculate(ability.level()))).append("\n"));
        saturation.ifPresent(value -> description.append(Component.translatable(SATURATION, value.calculate(ability.level()))).append("\n"));
        maxSaturation.ifPresent(value -> description.append(Component.translatable(MAX_SATURATION, value.calculate(ability.level()))).append("\n"));
        return List.of(description);
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
