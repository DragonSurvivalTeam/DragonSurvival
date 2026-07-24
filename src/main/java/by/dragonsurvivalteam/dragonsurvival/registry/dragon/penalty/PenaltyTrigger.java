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

@EventBusSubscriber
public interface PenaltyTrigger {
    ResourceKey<Registry<MapCodec<? extends PenaltyTrigger>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("penalty_trigger"));
    Registry<MapCodec<? extends PenaltyTrigger>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<PenaltyTrigger> CODEC = REGISTRY.byNameCodec().dispatch("penalty_trigger", PenaltyTrigger::codec, Function.identity());

    /** If this returns 'false' it will be applied per player tick */
    default boolean hasCustomTrigger() {
        return false;
    }

    default MutableComponent getDescription(final Player player) {
        return Component.empty();
    }

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
            event.register(REGISTRY_KEY, DragonSurvival.res("supply"), () -> SupplyTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("instant"), () -> InstantTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("item_used"), () -> ItemUsedTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("hit_by_projectile"), () -> HitByProjectileTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("hit_by_water_potion"), () -> HitByWaterPotionTrigger.CODEC);
        }
    }
}
