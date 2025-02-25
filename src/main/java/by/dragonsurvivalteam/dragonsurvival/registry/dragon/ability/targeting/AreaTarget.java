package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
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

// TODO :: optional direction parameter
public record AreaTarget(Either<BlockTargeting, EntityTargeting> target, LevelBasedValue radius) implements AbilityTargeting {
    @Translation(comments = "Targets a %s block radius")
    private static final String AREA_TARGET_BLOCK = Translation.Type.GUI.wrap("ability_target.area_target.block");

    @Translation(comments = "Targets %s in a %s block radius")
    public static final String AREA_TARGET_ENTITY = Translation.Type.GUI.wrap("ability_target.area_target.entity");

    public static final MapCodec<AreaTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> AbilityTargeting.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("radius").forGetter(AreaTarget::radius)).apply(instance, AreaTarget::new)
    );

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifLeft(blockTarget -> {
            BlockPos.betweenClosedStream(calculateAffectedArea(dragon, ability)).forEach(position -> {
                if (blockTarget.matches(dragon, position)) {
                    blockTarget.effects().forEach(target -> target.apply(dragon, ability, position, null));
                }
            });
        }).ifRight(entityTarget -> { // TODO :: for auto removal the search for relevant entities would have to be different
            dragon.serverLevel().getEntities(EntityTypeTest.forClass(Entity.class), calculateAffectedArea(dragon, ability),
                    entity -> entityTarget.targetingMode().isEntityRelevant(dragon, entity) && entityTarget.matches(dragon, entity, entity.position())
            ).forEach(entity -> entityTarget.effects().forEach(target -> target.apply(dragon, ability, entity)));
        });
    }

    @Override
    public MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability) {
        Component targetingComponent = target.map(block -> null, entity -> entity.targetingMode().translation());
        MutableComponent area = DSColors.dynamicValue(FORMAT.format(getArea(ability)));

        if (targetingComponent == null) {
            return Component.translatable(AREA_TARGET_BLOCK, area);
        } else {
            return Component.translatable(AREA_TARGET_ENTITY, DSColors.dynamicValue(targetingComponent), area);
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
