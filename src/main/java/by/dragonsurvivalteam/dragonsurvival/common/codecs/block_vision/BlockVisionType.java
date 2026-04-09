package by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Function;

@EventBusSubscriber
public interface BlockVisionType {
    ResourceKey<Registry<MapCodec<? extends BlockVisionType>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("block_vision_type"));
    Registry<MapCodec<? extends BlockVisionType>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<BlockVisionType> CODEC = REGISTRY.byNameCodec().dispatch("block_vision_type", BlockVisionType::codec, Function.identity());

    MapCodec<? extends BlockVisionType> codec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("outline"), () -> BlockVisionOutline.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> BlockVisionParticle.CODEC);
        }
    }
}
