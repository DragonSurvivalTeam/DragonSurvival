package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.common.PercentageAttribute;

import java.text.NumberFormat;
import java.util.Optional;

public record Modifier(Holder<Attribute> attribute, LevelBasedValue amount, AttributeModifier.Operation operation, Optional<ResourceKey<DragonType>> dragonType) {
    public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(Modifier::attribute),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(Modifier::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(Modifier::operation),
            ResourceLocation.CODEC.xmap(location -> ResourceKey.create(DragonType.REGISTRY, location), ResourceKey::location).optionalFieldOf("dragon_type").forGetter(Modifier::dragonType)
    ).apply(instance, Modifier::new));

    public static Modifier constant(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, LevelBasedValue.constant(amount), operation, Optional.empty());
    }

    public static Modifier constant(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation, final ResourceKey<DragonType> dragonType) {
        return new Modifier(attribute, LevelBasedValue.constant(amount), operation, Optional.of(dragonType));
    }

    public static Modifier per(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, LevelBasedValue.perLevel(amount), operation, Optional.empty());
    }

    public static Modifier per(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation, final ResourceKey<DragonType> dragonType) {
        return new Modifier(attribute, LevelBasedValue.perLevel(amount), operation, Optional.of(dragonType));
    }

    public AttributeModifier getModifier(final ModifierType type, double level) {
        return getModifier(type, (int) level);
    }

    public AttributeModifier getModifier(final ModifierType type, int level) {
        return new AttributeModifier(type.randomId(attribute(), operation()), amount().calculate(level), operation());
    }

    public MutableComponent getFormattedDescription(int level) {
        MutableComponent name = Component.literal("§6■ ").append(Component.translatable(attribute().value().getDescriptionId()).withColor(DSColors.ORANGE));
        float amount = amount().calculate(level);
        // If the number is negative it will already contain a '-'
        String number = amount > 0 ? "+" : "";

        if (attribute().value() instanceof PercentageAttribute) {
            number += NumberFormat.getPercentInstance().format(amount);
        } else {
            number += String.format("%.2f", amount);
        }

        Component value = Component.literal("§6: ").append(Component.literal(number).withStyle(attribute().value().getStyle(amount > 0)));
        name = name.append(value);

        return name;
    }
}
