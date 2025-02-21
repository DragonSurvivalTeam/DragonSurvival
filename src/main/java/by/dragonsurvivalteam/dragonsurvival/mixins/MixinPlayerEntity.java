package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;


@Mixin(Player.class)
public abstract class MixinPlayerEntity extends LivingEntity{
	private static final UUID SLOW_FALLING_ID = UUID.fromString("A5B6CF2A-2F7C-31EF-9022-7C3E7D5E6ABA");
	private static final AttributeModifier SLOW_FALLING = new AttributeModifier(SLOW_FALLING_ID, "Slow falling acceleration reduction", -0.07, AttributeModifier.Operation.ADDITION); // Add -0.07 to 0.08 so we get the vanilla default of 0.01

	protected MixinPlayerEntity(EntityType<? extends LivingEntity> p_20966_, Level p_20967_){
		super(p_20966_, p_20967_);
	}

	@Inject( method = "isInvulnerableTo", at = @At( "HEAD" ), cancellable = true )
	public void isInvulnerableTo(DamageSource pSource, CallbackInfoReturnable<Boolean> cir){
		if(pSource == DamageSource.IN_WALL && DragonUtils.isDragon(this)){
			if(ServerConfig.disableSuffocation){
				cir.setReturnValue(true);
			}
		}
	}

	@Inject( method = "isImmobile", at = @At( "HEAD" ), cancellable = true )
	private void castMovement(CallbackInfoReturnable<Boolean> ci){
		DragonStateHandler cap = DragonUtils.getHandler(this);

		if(!isDeadOrDying() && !isSleeping()){
			if(cap.getMagicData().getCurrentlyCasting() != null){
				if(cap.getMagicData().getCurrentlyCasting().requiresStationaryCasting()){
					if(!ServerConfig.canMoveWhileCasting){
						ci.setReturnValue(true);
					}
				}
			}

			if(Arrays.stream(cap.getEmoteData().currentEmotes).noneMatch(Objects::nonNull)){
				if(!ServerConfig.canMoveInEmote){
					ci.setReturnValue(true);
				}
			}
		}
	}

	@Inject( method = "isSleepingLongEnough", at = @At( "HEAD" ), cancellable = true )
	public void isSleepingLongEnough(CallbackInfoReturnable<Boolean> ci){
		if(DragonUtils.isDragon(this)){
			DragonStateProvider.getCap(this).ifPresent(cap -> {
				if(cap.treasureResting && cap.treasureSleepTimer >= 100){
					ci.setReturnValue(true);
				}
			});
		}
	}

	@Inject(at = @At("HEAD"), method = "eat", cancellable = true)
	public void dragonEat(final Level level, final ItemStack itemStack, final CallbackInfoReturnable<ItemStack> callback) {
		if (DragonFoodHandler.disableDragonFoodHandling) {
			return;
		}

		DragonStateProvider.getCap(this).ifPresent(handler -> {
			if (handler.isDragon()) {
				DragonFoodHandler.dragonEat(getFoodData(), itemStack.getItem(), handler.getType());
				awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
				level.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
				callback.setReturnValue(super.eat(level, itemStack)); // TODO :: Not sure why it's handled this way - are all dragon foods correctly tagged as edible?
			}
		});
	}

	@Shadow
	public FoodData getFoodData(){
		throw new IllegalStateException("Mixin failed to shadow getFoodData()");
	}

	@Shadow
	public void awardStat(Stat<Item> stat){
		throw new IllegalStateException("Mixin failed to shadow awardStat()");
	}

	@Inject( at = @At( "HEAD" ), method = "Lnet/minecraft/world/entity/player/Player;getMyRidingOffset()D", cancellable = true )
	public void dragonRidingOffset(CallbackInfoReturnable<Double> ci){
		DragonStateProvider.getCap(this).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				ci.setReturnValue(0.25D);
			}
		});
	}

	@Inject( method = "travel", at = @At( "HEAD" ))
	public void travel(Vec3 pTravelVector, CallbackInfo ci){
		if(DragonUtils.isDragon(this)){
			double d01 = getX();
			double d11 = getY();
			double d21 = getZ();
			if(DragonStateProvider.getCap(this).isPresent() && ServerConfig.bonuses && ServerConfig.caveLavaSwimming && DragonUtils.isDragonType(this, DragonTypes.CAVE) && DragonSizeHandler.getOverridePose(this) == Pose.SWIMMING || isSwimming() && !isPassenger()){
				double d3 = getLookAngle().y;
				double d4 = d3 < -0.2D ? 0.185D : 0.06D;
				if(d3 <= 0.0D || jumping || !level.getBlockState(new BlockPos(getX(), getY() + 1.0D - 0.1D, getZ())).getFluidState().isEmpty()){
					Vec3 vector3d1 = getDeltaMovement();
					setDeltaMovement(vector3d1.add(0.0D, (d3 - vector3d1.y) * d4, 0.0D));
				}

				if(isEffectiveAi() || isControlledByLocalInstance()){
					double d0 = 0.08D;
					AttributeInstance gravity = getAttribute(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get());
					boolean flag = getDeltaMovement().y <= 0.0D;
					if(flag && hasEffect(MobEffects.SLOW_FALLING)){
						if(gravity != null && !gravity.hasModifier(SLOW_FALLING)){
							gravity.addTransientModifier(SLOW_FALLING);
						}
						fallDistance = 0.0F;
					}else if(gravity != null && gravity.hasModifier(SLOW_FALLING)){
						gravity.removeModifier(SLOW_FALLING);
					}
					if (gravity != null)
						d0 = gravity.getValue();

					FluidState fluidstate = level.getFluidState(blockPosition());
					if(isInLava() && isAffectedByFluids() && !canStandOnFluid(fluidstate)
					   && ServerConfig.bonuses && ServerConfig.caveLavaSwimming && DragonUtils.isDragonType(this, DragonTypes.CAVE)){
						double d8 = getY();
						float f5 = isSprinting() ? 0.9F : getWaterSlowDown();
						float f6 = 0.05F;
						float f7 = Math.min(3, (float)EnchantmentHelper.getDepthStrider(this));

						if(!onGround){
							f7 *= 0.5F;
						}

						if(f7 > 0.0F){
							f5 += (0.54600006F - f5) * f7 / 2.5F;
							f6 += (getSpeed() - f6) * f7 / 2.5F;
						}

						if(hasEffect(MobEffects.DOLPHINS_GRACE)){
							f5 = 0.96F;
						}

						f6 *= (float)getAttribute(net.minecraftforge.common.ForgeMod.SWIM_SPEED.get()).getValue();
						moveRelative(f6, pTravelVector);
						move(MoverType.SELF, getDeltaMovement());
						Vec3 vector3d6 = getDeltaMovement();
						if(horizontalCollision && onClimbable()){
							vector3d6 = new Vec3(vector3d6.x, 0.2D, vector3d6.z);
						}

						setDeltaMovement(vector3d6.multiply(f5, 0.8F, f5));
						Vec3 vector3d2 = getFluidFallingAdjustedMovement(d0, flag, getDeltaMovement());
						setDeltaMovement(vector3d2);
						if(horizontalCollision && isFree(vector3d2.x, vector3d2.y + (double)0.6F - getY() + d8, vector3d2.z)){
							setDeltaMovement(vector3d2.x, 0.3F, vector3d2.z);
						}
					}
				}

				calculateEntityAnimation(this, this instanceof FlyingAnimal);
				checkMovementStatistics(getX() - d01, getY() - d11, getZ() - d21);
			}
		}
	}



	@Shadow
	public void checkMovementStatistics(double d, double e, double f){
		throw new IllegalStateException("Mixin failed to shadow checkMovementStatistics()");
	}
}