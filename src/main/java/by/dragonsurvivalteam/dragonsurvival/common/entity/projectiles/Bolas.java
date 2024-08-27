package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import javax.annotation.Nullable;

import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class Bolas extends AbstractArrow {
	public Bolas(Level world){
		super(DSEntities.BOLAS_ENTITY.get(), world);
	}

	public Bolas(
			double pX,
			double pY,
			double pZ,
			Level pLevel,
			ItemStack pPickupItemStack,
			@Nullable ItemStack pFiredFromWeapon
	) {
        super(DSEntities.BOLAS_ENTITY.value(), pX, pY, pZ, pLevel, pPickupItemStack, pFiredFromWeapon);
    }
	//@Override
	//protected Item getDefaultItem(){
	//	return DSItems.HUNTING_NET.value();
	//}

	@Override
	protected void onHit(HitResult result){
		super.onHit(result);
		if(!level().isClientSide()){
			remove(RemovalReason.DISCARDED);
		}
	}

	protected void onHitEntity(EntityHitResult entityHitResult){
		Entity entity = entityHitResult.getEntity();
		if(!entity.level().isClientSide()) {
			if(entity instanceof LivingEntity living){
				living.hurt(this.damageSources().arrow(this, this.getOwner()), 1.0f);
				living.addEffect(new MobEffectInstance(DSEffects.TRAPPED, Functions.secondsToTicks(ServerConfig.hunterTrappedDebuffDuration), 0, false, false));
			}
		}
	}

	@Override
	protected @NotNull ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.ARROW);
	}
}