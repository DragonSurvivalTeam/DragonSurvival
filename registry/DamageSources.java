package by.dragonsurvivalteam.dragonsurvival.registry;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Fireball;

import javax.annotation.Nullable;

public class DamageSources{
	public static final DamageSource STAR_DRAIN = new DamageSource("starDrain").bypassArmor().bypassMagic();
	public static final DamageSource WATER_BURN = new DamageSource("waterBurn").bypassArmor();
	public static final DamageSource RAIN_BURN = new DamageSource("rainBurn").bypassArmor();
	public static final DamageSource DEHYDRATION = new DamageSource("dehydration").bypassArmor();
	public static final DamageSource CRUSHED = new DamageSource("crushed");

	public static DamageSource dragonBallLightning(Fireball pFireball, @Nullable Entity pIndirectEntity) {
		return pIndirectEntity == null ? (new IndirectEntityDamageSource("onFire", pFireball, pFireball)).setProjectile() : (new IndirectEntityDamageSource("fireball", pFireball, pIndirectEntity)).setProjectile();
	}
}