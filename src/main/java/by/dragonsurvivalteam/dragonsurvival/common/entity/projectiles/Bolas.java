package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class Bolas extends ThrowableItemProjectile{
	public static final UUID DISABLE_MOVEMENT = UUID.fromString("eab67409-4834-43d8-bdf6-736dc96375f2");
	public static final UUID DISABLE_JUMP = UUID.fromString("d7c976cd-edba-46aa-9002-294d429d7741");

	public Bolas(Level world){
		super(DSEntities.BOLAS_ENTITY, world);
	}

	public Bolas(double p_i50156_2_, double p_i50156_4_, double p_i50156_6_, Level world){
		super(DSEntities.BOLAS_ENTITY, p_i50156_2_, p_i50156_4_, p_i50156_6_, world);
	}

	public Bolas(LivingEntity shooter, Level world){
		super(DSEntities.BOLAS_ENTITY, shooter, world);
	}


	@Override
	protected Item getDefaultItem(){
		return DSItems.huntingNet;
	}

	@Override
	protected void defineSynchedData(){

	}

	@Override
	protected void onHit(HitResult p_70227_1_){
		super.onHit(p_70227_1_);
		if(!level().isClientSide()){
			remove(RemovalReason.DISCARDED);
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(){
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	protected void onHit(EntityHitResult entityHitResult){
		super.onHit(entityHitResult);
		Entity entity = entityHitResult.getEntity();
		if(!entity.level().isClientSide()){
			if(entity instanceof LivingEntity living){
				AttributeInstance attributeInstance = living.getAttribute(Attributes.MOVEMENT_SPEED);
				AttributeModifier bolasTrap = new AttributeModifier(DISABLE_MOVEMENT, "Bolas trap", -attributeInstance.getValue(), AttributeModifier.Operation.ADDITION);
				boolean addEffect = false;
				if(!attributeInstance.hasModifier(bolasTrap)){
					attributeInstance.addTransientModifier(bolasTrap);
					addEffect = true;
				}

				AttributeInstance jump = living.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
				if(jump != null){
					AttributeModifier disableJump = new AttributeModifier(DISABLE_JUMP, "Jump debuff", 3, AttributeModifier.Operation.MULTIPLY_TOTAL);
					if(!jump.hasModifier(disableJump)){
						jump.addTransientModifier(disableJump);
						addEffect = true;
					}
				}
				if(addEffect){
					living.addEffect(new MobEffectInstance(DragonEffects.TRAPPED, Functions.secondsToTicks(20)));
				}
			}
		}
	}
}