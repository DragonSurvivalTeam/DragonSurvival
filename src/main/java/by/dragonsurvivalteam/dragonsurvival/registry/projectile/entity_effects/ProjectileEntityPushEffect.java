package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record ProjectileEntityPushEffect(
        TargetDirection targetDirection,
        LevelBasedValue pushForce
) implements ProjectileEntityEffect {
    @Translation(comments = "Pulls targets towards the projectile with a strength of %s.")
    public static final String TOWARDS = Translation.Type.GUI.wrap("projectile_push_effect.towards");

    @Translation(comments = "Pushes targets away from the projectile with a strength of %s.")
    public static final String AWAY = Translation.Type.GUI.wrap("projectile_push_effect.away");

    @Translation(comments = "Pushes targets in the direction the projectile is travelling with a strength of %s")
    public static final String FACING = Translation.Type.GUI.wrap("projectile_push_effect.facing");

    @Translation(comments = "Pushes targets %s with a strength of %s.")
    public static final String DIRECTIONAL = Translation.Type.GUI.wrap("projectile_push_effect.directional");

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
    public List<MutableComponent> getDescription(final Player dragon, final int level) {
        if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.LOOKING_AT) {
            return List.of(Component.translatable(FACING, DSColors.dynamicValue(pushForce.calculate(level))));
        } else if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.TOWARDS_ENTITY) {
            if (pushForce.calculate(level) > 0) {
                return List.of(Component.translatable(TOWARDS, DSColors.dynamicValue(pushForce.calculate(level))));
            } else if (pushForce.calculate(level) < 0) {
                return List.of(Component.translatable(AWAY, DSColors.dynamicValue(pushForce.calculate(level))));
            }
        } else if (targetDirection.direction().right().isPresent()) {
            return List.of(Component.translatable(DIRECTIONAL, DSColors.dynamicValue(pushForce.calculate(level))));
        }

        return List.of();
    }

    @Override
    public MapCodec<? extends ProjectileEntityEffect> codec() {
        return CODEC;
    }
}