package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.ParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.RunFunctionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface AbilityBlockEffect {
    ResourceKey<Registry<MapCodec<? extends AbilityBlockEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("ability_block_effect"));
    Registry<MapCodec<? extends AbilityBlockEffect>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<AbilityBlockEffect> CODEC = REGISTRY.byNameCodec().dispatch("effect_type", AbilityBlockEffect::blockCodec, Function.identity());

    default List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        return List.of();
    }

    void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction);

    MapCodec<? extends AbilityBlockEffect> blockCodec();

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("bonemeal"), () -> BonemealEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("conversion"), () -> BlockConversionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("summon_entity"), () -> SummonEntityEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("fire"), () -> FireEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("area_cloud"), () -> AreaCloudEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("block_break"), () -> BlockBreakEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> ParticleEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("run_function"), () -> RunFunctionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("use_item"), () -> UseItemOnBlockEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("explosion"), () -> ExplodeBlockEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("block_harvest"), () -> BlockHarvestEffect.CODEC);
        }
    }
}
