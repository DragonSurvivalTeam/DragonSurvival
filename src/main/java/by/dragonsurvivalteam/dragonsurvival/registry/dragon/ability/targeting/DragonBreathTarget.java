package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

// TODO :: add sub entity predicate for easy is ally / team check (and tamable animals) / spectator
public record DragonBreathTarget(Either<BlockTargeting, EntityTargeting> target, LevelBasedValue rangeMultiplier) implements AbilityTargeting {
    public static final MapCodec<DragonBreathTarget> CODEC = RecordCodecBuilder.mapCodec(instance -> AbilityTargeting.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("range_multiplier").forGetter(DragonBreathTarget::rangeMultiplier)).apply(instance, DragonBreathTarget::new)
    );

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        target().ifLeft(blockTarget -> {
            AABB breathArea = calculateBreathArea(dragon, ability);

            BlockPos.betweenClosedStream(breathArea).forEach(position -> {
                if (blockTarget.matches(dragon.serverLevel(), position)) {
                    // TODO :: Is this too expensive to calculate for each block?
                    BlockHitResult blockHitResult = getBlockHitResult(dragon, ability);
                    blockTarget.effect().forEach(target -> target.apply(dragon, ability, position, blockHitResult.getDirection()));
                }
            });
        }).ifRight(entityTarget -> {
            AABB breathArea = calculateBreathArea(dragon, ability);

            dragon.serverLevel().getEntities(EntityTypeTest.forClass(Entity.class), breathArea,
                    entity -> isEntityRelevant(dragon, entityTarget, entity) && entityTarget.matches(dragon.serverLevel(), entity, entity.position())
            ).forEach(entity -> entityTarget.effects().forEach(target -> target.apply(dragon, ability, entity)));
        });
    }

    @Override
    public MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability) {
        Component targetingComponent = target.map(block -> null, entity -> entity.targetingMode().translation());

        if (targetingComponent == null) {
            return Component.translatable(LangKey.ABILITY_CONE, DSColors.blue(getRange(dragon, ability)));
        } else {
            return Component.translatable(LangKey.ABILITY_TO_TARGET_CONE, DSColors.blue(targetingComponent), DSColors.blue(getRange(dragon, ability)));
        }
    }

    @Override
    public MapCodec<? extends AbilityTargeting> codec() {
        return CODEC;
    }

    public BlockHitResult getBlockHitResult(Player dragon, final DragonAbilityInstance ability) {
        Vec3 viewVector = dragon.getLookAngle().scale(rangeMultiplier.calculate(ability.level()) * dragon.getAttributeValue(DSAttributes.DRAGON_BREATH_RANGE));
        return dragon.level().clip(new ClipContext(viewVector, viewVector, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
    }

    public AABB calculateBreathArea(final Player dragon, final DragonAbilityInstance ability) {
        Vec3 viewVector = dragon.getLookAngle().scale(rangeMultiplier.calculate(ability.level()) * dragon.getAttributeValue(DSAttributes.DRAGON_BREATH_RANGE));
        double defaultRadius = DragonStateProvider.getData(dragon).getSize() * 0.03;

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

        return new AABB(dragon.position().subtract(min), dragon.position().add(max));
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
}
