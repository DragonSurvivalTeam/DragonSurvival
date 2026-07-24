package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.common.items.food.CustomOnFinishEffectItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;


public class ChargedCoalItem extends CustomOnFinishEffectItem {
    public ChargedCoalItem(Properties properties, @Nullable String key, @Nullable Consumer<LivingEntity> onEat) {
        super(properties, key, onEat);
    }

    @Override
    public int getBurnTime(@NotNull ItemStack itemStack, RecipeType<?> recipeType) {
        return 4000;
    }
}