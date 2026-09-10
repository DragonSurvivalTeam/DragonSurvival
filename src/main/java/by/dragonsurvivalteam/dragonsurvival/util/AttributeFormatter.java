
package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.common.PercentageAttribute;
import by.dragonsurvivalteam.dragonsurvival.config.AttributeConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AttributeFormatter {
    @Translation(key = "percentage_attributes", type = Translation.Type.CONFIGURATION, comments = {
            "Attributes whose modifiers should be displayed as percentages (e.g. additions to movement speed)",
            "Format: resource / tag;attribute_scale",
            "The resource can also be defined using regular expressions (for both namespace and path)",
    })
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "percentage_attributes"}, key = "percentage_attributes")
    public static List<AttributeConfig> percentageAttributes = List.of(
            // NeoForge 1.21.1 comment:
            // Neo: Convert Movement Speed to percent-based for more appropriate display using IAttributeExtension. Use a scale factor of 1000 since movement speed has 0.001 units.
            AttributeConfig.create(Attributes.MOVEMENT_SPEED, 1_000),
            AttributeConfig.create(Attributes.KNOCKBACK_RESISTANCE, 100)
    );

    private static final DecimalFormat FORMAT = Util.make(new DecimalFormat("#.##"), (fmt) -> fmt.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT)));

    public static MutableComponent toValueComponent(final Holder<Attribute> attribute, @Nullable final AttributeModifier.Operation operation, final double value) {
        if (operation != null && operation != AttributeModifier.Operation.ADDITION) {
            return Component.literal(NumberFormat.getPercentInstance().format(value));
        }

        for (AttributeConfig config : percentageAttributes) {
            Integer scale = config.getScale(attribute.unwrapKey().orElse(null));

            if (scale != null) {
                // The value will be formatted based on a scale of 100 by default
                // Therefor we divide by said default value so only differences will be considered
                return Component.literal(NumberFormat.getPercentInstance().format(value * (scale / 100.0)));
            }
        }

        if (attribute.value() instanceof PercentageAttribute) {
            return Component.literal(NumberFormat.getPercentInstance().format(value));
        }

        return Component.literal(FORMAT.format(value));
    }
}
