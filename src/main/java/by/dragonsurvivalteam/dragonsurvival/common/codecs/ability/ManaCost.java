package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

public record ManaCost(Type type, LevelBasedValue manaCost) {
    public static final Codec<ManaCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(ManaCost::type),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(ManaCost::manaCost)
    ).apply(instance, ManaCost::new));

    public static ManaCost ticking(final LevelBasedValue manaCost) {
        return new ManaCost(Type.TICKING, manaCost);
    }

    public static ManaCost reserved(final LevelBasedValue manaCost) {
        return new ManaCost(Type.RESERVED, manaCost);
    }

    public enum Type implements StringRepresentable {
        TICKING("ticking"),
        RESERVED("reserved");

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
