package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DragonFood {
    private final @Nullable ItemStackTemplate baseStack;
    private final Optional<FoodProperties> overrideProperties;
    private final Optional<Consumable> overrideConsumable;
    private final Optional<UseRemainder> overrideUseRemainder;
    private final Optional<Either<Boolean, DietEntry.RetainEffects>> retainEffects;

    private boolean resolvedProperties;
    private @Nullable FoodProperties properties;

    private boolean resolvedConsumable;
    private @Nullable Consumable consumable;

    private @Nullable Optional<UseRemainder> useRemainder;

    public DragonFood(
        final Item item,
        final Optional<FoodProperties> overrideProperties,
        final Optional<Consumable> overrideConsumable,
        final Optional<UseRemainder> overrideUseRemainder,
        final Optional<Either<Boolean, DietEntry.RetainEffects>> retainEffects
    ) {
        this.baseStack = new ItemStackTemplate(item);
        this.overrideProperties = overrideProperties;
        this.overrideConsumable = overrideConsumable;
        this.overrideUseRemainder = overrideUseRemainder;
        this.retainEffects = retainEffects;
    }

    public DragonFood(final FoodProperties properties, final Consumable consumable, final Optional<UseRemainder> useRemainder) {
        this.baseStack = null;
        this.overrideProperties = Optional.of(properties);
        this.overrideConsumable = Optional.of(consumable);
        this.overrideUseRemainder = useRemainder;
        this.retainEffects = Optional.empty();
    }

    public @Nullable FoodProperties properties() {
        if (!resolvedProperties) {
            properties = overrideProperties.orElse(baseStack != null ? baseStack.get(DataComponents.FOOD) : null);
            resolvedProperties = true;
        }

        return properties;
    }

    public Consumable consumable() {
        if (!resolvedConsumable) {
            Consumable originalConsumable = baseStack != null ? baseStack.get(DataComponents.CONSUMABLE) : null;
            Consumable baseConsumable = overrideConsumable.orElseGet(() -> originalConsumable != null ? originalConsumable : Consumables.DEFAULT_FOOD);
            List<ConsumeEffect> consumeEffects = new ArrayList<>();

            if (overrideConsumable.isPresent()) {
                consumeEffects.addAll(overrideConsumable.get().onConsumeEffects());
            }

            if (retainEffects.isPresent() && originalConsumable != null) {
                consumeEffects.addAll(getRetainedEffects(originalConsumable, retainEffects.get()));
            }

            consumable = new Consumable(
                baseConsumable.consumeSeconds(),
                baseConsumable.animation(),
                baseConsumable.sound(),
                baseConsumable.hasConsumeParticles(),
                consumeEffects
            );
            resolvedConsumable = true;
        }

        return consumable;
    }

    public Optional<UseRemainder> useRemainder() {
        if (useRemainder == null) {
            useRemainder = overrideUseRemainder.isPresent() ? overrideUseRemainder : Optional.ofNullable(baseStack != null ? baseStack.get(DataComponents.USE_REMAINDER) : null);
        }

        return useRemainder;
    }

    private static List<ConsumeEffect> getRetainedEffects(final Consumable originalConsumable, final Either<Boolean, DietEntry.RetainEffects> retainEffects) {
        if (retainEffects.left().orElse(false)) {
            return originalConsumable.onConsumeEffects();
        }

        DietEntry.RetainEffects filter = retainEffects.right().orElseThrow();
        return originalConsumable.onConsumeEffects()
            .stream()
            .map(effect -> retain(effect, filter))
            .filter(Objects::nonNull)
            .toList();
    }

    private static @Nullable ConsumeEffect retain(final ConsumeEffect effect, final DietEntry.RetainEffects filter) {
        if (!(effect instanceof ApplyStatusEffectsConsumeEffect applyStatusEffects)) {
            return null;
        }

        List<net.minecraft.world.effect.MobEffectInstance> retained = applyStatusEffects.effects().stream().filter(filter::retain).toList();
        if (retained.isEmpty()) {
            return null;
        }

        return new ApplyStatusEffectsConsumeEffect(retained, applyStatusEffects.probability());
    }
}
