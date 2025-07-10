package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.PassiveActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.ActivationTrigger;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record SelfTarget(Either<BlockTargeting, EntityTargeting> target) implements AbilityTargeting {
    @Translation(comments = "Targets self")
    private static final String SELF_TARGET = Translation.Type.GUI.wrap("ability_target.self_target");

    public static final MapCodec<SelfTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> AbilityTargeting.codecStart(instance).apply(instance, SelfTarget::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifLeft(blockTarget -> {
            if (blockTarget.matches(dragon, dragon.blockPosition())) {
                blockTarget.effects().forEach(target -> target.apply(dragon, ability, dragon.blockPosition(), null));
            }
        }).ifRight(entityTarget -> {
            if (entityTarget.matches(dragon, dragon, dragon.position())) {
                entityTarget.effects().forEach(target -> target.apply(dragon, ability, dragon));
            } else if (ability.value().activation() instanceof PassiveActivation passive && passive.trigger().type() == ActivationTrigger.TriggerType.CONSTANT) {
                entityTarget.effects().forEach(target -> target.remove(dragon, ability, dragon, true));
            }
        });
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifRight(entityTarget -> entityTarget.effects().forEach(target -> target.remove(dragon, ability, dragon, false)));
    }

    @Override
    public MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability) {
        return Component.translatable(SELF_TARGET);
    }

    @Override
    public MapCodec<? extends AbilityTargeting> codec() {
        return CODEC;
    }
}
