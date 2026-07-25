package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
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
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AbilityBlockEffect {
    ResourceKey<Registry<MapCodec<? extends AbilityBlockEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("ability_block_effect"));
    MiscCodecs.RegistryHolder<MapCodec<? extends AbilityBlockEffect>> REGISTRY = new MiscCodecs.RegistryHolder<>();

    Codec<AbilityBlockEffect> CODEC = MiscCodecs.registryDispatchCodec(REGISTRY, "effect_type", AbilityBlockEffect::blockCodec);

    default List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        return List.of();
    }

    void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction);

    MapCodec<? extends AbilityBlockEffect> blockCodec();

    public static void register(final NewRegistryEvent event) {
        event.create(new RegistryBuilder<MapCodec<? extends AbilityBlockEffect>>().setName(REGISTRY_KEY.location()), REGISTRY::set);
    }

    public static void registerEntries(final RegisterEvent event) {
        if (event.getRegistryKey().equals(REGISTRY_KEY)) {
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
