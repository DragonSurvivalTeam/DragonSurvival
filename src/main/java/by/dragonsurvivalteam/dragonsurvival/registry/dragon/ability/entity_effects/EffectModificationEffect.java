package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.EffectModification;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public record EffectModificationEffect(List<EffectModification> modifications) implements AbilityEntityEffect {
    public static final MapCodec<EffectModificationEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EffectModification.CODEC.listOf().fieldOf("modifications").forGetter(EffectModificationEffect::modifications)
    ).apply(instance, EffectModificationEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            modifications.forEach(modification -> modification.apply(dragon, ability, livingEntity));
        }
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            modifications.forEach(modification -> modification.remove(livingEntity));
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        for (EffectModification modification : modifications) {
            components.add(modification.getDescription(ability.level()));
        }
        return components;
    }

    public static List<AbilityEntityEffect> single(final EffectModification modification) {
        return List.of(new EffectModificationEffect(List.of(modification)));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
