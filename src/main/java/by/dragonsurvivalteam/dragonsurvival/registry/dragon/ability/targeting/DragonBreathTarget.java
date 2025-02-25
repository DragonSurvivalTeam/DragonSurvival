package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// TODO :: add sub entity predicate for easy is ally / team check (and tamable animals) / spectator
public record DragonBreathTarget(Either<BlockTargeting, EntityTargeting> target, LevelBasedValue rangeMultiplier) implements AbilityTargeting {
    @Translation(comments = "Targets a %s block cone")
    private static final String CONE_TARGET_BLOCK = Translation.Type.GUI.wrap("ability_target.cone_target.block");

    @Translation(comments = "Targets %s in a %s block cone")
    private static final String CONE_TARGET_ENTITY = Translation.Type.GUI.wrap("ability_target.cone_target.entity");

    public static final MapCodec<DragonBreathTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> AbilityTargeting.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("range_multiplier").forGetter(DragonBreathTarget::rangeMultiplier)).apply(instance, DragonBreathTarget::new)
    );

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifLeft(blockTarget -> {
            // Used by 'BlockGetter#clip' to determine the direction
            // 'Entity#pick' -> from: 'getEyePosition' / to: 'getEyePosition + getViewVector'
            Direction direction = Direction.getNearest(dragon.getEyePosition());

            BlockPos.betweenClosedStream(calculateBreathArea(dragon, ability)).forEach(position -> {
                if (blockTarget.matches(dragon, position)) {
                    blockTarget.effects().forEach(target -> target.apply(dragon, ability, position, direction));
                }
            });
        }).ifRight(entityTarget -> {
            dragon.serverLevel().getEntities(EntityTypeTest.forClass(Entity.class), calculateBreathArea(dragon, ability),
                    entity -> entityTarget.targetingMode().isEntityRelevant(dragon, entity) && entityTarget.matches(dragon, entity, entity.position())
            ).forEach(entity -> entityTarget.effects().forEach(target -> target.apply(dragon, ability, entity)));
        });
    }

    @Override
    public MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability) {
        Component targetingComponent = target.map(block -> null, entity -> entity.targetingMode().translation());
        MutableComponent range = DSColors.dynamicValue(FORMAT.format(getRange(dragon, ability)));

        if (targetingComponent == null) {
            return Component.translatable(CONE_TARGET_BLOCK, range);
        } else {
            return Component.translatable(CONE_TARGET_ENTITY, DSColors.dynamicValue(targetingComponent), range);
        }
    }

    public AABB calculateBreathArea(final Player dragon, final DragonAbilityInstance ability) {
        Vec3 viewVector = dragon.getLookAngle().scale(rangeMultiplier.calculate(ability.level()) * dragon.getAttributeValue(DSAttributes.DRAGON_BREATH_RANGE));
        double defaultRadius = dragon.getScale();

        // Set the radius (value will be at least the default radius)
        double xOffset = getOffset(viewVector.x(), defaultRadius);
        double yOffset = Math.abs(viewVector.y());
        double zOffset = getOffset(viewVector.z(), defaultRadius);

        // Check for look angle to avoid extending the range in the direction the player is not facing / looking
        double xMin = (dragon.getLookAngle().x() < 0 ? xOffset : defaultRadius);
        double yMin = (dragon.getLookAngle().y() < 0 ? yOffset : 0);
        double zMin = (dragon.getLookAngle().z() < 0 ? zOffset : defaultRadius);
        Vec3 min = new Vec3(Math.abs(xMin), Math.abs(yMin), Math.abs(zMin));

        double xMax = (dragon.getLookAngle().x() > 0 ? xOffset : defaultRadius);
        double yMax = (dragon.getLookAngle().y() > 0 ? yOffset + dragon.getEyeHeight() : dragon.getEyeHeight());
        double zMax = (dragon.getLookAngle().z() > 0 ? zOffset : defaultRadius);
        Vec3 max = new Vec3(Math.abs(xMax), Math.abs(yMax), Math.abs(zMax));

        Vec3 startPosition = dragon.getEyePosition().subtract(0, (dragon.getEyeHeight() / 2), 0);
        return new AABB(startPosition.subtract(min), startPosition.add(max));
    }

    private float getRange(final Player dragon, final DragonAbilityInstance ability) {
        return (float) (rangeMultiplier.calculate(ability.level()) * dragon.getAttributeValue(DSAttributes.DRAGON_BREATH_RANGE));
    }

    private static double getOffset(double value, double defaultValue) {
        if (value < 0) {
            return Math.min(value, -defaultValue);
        }

        return Math.max(value, defaultValue);
    }

    @Override
    public MapCodec<? extends AbilityTargeting> codec() {
        return CODEC;
    }
}
