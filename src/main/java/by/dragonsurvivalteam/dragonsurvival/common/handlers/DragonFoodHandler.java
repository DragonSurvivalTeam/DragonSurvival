package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonFood;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryCache;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public class DragonFoodHandler {
    @Translation(key = "disable_dragon_food_handling", type = Translation.Type.CONFIGURATION, comments = "Disable all modifications that dragon survival does to the food system. Some mods will have this setting automatically enabled (such as TFC).")
    @ConfigOption(side = ConfigSide.SERVER, category = "food", key = "disable_dragon_food_handling")
    public static Boolean disableDragonFoodHandling = false;

    @Translation(key = "dragon_food_is_required", type = Translation.Type.CONFIGURATION, comments = "Dragons will need to adhere to their diets if enabled")
    @ConfigOption(side = ConfigSide.SERVER, category = "food", key = "dragon_food_is_required")
    public static Boolean requireDragonFood = true;

    @ConfigRange(min = 0, max = 1)
    @Translation(key = "bad_food_poison_chance", type = Translation.Type.CONFIGURATION, comments = "Chance of getting poisoned from eating non-dragon food")
    @ConfigOption(side = ConfigSide.SERVER, category = "food", key = "bad_food_poison_chance")
    public static Float badFoodPoisonChance = 0.5F;

    public static boolean dragonFoodHandlingIsDisabled() {
        return disableDragonFoodHandling || ModID.TFC.isLoaded();
    }

    public static @Nullable FoodProperties getFoodProperties(final @Nullable LivingEntity entity, final ItemStack stack, final @Nullable FoodProperties original) {
        DragonFood dragonFood = getDragonFood(entity, stack, original, stack.get(DataComponents.CONSUMABLE), stack.get(DataComponents.USE_REMAINDER));
        return dragonFood != null ? dragonFood.properties() : original;
    }

    public static @Nullable Consumable getConsumable(final @Nullable LivingEntity entity, final ItemStack stack, final @Nullable Consumable original) {
        DragonFood dragonFood = getDragonFood(entity, stack, stack.get(DataComponents.FOOD), original, stack.get(DataComponents.USE_REMAINDER));
        return dragonFood != null ? dragonFood.consumable() : original;
    }

    public static @Nullable UseRemainder getUseRemainder(final @Nullable LivingEntity entity, final ItemStack stack, final @Nullable UseRemainder original) {
        DragonFood dragonFood = getDragonFood(entity, stack, stack.get(DataComponents.FOOD), stack.get(DataComponents.CONSUMABLE), original);
        return dragonFood != null ? dragonFood.useRemainder().orElse(null) : original;
    }

    /** Checks if the item can be eaten (not whether it makes sense, see {@link DragonFoodHandler#getBadFoodProperties()}) */
    public static boolean isEdible(final Player player, final ItemStack stack) {
        return getDragonFood(player, stack, stack.get(DataComponents.FOOD), stack.get(DataComponents.CONSUMABLE), stack.get(DataComponents.USE_REMAINDER)) != null;
    }

    public static int getUseDuration(final ItemStack stack, final Player entity, int original) {
        Consumable consumable = getConsumable(entity, stack, stack.get(DataComponents.CONSUMABLE));
        return consumable != null ? consumable.consumeTicks() : original;
    }

    @SubscribeEvent
    public static void setDragonFoodUseDuration(final LivingEntityUseItemEvent.Start event) {
        if (DragonFoodHandler.dragonFoodHandlingIsDisabled() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);
        if (!data.isDragon() || !DragonFoodHandler.isEdible(player, event.getItem())) {
            return;
        }

        event.setDuration(getUseDuration(event.getItem(), player, event.getDuration()));
    }

    private static @Nullable DragonFood getDragonFood(final @Nullable LivingEntity entity, final ItemStack stack, final @Nullable FoodProperties originalFood, final @Nullable Consumable originalConsumable, final @Nullable UseRemainder originalUseRemainder) {
        if (!(entity instanceof Player player)) {
            return originalFood != null ? createFood(originalFood, originalConsumable, originalUseRemainder) : null;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);
        if (!data.isDragon()) {
            return originalFood != null ? createFood(originalFood, originalConsumable, originalUseRemainder) : null;
        }

        return getDragonFood(data.species(), stack, originalFood, originalConsumable, originalUseRemainder);
    }

    private static @Nullable DragonFood getDragonFood(
        final Holder<DragonSpecies> species,
        final ItemStack stack,
        final @Nullable FoodProperties originalFood,
        final @Nullable Consumable originalConsumable,
        final @Nullable UseRemainder originalUseRemainder
    ) {
        if (dragonFoodHandlingIsDisabled()) {
            return originalFood != null ? createFood(originalFood, originalConsumable, originalUseRemainder) : null;
        }

        DragonFood properties = DietEntryCache.getDiet(species, stack.getItem());
        if (properties != null) {
            return properties;
        }

        if (originalFood == null) {
            return null;
        }

        if (requireDragonFood) {
            return getBadFood(originalConsumable, originalUseRemainder);
        }

        return createFood(originalFood, originalConsumable, originalUseRemainder);
    }

    private static DragonFood createFood(final FoodProperties properties, final @Nullable Consumable originalConsumable, final @Nullable UseRemainder originalUseRemainder) {
        Consumable consumable = originalConsumable != null ? originalConsumable : Consumables.DEFAULT_FOOD;
        return new DragonFood(properties, consumable, Optional.ofNullable(originalUseRemainder));
    }

    private static DragonFood getBadFood(final @Nullable Consumable originalConsumable, final @Nullable UseRemainder originalUseRemainder) {
        Consumable baseConsumable = originalConsumable != null ? originalConsumable : Consumables.DEFAULT_FOOD;
        List<ConsumeEffect> effects = new ArrayList<>();
        effects.add(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F));
        effects.add(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 600, 0), badFoodPoisonChance));

        return new DragonFood(
            new FoodProperties(1, 0.0F, false),
            new Consumable(
                baseConsumable.consumeSeconds(),
                baseConsumable.animation(),
                baseConsumable.sound(),
                baseConsumable.hasConsumeParticles(),
                effects
            ),
            Optional.ofNullable(originalUseRemainder)
        );
    }

    private static FoodProperties getBadFoodProperties() {
        return new FoodProperties(1, 0.0F, false);
    }
}
