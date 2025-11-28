package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
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
import java.util.function.Supplier;

public record DietEntry(String items, Optional<FoodProperties> properties, boolean retainEffects) {
    public static final Codec<DietEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocationWrapper.validatedCodec().fieldOf("items").forGetter(DietEntry::items),
            FoodProperties.DIRECT_CODEC.optionalFieldOf("properties").forGetter(DietEntry::properties),
            Codec.BOOL.optionalFieldOf("retain_effects", true).forGetter(DietEntry::retainEffects)
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

                    if (entry.retainEffects() && entry.properties().isPresent()) {
                        // Custom food properties were specified -> retain the effects of the original properties
                        FoodProperties original = item.getDefaultInstance().getFoodProperties(null);

                        if (original != null) {
                            effects.addAll(original.effects());
                        }
                    } else if (!entry.retainEffects() && entry.properties().isEmpty()) {
                        // Remove the original effects
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

    public static Builder create() {
        return new Builder();
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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static class Builder {
        // Copied from FoodProperties.java. Trying to AT this didn't work out well.
        private static final float DEFAULT_EAT_SECONDS = 1.6f;

        private String items;
        private Optional<FoodProperties> properties = Optional.empty();
        private boolean retainEffects = true;

        private boolean customProperties;
        
        private int nutriton;
        private float saturation;
        private boolean canAlwaysEat;
        private float seconds = DEFAULT_EAT_SECONDS;

        private final List<FoodProperties.PossibleEffect> effects = new ArrayList<>();
        private Optional<ItemStack> convertsTo = Optional.empty();

        public Builder items(final String items) {
            this.items = items;
            return this;
        }

        public Builder items(final TagKey<Item> tag) {
            return items("#" + tag.location());
        }

        public Builder items(final ResourceLocation location) {
            return items(location.toString());
        }

        public Builder items(final Item item) {
            //noinspection deprecation, DataFlowIssue -> ignore deprecated / key is present
            return items(item.builtInRegistryHolder().getKey().location());
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

        public Builder removeOriginalEffects() {
            this.retainEffects = false;
            return this;
        }

        public DietEntry build() {
            if (customProperties && properties.isEmpty()) {
                properties = Optional.of(new FoodProperties(nutriton, saturation, canAlwaysEat, seconds, convertsTo, effects));
            }

            return new DietEntry(items, properties, retainEffects);
        }
    }
}
