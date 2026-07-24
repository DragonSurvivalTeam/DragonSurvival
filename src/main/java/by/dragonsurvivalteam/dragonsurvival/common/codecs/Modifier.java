package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record Modifier(Holder<Attribute> attribute, Either<LevelBasedValue, PreciseLevelBasedValue> amount, AttributeModifier.Operation operation) {
    public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(Modifier::attribute),
            Codec.either(LevelBasedValue.CODEC, PreciseLevelBasedValue.CODEC).fieldOf("amount").forGetter(Modifier::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(Modifier::operation)
    ).apply(instance, Modifier::new));

    public record PreciseLevelBasedValue(float base, float amount) {
        public static final Codec<PreciseLevelBasedValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("precise_base").forGetter(PreciseLevelBasedValue::base),
                Codec.FLOAT.fieldOf("precise_amount").forGetter(PreciseLevelBasedValue::amount)
        ).apply(instance, PreciseLevelBasedValue::new));

        public double calculate(double level) {
            return base + amount * level;
        }
    }

    public static Modifier constant(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, Either.left(LevelBasedValue.constant(amount)), operation);
    }

    public static Modifier per(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, Either.left(LevelBasedValue.perLevel(amount)), operation);
    }

    public static Modifier precisePer(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, Either.right(new PreciseLevelBasedValue(0, amount)), operation);
    }

    public static Modifier precisePerWithBase(final Holder<Attribute> attribute, float base, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, Either.right(new PreciseLevelBasedValue(base, amount)), operation);
    }

    /**
     * The base will be treated as a separate 'ADD', meaning the amount will be added to it <br>
     * This results in the 'per-amount' being properly applied at the first level - example: <br>
     * Base is -5 and the amount is 0.05 -> first level is -4.95 <br>
     */
    public static Modifier perWithBase(final Holder<Attribute> attribute, float base, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, Either.left(LevelBasedValue.perLevel(base + amount, amount)), operation);
    }

    public double calculate(double level) {
        return amount.map(value -> (double) value.calculate((int) level), value -> value.calculate(level));
    }

    public AttributeModifier getModifier(final ResourceLocation id, double level) {
        return new AttributeModifier(id, calculate(level), operation);
    }

    public MutableComponent getFormattedDescription(int level, boolean fancy) {
        MutableComponent name;

        if (fancy) {
            name = Component.literal("§6■ ").append(Component.translatable(attribute.value().getDescriptionId()).withColor(DSColors.GOLD)).append(Component.literal("§6: "));
        } else {
            name = Component.literal("- ").append(Component.translatable(attribute.value().getDescriptionId())).append(Component.literal(": "));
        }

        double amount = calculate(level);
        return name.append(attribute.value().toValueComponent(operation, amount, TooltipFlag.NORMAL).withStyle(attribute.value().getStyle(amount > 0)));
    }
}
