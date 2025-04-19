package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.TargetDirection;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record PushEffect(
        TargetDirection targetDirection,
        LevelBasedValue pushForce
) implements AbilityEntityEffect {
    @Translation(comments = "Pulls targets towards you with a strength of %s.")
    public static final String TOWARDS = Translation.Type.GUI.wrap("push_effect.towards");

    @Translation(comments = "Pushes targets away from you with a strength of %s.")
    public static final String AWAY = Translation.Type.GUI.wrap("push_effect.away");

    @Translation(comments = "Pushes targets in the direction you are facing with a strength of %s")
    public static final String FACING = Translation.Type.GUI.wrap("push_effect.facing");

    @Translation(comments = "Pushes targets %s with a strength of %s.")
    public static final String DIRECTIONAL = Translation.Type.GUI.wrap("push_effect.directional");

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

    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.LOOKING_AT) {
            return List.of(Component.translatable(FACING, DSColors.dynamicValue(pushForce.calculate(ability.level()))));
        } else if (targetDirection.direction().left().orElse(null) == TargetDirection.Type.TOWARDS_ENTITY) {
            if (pushForce.calculate(ability.level()) > 0) {
                return List.of(Component.translatable(TOWARDS, DSColors.dynamicValue(pushForce.calculate(ability.level()))));
            } else if (pushForce.calculate(ability.level()) < 0) {
                return List.of(Component.translatable(AWAY, DSColors.dynamicValue(pushForce.calculate(ability.level()))));
            }
        } else if (targetDirection.direction().right().isPresent()) {
            return List.of(Component.translatable(DIRECTIONAL, DSColors.dynamicValue(pushForce.calculate(ability.level()))));
        }

        return List.of();
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
