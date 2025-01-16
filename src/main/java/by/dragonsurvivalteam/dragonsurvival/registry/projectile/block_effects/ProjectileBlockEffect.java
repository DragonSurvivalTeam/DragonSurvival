package by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface ProjectileBlockEffect extends ProjectileEffect<BlockPos> {
    ResourceKey<Registry<MapCodec<? extends ProjectileBlockEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_block_effects"));
    Registry<MapCodec<? extends ProjectileBlockEffect>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<ProjectileBlockEffect> CODEC = REGISTRY.byNameCodec().dispatch("block_effect", ProjectileBlockEffect::codec, Function.identity());
    MapCodec<? extends ProjectileBlockEffect> codec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> ProjectileBlockParticleEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("run_function"), () -> ProjectileBlockRunFunctionEffect.CODEC);
        }
    }
}
