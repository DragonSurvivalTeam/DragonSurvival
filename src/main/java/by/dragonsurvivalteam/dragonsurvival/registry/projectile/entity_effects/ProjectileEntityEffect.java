package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;


public interface ProjectileEntityEffect extends ProjectileEffect<Entity> {
    ResourceKey<Registry<MapCodec<? extends ProjectileEntityEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_entity_effect"));
    MiscCodecs.RegistryHolder<MapCodec<? extends ProjectileEntityEffect>> REGISTRY = new MiscCodecs.RegistryHolder<>();

    Codec<ProjectileEntityEffect> CODEC = MiscCodecs.registryDispatchCodec(REGISTRY, "entity_effect", ProjectileEntityEffect::codec);

    MapCodec<? extends ProjectileEntityEffect> codec();

    public static void register(final NewRegistryEvent event) {
        event.create(new RegistryBuilder<MapCodec<? extends ProjectileEntityEffect>>().setName(REGISTRY_KEY.location()), REGISTRY::set);
    }

    public static void registerEntries(final RegisterEvent event) {
        if (event.getRegistryKey().equals(REGISTRY_KEY)) {
            event.register(REGISTRY_KEY, DragonSurvival.res("damage"), () -> ProjectileDamageEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("potion"), () -> ProjectilePotionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("lightning"), () -> ProjectileLightningEntityEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> ProjectileEntityParticleEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("run_function"), () -> ProjectileEntityRunFunctionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("push"), () -> ProjectileEntityPushEffect.CODEC);
        }
    }
}
