package by.dragonsurvivalteam.dragonsurvival.common.effects;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.jetbrains.annotations.NotNull;

public class Stress extends MobEffect {
    /** See initial value of {@link FoodData#foodLevel} */
    public static final int FULL_FOOD_LEVEL = 20;

    @ConfigRange(min = 0, max = 100)
    @Translation(key = "stress_exhaustion", type = Translation.Type.CONFIGURATION, comments = "The amount of exhaustion applied every 20 ticks while stressed. Each amplifier level cuts the delay in half.")
    @ConfigOption(side = ConfigSide.SERVER, category = {"effects", "stress"}, key = "stress_exhaustion")
    public static Float stressExhaustion = 1f;

    public Stress(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public boolean applyEffectTick(@NotNull final LivingEntity living, int amplifier) {
        if (living instanceof Player player) {
            FoodData food = player.getFoodData();

            if (food.getSaturationLevel() > 0) {
                int oldFood = food.getFoodLevel();
                food.eat(1, (-0.5f * food.getSaturationLevel()) * stressExhaustion);

                if (oldFood < FULL_FOOD_LEVEL) {
                    food.setFoodLevel((int) (food.getFoodLevel() - 1 * stressExhaustion));
                }
            }

            player.causeFoodExhaustion(1.0f * stressExhaustion);
            return true;
        }

        return false;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int i = 20 >> amplifier;

        if (i > 0) {
            return duration % i == 0;
        } else {
            return true;
        }
    }
}