package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.ParticleEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.RunFunctionEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

@EventBusSubscriber
public interface AbilityEntityEffect {
    ResourceKey<Registry<MapCodec<? extends AbilityEntityEffect>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("ability_entity_effect"));
    Registry<MapCodec<? extends AbilityEntityEffect>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<AbilityEntityEffect> CODEC = REGISTRY.byNameCodec().dispatch("effect_type", AbilityEntityEffect::entityCodec, Function.identity());

    void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target);

    MapCodec<? extends AbilityEntityEffect> entityCodec();

    default List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        return List.of();
    }

    default void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity, boolean isAutoRemoval) { /* Nothing to do */ }

    default boolean shouldRemoveAutomatically() {
        return false;
    }

    default List<ResourceLocation> getEffectIDs() { return List.of(); }

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
            event.register(REGISTRY_KEY, DragonSurvival.res("flight"), () -> FlightEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("spin"), () -> SpinEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("item_conversion"), () -> ItemConversionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("swim"), () -> SwimEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("effect_modification"), () -> EffectModificationEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("particle"), () -> ParticleEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("glow"), () -> GlowEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("oxygen_bonus"), () -> OxygenBonusEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("block_vision"), () -> BlockVisionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("run_function"), () -> RunFunctionEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("smelting"), () -> SmeltItemEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("heal"), () -> HealEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("teleport"), () -> TeleportEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("push"), () -> PushEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("hunger"), () -> HungerEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("effect_removal"), () -> MobEffectRemovalEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("use_item"), () -> UseItemOnLivingEntityEffect.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("dragon_growth"), () -> DragonGrowthEffect.CODEC);
        }
    }
}
