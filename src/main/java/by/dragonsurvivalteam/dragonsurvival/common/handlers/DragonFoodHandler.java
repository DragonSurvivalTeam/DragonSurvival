package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryCache;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

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

    public static @Nullable FoodProperties getDragonFoodProperties(final Holder<DragonSpecies> species, final ItemStack stack, @Nullable final FoodProperties original) {
        if (dragonFoodHandlingIsDisabled()) {
            return original;
        }

        if (DietEntryCache.isEmpty(species)) {
            return original;
        }

        FoodProperties properties = DietEntryCache.getDiet(species, stack.getItem());

        if (properties != null) {
            return properties;
        }

        if (original != null) {
            if (requireDragonFood) {
                return getBadFoodProperties();
            } else {
                return original;
            }
        }

        return null;
    }

    /** Checks if the item can be eaten (not whether it makes sense, see {@link DragonFoodHandler#getBadFoodProperties()}) */
    public static boolean isEdible(final Player player, final ItemStack stack) {
        return stack.getFoodProperties(player) != null;
    }

    public static int getUseDuration(final ItemStack stack, final Player entity, int original) {
        FoodProperties properties = getDragonFoodProperties(DragonStateProvider.getData(entity).species(), stack, null);

        if (properties != null) {
            return properties.eatDurationTicks();
        } else {
            return original;
        }
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

    private static FoodProperties getBadFoodProperties() {
        FoodProperties.Builder builder = new FoodProperties.Builder();
        builder.effect(() -> new MobEffectInstance(MobEffects.HUNGER, 600, 0), 1.0F);
        builder.effect(() -> new MobEffectInstance(MobEffects.POISON, 600, 0), badFoodPoisonChance);
        builder.nutrition(1);
        return builder.build();
    }

    /** Diet food that should be applied when a dragon eats from a placed food block (marked on right-click, consumed on 'FoodData#eat') */
    private static final Map<FoodData, PendingBlockFood> PENDING_BLOCK_FOOD = new WeakHashMap<>();

    private record PendingBlockFood(Player player, FoodProperties food) { }

    private static void markBlockFood(final Player player, final FoodProperties diet) {
        PENDING_BLOCK_FOOD.put(player.getFoodData(), new PendingBlockFood(player, diet));
    }

    public static void clearBlockFood(final FoodData foodData) {
        PENDING_BLOCK_FOOD.remove(foodData);
    }

    /** Returns and removes the marked diet food for the food data, or 'null' if there is none */
    private static @Nullable PendingBlockFood consumeBlockFood(final FoodData foodData) {
        return PENDING_BLOCK_FOOD.remove(foodData);
    }

    /** Called from 'FoodDataMixin' when a dragon eats from a placed food block that applies its food values directly*/
    public static boolean applyPendingBlockFood(final FoodData foodData) {
        PendingBlockFood pending = consumeBlockFood(foodData);

        if (pending == null) {
            return false;
        }

        foodData.eat(pending.food());

        Player player = pending.player();

        if (player != null && !player.level().isClientSide()) {
            for (FoodProperties.PossibleEffect possibleEffect : pending.food().effects()) {
                if (possibleEffect.effect() != null && player.getRandom().nextFloat() < possibleEffect.probability()) {
                    player.addEffect(possibleEffect.effect());
                }
            }
        }

        return true;
    }

    @SubscribeEvent
    public static void markFoodBlocks(final PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        // Reset a possible marker from a previous interaction that did not result in eating
        clearBlockFood(player.getFoodData());

        if (dragonFoodHandlingIsDisabled() || player.isSpectator() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        Item foodItem = event.getLevel().getBlockState(event.getPos()).getBlock().asItem();

        if (foodItem == Items.AIR) {
            return;
        }

        FoodProperties diet = DietEntryCache.getDiet(DragonStateProvider.getData(player).species(), foodItem);

        if (diet != null) {
            markBlockFood(player, diet);
        }
    }
}