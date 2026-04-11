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
        Vec3 startPosition = getBreathStart(dragon);
        Vec3 endPosition = getBreathEnd(dragon, ability);
        double radius = getBreathRadius(dragon);
        AABB breathArea = calculateBreathArea(startPosition, endPosition, radius);

        target().ifLeft(blockTarget -> {
            // Used by 'BlockGetter#clip' to determine the direction
            // 'Entity#pick' -> from: 'getEyePosition' / to: 'getEyePosition + getViewVector'
            Vec3 eyePos = dragon.getEyePosition();
            Direction direction = Direction.getNearest((int)eyePos.x, (int)eyePos.y, (int)eyePos.z, null);

            BlockPos.betweenClosedStream(breathArea).forEach(position -> {
                if (intersectsBreath(new AABB(position), startPosition, endPosition, radius) && blockTarget.matches(dragon, position)) {
                    blockTarget.effects().forEach(target -> target.apply(dragon, ability, position, direction));
                }
            });
        }).ifRight(entityTarget -> {
            dragon.level().getEntities(EntityTypeTest.forClass(Entity.class), breathArea,
                    entity -> entityTarget.targetingMode().isEntityRelevant(dragon, entity)
                        && intersectsBreath(entity.getBoundingBox(), startPosition, endPosition, radius)
                        && entityTarget.matches(dragon, entity, entity.position())
            ).forEach(entity -> entityTarget.effects().forEach(target -> target.apply(dragon, ability, entity)));
        });
    }

    @Override
    public MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability) {
        Component targetingComponent = target.map(block -> null, entity -> entity.targetingMode().translation());
        MutableComponent range = DSColors.dynamicValue(FORMAT.format(getDistance(dragon, ability)));

        if (targetingComponent == null) {
            return Component.translatable(CONE_TARGET_BLOCK, range);
        } else {
            return Component.translatable(CONE_TARGET_ENTITY, DSColors.dynamicValue(targetingComponent), range);
        }
    }

    public AABB calculateBreathArea(final Player dragon, final DragonAbilityInstance ability) {
        return calculateBreathArea(getBreathStart(dragon), getBreathEnd(dragon, ability), getBreathRadius(dragon));
    }

    @Override
    public float getDistance(final Player dragon, final DragonAbilityInstance instance) {
        return (float) (rangeMultiplier.calculate(instance.level()) * dragon.getAttributeValue(DSAttributes.DRAGON_BREATH_RANGE));
    }

    private Vec3 getBreathEnd(final Player dragon, final DragonAbilityInstance ability) {
        double breathRange = rangeMultiplier.calculate(ability.level()) * dragon.getAttributeValue(DSAttributes.DRAGON_BREATH_RANGE);
        Vec3 breathOrigin = getBreathOrigin(dragon);
        double forwardOffset = getBreathForwardOffset(dragon);

        return breathOrigin.add(dragon.getLookAngle().scale(Math.max(breathRange, forwardOffset)));
    }

    private static Vec3 getBreathStart(final Player dragon) {
        Vec3 breathOrigin = getBreathOrigin(dragon);
        return breathOrigin.add(dragon.getLookAngle().scale(getBreathForwardOffset(dragon)));
    }

    private static double getBreathRadius(final Player dragon) {
        return dragon.getScale();
    }

    private static Vec3 getBreathOrigin(final Player dragon) {
        return dragon.getEyePosition().subtract(0, dragon.getEyeHeight() / 2, 0);
    }

    private static double getBreathForwardOffset(final Player dragon) {
        return getBreathRadius(dragon);
    }

    private static AABB calculateBreathArea(final Vec3 startPosition, final Vec3 endPosition, final double radius) {
        return new AABB(startPosition, endPosition).inflate(radius);
    }

    private static boolean intersectsBreath(final AABB boundingBox, final Vec3 startPosition, final Vec3 endPosition, final double radius) {
        AABB expandedBounds = boundingBox.inflate(radius);
        return expandedBounds.contains(startPosition) || expandedBounds.clip(startPosition, endPosition).isPresent();
    }

    @Override
    public MapCodec<? extends AbilityTargeting> codec() {
        return CODEC;
    }
}
