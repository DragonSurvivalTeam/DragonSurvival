package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface ActivationTrigger {
    ResourceKey<Registry<MapCodec<? extends ActivationTrigger>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("activation_trigger"));
    Registry<MapCodec<? extends ActivationTrigger>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<ActivationTrigger> CODEC = REGISTRY.byNameCodec().dispatch("trigger_type", ActivationTrigger::codec, Function.identity());

    enum TriggerType {
        @Translation(comments = "Constant")
        CONSTANT,
        @Translation(comments = "On Self Hit")
        ON_SELF_HIT,
        @Translation(comments = "On Target Hit")
        ON_TARGET_HIT,
        @Translation(comments = "On Target Killed")
        ON_TARGET_KILLED,
        @Translation(comments = "On Death")
        ON_DEATH,
        @Translation(comments = "On Block Break")
        ON_BLOCK_BREAK
    }

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
        }
    }

    default boolean test(final LootContext context) {
        return true;
    }

    TriggerType type();

    MapCodec<? extends ActivationTrigger> codec();
}
