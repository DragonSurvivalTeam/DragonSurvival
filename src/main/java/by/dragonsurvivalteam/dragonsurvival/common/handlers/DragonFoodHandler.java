package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

import javax.annotation.Nullable;

@EventBusSubscriber
public class DragonFoodHandler {
    @Translation(key = "dragon_food_is_required", type = Translation.Type.CONFIGURATION, comments = "Dragons will need to adhere to their diets if enabled")
    @ConfigOption(side = ConfigSide.SERVER, category = "food", key = "dragon_food_is_required")
    public static Boolean requireDragonFood = true;

    @ConfigRange(min = 0, max = 1)
    @Translation(key = "bad_food_poison_chance", type = Translation.Type.CONFIGURATION, comments = "Chance of getting poisoned from eating non-dragon food")
    @ConfigOption(side = ConfigSide.SERVER, category = {"food"}, key = "bad_food_poison_chance")
    public static Float badFoodPoisonChance = 0.5F;

    @ConfigRange(min = 0, max = 10_000)
    @Translation(key = "charged_soup_effect_duration", type = Translation.Type.CONFIGURATION, comments = "Determines the duration of the fire effect from eating charged soup - disabled if set to 0")
    @ConfigOption(side = ConfigSide.SERVER, category = {"food"}, key = "charged_soup_effect_duration")
    public static Integer chargedSoupBuffDuration = 300;

    public static @Nullable FoodProperties getDragonFoodProperties(final Item item, final Holder<DragonSpecies> type) {
        return getDragonFoodProperties(item.getDefaultInstance(), type);
    }

    public static @Nullable FoodProperties getDragonFoodProperties(final ItemStack stack, final Holder<DragonSpecies> type) {
        FoodProperties properties = type.value().getDiet(stack.getItem());

        if (properties != null) {
            return properties;
        }

        FoodProperties baseProperties = stack.getFoodProperties(null);

        if (baseProperties != null) {
            if (requireDragonFood) {
                return getBadFoodProperties();
            } else {
                return baseProperties;
            }
        }

        return null;
    }

    /** Checks if the item can be eaten (not whether it makes sense, see {@link DragonFoodHandler#getBadFoodProperties()}) */
    public static boolean isEdible(final ItemStack stack, final Holder<DragonSpecies> type) {
        if (stack.getFoodProperties(null) != null) {
            return true;
        }

        // The mixin in 'IItemExtensionMixin' would require player context so we check this separately here
        return type.value().getDiet(stack.getItem()) != null;
    }

    public static int getUseDuration(final ItemStack stack, final Player entity) {
        FoodProperties properties = getDragonFoodProperties(stack.getItem(), DragonStateProvider.getData(entity).species());

        if (properties != null) {
            return properties.eatDurationTicks();
        } else {
            return stack.getUseDuration(entity);
        }
    }

    @SubscribeEvent
    public static void setDragonFoodUseDuration(final LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon() || !DragonFoodHandler.isEdible(event.getItem(), data.species())) {
            return;
        }

        event.setDuration(getUseDuration(event.getItem(), player));
    }

    private static FoodProperties getBadFoodProperties() {
        FoodProperties.Builder builder = new FoodProperties.Builder();
        builder.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F);
        builder.effect(() -> new MobEffectInstance(MobEffects.POISON, 600, 0), badFoodPoisonChance);
        builder.nutrition(1);
        return builder.build();
    }
}