package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;


@EventBusSubscriber(modid = DragonSurvival.MODID, bus = EventBusSubscriber.Bus.MOD)
public interface PenaltyTrigger {
    ResourceKey<Registry<MapCodec<? extends PenaltyTrigger>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("penalty_trigger"));
    MiscCodecs.RegistryHolder<MapCodec<? extends PenaltyTrigger>> REGISTRY = new MiscCodecs.RegistryHolder<>();

    Codec<PenaltyTrigger> CODEC = MiscCodecs.registryDispatchCodec(REGISTRY, "penalty_trigger", PenaltyTrigger::codec);

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
        event.create(new RegistryBuilder<MapCodec<? extends PenaltyTrigger>>().setName(REGISTRY_KEY.location()), REGISTRY::set);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistryKey().equals(REGISTRY_KEY)) {
            event.register(REGISTRY_KEY, DragonSurvival.res("supply"), () -> SupplyTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("instant"), () -> InstantTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("item_used"), () -> ItemUsedTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("hit_by_projectile"), () -> HitByProjectileTrigger.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("hit_by_water_potion"), () -> HitByWaterPotionTrigger.CODEC);
        }
    }
}
