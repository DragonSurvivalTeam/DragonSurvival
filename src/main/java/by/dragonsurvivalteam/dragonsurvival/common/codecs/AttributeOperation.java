package by.dragonsurvivalteam.dragonsurvival.common.codecs;


import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

public enum AttributeOperation implements StringRepresentable {
    ADD_VALUE("add_value", AttributeModifier.Operation.ADDITION),
    ADD_MULTIPLIED_BASE("add_multiplied_base", AttributeModifier.Operation.MULTIPLY_BASE),
    ADD_MULTIPLIED_TOTAL("add_multiplied_total", AttributeModifier.Operation.MULTIPLY_TOTAL);

    public static final Codec<AttributeOperation> CODEC = StringRepresentable.fromEnum(AttributeOperation::values);

    private final String serializedName;
    private final AttributeModifier.Operation legacy;

    AttributeOperation(final String serializedName, final AttributeModifier.Operation legacy) {
        this.serializedName = serializedName;
        this.legacy = legacy;
    }

    public AttributeModifier.Operation legacy() {
        return legacy;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }
}
