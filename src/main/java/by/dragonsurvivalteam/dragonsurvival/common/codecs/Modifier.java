package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record Modifier(Holder<Attribute> attribute, LevelBasedValue amount, AttributeModifier.Operation operation, Optional<ResourceKey<DragonSpecies>> dragonSpecies) {
    public static final Codec<Modifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(Modifier::attribute),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(Modifier::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(Modifier::operation),
            ResourceLocation.CODEC.xmap(location -> ResourceKey.create(DragonSpecies.REGISTRY, location), ResourceKey::location).optionalFieldOf("dragon_species").forGetter(Modifier::dragonSpecies)
    ).apply(instance, Modifier::new));

    public static Modifier constant(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, LevelBasedValue.constant(amount), operation, Optional.empty());
    }

    public static Modifier constant(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation, final ResourceKey<DragonSpecies> dragonSpecies) {
        return new Modifier(attribute, LevelBasedValue.constant(amount), operation, Optional.of(dragonSpecies));
    }

    public static Modifier per(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation) {
        return new Modifier(attribute, LevelBasedValue.perLevel(amount), operation, Optional.empty());
    }

    public static Modifier per(final Holder<Attribute> attribute, float amount, final AttributeModifier.Operation operation, final ResourceKey<DragonSpecies> dragonSpecies) {
        return new Modifier(attribute, LevelBasedValue.perLevel(amount), operation, Optional.of(dragonSpecies));
    }

    public AttributeModifier getModifier(final ResourceLocation id, double level) {
        return new AttributeModifier(id, amount.calculate((int) level), operation);
    }

    public MutableComponent getFormattedDescription(int level, boolean fancy) {
        MutableComponent name;

        if (fancy) {
            name = Component.literal("§6■ ").append(Component.translatable(attribute.value().getDescriptionId()).withColor(DSColors.GOLD)).append(Component.literal("§6: "));
        } else {
            name = Component.literal("- ").append(Component.translatable(attribute.value().getDescriptionId())).append(Component.literal(": "));
        }

        float amount = this.amount.calculate(level);
        return name.append(attribute.value().toValueComponent(operation, amount, TooltipFlag.NORMAL).withStyle(attribute.value().getStyle(amount > 0)));
    }
}
