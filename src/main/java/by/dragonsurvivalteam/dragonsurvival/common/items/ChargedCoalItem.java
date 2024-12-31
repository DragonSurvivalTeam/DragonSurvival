//package by.dragonsurvivalteam.dragonsurvival.common.items;
//
//import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
//import by.dragonsurvivalteam.dragonsurvival.common.items.food.DragonFoodItem;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.crafting.RecipeType;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.function.Consumer;
//
//public class ChargedCoalItem extends DragonFoodItem {
//    public ChargedCoalItem(Properties properties, AbstractDragonType dragonSpecies, Consumer<LivingEntity> onEat) {
//        super(properties, dragonSpecies, onEat);
//    }
//
//    @Override
//    public int getBurnTime(@NotNull ItemStack itemStack, RecipeType<?> recipeType) {
//        return 4000;
//    }
//}