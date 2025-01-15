package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public record ModifierEffect(List<ModifierWithDuration> modifiers) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Attribute modifiers:§r")
    public static final String ATTRIBUTE_MODIFIERS = Translation.Type.GUI.wrap("general.attribute_modifiers");

    public static final MapCodec<ModifierEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ModifierWithDuration.CODEC.listOf().fieldOf("modifiers").forGetter(ModifierEffect::modifiers)
    ).apply(instance, ModifierEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target instanceof LivingEntity livingEntity) {
            modifiers.forEach(modifier -> modifier.apply(dragon, ability, livingEntity));
        }
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            modifiers.forEach(modifier -> modifier.remove(livingEntity));
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        MutableComponent description = null;

        for (ModifierWithDuration modifier : modifiers) {
            MutableComponent modifierDescription = modifier.getDescription(ability.level());

            if (description == null) {
                description = modifierDescription;
            } else if (modifierDescription != null) {
                description.append(modifierDescription);
            }
        }

        if (description != null) {
            description = Component.translatable(ATTRIBUTE_MODIFIERS).append(description);
        }

        return description == null ? List.of() : List.of(description);
    }

    public static List<AbilityEntityEffect> only(final ModifierWithDuration modifier) {
        return List.of(new ModifierEffect(List.of(modifier)));
    }

    public static AbilityEntityEffect single(final ModifierWithDuration modifier) {
        return new ModifierEffect(List.of(modifier));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
