package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;


import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaDragon.LargeLightningParticleData;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.BallLightningAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.StormBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import by.dragonsurvivalteam.dragonsurvival.util.TargetingFunctions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import javax.annotation.Nullable;
import java.util.List;

public class BallLightningEntity extends DragonBallEntity{
	protected boolean isLingering = false;
	protected int lingerTicks = 100;
	protected LargeLightningParticleData trail = new LargeLightningParticleData(37, false);
	public BallLightningEntity(Level p_i50168_9_, LivingEntity p_i50168_2_, double p_i50168_3_, double p_i50168_5_, double p_i50168_7_){
		super(DSEntities.BALL_LIGHTNING.get(), p_i50168_2_, p_i50168_3_, p_i50168_5_, p_i50168_7_, p_i50168_9_);
	}

	public BallLightningEntity(EntityType<? extends Fireball> p_i50166_1_, Level p_i50166_2_){
		super(p_i50166_1_, p_i50166_2_);
	}

	@Override
	protected ParticleOptions getTrailParticle(){
		return trail;
	}

	@Override
	protected boolean shouldBurn(){
		return false;
	}

	@Override
	public float getExplosivePower(){
		return getSkillLevel() / 1.25f;
	}

	@Override
	protected DamageSource getDamageSource(Fireball pFireball, @Nullable Entity pIndirectEntity){
		// This damage source is used since it is specifically not fire damage, so that cave dragons don't ignore the ball lightning damage
		return DSDamageTypes.damageSource(pFireball.level(), DSDamageTypes.DRAGON_BALL_LIGHTNING);
	}

	protected void onHitCommon() {
		if ((getOwner() == null || !getOwner().isRemoved()) && this.level().hasChunkAt(this.blockPosition())) {
			if (!this.level().isClientSide) {
				level().playSound(null, getX(), getY(), getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 3.0F, 0.5f);
			}

			isLingering = true;
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult hitResult){
		super.onHitEntity(hitResult);
		onHitCommon();
	}

	@Override
	protected void onHitBlock(BlockHitResult hitResult){
		super.onHitBlock(hitResult);
		onHitCommon();
	}

	@Override
	public void tick() {
		super.tick();
		if (level().getGameTime() % 5 == 0) // Once per 5 ticks (0.25 seconds)
			attackMobs();
		if(isLingering) {
			lingerTicks--;
			if(lingerTicks <= 0) {
				this.discard();
			}
		}
	}

	@Override
	public @NotNull Vec3 getDeltaMovement() {
		if (isLingering) {
			return Vec3.ZERO;
		}

		return super.getDeltaMovement();
	}

	public void attackMobs(){
		int range = 4;
		Entity owner = getOwner();
		DamageSource source;
		
		if (owner instanceof Player) {
			range = DragonAbilities.getSelfAbility((Player) owner, BallLightningAbility.class).getRange();
			source = owner.damageSources().playerAttack((Player) owner);
		} else {
            source = damageSources().lightningBolt();
        }

        List<Entity> entities = level().getEntities(owner, new AABB(position().x - range, position().y - range, position().z - range, position().x + range, position().y + range, position().z + range));
		entities.removeIf(e -> e instanceof BallLightningEntity);
		entities.removeIf(e -> !(e instanceof LivingEntity));

		for(Entity ent : entities){
			if(!level().isClientSide()){
				TargetingFunctions.attackTargets(owner, ent1 -> ent1.hurt(source, BallLightningAbility.getDamage(getSkillLevel())), ent);

				if(ent instanceof LivingEntity livingEntity){
					if(livingEntity.getRandom().nextInt(100) < 40){
						if(!livingEntity.level().isClientSide() && !StormBreathAbility.chargedBlacklist.contains(ResourceHelper.getKey(livingEntity).toString())){
							livingEntity.addEffect(new MobEffectInstance(DragonEffects.CHARGED, Functions.secondsToTicks(10), 0, false, true));
						}
					}

					level().playLocalSound(blockPosition(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 2.0F, 0.5f, true);
				}

				if(owner instanceof LivingEntity livingEntity) {
					doEnchantDamageEffects(livingEntity, ent);
				}
			}

			if (level().isClientSide()) {
				// Creates a trail of particles between the entity and target(s)
				int steps = 10;
				float stepSize = 1.f / steps;
				Vec3 distV = new Vec3(getX() - ent.getX(), getY() - ent.getY(), getZ() - ent.getZ());
				for (int i = 0; i < steps; i++) {
					// the current entity coordinate + ((the distance between it and the target) * (the fraction of the total))
					Vec3 step = ent.position().add(distV.scale(stepSize * i));
					level().addParticle(new LargeLightningParticleData(16F, false), step.x(), step.y(), step.z(), 0.0, 0.0, 0.0);
				}
			}
		}

		if (!level().isClientSide) {
			if (level().isThundering()) {
				if (level().random.nextInt(100) < 30) {
					if (level().canSeeSky(blockPosition())) {
						LightningBolt lightningboltentity = EntityType.LIGHTNING_BOLT.create(level());
						lightningboltentity.moveTo(new Vec3(position().x, position().y, position().z));

						level().addFreshEntity(lightningboltentity);
					}
				}
			}
		}
	}
	@Override
	public PlayState predicate(final AnimationState<DragonBallEntity> state) {
		if(!isLingering) {
			state.getController().setAnimation(FLY);
			return PlayState.CONTINUE;
		} else if (lingerTicks < 16) {
			state.getController().setAnimation(EXPLOSION);
		} else {
			state.getController().setAnimation(IDLE);
		}

		return PlayState.CONTINUE;
	}

	private static final RawAnimation EXPLOSION = RawAnimation.begin().thenLoop("explosion");
	private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
	private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
}