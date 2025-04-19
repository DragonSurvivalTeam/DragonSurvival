package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

public record PushEffect(
        TargetDirection targetDirection,
        LevelBasedValue pushForce
) implements AbilityEntityEffect {
    public static final MapCodec<PushEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TargetDirection.CODEC.fieldOf("target_direction").forGetter(PushEffect::targetDirection),
            LevelBasedValue.CODEC.fieldOf("push_force").forGetter(PushEffect::pushForce)
    ).apply(instance, PushEffect::new));

    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, Entity target) {
        if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.LOOKING_AT) {
            target.addDeltaMovement(dragon.getLookAngle().scale(pushForce.calculate(ability.level())));
        } else if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.TOWARDS_ENTITY) {
            Vec3 offset = dragon.getEyePosition().subtract(target.getEyePosition()).normalize();
            target.addDeltaMovement(offset.scale(pushForce.calculate(ability.level())));
        } else if (targetDirection.direction().right().isPresent()) {
            target.addDeltaMovement(new Vec3(targetDirection.direction().right().get().step()).normalize().scale(this.pushForce.calculate(ability.level())));
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
