package by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;


@EventBusSubscriber(modid = DragonSurvival.MODID, bus = EventBusSubscriber.Bus.MOD)
public interface ProjectileBlockEffect extends ProjectileEffect<BlockPos> {
    ResourceKey<Registry<MapCodec<? extends ProjectileBlockEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_block_effect"));
    MiscCodecs.RegistryHolder<MapCodec<? extends ProjectileBlockEffect>> REGISTRY = new MiscCodecs.RegistryHolder<>();

    Codec<ProjectileBlockEffect> CODEC = MiscCodecs.registryDispatchCodec(REGISTRY, "block_effect", ProjectileBlockEffect::codec);

    MapCodec<? extends ProjectileBlockEffect> codec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.create(new RegistryBuilder<MapCodec<? extends ProjectileBlockEffect>>().setName(REGISTRY_KEY.location()), REGISTRY::set);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistryKey().equals(REGISTRY_KEY)) {
            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> ProjectileBlockParticleEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("run_function"), () -> ProjectileBlockRunFunctionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("area_cloud"), () -> ProjectileAreaCloudEffect.CODEC);
        }
    }
}
