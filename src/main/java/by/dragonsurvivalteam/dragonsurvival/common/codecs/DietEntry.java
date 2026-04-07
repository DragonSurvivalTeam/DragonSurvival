package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public record DietEntry(
    String items,
    Optional<FoodProperties> properties,
    Optional<Consumable> consumable,
    Optional<ItemStackTemplate> useRemainder,
    Optional<Either<Boolean, RetainEffects>> retainEffects
) {
    private static final float DEFAULT_EAT_SECONDS = 1.6f;

    public static final Codec<DietEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        IdentifierWrapper.validatedCodec().fieldOf("items").forGetter(DietEntry::items),
        FoodProperties.DIRECT_CODEC.optionalFieldOf("properties").forGetter(DietEntry::properties),
        Consumable.CODEC.optionalFieldOf("consumable").forGetter(DietEntry::consumable),
        ItemStackTemplate.CODEC.optionalFieldOf("use_remainder").forGetter(DietEntry::useRemainder),
        Codec.either(Codec.BOOL, RetainEffects.CODEC).optionalFieldOf("retain_effects").forGetter(DietEntry::retainEffects)
    ).apply(instance, DietEntry::new));

    public static Map<Item, DragonFood> map(final List<DietEntry> entries) {
        Map<Item, DragonFood> diet = new HashMap<>();

        for (DietEntry entry : entries) {
            IdentifierWrapper.map(entry.items(), BuiltInRegistries.ITEM).forEach(resource -> {
                BuiltInRegistries.ITEM.get(resource).ifPresent(holder -> {
                    diet.put(holder.value(), new DragonFood(
                        holder.value(),
                        entry.properties(),
                        entry.consumable(),
                        entry.useRemainder().map(UseRemainder::new),
                        entry.retainEffects()
                    ));
                });
            });
        }

        return diet;
    }

    public static Builder create(final String items) {
        return new Builder(items);
    }

    public static Builder create(final TagKey<Item> tag) {
        return create("#" + tag.location());
    }

    public static Builder create(final Identifier location) {
        return create(location.toString());
    }

    public static Builder create(final Item item) {
        //noinspection deprecation, DataFlowIssue -> ignore deprecated / key is present
        return create(item.builtInRegistryHolder().getKey().identifier());
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DietEntry otherDiet)) {
            return false;
        }

        return this.items.equals(otherDiet.items());
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    public record RetainEffects(boolean beneficial, boolean neutral, boolean harmful) {
        public static final Codec<RetainEffects> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("beneficial", false).forGetter(RetainEffects::beneficial),
            Codec.BOOL.optionalFieldOf("neutral", false).forGetter(RetainEffects::neutral),
            Codec.BOOL.optionalFieldOf("harmful", false).forGetter(RetainEffects::harmful)
        ).apply(instance, RetainEffects::new));

        public boolean retain(final MobEffectInstance effect) {
            return switch (effect.getEffect().value().getCategory()) {
                case BENEFICIAL -> beneficial;
                case HARMFUL -> harmful;
                case NEUTRAL -> neutral;
            };
        }

    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Builder {
        private final String items;

        private Optional<FoodProperties> properties = Optional.empty();
        private Optional<Consumable> consumable = Optional.empty();
        private Optional<ItemStackTemplate> useRemainder = Optional.empty();

        private boolean retainEffects = false;
        private boolean retainBeneficial = false;
        private boolean retainNeutral = false;
        private boolean retainHarmful = false;

        private boolean customProperties;
        private boolean customConsumeDuration;

        private int nutriton;
        private float saturation;
        private boolean canAlwaysEat;
        private float seconds = DEFAULT_EAT_SECONDS;

        private final List<ConsumeEffect> consumeEffects = new ArrayList<>();

        public Builder(final String items) {
            this.items = items;
        }

        public Builder nutrition(final int nutrition) {
            this.nutriton = nutrition;
            this.customProperties = true;
            return this;
        }

        public Builder saturation(final float saturation) {
            this.saturation = saturation;
            this.customProperties = true;
            return this;
        }

        public Builder canAlwaysEat() {
            this.canAlwaysEat = true;
            this.customProperties = true;
            return this;
        }

        public Builder seconds(final float seconds) {
            this.seconds = seconds;
            this.customConsumeDuration = true;
            return this;
        }

        public Builder fast() {
            this.seconds = DEFAULT_EAT_SECONDS / 2;
            this.customConsumeDuration = true;
            return this;
        }

        public Builder effect(final Supplier<MobEffectInstance> effect, final float probability) {
            this.consumeEffects.add(new ApplyStatusEffectsConsumeEffect(effect.get(), probability));
            return this;
        }

        public Builder convertsTo(final ItemLike item) {
            this.useRemainder = Optional.of(new ItemStackTemplate(item.asItem()));
            return this;
        }

        public Builder properties(final FoodProperties properties) {
            this.properties = Optional.ofNullable(properties);
            return this;
        }

        public Builder consumable(final Consumable consumable) {
            this.consumable = Optional.ofNullable(consumable);
            return this;
        }

        public Builder retainEffects() {
            this.retainEffects = true;
            return this;
        }

        public Builder retainBeneficial() {
            this.retainBeneficial = true;
            return this;
        }

        public Builder retainNeutral() {
            this.retainNeutral = true;
            return this;
        }

        public Builder retainHarmful() {
            this.retainHarmful = true;
            return this;
        }

        public DietEntry build() {
            if (customProperties && properties.isEmpty()) {
                properties = Optional.of(new FoodProperties(nutriton, saturation, canAlwaysEat));
            }

            if (consumable.isEmpty() && (customConsumeDuration || !consumeEffects.isEmpty())) {
                Consumable.Builder builder = Consumable.builder();

                if (customConsumeDuration) {
                    builder.consumeSeconds(seconds);
                }

                consumeEffects.forEach(builder::onConsume);
                consumable = Optional.of(builder.build());
            }

            Either<Boolean, RetainEffects> retainEffects;

            if (retainBeneficial || retainNeutral || retainHarmful) {
                retainEffects = Either.right(new RetainEffects(retainBeneficial, retainNeutral, retainHarmful));
            } else if (this.retainEffects) {
                retainEffects = Either.left(true);
            } else {
                retainEffects = null;
            }

            return new DietEntry(items, properties, consumable, useRemainder, Optional.ofNullable(retainEffects));
        }
    }
}
