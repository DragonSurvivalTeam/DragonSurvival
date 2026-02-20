package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.common.items.food.CustomOnFinishEffectItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;


public class ChargedCoalItem extends CustomOnFinishEffectItem {
    public ChargedCoalItem(Properties properties, @Nullable String key, @Nullable Consumer<LivingEntity> onEat) {
        super(properties, key, onEat);
    }

    // TODO :: This shouldn't be hardcoded inside of the item class, instead it should use a data map. See the default function here for more info.
    @Override
    public int getBurnTime(@NotNull ItemStack itemStack, @Nullable RecipeType<?> recipeType, @NotNull FuelValues fuelValues) {
        return 4000;
    }
}