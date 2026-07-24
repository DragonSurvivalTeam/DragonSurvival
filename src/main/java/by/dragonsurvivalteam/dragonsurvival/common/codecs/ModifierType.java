package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public enum ModifierType implements StringRepresentable {
    DRAGON_TYPE("type"),
    DRAGON_BODY("body"),
    DRAGON_STAGE("stage"),
    CUSTOM("custom");

    public static final Codec<ModifierType> CODEC = StringRepresentable.fromEnum(ModifierType::values);

    private final String path;

    ModifierType(final String path) {
        this.path = "dragonsurvival/" + path + "/";
    }

    public String path() {
        return path;
    }

    public UUID randomId(final Holder<Attribute> attribute, final AttributeOperation operation, final RandomSource random) {
        return new UUID(random.nextLong(), random.nextLong());
    }

    @Override
    public @NotNull String getSerializedName() {
        return path();
    }
}
