package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
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
import net.minecraft.world.phys.Vec3;

public record DiscTarget(Either<BlockTargeting, EntityTargeting> target, LevelBasedValue radius, LevelBasedValue height, boolean heightStartsBelow) implements AbilityTargeting {
    @Translation(comments = "Targets a disc around you (radius: %s / height: %s)")
    private static final String DISC_TARGET_BLOCK = Translation.Type.GUI.wrap("ability_target.disc_target.block");

    @Translation(comments = "Targets %s in a disc around you (radius: %s / height: %s)")
    public static final String DISC_TARGET_ENTITY = Translation.Type.GUI.wrap("ability_target.disc_target.entity");

    public static final MapCodec<DiscTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> AbilityTargeting.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("radius").forGetter(DiscTarget::radius))
            .and(LevelBasedValue.CODEC.optionalFieldOf("height", LevelBasedValue.constant(1)).forGetter(DiscTarget::height))
            .and(Codec.BOOL.optionalFieldOf("height_starts_below", false).forGetter(DiscTarget::heightStartsBelow))
            .apply(instance, DiscTarget::new)
    );

    @Override
    public MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability) {
        Component targetingComponent = target.map(block -> null, entity -> entity.targetingMode().translation());
        Component radius = DSColors.dynamicValue((int) this.radius.calculate(ability.level()));
        Component height = DSColors.dynamicValue((int) this.height.calculate(ability.level()));

        if (targetingComponent == null) {
            return Component.translatable(DISC_TARGET_BLOCK, radius, height);
        } else {
            return Component.translatable(DISC_TARGET_ENTITY, DSColors.dynamicValue(targetingComponent), radius, height);
        }
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        int radius = (int) this.radius.calculate(ability.level());
        int height = (int) this.height.calculate(ability.level());

        target.ifLeft(blockTarget -> {
            BlockPos.betweenClosedStream(calculateAffectedArea(dragon.position(), radius, height)).forEach(position -> {
                if (blockTarget.matches(dragon, position)) {
                    blockTarget.effects().forEach(target -> target.apply(dragon, ability, position, null));
                }
            });
        }).ifRight(entityTarget -> {
            dragon.serverLevel().getEntities(EntityTypeTest.forClass(Entity.class), calculateAffectedArea(dragon.position(), radius, height),
                    entity -> entityTarget.targetingMode().isEntityRelevant(dragon, entity) && entityTarget.matches(dragon, entity, entity.position())
            ).forEach(entity -> entityTarget.effects().forEach(target -> target.apply(dragon, ability, entity)));
        });
    }

    public AABB calculateAffectedArea(final Vec3 origin, int radius, int height) {
        return new AABB(origin.subtract(radius, heightStartsBelow ? 1 : 0, radius), origin.add(radius, heightStartsBelow ? height - 1 : height, radius));
    }

    @Override
    public MapCodec<? extends AbilityTargeting> codec() {
        return CODEC;
    }
}
