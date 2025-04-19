package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record ProjectileEntityPushEffect(
        TargetDirection targetDirection,
        LevelBasedValue pushForce
) implements ProjectileEntityEffect {
    public static final MapCodec<ProjectileEntityPushEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TargetDirection.CODEC.fieldOf("target_direction").forGetter(ProjectileEntityPushEffect::targetDirection),
            LevelBasedValue.CODEC.fieldOf("push_force").forGetter(ProjectileEntityPushEffect::pushForce)
    ).apply(instance, ProjectileEntityPushEffect::new));

    @Override
    public void apply(final Projectile projectile, Entity target, final int level) {
        if (projectile.level() instanceof ServerLevel) {
            if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.LOOKING_AT) {
                target.addDeltaMovement(projectile.getLookAngle().scale(pushForce.calculate(level)));
            } else if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.TOWARDS_ENTITY) {
                Vec3 offset = new Vec3(projectile.position().x, projectile.position().y, projectile.position().z()).subtract(target.position()).normalize();
                target.addDeltaMovement(offset.scale(pushForce.calculate(level)));
            } else if (targetDirection.direction().right().isPresent()) {
                target.addDeltaMovement(new Vec3(targetDirection.direction().right().get().step()).normalize().scale(this.pushForce.calculate(level)));
            }
        }
    }

    @Override
    public MapCodec<? extends ProjectileEntityEffect> codec() {
        return CODEC;
    }
}