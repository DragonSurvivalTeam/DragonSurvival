package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.mixins.AttributeMapAccessor;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface AttributeModifierSupplier {
    static void removeModifiers(final ModifierType type, final LivingEntity entity) {
        if (type == ModifierType.CUSTOM) {
            Functions.logOrThrow("Modifiers of the type " + ModifierType.CUSTOM.name() + " need to be handled using the stored ids");
            return;
        }

        ((AttributeMapAccessor) entity.getAttributes()).dragonSurvival$getAttributes().values().forEach(instance -> instance.getModifiers().forEach(modifier -> {
            if (modifier.id().getPath().startsWith(type.path())) {
                instance.removeModifier(modifier);
            }
        }));
    }

    /** Filters out the attribute modifiers that belong to this supplier */
    default List<AttributeModifier> filterModifiers(final AttributeInstance instance) {
        List<AttributeModifier> modifiers = new ArrayList<>();

        if (getModifierType() == ModifierType.CUSTOM) {
            List<ResourceLocation> ids = getStoredIds().getOrDefault(instance.getAttribute(), List.of());

            for (AttributeModifier modifier : instance.getModifiers()) {
                if (!ids.contains(modifier.id())) {
                    modifiers.add(modifier);
                }
            }
        } else {
            for (AttributeModifier modifier : instance.getModifiers()) {
                if (!modifier.id().getPath().startsWith(getModifierType().path())) {
                    modifiers.add(modifier);
                }
            }
        }

        return modifiers;
    }

    default void applyModifiers(final LivingEntity entity) {
        applyModifiers(entity, 1);
    }

    default void applyModifiers(final LivingEntity entity, double level) {
        modifiers().forEach(modifier -> {
            AttributeInstance instance = entity.getAttribute(modifier.attribute());
            applyModifier(modifier, instance, level);
        });
    }

    default void removeModifiers(final LivingEntity entity) {
        if (getModifierType() != ModifierType.CUSTOM) {
            removeModifiers(getModifierType(), entity);
            return;
        }

        Map<Holder<Attribute>, List<ResourceLocation>> ids = getStoredIds();

        ids.forEach((attribute, modifiers) -> {
            AttributeInstance instance = entity.getAttribute(attribute);

            if (instance != null) {
                modifiers.forEach(instance::removeModifier);
            }
        });

        ids.clear();
    }

    private void applyModifier(final Modifier modifier, @Nullable final AttributeInstance instance, double level) {
        if (instance == null) {
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

    default void storeId(final Holder<Attribute> attribute, final ResourceLocation id) { /* Nothing to do */ }

    default Map<Holder<Attribute>, List<ResourceLocation>> getStoredIds() {
        return Collections.emptyMap();
    }

    default List<Modifier> modifiers() {
        return List.of();
    }

    ModifierType getModifierType();
}
