package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface PenaltyEffect {
    ResourceKey<Registry<MapCodec<? extends PenaltyEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("penalty_effects"));
    Registry<MapCodec<? extends PenaltyEffect>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<PenaltyEffect> CODEC = REGISTRY.byNameCodec().dispatch(PenaltyEffect::codec, Function.identity());

    void apply(final Player player);
    MapCodec<? extends PenaltyEffect> codec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }
}