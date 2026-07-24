package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.particles.BeaconParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaSweepParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.TreasureParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.LargeFireParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.LargeLightningParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.LargePoisonParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.LargeSunParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.SmallFireParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.SmallLightningParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.SmallPoisonParticle;
import by.dragonsurvivalteam.dragonsurvival.client.particles.dragon.SmallSunParticle;
import by.dragonsurvivalteam.dragonsurvival.common.particles.CustomGlowParticle;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeFireParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargePoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.LargeSunParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SeaSweepParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallFireParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallLightningParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallPoisonParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SmallSunParticleOption;
import by.dragonsurvivalteam.dragonsurvival.common.particles.TreasureParticleOption;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@EventBusSubscriber(Dist.CLIENT)
public class DSParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, DragonSurvival.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SmallFireParticleOption>> FIRE = register("fire", () -> SmallFireParticleOption.CODEC, () -> SmallFireParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<LargeFireParticleOption>> LARGE_FIRE = register("large_fire", () -> LargeFireParticleOption.CODEC, () -> LargeFireParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SmallPoisonParticleOption>> POISON = register("poison", () -> SmallPoisonParticleOption.CODEC, () -> SmallPoisonParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<LargePoisonParticleOption>> LARGE_POISON = register("large_poison", () -> LargePoisonParticleOption.CODEC, () -> LargePoisonParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SmallSunParticleOption>> SUN = register("sun", () -> SmallSunParticleOption.CODEC, () -> SmallSunParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<LargeSunParticleOption>> LARGE_SUN = register("large_sun", () -> LargeSunParticleOption.CODEC, () -> LargeSunParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SmallLightningParticleOption>> LIGHTNING = register("lightning", () -> SmallLightningParticleOption.CODEC, () -> SmallLightningParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<LargeLightningParticleOption>> LARGE_LIGHTNING = register("large_lightning", () -> LargeLightningParticleOption.CODEC, () -> LargeLightningParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<TreasureParticleOption>> TREASURE = register("treasure", () -> TreasureParticleOption.CODEC, () -> TreasureParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SeaSweepParticleOption>> SEA_SWEEP = register("sea_sweep", () -> SeaSweepParticleOption.CODEC, () -> SeaSweepParticleOption.STREAM_CODEC);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CAVE_BEACON_PARTICLE = REGISTRY.register("netherite_particle", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SEA_BEACON_PARTICLE = REGISTRY.register("diamond_particle", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FOREST_BEACON_PARTICLE = REGISTRY.register("gold_particle", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GLOW = REGISTRY.register("glow", () -> new SimpleParticleType(false));

    private static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> register(final String name, final Supplier<MapCodec<T>> codecSupplier, final Supplier<StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodecSupplier) {
        return REGISTRY.register(name, () -> new ParticleType<>(false) {
            @Override
            public @NotNull MapCodec<T> codec() {
                return codecSupplier.get();
            }

            @Override
            public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodecSupplier.get();
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(DSParticles.FIRE.get(), SmallFireParticle.Factory::new);
        event.registerSpriteSet(DSParticles.LARGE_FIRE.get(), LargeFireParticle.Factory::new);
        event.registerSpriteSet(DSParticles.POISON.get(), SmallPoisonParticle.Factory::new);
        event.registerSpriteSet(DSParticles.LARGE_POISON.get(), LargePoisonParticle.Factory::new);
        event.registerSpriteSet(DSParticles.SUN.get(), SmallSunParticle.Factory::new);
        event.registerSpriteSet(DSParticles.LARGE_SUN.get(), LargeSunParticle.Factory::new);
        event.registerSpriteSet(DSParticles.LIGHTNING.get(), SmallLightningParticle.Factory::new);
        event.registerSpriteSet(DSParticles.LARGE_LIGHTNING.get(), LargeLightningParticle.Factory::new);
        event.registerSpriteSet(DSParticles.TREASURE.get(), TreasureParticle.Factory::new);
        event.registerSpriteSet(DSParticles.SEA_SWEEP.get(), SeaSweepParticle.Factory::new);
        event.registerSpriteSet(DSParticles.CAVE_BEACON_PARTICLE.get(), BeaconParticle.FireFactory::new);
        event.registerSpriteSet(DSParticles.SEA_BEACON_PARTICLE.get(), BeaconParticle.MagicFactory::new);
        event.registerSpriteSet(DSParticles.FOREST_BEACON_PARTICLE.get(), BeaconParticle.PeaceFactory::new);
        event.registerSpriteSet(DSParticles.GLOW.get(), CustomGlowParticle.Provider::new);
    }
}