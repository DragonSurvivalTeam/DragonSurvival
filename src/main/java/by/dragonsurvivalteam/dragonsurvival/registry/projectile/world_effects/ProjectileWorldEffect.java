package by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.common_effects.ProjectileParticleEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.List;
import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface ProjectileWorldEffect {
    ResourceKey<Registry<MapCodec<? extends ProjectileWorldEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_world_effects"));
    Registry<MapCodec<? extends ProjectileWorldEffect>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<ProjectileWorldEffect> CODEC = REGISTRY.byNameCodec().dispatch(ProjectileWorldEffect::worldCodec, Function.identity());

    default List<MutableComponent> getDescription(final Player dragon, final int level) { return List.of(); }
    void apply(final Projectile projectile, final int level);
    MapCodec<? extends ProjectileWorldEffect> worldCodec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("explosion"), () -> ProjectileExplosionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("lightning"), () -> ProjectileLightningWorldEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> ProjectileParticleEffect.CODEC);
        }
    }
}