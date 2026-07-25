package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;


@EventBusSubscriber
public interface ActivationTrigger<T> {
    ResourceKey<Registry<MapCodec<? extends ActivationTrigger<?>>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("activation_trigger"));
    Registry<MapCodec<? extends ActivationTrigger<?>>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<ActivationTrigger<?>> CODEC = REGISTRY.byNameCodec().dispatch("trigger_type", ActivationTrigger::codec, MapCodec::codec);

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("constant"), () -> ConstantTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("on_self_hit"), () -> OnSelfHit.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("on_target_hit"), () -> OnTargetHit.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("on_target_killed"), () -> OnTargetKilled.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("on_death"), () -> OnDeath.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("on_block_break"), () -> OnBlockBreak.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("on_key_pressed"), () -> OnKeyPressed.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("on_key_released"), () -> OnKeyReleased.CODEC);
        }
    }

    default boolean test(final T testContext) {
        return true;
    }

    Component translation();

    MapCodec<? extends ActivationTrigger<?>> codec();
}
