package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public record TeleportEffect(
        TargetDirection targetDirection,
        LevelBasedValue maxDistance
) implements AbilityEntityEffect {
    public static final MapCodec<TeleportEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TargetDirection.CODEC.fieldOf("target_direction").forGetter(TeleportEffect::targetDirection),
            LevelBasedValue.CODEC.fieldOf("range").forGetter(TeleportEffect::maxDistance)
    ).apply(instance, TeleportEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target.level() instanceof ServerLevel serverLevel && serverLevel.isLoaded(target.getOnPos())) {
            if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.TOWARDS_ENTITY) {
                dragon.changeDimension(
                        new DimensionTransition(
                                serverLevel, target.getPosition(0), target.getDeltaMovement(), target.getYRot(), target.getXRot(), DimensionTransition.DO_NOTHING
                        )
                );
            }
            Vec3 direction = null;

            if (targetDirection.direction().right().isPresent()) {
                direction = new Vec3(targetDirection.direction().right().get().step()).scale(this.maxDistance.calculate(ability.level())).add(target.getEyePosition());
            } else if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.LOOKING_AT) {
                direction = dragon.getLookAngle().scale(this.maxDistance.calculate(ability.level())).add(dragon.getEyePosition());
            }

            if (direction != null) {
                BlockHitResult res = target.level().clip(new ClipContext(
                        dragon.getEyePosition(),
                        direction,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        dragon));

                // Displace the teleport point back 1 block to avoid teleporting entities into blocks
                Vec3 destination = new Vec3(res.getLocation().toVector3f()).add(new Vec3(res.getDirection().step()));

                target.changeDimension(
                        new DimensionTransition(
                                serverLevel, destination, target.getDeltaMovement(), target.getYRot(), target.getXRot(), DimensionTransition.DO_NOTHING
                        )
                );
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
