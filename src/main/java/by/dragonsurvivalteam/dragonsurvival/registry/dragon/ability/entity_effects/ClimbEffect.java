package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Climbable;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public record ClimbEffect(List<Climbable> climbables) implements AbilityEntityEffect {
    public static final MapCodec<ClimbEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Climbable.CODEC.listOf().fieldOf("climbables").forGetter(ClimbEffect::climbables)
    ).apply(instance, ClimbEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        climbables.forEach(modification -> modification.apply(dragon, ability, target));
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity, final boolean isAutoRemoval) {
        climbables.forEach(modification -> {
            if (!isAutoRemoval || modification.shouldRemoveAutomatically()) {
                modification.remove(entity);
            }
        });
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        for (Climbable climbable : climbables) {
            components.add(climbable.getDescription(ability.level()));
        }

        return components;
    }

    @Override
    public List<ResourceLocation> getEffectIDs() {
        List<ResourceLocation> ids = new ArrayList<>();

        for (Climbable climbable : climbables) {
            ids.add(climbable.id());
        }

        return ids;
    }

    public static List<AbilityEntityEffect> only(final Climbable climbable) {
        return List.of(new ClimbEffect(List.of(climbable)));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
