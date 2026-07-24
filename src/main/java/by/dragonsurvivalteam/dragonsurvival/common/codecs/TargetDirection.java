package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public record TargetDirection(Either<Type, Direction> direction) {
    public static final Codec<TargetDirection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(Type.CODEC, Direction.CODEC).fieldOf("direction").forGetter(TargetDirection::direction)
    ).apply(instance, TargetDirection::new));

    public static TargetDirection lookingAt() {
        return new TargetDirection(Either.left(Type.LOOKING_AT));
    }

    public static TargetDirection towardsEntity() {
        return new TargetDirection(Either.left(Type.TOWARDS_ENTITY));
    }

    public static TargetDirection of(final Direction direction) {
        return new TargetDirection(Either.right(direction));
    }

    public enum Type implements StringRepresentable {
        TOWARDS_ENTITY("towards_entity"),
        LOOKING_AT("looking_at");

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
