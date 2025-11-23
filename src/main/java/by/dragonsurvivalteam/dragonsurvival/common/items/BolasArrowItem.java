package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BolasArrowItem extends ArrowItem {
    public BolasArrowItem(Properties pProperties) {
        super(pProperties);
    }

    public @NotNull AbstractArrow createArrow(@NotNull Level level, @NotNull ItemStack ammo, @NotNull LivingEntity shooter, @Nullable ItemStack weapon) {
        return new Bolas(shooter, level, new ItemStack(Items.ARROW), weapon);
    }

    @Override
    public @NotNull Projectile asProjectile(@NotNull Level level, Position position, @NotNull ItemStack stack, @NotNull Direction direction) {
        return new Bolas(position.x(), position.y(), position.z(), level, stack, null);
    }
}
