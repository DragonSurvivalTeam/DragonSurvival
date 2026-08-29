package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class CeilingClimbDimensions {
    /** Crawling / Swimming hit box height */
    public static final Function<Float, Float> HEIGHT = scale -> scale * 0.3f;

    /** Distance from the ceiling to the eye */
    private static final float CEILING_DISTANCE = 0.1f;

    public static boolean isCeilingClimbing(final LivingEntity entity) {
        return entity.getExistingData(DSDataAttachments.CLIMBABLE_DATA).map(ClimbableData::isCeilingClimbing).orElse(false);
    }

    /**
     * Adjusts the dimensions when climbing on the ceiling and stores the applied offset </br>
     * @param trackOffset Whether the supplied dimensions are the ones that will be applied to the entity
     */
    public static EntityDimensions apply(final LivingEntity entity, final EntityDimensions dimensions, boolean trackOffset) {
        boolean isCeilingClimbing = isCeilingClimbing(entity);

        if (trackOffset) {
            ((IBoundingBoxOffset) entity).dragonSurvival$setBoundingBoxOffset(isCeilingClimbing ? adjustOffset(entity, dimensions) : 0);
        }

        return isCeilingClimbing ? adjust(entity, dimensions) : dimensions;
    }

    /** Adjust the height of the dimension for ceiling crawling */
    public static EntityDimensions adjust(final LivingEntity entity, final EntityDimensions dimensions) {
        return EntityDimensions.fixed(dimensions.width(), HEIGHT.apply(entity.getScale())).withEyeHeight(dimensions.height() - CEILING_DISTANCE);
    }

    /** Amount the bounding box needs to be raised by to keep its top at the ceiling */
    public static double adjustOffset(final LivingEntity entity, final EntityDimensions dimensions) {
        return dimensions.height() - HEIGHT.apply(entity.getScale());
    }

    /**
     * Either returns the original {@link Entity#getBbHeight()} </br>
     * Or the offset + {@link #HEIGHT}, which results in the original height
     */
    public static double getUnmodifiedHeight(final LivingEntity entity) {
        double offset = ((IBoundingBoxOffset) entity).dragonSurvival$getBoundingBoxOffset();
        double height;

        if (offset == 0) {
            height = entity.getBbHeight();
        } else {
            height = offset + HEIGHT.apply(entity.getScale());
        }

        if (DragonStateProvider.isDragon(entity)) {
            // Not entirely sure, but adding the scale here breaks the scaling offset
            // Causing it to no longer uniformly work across the different growth stages
            return height - CEILING_DISTANCE;
        } else {
            return height / entity.getScale() - CEILING_DISTANCE;
        }
    }
}
