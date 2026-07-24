package by.dragonsurvivalteam.dragonsurvival.common;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * Marks attributes whose additive values are displayed as percentages.
 */
public class PercentageAttribute extends RangedAttribute {
    public PercentageAttribute(final String descriptionId, final double defaultValue, final double min, final double max) {
        super(descriptionId, defaultValue, min, max);
    }
}
