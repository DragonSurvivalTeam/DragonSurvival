package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Phasing;
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

public record BlockPhasingEffect(List<Phasing> phases) implements AbilityEntityEffect {
    public static final MapCodec<BlockPhasingEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Phasing.CODEC.listOf().fieldOf("phases").forGetter(BlockPhasingEffect::phases)
    ).apply(instance, BlockPhasingEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        phases.forEach(phase -> phase.apply(dragon, ability, target));
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity, final boolean isAutoRemoval) {
        phases.forEach(phase -> {
            if (!isAutoRemoval || phase.shouldRemoveAutomatically()) {
                phase.remove(entity);
            }
        });
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        for (Phasing phase : phases) {
            components.add(phase.getDescription(ability.level()));
        }

        return components;
    }

    @Override
    public List<ResourceLocation> getEffectIDs() {
        List<ResourceLocation> ids = new ArrayList<>();

        for (Phasing phase : phases) {
            ids.add(phase.id());
        }

        return ids;
    }

    public static List<AbilityEntityEffect> only(final Phasing modifier) {
        return List.of(new BlockPhasingEffect(List.of(modifier)));
    }

    public static BlockPhasingEffect single(final Phasing modifier) {
        return new BlockPhasingEffect(List.of(modifier));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
