package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

public record ManaCost(ManaCostType manaCostType, LevelBasedValue manaCost) {
    public static final Codec<ManaCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ManaCostType.CODEC.fieldOf("type").forGetter(ManaCost::manaCostType),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(ManaCost::manaCost)
    ).apply(instance, ManaCost::new));

    public static ManaCost ticking(final LevelBasedValue manaCost) {
        return new ManaCost(ManaCostType.TICKING, manaCost);
    }

    public static ManaCost reserved(final LevelBasedValue manaCost) {
        return new ManaCost(ManaCostType.RESERVED, manaCost);
    }

    public enum ManaCostType implements StringRepresentable {
        @Translation(comments = "Ticking")
        TICKING("ticking"),
        @Translation(comments = "Reserved")
        RESERVED("reserved");

        public static final Codec<ManaCostType> CODEC = StringRepresentable.fromEnum(ManaCostType::values);

        private final String name;

        ManaCostType(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
