package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

public record Modification(ModificationType type, LevelBasedValue amount) {
    public static final Codec<Modification> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ModificationType.CODEC.fieldOf("type").forGetter(Modification::type),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(Modification::amount)
    ).apply(instance, Modification::new));

    public enum ModificationType implements StringRepresentable {
        ADDITIVE("additive"), MULTIPLICATIVE("multiplicative");

        public static final Codec<ModificationType> CODEC = StringRepresentable.fromEnum(ModificationType::values);
        private final String name;

        ModificationType(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}