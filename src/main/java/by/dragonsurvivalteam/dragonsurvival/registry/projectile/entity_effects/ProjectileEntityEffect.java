package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
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
public interface ProjectileEntityEffect {
    ResourceKey<Registry<MapCodec<? extends ProjectileEntityEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_entity_effects"));
    Registry<MapCodec<? extends ProjectileEntityEffect>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<ProjectileEntityEffect> CODEC = REGISTRY.byNameCodec().dispatch(ProjectileEntityEffect::entityCodec, Function.identity());

    default List<MutableComponent> getDescription(final Player dragon, final int level) { return List.of(); }
    void apply(final Projectile projectile, final Entity target, final int projectileLevel);
    MapCodec<? extends ProjectileEntityEffect> entityCodec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("damage"), () -> ProjectileDamageEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("mob_effect"), () -> ProjectilePotionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("lightning"), () -> ProjectileLightningEntityEffect.CODEC);
        }
    }
}
