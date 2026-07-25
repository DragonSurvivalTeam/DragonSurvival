package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;


public interface PenaltyEffect {
    ResourceKey<Registry<MapCodec<? extends PenaltyEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("penalty_effect"));
    MiscCodecs.RegistryHolder<MapCodec<? extends PenaltyEffect>> REGISTRY = new MiscCodecs.RegistryHolder<>();

    Codec<PenaltyEffect> CODEC = MiscCodecs.registryDispatchCodec(REGISTRY, "penalty_type", PenaltyEffect::codec);

    default MutableComponent getDescription() {
        return Component.empty();
    }

    void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty);

    MapCodec<? extends PenaltyEffect> codec();

    public static void register(final NewRegistryEvent event) {
        event.create(new RegistryBuilder<MapCodec<? extends PenaltyEffect>>().setName(REGISTRY_KEY.location()), REGISTRY::set);
    }

    public static void registerEntries(final RegisterEvent event) {
        if (event.getRegistryKey().equals(REGISTRY_KEY)) {
            event.register(REGISTRY_KEY, DragonSurvival.res("take_damage"), () -> DamagePenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("mob_effect"), () -> MobEffectPenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("item_blacklist"), () -> ItemBlacklistPenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("damage_modification"), () -> DamageModificationPenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("fear"), () -> FearPenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("informational"), () -> InformationalPenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("modifier"), () -> ModifierPenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("effect_modification"), () -> EffectModificationPenalty.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("run_function"), () -> RunFunctionPenalty.CODEC);
        }
    }
}
