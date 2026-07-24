package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.PercentageAttribute;
import by.dragonsurvivalteam.dragonsurvival.common.TimeAttribute;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;
import java.util.function.Supplier;

public record Modifier(Holder<Attribute> attribute, Either<LevelBasedValue, PreciseLevelBasedValue> amount, AttributeOperation operation) {
    private static final ResourceLocation SCALE_ID = new ResourceLocation("minecraft", "scale");
    private static final ResourceLocation GENERIC_SCALE_ID = new ResourceLocation("minecraft", "generic.scale");
    private static final Codec<Holder<Attribute>> ATTRIBUTE_CODEC = ResourceLocation.CODEC.comapFlatMap(
            id -> {
                if (isScaleId(id)) {
                    return DataResult.success(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(DSAttributes.SCALE.get()));
                }

                return BuiltInRegistries.ATTRIBUTE.getHolder(ResourceKey.create(Registries.ATTRIBUTE, id))
                        .map(DataResult::success)
                        .orElseGet(() -> DataResult.error(() -> "Unknown attribute: " + id));
            },
            attribute -> attribute.value() == DSAttributes.SCALE.get() ? SCALE_ID : BuiltInRegistries.ATTRIBUTE.getKey(attribute.value())
    );

    public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ATTRIBUTE_CODEC.fieldOf("attribute").forGetter(Modifier::attribute),
            Codec.either(LevelBasedValue.CODEC, PreciseLevelBasedValue.CODEC).fieldOf("amount").forGetter(Modifier::amount),
            AttributeOperation.CODEC.fieldOf("operation").forGetter(Modifier::operation)
    ).apply(instance, Modifier::new));

    private static boolean isScaleId(final ResourceLocation id) {
        return id.equals(SCALE_ID) || id.equals(GENERIC_SCALE_ID);
    }

    public record PreciseLevelBasedValue(float base, float amount) {
        public static final Codec<PreciseLevelBasedValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("precise_base").forGetter(PreciseLevelBasedValue::base),
                Codec.FLOAT.fieldOf("precise_amount").forGetter(PreciseLevelBasedValue::amount)
        ).apply(instance, PreciseLevelBasedValue::new));

        public double calculate(double level) {
            return base + amount * level;
        }
    }

    public static Modifier constant(final Holder<Attribute> attribute, float amount, final AttributeOperation operation) {
        return new Modifier(attribute, Either.left(LevelBasedValue.constant(amount)), operation);
    }

    public static Modifier constant(final Attribute attribute, float amount, final AttributeOperation operation) {
        return constant(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), amount, operation);
    }

    public static Modifier constant(final Supplier<? extends Attribute> attribute, float amount, final AttributeOperation operation) {
        return constant(attribute.get(), amount, operation);
    }

    public static Modifier per(final Holder<Attribute> attribute, float amount, final AttributeOperation operation) {
        return new Modifier(attribute, Either.left(LevelBasedValue.perLevel(amount)), operation);
    }

    public static Modifier per(final Attribute attribute, float amount, final AttributeOperation operation) {
        return per(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), amount, operation);
    }

    public static Modifier per(final Supplier<? extends Attribute> attribute, float amount, final AttributeOperation operation) {
        return per(attribute.get(), amount, operation);
    }

    public static Modifier precisePer(final Holder<Attribute> attribute, float amount, final AttributeOperation operation) {
        return new Modifier(attribute, Either.right(new PreciseLevelBasedValue(0, amount)), operation);
    }

    public static Modifier precisePer(final Attribute attribute, float amount, final AttributeOperation operation) {
        return precisePer(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), amount, operation);
    }

    public static Modifier precisePer(final Supplier<? extends Attribute> attribute, float amount, final AttributeOperation operation) {
        return precisePer(attribute.get(), amount, operation);
    }

    public static Modifier precisePerWithBase(final Holder<Attribute> attribute, float base, float amount, final AttributeOperation operation) {
        return new Modifier(attribute, Either.right(new PreciseLevelBasedValue(base, amount)), operation);
    }

    public static Modifier precisePerWithBase(final Attribute attribute, float base, float amount, final AttributeOperation operation) {
        return precisePerWithBase(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), base, amount, operation);
    }

    public static Modifier precisePerWithBase(final Supplier<? extends Attribute> attribute, float base, float amount, final AttributeOperation operation) {
        return precisePerWithBase(attribute.get(), base, amount, operation);
    }

    /**
     * The base will be treated as a separate 'ADD', meaning the amount will be added to it <br>
     * This results in the 'per-amount' being properly applied at the first level - example: <br>
     * Base is -5 and the amount is 0.05 -> first level is -4.95 <br>
     */
    public static Modifier perWithBase(final Holder<Attribute> attribute, float base, float amount, final AttributeOperation operation) {
        return new Modifier(attribute, Either.left(LevelBasedValue.perLevel(base + amount, amount)), operation);
    }

    public static Modifier perWithBase(final Attribute attribute, float base, float amount, final AttributeOperation operation) {
        return perWithBase(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), base, amount, operation);
    }

    public static Modifier perWithBase(final Supplier<? extends Attribute> attribute, float base, float amount, final AttributeOperation operation) {
        return perWithBase(attribute.get(), base, amount, operation);
    }

    public double calculate(double level) {
        return amount.map(value -> (double) value.calculate((int) level), value -> value.calculate(level));
    }

    public AttributeModifier getModifier(final UUID id, final String namePrefix, double level) {
        return new AttributeModifier(id, namePrefix + BuiltInRegistries.ATTRIBUTE.getKey(attribute.value()), calculate(level), operation.legacy());
    }

    public MutableComponent getFormattedDescription(int level, boolean fancy) {
        MutableComponent name;

        if (fancy) {
            name = Component.literal("§6■ ").append(Component.translatable(attribute.value().getDescriptionId()).withColor(DSColors.GOLD)).append(Component.literal("§6: "));
        } else {
            name = Component.literal("- ").append(Component.translatable(attribute.value().getDescriptionId())).append(Component.literal(": "));
        }

        double amount = calculate(level);
        MutableComponent value;

        if (attribute.value() instanceof TimeAttribute && operation == AttributeOperation.ADD_VALUE) {
            value = Component.translatable(LangKey.SECONDS, Functions.ticksToSeconds((int) amount));
        } else {
            double displayValue = operation == AttributeOperation.ADD_VALUE && !(attribute.value() instanceof PercentageAttribute) ? amount : amount * 100;
            value = Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(displayValue));
        }

        return name.append(value.withStyle(amount >= 0 ? ChatFormatting.BLUE : ChatFormatting.RED));
    }
}
