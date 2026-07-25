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
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@EventBusSubscriber(modid = DragonSurvival.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class DSParticles {
    public static final DeferredRegister<ParticleType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DragonSurvival.MODID);

    public static final RegistryObject<ParticleType<SmallFireParticleOption>> FIRE = register("fire", () -> SmallFireParticleOption.CODEC, SmallFireParticleOption.DESERIALIZER);
    public static final RegistryObject<ParticleType<LargeFireParticleOption>> LARGE_FIRE = register("large_fire", () -> LargeFireParticleOption.CODEC, LargeFireParticleOption.DESERIALIZER);
    public static final RegistryObject<ParticleType<SmallPoisonParticleOption>> POISON = register("poison", () -> SmallPoisonParticleOption.CODEC, SmallPoisonParticleOption.DESERIALIZER);
    public static final RegistryObject<ParticleType<LargePoisonParticleOption>> LARGE_POISON = register("large_poison", () -> LargePoisonParticleOption.CODEC, LargePoisonParticleOption.DESERIALIZER);
    public static final RegistryObject<ParticleType<SmallSunParticleOption>> SUN = register("sun", () -> SmallSunParticleOption.CODEC, SmallSunParticleOption.DESERIALIZER);
    public static final RegistryObject<ParticleType<LargeSunParticleOption>> LARGE_SUN = register("large_sun", () -> LargeSunParticleOption.CODEC, LargeSunParticleOption.DESERIALIZER);
    public static final RegistryObject<ParticleType<SmallLightningParticleOption>> LIGHTNING = register("lightning", () -> SmallLightningParticleOption.CODEC, SmallLightningParticleOption.DESERIALIZER);
    public static final RegistryObject<ParticleType<LargeLightningParticleOption>> LARGE_LIGHTNING = register("large_lightning", () -> LargeLightningParticleOption.CODEC, LargeLightningParticleOption.DESERIALIZER);
    public static final RegistryObject<ParticleType<TreasureParticleOption>> TREASURE = register("treasure", () -> TreasureParticleOption.CODEC, TreasureParticleOption.DESERIALIZER);
    public static final RegistryObject<ParticleType<SeaSweepParticleOption>> SEA_SWEEP = register("sea_sweep", () -> SeaSweepParticleOption.CODEC, SeaSweepParticleOption.DESERIALIZER);

    public static final RegistryObject<SimpleParticleType> CAVE_BEACON_PARTICLE = REGISTRY.register("netherite_particle", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> SEA_BEACON_PARTICLE = REGISTRY.register("diamond_particle", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> FOREST_BEACON_PARTICLE = REGISTRY.register("gold_particle", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> GLOW = REGISTRY.register("glow", () -> new SimpleParticleType(false));

    private static <T extends ParticleOptions> RegistryObject<ParticleType<T>> register(
            final String name,
            final Supplier<Codec<T>> codecSupplier,
            final ParticleOptions.Deserializer<T> deserializer
    ) {
        return REGISTRY.register(name, () -> new ParticleType<>(false, deserializer) {
            @Override
            public @NotNull Codec<T> codec() {
                return codecSupplier.get();
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
