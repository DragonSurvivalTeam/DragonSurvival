package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
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

import javax.annotation.Nullable;

public class BolasArrowItem extends ArrowItem {
    public BolasArrowItem(Properties pProperties) {
        super(pProperties);
    }

    public AbstractArrow createArrow(Level pLevel, ItemStack pAmmo, LivingEntity pShooter, @Nullable ItemStack pWeapon) {
        return new Bolas(pShooter.getX(), pShooter.getY(), pShooter.getZ(), pShooter.level(), new ItemStack(Items.ARROW), pWeapon);
    }

    @Override
    public @NotNull Projectile asProjectile(@NotNull Level pLevel, Position pPos, @NotNull ItemStack pStack, @NotNull Direction pDirection) {
        return new Bolas(pPos.x(), pPos.y(), pPos.z(), pLevel, pStack, null);
    }
}
