package by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Activation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Function;

//@EventBusSubscriber
// TODO :: implement this properly for 1.22 (breaking change for block vision
public interface BlockVisionType {
    ResourceKey<Registry<MapCodec<? extends Activation>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("block_vision"));
    Registry<MapCodec<? extends Activation>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<Activation> CODEC = REGISTRY.byNameCodec().dispatch("block_vision_type", Activation::codec, Function.identity());

//    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

//    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
//            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> ....CODEC);
//            event.register(REGISTRY_KEY, DragonSurvival.res("outline"), () -> ....CODEC);
//            event.register(REGISTRY_KEY, DragonSurvival.res("treasure"), () -> ....CODEC);
//            event.register(REGISTRY_KEY, DragonSurvival.res("treasure_shader"), () -> ....CODEC);
        }
    }

    MapCodec<? extends BlockVisionType> codec();
}
