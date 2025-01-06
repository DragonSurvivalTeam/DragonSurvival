package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DamageModification;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public record DamageModificationEffect(List<DamageModification> modifications) implements AbilityEntityEffect {
    public static final MapCodec<DamageModificationEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageModification.CODEC.listOf().fieldOf("modifications").forGetter(DamageModificationEffect::modifications)
    ).apply(instance, DamageModificationEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        modifications.forEach(modification -> modification.apply(dragon, ability, entity));
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        modifications.forEach(modification -> modification.remove(entity));
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        for (DamageModification modification : modifications) {
            components.add(modification.getDescription(ability.level()));
        }

        return components;
    }

    public static List<AbilityEntityEffect> only(final DamageModification modification) {
        return List.of(new DamageModificationEffect(List.of(modification)));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
