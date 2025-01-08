package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface PenaltyEffect {
    ResourceKey<Registry<MapCodec<? extends PenaltyEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("penalty_effects"));
    Registry<MapCodec<? extends PenaltyEffect>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<PenaltyEffect> CODEC = REGISTRY.byNameCodec().dispatch("penalty_type", PenaltyEffect::codec, Function.identity());

    default MutableComponent getDescription() { return Component.empty(); }

    void apply(final ServerPlayer player);
    MapCodec<? extends PenaltyEffect> codec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("take_damage"), () -> DamagePenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("mob_effect"), () -> MobEffectPenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("item_blacklist"), () -> ItemBlacklistPenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("damage_modification"), () -> DamageModificationPenalty.CODEC);
        }
    }
}
