package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface PenaltyTrigger {
    ResourceKey<Registry<MapCodec<? extends PenaltyTrigger>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("penalty_triggers"));
    Registry<MapCodec<? extends PenaltyTrigger>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<PenaltyTrigger> CODEC = REGISTRY.byNameCodec().dispatch("effect_type", PenaltyTrigger::codec, Function.identity());

    default MutableComponent getDescription(Player player) { return Component.empty(); }
    default String id() { return ""; }

    MapCodec<? extends PenaltyTrigger> codec();
    boolean matches(final ServerPlayer dragon, boolean conditionMatched);

    static PenaltyTrigger instant() {
        return new InstantTrigger(1);
    }

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("supply_trigger"), () -> SupplyTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("instant_trigger"), () -> InstantTrigger.CODEC);
        }
    }
}
