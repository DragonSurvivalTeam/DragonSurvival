package by.dragonsurvivalteam.dragonsurvival.common;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import org.jetbrains.annotations.NotNull;

public class TimeAttribute extends RangedAttribute {
    public TimeAttribute(final String descriptionId, final double defaultValue, final double min, final double max) {
        super(descriptionId, defaultValue, min, max);
    }

    @Override
    public @NotNull MutableComponent toValueComponent(final AttributeModifier.Operation operation, final double value, @NotNull final TooltipFlag flag) {
        if (IAttributeExtension.isNullOrAddition(operation)) {
            return Component.translatable(LangKey.SECONDS, Functions.ticksToSeconds((int) value));
        }

        return super.toValueComponent(operation, value, flag);
    }
}
