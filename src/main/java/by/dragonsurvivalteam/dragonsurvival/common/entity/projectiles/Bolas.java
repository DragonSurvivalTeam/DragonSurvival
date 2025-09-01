package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
public class Bolas extends AbstractArrow {
    public Bolas(Level world) {
        super(DSEntities.BOLAS_ENTITY.get(), world);
    }

    public Bolas(double x, double y, double z, final Level level, final ItemStack pickup, @Nullable final ItemStack firedFrom) {
        super(DSEntities.BOLAS_ENTITY.value(), x, y, z, level, pickup, firedFrom);
    }

    public Bolas(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(DSEntities.BOLAS_ENTITY.value(), owner, level, pickupItemStack, firedFromWeapon);
    }

    @SubscribeEvent
    public static void causeFall(final EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity && entity.hasEffect(DSEffects.TRAPPED)) {
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.25, 0));
        }
    }

    @Override
    protected void onHit(@NotNull final HitResult result) {
        super.onHit(result);
    }

    @Override
    protected void onHitEntity(final EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();

        if (!entity.level().isClientSide()) {
            if (entity instanceof LivingEntity living) {
                living.hurt(damageSources().arrow(this, getOwner()), 1);

                if (ServerConfig.hunterTrappedDebuffDuration > 0) {
                    living.addEffect(new MobEffectInstance(DSEffects.TRAPPED, Functions.secondsToTicks(ServerConfig.hunterTrappedDebuffDuration), 0, false, false), getOwner());
                }
            }
        }

        super.onHitEntity(entityHitResult);
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(Items.ARROW);
    }
}