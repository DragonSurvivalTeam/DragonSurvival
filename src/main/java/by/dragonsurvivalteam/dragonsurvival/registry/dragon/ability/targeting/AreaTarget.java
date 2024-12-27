package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

// TODO :: provide boolean to only target exposed blocks / visible entities (isVisible)
public record AreaTarget(Either<BlockTargeting, EntityTargeting> target, LevelBasedValue radius) implements AbilityTargeting {
    public static final MapCodec<AreaTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> AbilityTargeting.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("radius").forGetter(AreaTarget::radius)).apply(instance, AreaTarget::new)
    );

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifLeft(blockTarget -> {
            BlockPos.betweenClosedStream(calculateAffectedArea(dragon, ability)).forEach(position -> {
                if (blockTarget.matches(dragon.serverLevel(), position)) {
                    blockTarget.effect().forEach(target -> target.apply(dragon, ability, position, null));
                }
            });
        }).ifRight(entityTarget -> {
            // TODO :: add field 'visible' and check using ProjectileUtil.getHitResultOnViewVector()
            //  maybe need to differentiate between behind blocks and below player (under blocks)?
            dragon.serverLevel().getEntities(EntityTypeTest.forClass(Entity.class), calculateAffectedArea(dragon, ability),
                    entity -> isEntityRelevant(dragon, entityTarget, entity) && entityTarget.matches(dragon.serverLevel(), entity, entity.position())
            ).forEach(entity -> entityTarget.effects().forEach(target -> target.apply(dragon, ability, entity)));
        });
    }

    @Override
    public MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability) {
        Component targetingComponent = target.map(block -> null, entity -> entity.targetingMode().translation());

        if (targetingComponent == null) {
            return Component.translatable(LangKey.ABILITY_AREA, DSColors.blue(getArea(ability)));
        } else {
            return Component.translatable(LangKey.ABILITY_TO_TARGET_AREA, DSColors.blue(targetingComponent), DSColors.blue(getArea(ability)));
        }
    }

    private float getArea(final DragonAbilityInstance ability) {
        return radius().calculate(ability.level());
    }

    public AABB calculateAffectedArea(final Player dragon, final DragonAbilityInstance ability) {
        double radius = radius().calculate(ability.level());
        return AABB.ofSize(dragon.position(), radius * 2, radius * 2, radius * 2);
    }

    @Override
    public MapCodec<? extends AbilityTargeting> codec() {
        return CODEC;
    }
}
