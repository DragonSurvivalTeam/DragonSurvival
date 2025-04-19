package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface ProjectileEntityEffect extends ProjectileEffect<Entity> {
    ResourceKey<Registry<MapCodec<? extends ProjectileEntityEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_entity_effect"));
    Registry<MapCodec<? extends ProjectileEntityEffect>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<ProjectileEntityEffect> CODEC = REGISTRY.byNameCodec().dispatch("entity_effect", ProjectileEntityEffect::codec, Function.identity());

    MapCodec<? extends ProjectileEntityEffect> codec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("damage"), () -> ProjectileDamageEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("potion"), () -> ProjectilePotionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("lightning"), () -> ProjectileLightningEntityEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> ProjectileEntityParticleEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("run_function"), () -> ProjectileEntityRunFunctionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("push"), () -> ProjectileEntityPushEffect.CODEC);
        }
    }
}
