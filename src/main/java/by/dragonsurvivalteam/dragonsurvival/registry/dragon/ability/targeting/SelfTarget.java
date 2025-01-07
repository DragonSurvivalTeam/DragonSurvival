package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
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
    @Translation(comments = "to self")
    private static final String SELF_TARGET = Translation.Type.GUI.wrap("ability_target.self_target");

    public static final MapCodec<SelfTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> AbilityTargeting.codecStart(instance)
            .and(Codec.BOOL.optionalFieldOf("remove_automatically", true).forGetter(SelfTarget::removeAutomatically)).apply(instance, SelfTarget::new)
    );

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifLeft(blockTarget -> {
            if (blockTarget.matches(dragon, dragon.blockPosition())) {
                blockTarget.effect().forEach(target -> target.apply(dragon, ability, dragon.blockPosition(), null));
            }
        }).ifRight(entityTarget -> {
            if (entityTarget.matches(dragon, dragon, dragon.position())) {
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
        return Component.translatable(SELF_TARGET);
    }

    @Override
    public MapCodec<? extends AbilityTargeting> codec() {
        return CODEC;
    }
}
