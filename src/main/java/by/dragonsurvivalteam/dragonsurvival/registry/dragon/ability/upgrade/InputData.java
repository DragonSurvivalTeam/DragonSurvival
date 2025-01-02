package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Used in case upgrade types use the same variable type for their input <br>
 * This is then needed to differentiate when attempting the up- or downgrade
 */
public record InputData(Type type, Integer input) {
    public static InputData experienceLevels(int experienceLevels) {
        return new InputData(Type.EXPERIENCE_LEVELS, experienceLevels);
    }

    public static InputData size(int size) {
        return new InputData(Type.SIZE, size);
    }

    public enum Type implements StringRepresentable {
        EXPERIENCE_LEVELS("experience_levels"),
        SIZE("size");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        private final String name;

        Type(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}