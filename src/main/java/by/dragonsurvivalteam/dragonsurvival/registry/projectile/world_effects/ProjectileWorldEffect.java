package by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;


@EventBusSubscriber(modid = DragonSurvival.MODID, bus = EventBusSubscriber.Bus.MOD)
public interface ProjectileWorldEffect extends ProjectileEffect<Void> {
    ResourceKey<Registry<MapCodec<? extends ProjectileWorldEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_world_effect"));
    MiscCodecs.RegistryHolder<MapCodec<? extends ProjectileWorldEffect>> REGISTRY = new MiscCodecs.RegistryHolder<>();

    Codec<ProjectileWorldEffect> CODEC = MiscCodecs.registryDispatchCodec(REGISTRY, "world_effect", ProjectileWorldEffect::codec);

    MapCodec<? extends ProjectileWorldEffect> codec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.create(new RegistryBuilder<MapCodec<? extends ProjectileWorldEffect>>().setName(REGISTRY_KEY.location()), REGISTRY::set);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistryKey().equals(REGISTRY_KEY)) {
            event.register(REGISTRY_KEY, DragonSurvival.res("explosion"), () -> ProjectileExplosionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("lightning"), () -> ProjectileLightningWorldEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> ProjectileWorldParticleEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("run_function"), () -> ProjectileWorldRunFunctionEffect.CODEC);
        }
    }
}
