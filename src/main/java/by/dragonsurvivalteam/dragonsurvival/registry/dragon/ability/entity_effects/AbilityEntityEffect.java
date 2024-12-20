package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.List;
import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface AbilityEntityEffect {
    ResourceKey<Registry<MapCodec<? extends AbilityEntityEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("ability_entity_effects"));
    Registry<MapCodec<? extends AbilityEntityEffect>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<AbilityEntityEffect> CODEC = REGISTRY.byNameCodec().dispatch("effect_type", AbilityEntityEffect::entityCodec, Function.identity());

    void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity);
    MapCodec<? extends AbilityEntityEffect> entityCodec();

    default boolean shouldAppendSelfTargetingToDescription() { return true; }
    default List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) { return List.of(); }
    default void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) { /* Nothing to do */ }

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("damage"), () -> DamageEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("modifier"), () -> ModifierEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("potion"), () -> PotionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("projectile"), () -> ProjectileEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("summon_entity"), () -> SummonEntityEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("damage_modification"), () -> DamageModificationEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("breath_particles"), () -> BreathParticlesEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("ignite"), () -> IgniteEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("harvest_bonus"), () -> HarvestBonusEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("on_attack"), () -> OnAttackEffect.CODEC);
        }
    }
}
