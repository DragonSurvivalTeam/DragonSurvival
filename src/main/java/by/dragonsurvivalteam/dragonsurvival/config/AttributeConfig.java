package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AttributeConfig implements CustomConfig {
    private static final String SPLIT = ";";

    private final Map<ResourceKey<Attribute>, /* Scale */ Integer> percentageAttributes;
    private final String originalData;

    public @Nullable Integer getScale(@Nullable final ResourceKey<Attribute> attribute) {
        return percentageAttributes.get(attribute);
    }

    /** Required to handle creation, parsing etc. (created through reflection) */
    public AttributeConfig() {
        this("", 0, "");
    }

    private AttributeConfig(final String attribute, final Integer scale, final String originalData) {
        this.percentageAttributes = new HashMap<>();
        this.originalData = originalData;

        ResourceLocationWrapper.getEntries(attribute, BuiltInRegistries.ATTRIBUTE).forEach(resource -> {
            percentageAttributes.put(ResourceKey.create(Registries.ATTRIBUTE, resource), scale);
        });
    }

    public static AttributeConfig create(final Attribute attribute, final Integer scale) {
        return create(ForgeRegistries.ATTRIBUTES.getKey(attribute).toString(), scale);
    }

    public static AttributeConfig create(final Holder<Attribute> attribute, final Integer scale) {
        return create(attribute.unwrapKey().orElseThrow().location().toString(), scale);
    }

    public static AttributeConfig create(final TagKey<Attribute> attributes, final Integer scale) {
        return create("#" + attributes.location(), scale);
    }

    public static AttributeConfig create(final String attribute, final Integer scale) {
        String data = attribute + SPLIT + scale;
        return new AttributeConfig(attribute, scale, data);
    }

    @Override
    public CustomConfig parse(final String data) {
        String[] elements = data.split(SPLIT);
        return new AttributeConfig(elements[ATTRIBUTE], Integer.parseInt(elements[SCALE]), data);
    }

    @Override
    public boolean validate(final Object configValue) {
        if (configValue instanceof String string) {
            String[] elements = string.split(SPLIT);

            if (elements.length != 2) {
                return false;
            }

            if (!ResourceLocationWrapper.validateRegexResourceLocation(elements[ATTRIBUTE])) {
                return false;
            }

            try {
                Integer.parseInt(elements[SCALE]);
            } catch (NumberFormatException ignored) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public String convert() {
        return originalData;
    }

    private static final int ATTRIBUTE = 0;
    private static final int SCALE = 1;
}
