package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record SelfTarget(Either<BlockTargeting, EntityTargeting> target, boolean removeAutomatically) implements AbilityTargeting {
    public static final MapCodec<SelfTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> AbilityTargeting.codecStart(instance)
            .and(Codec.BOOL.optionalFieldOf("remove_automatically", true).forGetter(SelfTarget::removeAutomatically)).apply(instance, SelfTarget::new)
    );

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifLeft(blockTarget -> {
            if (blockTarget.matches(dragon.serverLevel(), dragon.blockPosition())) {
                blockTarget.effect().forEach(target -> target.apply(dragon, ability, dragon.blockPosition(), null));
            }
        }).ifRight(entityTarget -> {
            if (entityTarget.matches(dragon.serverLevel(), dragon, dragon.position())) {
                entityTarget.effects().forEach(target -> target.apply(dragon, ability, dragon));
            } else if (removeAutomatically) {
                entityTarget.effects().forEach(target -> target.remove(dragon, ability, dragon));
            }
        });
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifRight(entityTarget -> entityTarget.effects().forEach(target -> target.remove(dragon, ability, dragon)));
    }

    @Override
    public MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability) {
        return Component.translatable(LangKey.ABILITY_TO_TARGET, Component.translatable(LangKey.ABILITY_TARGET_SELF));
    }

    @Override
    public MapCodec<? extends AbilityTargeting> codec() {
        return CODEC;
    }
}
