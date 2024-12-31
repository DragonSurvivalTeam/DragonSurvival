package by.dragonsurvivalteam.dragonsurvival.common.items.food;

import by.dragonsurvivalteam.dragonsurvival.common.items.TooltipItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class CustomOnFinishEffectItem extends TooltipItem {
    private final @Nullable Consumer<LivingEntity> onEat;

    public CustomOnFinishEffectItem(final Item.Properties properties, final @Nullable String key, final @Nullable Consumer<LivingEntity> onEat) {
        super(properties, key);
        this.onEat = onEat;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (onEat != null) {
            onEat.accept(entity);
        }

        return super.finishUsingItem(stack, level, entity);
    }
}