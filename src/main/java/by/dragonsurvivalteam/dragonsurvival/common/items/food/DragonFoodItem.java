//package by.dragonsurvivalteam.dragonsurvival.common.items.food;
//
//import by.dragonsurvivalteam.dragonsurvival.common.items.TooltipItem;
//import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
//import by.dragonsurvivalteam.dragonsurvival.util.Functions;
//import net.minecraft.world.effect.MobEffectInstance;
//import net.minecraft.world.effect.MobEffects;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.food.FoodProperties;
//import net.minecraft.world.food.FoodProperties.Builder;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.List;
//import java.util.function.Consumer;
//import java.util.function.Supplier;
//import javax.annotation.ParametersAreNonnullByDefault;
//
//@ParametersAreNonnullByDefault
//public class DragonFoodItem extends TooltipItem {
//    private final List<Supplier<MobEffectInstance>> effects;
//    private final @Nullable AbstractDragonType dragonSpecies;
//    private final @Nullable Consumer<LivingEntity> onEat;
//
//    public DragonFoodItem(Properties properties) {
//        this(properties, null, null, List.of());
//    }
//
//    public DragonFoodItem(Properties properties, @Nullable AbstractDragonType dragonSpecies, @Nullable Consumer<LivingEntity> onEat) {
//        this(properties, dragonSpecies, onEat, List.of());
//    }
//
//    public DragonFoodItem(Properties properties, @Nullable AbstractDragonType dragonSpecies, Supplier<MobEffectInstance> effectInstance) {
//        this(properties, dragonSpecies, null, List.of(effectInstance));
//    }
//
//    public DragonFoodItem(Properties properties, @Nullable AbstractDragonType dragonSpecies, List<Supplier<MobEffectInstance>> effectInstance) {
//        this(properties, dragonSpecies, null, effectInstance);
//    }
//
//    public DragonFoodItem(Properties properties, @Nullable AbstractDragonType dragonSpecies, @Nullable Consumer<LivingEntity> onEat, Supplier<MobEffectInstance> effectInstance) {
//        this(properties, dragonSpecies, onEat, List.of(effectInstance));
//    }
//
//    public DragonFoodItem(Properties properties, @Nullable AbstractDragonType dragonSpecies, @Nullable Consumer<LivingEntity> onEat, List<Supplier<MobEffectInstance>> effectInstances) {
//        super(properties.food(genFoodProperties(effectInstances)), null);
//        this.dragonSpecies = dragonSpecies;
//        this.effects = effectInstances;
//        this.onEat = onEat;
//    }
//
//    private static @NotNull FoodProperties genFoodProperties(List<Supplier<MobEffectInstance>> effectInstances) {
//        Builder builder = new Builder().nutrition(1).saturationModifier(0.4f).effect(() -> new MobEffectInstance(MobEffects.HUNGER, Functions.secondsToTicks(15)), 1);
//
//        if (!effectInstances.isEmpty()) {
//            builder.alwaysEdible();
//        }
//
//        return builder.build();
//    }
//
//    @Override
//    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
//        ItemStack result = super.finishUsingItem(stack, level, entity);
//
//        if (dragonSpecies == null || DragonUtils.isType(entity, dragonSpecies)) {
//            if (onEat != null) {
//                onEat.accept(entity);
//            }
//
//            if (!effects.isEmpty()) {
//                for (Supplier<MobEffectInstance> effect : effects) {
//                    entity.addEffect(effect.get());
//                }
//            }
//        }
//
//        return result;
//    }
//}