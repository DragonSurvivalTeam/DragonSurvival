package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.mixins.AttributeMapAccessor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface AttributeModifierSupplier {
    static void removeModifiers(final ModifierType type, final LivingEntity entity) {
        removeModifiers(type, ((AttributeMapAccessor) entity.getAttributes()).dragonSurvival$getAttributes());
    }

    // TODO :: Throw an exception if this is called with the type 'CUSTOM'? Since CUSTOM would remove modifiers it shouldn't remove
    static void removeModifiers(final ModifierType type, final Map<Holder<Attribute>, AttributeInstance> attributes) {
        attributes.values().forEach(instance -> instance.getModifiers().forEach(modifier -> {
            if (modifier.id().getPath().startsWith(type.path())) {
                instance.removeModifier(modifier);
            }
        }));
    }

    default void applyModifiers(final LivingEntity entity, @Nullable final Holder<DragonSpecies> dragonSpecies, double level) {
        modifiers().forEach(modifier -> {
            AttributeInstance instance = entity.getAttribute(modifier.attribute());
            applyModifier(modifier, instance, dragonSpecies, level);
        });
    }

    default void removeModifiers(final LivingEntity entity) {
        Map<Holder<Attribute>, List<ResourceLocation>> ids = getStoredIds();

        ids.forEach((attribute, modifiers) -> {
            AttributeInstance instance = entity.getAttribute(attribute);

            if (instance != null) {
                modifiers.forEach(instance::removeModifier);
            }
        });

        ids.clear();
    }

    /** Intended for usage within descriptions */
    default double getAttributeValue(final Holder<DragonSpecies> dragonSpecies, double value, final Holder<Attribute> attribute) {
        AttributeInstance instance = new AttributeInstance(attribute, ignored -> { /* Nothing to do */ });
        applyModifiers(instance, dragonSpecies, value);
        return instance.getValue();
    }

    private void applyModifier(final Modifier modifier, @Nullable final AttributeInstance instance, @Nullable final Holder<DragonSpecies> dragonSpecies, double level) {
        if (instance == null || modifier.dragonSpecies().isPresent() && (dragonSpecies == null || !dragonSpecies.is(modifier.dragonSpecies().get()))) {
            return;
        }

        ModifierType type = getModifierType();
        ResourceLocation id;

        do {
            id = type.randomId(modifier.attribute(), modifier.operation());
        } while (instance.hasModifier(id));

        AttributeModifier attributeModifier = modifier.getModifier(id, level);
        instance.addPermanentModifier(attributeModifier);
        storeId(instance.getAttribute(), attributeModifier.id());
    }

    private void applyModifiers(@Nullable final AttributeInstance instance, final Holder<DragonSpecies> dragonSpecies, double value) {
        if (instance == null) {
            return;
        }

        modifiers().forEach(modifier -> {
            if (modifier.attribute().is(instance.getAttribute())) {
                applyModifier(modifier, instance, dragonSpecies, value);
            }
        });
    }

    default void storeId(final Holder<Attribute> attribute, final ResourceLocation id) { /* Nothing to do */ }

    default Map<Holder<Attribute>, List<ResourceLocation>> getStoredIds() {
        return Collections.emptyMap();
    }

    default List<Modifier> modifiers() {
        return List.of();
    }

    ModifierType getModifierType();
}
