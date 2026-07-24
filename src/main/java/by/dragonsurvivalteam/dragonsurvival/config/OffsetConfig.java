package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OffsetConfig implements CustomConfig {
    private static final String SPLIT = ";";

    private final Map<ResourceKey<EntityType<?>>, Vec3> offsets;
    private final String originalData;

    public @Nullable Vec3 getOffset(final ResourceKey<EntityType<?>> entityType) {
        return offsets.get(entityType);
    }

    /** Required to handle creation, parsing etc. (created through reflection) */
    public OffsetConfig() {
        this("", Vec3.ZERO, "");
    }

    private OffsetConfig(final String entityTypeResource, final Vec3 offset, final String originalData) {
        this.offsets = new HashMap<>();
        this.originalData = originalData;

        ResourceLocationWrapper.getEntries(entityTypeResource, BuiltInRegistries.ENTITY_TYPE).forEach(resource -> {
            offsets.put(ResourceKey.create(Registries.ENTITY_TYPE, resource), offset);
        });
    }

    public static OffsetConfig create(final Holder<EntityType<?>> entityType, final Vec3 offset) {
        return create(entityType.getRegisteredName(), offset);
    }

    public static OffsetConfig create(final TagKey<EntityType<?>> entityType, final Vec3 offset) {
        return create("#" + entityType.location(), offset);
    }

    public static OffsetConfig create(final String entityType, final Vec3 offset) {
        String data = entityType + SPLIT + offset.x() + SPLIT + offset.y() + SPLIT + offset.z();
        return new OffsetConfig(entityType, offset, data);
    }

    @Override
    public CustomConfig parse(final String data) {
        String[] elements = data.split(SPLIT);

        double xOffset = Double.parseDouble(elements[X_OFFSET]);
        double yOffset = Double.parseDouble(elements[Y_OFFSET]);
        double zOffset = Double.parseDouble(elements[Z_OFFSET]);

        return new OffsetConfig(elements[ENTITY_TYPE], new Vec3(xOffset, yOffset, zOffset), data);
    }

    @Override
    public boolean validate(final Object configValue) {
        if (configValue instanceof String string) {
            String[] elements = string.split(SPLIT);

            if (elements.length != 4) {
                return false;
            }

            if (!ResourceLocationWrapper.validateRegexResourceLocation(elements[ENTITY_TYPE])) {
                return false;
            }

            try {
                Double.parseDouble(elements[X_OFFSET]);
                Double.parseDouble(elements[Y_OFFSET]);
                Double.parseDouble(elements[Z_OFFSET]);
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

    private static final int ENTITY_TYPE = 0;
    private static final int X_OFFSET = 1;
    private static final int Y_OFFSET = 2;
    private static final int Z_OFFSET = 3;
}
