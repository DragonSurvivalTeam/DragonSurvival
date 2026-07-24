package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
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

public record BlockVisionEffect(List<BlockVision> visions) implements AbilityEntityEffect {
    public static final MapCodec<BlockVisionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockVision.CODEC.listOf().fieldOf("block_visions").forGetter(BlockVisionEffect::visions)
    ).apply(instance, BlockVisionEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target instanceof Player player) {
            visions.forEach(bonus -> bonus.apply(dragon, ability, player));
        }
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity, final boolean isAutoRemoval) {
        if (entity instanceof Player player) {
            visions.forEach(bonus -> {
                if (!isAutoRemoval || bonus.shouldRemoveAutomatically()) {
                    bonus.remove(player);
                }
            });
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> components = new ArrayList<>();

        for (BlockVision vision : visions) {
            components.add(vision.getDescription(ability.level()));
        }

        return components;
    }

    @Override
    public List<ResourceLocation> getEffectIDs() {
        return visions.stream().map(DurationInstanceBase::id).toList();
    }

    public static AbilityEntityEffect single(final BlockVision vision) {
        return new BlockVisionEffect(List.of(vision));
    }

    public static List<AbilityEntityEffect> only(final BlockVision vision) {
        return List.of(single(vision));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
