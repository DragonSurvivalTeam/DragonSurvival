package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Supplier;

/**
 * Bridges the 1.21 food data codec to the older runtime representation without
 * changing Dragon Survival's data map format.
 */
public final class FoodPropertiesCompat {
    private static final float DEFAULT_EAT_SECONDS = 1.6F;
    private static final Map<FoodProperties, FoodData> METADATA = Collections.synchronizedMap(new WeakHashMap<>());

    public static final Codec<FoodProperties> DIRECT_CODEC = FoodData.CODEC.xmap(FoodData::toFoodProperties, FoodData::fromFoodProperties);

    private FoodPropertiesCompat() {}

    public static FoodProperties create(
            final int nutrition,
            final float saturation,
            final boolean canAlwaysEat,
            final float eatSeconds,
            final Optional<ItemStack> usingConvertsTo,
            final List<PossibleEffect> effects
    ) {
        Optional<ConversionItem> conversion = usingConvertsTo.map(ConversionItem::fromStack);
        return new FoodData(nutrition, saturation, canAlwaysEat, eatSeconds, conversion, effects).toFoodProperties();
    }

    public static int nutrition(final FoodProperties properties) {
        return FoodData.fromFoodProperties(properties).nutrition();
    }

    public static float saturation(final FoodProperties properties) {
        return FoodData.fromFoodProperties(properties).saturation();
    }

    public static boolean canAlwaysEat(final FoodProperties properties) {
        return FoodData.fromFoodProperties(properties).canAlwaysEat();
    }

    public static float eatSeconds(final FoodProperties properties) {
        return FoodData.fromFoodProperties(properties).eatSeconds();
    }

    public static int eatDurationTicks(final FoodProperties properties) {
        return (int) (eatSeconds(properties) * 20.0F);
    }

    public static Optional<ItemStack> usingConvertsTo(final FoodProperties properties) {
        return FoodData.fromFoodProperties(properties).usingConvertsTo().map(ConversionItem::toStack);
    }

    public static List<PossibleEffect> effects(final FoodProperties properties) {
        return FoodData.fromFoodProperties(properties).effects();
    }

    public record PossibleEffect(EffectData effectData, float probability) {
        public static final Codec<PossibleEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EffectData.CODEC.fieldOf("effect").forGetter(PossibleEffect::effectData),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("probability", 1.0F).forGetter(PossibleEffect::probability)
        ).apply(instance, PossibleEffect::new));

        public PossibleEffect(final Supplier<MobEffectInstance> effect, final float probability) {
            this(EffectData.fromInstance(effect.get()), probability);
        }

        public MobEffectInstance effect() {
            return effectData.toInstance();
        }
    }

    private record FoodData(
            int nutrition,
            float saturation,
            boolean canAlwaysEat,
            float eatSeconds,
            Optional<ConversionItem> usingConvertsTo,
            List<PossibleEffect> effects
    ) {
        private static final Codec<FoodData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(FoodData::nutrition),
                Codec.FLOAT.fieldOf("saturation").forGetter(FoodData::saturation),
                Codec.BOOL.optionalFieldOf("can_always_eat", false).forGetter(FoodData::canAlwaysEat),
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("eat_seconds", DEFAULT_EAT_SECONDS).forGetter(FoodData::eatSeconds),
                ConversionItem.CODEC.optionalFieldOf("using_converts_to").forGetter(FoodData::usingConvertsTo),
                PossibleEffect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(FoodData::effects)
        ).apply(instance, FoodData::new));

        private FoodData {
            effects = List.copyOf(effects);
        }

        private FoodProperties toFoodProperties() {
            FoodProperties.Builder builder = new FoodProperties.Builder()
                    .nutrition(nutrition)
                    .saturationMod(nutrition == 0 ? 0.0F : saturation / (nutrition * 2.0F));

            if (canAlwaysEat) {
                builder.alwaysEat();
            }
            if (eatSeconds <= DEFAULT_EAT_SECONDS / 2.0F) {
                builder.fast();
            }
            effects.forEach(effect -> builder.effect(effect::effect, effect.probability()));

            FoodProperties properties = builder.build();
            METADATA.put(properties, this);
            return properties;
        }

        private static FoodData fromFoodProperties(final FoodProperties properties) {
            FoodData metadata = METADATA.get(properties);
            if (metadata != null) {
                return metadata;
            }

            int nutrition = properties.getNutrition();
            List<PossibleEffect> effects = new ArrayList<>();
            for (Pair<MobEffectInstance, Float> effect : properties.getEffects()) {
                effects.add(new PossibleEffect(() -> new MobEffectInstance(effect.getFirst()), effect.getSecond()));
            }

            return new FoodData(
                    nutrition,
                    properties.getSaturationModifier() * nutrition * 2.0F,
                    properties.canAlwaysEat(),
                    properties.isFastFood() ? DEFAULT_EAT_SECONDS / 2.0F : DEFAULT_EAT_SECONDS,
                    Optional.empty(),
                    effects
            );
        }
    }

    private record ConversionItem(Item item, Optional<Dynamic<?>> components) {
        private static final Codec<ConversionItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("id").forGetter(ConversionItem::item),
                Codec.PASSTHROUGH.optionalFieldOf("components").forGetter(ConversionItem::components)
        ).apply(instance, ConversionItem::new));

        private static ConversionItem fromStack(final ItemStack stack) {
            return new ConversionItem(stack.getItem(), Optional.empty());
        }

        private ItemStack toStack() {
            return new ItemStack(item);
        }
    }

    private record EffectData(
            MobEffect effect,
            int amplifier,
            int duration,
            boolean ambient,
            boolean showParticles,
            boolean showIcon,
            Optional<EffectDetails> hiddenEffect,
            Optional<Dynamic<?>> cures
    ) {
        private static final Codec<EffectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("id").forGetter(EffectData::effect),
                Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(EffectData::amplifier),
                Codec.INT.optionalFieldOf("duration", 0).forGetter(EffectData::duration),
                Codec.BOOL.optionalFieldOf("ambient", false).forGetter(EffectData::ambient),
                Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(EffectData::showParticles),
                Codec.BOOL.optionalFieldOf("show_icon").forGetter(data -> Optional.of(data.showIcon())),
                EffectDetails.CODEC.optionalFieldOf("hidden_effect").forGetter(EffectData::hiddenEffect),
                Codec.PASSTHROUGH.optionalFieldOf("neoforge:cures").forGetter(EffectData::cures)
        ).apply(instance, EffectData::create));

        private static EffectData create(
                final MobEffect effect,
                final int amplifier,
                final int duration,
                final boolean ambient,
                final boolean showParticles,
                final Optional<Boolean> showIcon,
                final Optional<EffectDetails> hiddenEffect,
                final Optional<Dynamic<?>> cures
        ) {
            return new EffectData(effect, amplifier, duration, ambient, showParticles, showIcon.orElse(showParticles), hiddenEffect, cures);
        }

        private static EffectData fromInstance(final MobEffectInstance effect) {
            return new EffectData(
                    effect.getEffect(),
                    effect.getAmplifier(),
                    effect.getDuration(),
                    effect.isAmbient(),
                    effect.isVisible(),
                    effect.showIcon(),
                    Optional.empty(),
                    Optional.empty()
            );
        }

        private MobEffectInstance toInstance() {
            MobEffectInstance hidden = hiddenEffect.map(details -> details.toInstance(effect)).orElse(null);
            return new MobEffectInstance(effect, duration, amplifier, ambient, showParticles, showIcon, hidden, Optional.empty());
        }
    }

    private record EffectDetails(
            int amplifier,
            int duration,
            boolean ambient,
            boolean showParticles,
            boolean showIcon,
            Optional<EffectDetails> hiddenEffect,
            Optional<Dynamic<?>> cures
    ) {
        private static final Codec<EffectDetails> CODEC = ExtraCodecs.lazyInitializedCodec(EffectDetails::createCodec);

        private static Codec<EffectDetails> createCodec() {
            return RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(0, 255).optionalFieldOf("amplifier", 0).forGetter(EffectDetails::amplifier),
                    Codec.INT.optionalFieldOf("duration", 0).forGetter(EffectDetails::duration),
                    Codec.BOOL.optionalFieldOf("ambient", false).forGetter(EffectDetails::ambient),
                    Codec.BOOL.optionalFieldOf("show_particles", true).forGetter(EffectDetails::showParticles),
                    Codec.BOOL.optionalFieldOf("show_icon").forGetter(data -> Optional.of(data.showIcon())),
                    CODEC.optionalFieldOf("hidden_effect").forGetter(EffectDetails::hiddenEffect),
                    Codec.PASSTHROUGH.optionalFieldOf("neoforge:cures").forGetter(EffectDetails::cures)
            ).apply(instance, EffectDetails::create));
        }

        private static EffectDetails create(
                final int amplifier,
                final int duration,
                final boolean ambient,
                final boolean showParticles,
                final Optional<Boolean> showIcon,
                final Optional<EffectDetails> hiddenEffect,
                final Optional<Dynamic<?>> cures
        ) {
            return new EffectDetails(amplifier, duration, ambient, showParticles, showIcon.orElse(showParticles), hiddenEffect, cures);
        }

        private MobEffectInstance toInstance(final MobEffect effect) {
            MobEffectInstance hidden = hiddenEffect.map(details -> details.toInstance(effect)).orElse(null);
            return new MobEffectInstance(effect, duration, amplifier, ambient, showParticles, showIcon, hidden, Optional.empty());
        }
    }
}
