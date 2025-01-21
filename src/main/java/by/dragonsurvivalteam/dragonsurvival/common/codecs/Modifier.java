package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record Modifier(Holder<Attribute> attribute, Either<LevelBasedValue, PreciseLevelBasedValue> amount, AttributeModifier.Operation operation, Optional<ResourceKey<DragonSpecies>> dragonSpecies) {
    public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(Modifier::attribute),
            Codec.either(LevelBasedValue.CODEC, PreciseLevelBasedValue.CODEC).fieldOf("amount").forGetter(Modifier::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(Modifier::operation),
            ResourceLocation.CODEC.xmap(location -> ResourceKey.create(DragonSpecies.REGISTRY, location), ResourceKey::location).optionalFieldOf("dragon_species").forGetter(Modifier::dragonSpecies)
    ).apply(instance, Modifier::new));

    public record PreciseLevelBasedValue(float base, float amount) {
        public static final Codec<PreciseLevelBasedValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("precise_base").forGetter(PreciseLevelBasedValue::base),
                Codec.FLOAT.fieldOf("precise_amount").forGetter(PreciseLevelBasedValue::amount)
        ).apply(instance, PreciseLevelBasedValue::new));

        public float calculate(float level) {
            return base + amount * level;
        }
    }

    public static Modifier constant(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return constant(attribute, amount, operation, null);
    }

    public static Modifier constant(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation, final ResourceKey<DragonSpecies> dragonSpecies) {
        return new Modifier(attribute, Either.left(LevelBasedValue.constant(amount)), operation, Optional.ofNullable(dragonSpecies));
    }

    public static Modifier per(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return per(attribute, amount, operation, null);
    }

    public static Modifier per(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation, final ResourceKey<DragonSpecies> dragonSpecies) {
        return new Modifier(attribute, Either.left(LevelBasedValue.perLevel(amount)), operation, Optional.ofNullable(dragonSpecies));
    }

    /** See {@link Modifier#perWithBase(Holder, float, float, AttributeModifier.Operation, ResourceKey)} */
    public static Modifier perWithBase(final Holder<Attribute> attribute, float base, float amount, final AttributeModifier.Operation operation) {
        return perWithBase(attribute, base, amount, operation, null);
    }

    public static Modifier precisePer(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, Either.right(new PreciseLevelBasedValue(0, amount)), operation, Optional.empty());
    }

    public static Modifier precisePerWithBase(final Holder<Attribute> attribute, float base, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, Either.right(new PreciseLevelBasedValue(base, amount)), operation, Optional.empty());
    }

    /**
     * The base will be treated as a separate 'ADD', meaning the amount will be added to it <br>
     * This results in the amount also being properly applied at the first level - example: <br>
     * Base is -5 and the amount is 0.05 -> first level is -4.95 <br>
     * (Since this is done using {@link Math#abs(int)} on both values it will be consistent)
     */
    public static Modifier perWithBase(final Holder<Attribute> attribute, float base, float amount, final AttributeModifier.Operation operation, final ResourceKey<DragonSpecies> dragonSpecies) {
        float actualBase = Math.abs(base) - Math.abs(amount);
        return new Modifier(attribute, Either.left(LevelBasedValue.perLevel(base < 0 ? -actualBase : actualBase, amount)), operation, Optional.ofNullable(dragonSpecies));
    }

    public float calculate(float level) {
        return amount.map(levelBasedValue -> levelBasedValue.calculate((int)level), preciseLevelBasedValue -> preciseLevelBasedValue.calculate(level));
    }

    public AttributeModifier getModifier(final ResourceLocation id, double level) {
        return new AttributeModifier(id, calculate((float)level), operation);
    }

    public MutableComponent getFormattedDescription(int level, boolean fancy) {
        MutableComponent name;

        if (fancy) {
            name = Component.literal("§6■ ").append(Component.translatable(attribute.value().getDescriptionId()).withColor(DSColors.GOLD)).append(Component.literal("§6: "));
        } else {
            name = Component.literal("- ").append(Component.translatable(attribute.value().getDescriptionId())).append(Component.literal(": "));
        }

        float amount = calculate(level);
        return name.append(attribute.value().toValueComponent(operation, amount, TooltipFlag.NORMAL).withStyle(attribute.value().getStyle(amount > 0)));
    }
}
