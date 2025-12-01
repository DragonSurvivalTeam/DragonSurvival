package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public record DietEntry(String items, Optional<FoodProperties> properties, Optional<Either<Boolean, RetainEffects>> retainEffects) {
    public static final Codec<DietEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocationWrapper.validatedCodec().fieldOf("items").forGetter(DietEntry::items),
            FoodProperties.DIRECT_CODEC.optionalFieldOf("properties").forGetter(DietEntry::properties),
            Codec.either(Codec.BOOL, RetainEffects.CODEC).optionalFieldOf("retain_effects").forGetter(DietEntry::retainEffects)
    ).apply(instance, DietEntry::new));

    public static Map<Item, FoodProperties> map(final List<DietEntry> entries) {
        Map<Item, FoodProperties> diet = new HashMap<>();

        for (DietEntry entry : entries) {
            ResourceLocationWrapper.map(entry.items(), BuiltInRegistries.ITEM).forEach(resource -> {
                Item item = BuiltInRegistries.ITEM.get(resource);

                if (item != null) {
                    FoodProperties properties = entry.properties().orElse(item.getDefaultInstance().getFoodProperties(null));

                    if (properties == null) {
                        DragonSurvival.LOGGER.warn("Diet entry [{}] has neither original nor custom food properties - item will not be edible", entry);
                        return;
                    }

                    List<FoodProperties.PossibleEffect> effects = new ArrayList<>(properties.effects());

                    if (entry.retainEffects().isPresent() && entry.properties().isPresent()) {
                        FoodProperties original = item.getDefaultInstance().getFoodProperties(null);

                        if (original != null) {
                            for (FoodProperties.PossibleEffect effect : original.effects()) {
                                if (entry.retainEffects().get().map(Function.identity(), check -> check.retain(effect.effect()))) {
                                    effects.add(effect);
                                }
                            }
                        }
                    } else if (entry.retainEffects().isPresent()) {
                        // Only retain specific effects of the original item
                        effects.removeIf(effect -> !entry.retainEffects().get().map(Function.identity(), check -> check.retain(effect.effect())));
                    } else if (entry.properties().isEmpty()) {
                        // Don't retain effects of the original item
                        effects.clear();
                    }

                    properties = new FoodProperties(
                            properties.nutrition(),
                            properties.saturation(),
                            properties.canAlwaysEat(),
                            properties.eatSeconds(),
                            properties.usingConvertsTo(),
                            effects
                    );

                    diet.put(item, properties);
                }
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

    public static Builder create(final ResourceLocation location) {
        return create(location.toString());
    }

    public static Builder create(final Item item) {
        //noinspection deprecation, DataFlowIssue -> ignore deprecated / key is present
        return create(item.builtInRegistryHolder().getKey().location());
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
        // Copied from FoodProperties.java. Trying to AT this didn't work out well.
        private static final float DEFAULT_EAT_SECONDS = 1.6f;

        private final String items;
        private Optional<FoodProperties> properties = Optional.empty();
        private boolean retainEffects = false;
        private boolean retainBeneficial = false;
        private boolean retainNeutral = false;
        private boolean retainHarmful = false;

        private boolean customProperties;
        
        private int nutriton;
        private float saturation;
        private boolean canAlwaysEat;
        private float seconds = DEFAULT_EAT_SECONDS;

        private final List<FoodProperties.PossibleEffect> effects = new ArrayList<>();
        private Optional<ItemStack> convertsTo = Optional.empty();

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
            this.customProperties = true;
            return this;
        }

        public Builder fast() {
            this.seconds = DEFAULT_EAT_SECONDS / 2;
            this.customProperties = true;
            return this;
        }

        public Builder effect(final Supplier<MobEffectInstance> effect, final float probability) {
            this.effects.add(new FoodProperties.PossibleEffect(effect, probability));
            this.customProperties = true;
            return this;
        }

        public Builder convertsTo(final ItemLike item) {
            this.convertsTo = Optional.of(item.asItem().getDefaultInstance());
            this.customProperties = true;
            return this;
        }

        public Builder properties(final FoodProperties properties) {
            this.properties = Optional.ofNullable(properties);
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
                properties = Optional.of(new FoodProperties(nutriton, saturation, canAlwaysEat, seconds, convertsTo, effects));
            }

            Either<Boolean, RetainEffects> retainEffects;

            if (retainBeneficial || retainNeutral || retainHarmful) {
                retainEffects = Either.right(new RetainEffects(retainBeneficial, retainNeutral, retainHarmful));
            } else if (this.retainEffects) {
                retainEffects = Either.left(true);
            } else {
                retainEffects = null;
            }

            return new DietEntry(items, properties, Optional.ofNullable(retainEffects));
        }
    }
}
