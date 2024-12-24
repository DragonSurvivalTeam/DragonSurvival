package by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.WeatherPredicate;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects.ProjectileWorldEffect;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface ProjectileTargeting {
    ResourceKey<Registry<MapCodec<? extends ProjectileTargeting>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_targeting"));
    Registry<MapCodec<? extends ProjectileTargeting>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<ProjectileTargeting> CODEC = REGISTRY.byNameCodec().dispatch(ProjectileTargeting::codec, Function.identity());

    record BlockTargeting(Optional<BlockPredicate> targetConditions, Optional<WeatherPredicate> weatherConditions, List<ProjectileBlockEffect> effects, int tickRate, double chance) {
        public static final Codec<ProjectileTargeting.BlockTargeting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPredicate.CODEC.optionalFieldOf("target_conditions").forGetter(ProjectileTargeting.BlockTargeting::targetConditions),
                WeatherPredicate.CODEC.optionalFieldOf("weather_conditions").forGetter(ProjectileTargeting.BlockTargeting::weatherConditions),
                ProjectileBlockEffect.CODEC.listOf().fieldOf("block_effects").forGetter(ProjectileTargeting.BlockTargeting::effects),
                Codec.INT.optionalFieldOf("tick_rate", 1).forGetter(ProjectileTargeting.BlockTargeting::tickRate),
                Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(ProjectileTargeting.BlockTargeting::chance)
        ).apply(instance, ProjectileTargeting.BlockTargeting::new));
    }

    record EntityTargeting(Optional<EntityPredicate> targetConditions, List<ProjectileEntityEffect> effects, int tickRate, double chance) {
        public static final Codec<ProjectileTargeting.EntityTargeting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.CODEC.optionalFieldOf("target_conditions").forGetter(ProjectileTargeting.EntityTargeting::targetConditions),
                ProjectileEntityEffect.CODEC.listOf().fieldOf("entity_effects").forGetter(ProjectileTargeting.EntityTargeting::effects),
                Codec.INT.optionalFieldOf("tick_rate", 1).forGetter(ProjectileTargeting.EntityTargeting::tickRate),
                Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(ProjectileTargeting.EntityTargeting::chance)
        ).apply(instance, ProjectileTargeting.EntityTargeting::new));
    }

    record WorldTargeting(Optional<LocationPredicate> locationConditions, Optional<WeatherPredicate> weatherConditions, List<ProjectileWorldEffect> effects, int tickRate, double chance) {
        public static final Codec<ProjectileTargeting.WorldTargeting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LocationPredicate.CODEC.optionalFieldOf("location_conditions").forGetter(ProjectileTargeting.WorldTargeting::locationConditions),
                WeatherPredicate.CODEC.optionalFieldOf("weather_conditions").forGetter(ProjectileTargeting.WorldTargeting::weatherConditions),
                ProjectileWorldEffect.CODEC.listOf().fieldOf("world_effects").forGetter(ProjectileTargeting.WorldTargeting::effects),
                Codec.INT.optionalFieldOf("tick_rate", 1).forGetter(ProjectileTargeting.WorldTargeting::tickRate),
                Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(ProjectileTargeting.WorldTargeting::chance)
        ).apply(instance, ProjectileTargeting.WorldTargeting::new));
    }

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("area"), () -> ProjectileAreaTarget.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("point"), () -> ProjectilePointTarget.CODEC);
        }
    }

    default int tickRate() {
        if (target().right().isPresent()) {
            return target().right().get().tickRate();
        } else if (target().left().isPresent()) {
            if(target().left().get().left().isPresent()) {
                return target().left().get().left().get().tickRate();
            } else if(target().left().get().right().isPresent()) {
                return target().left().get().right().get().tickRate();
            }
        }

        return 1;
    }

    default List<MutableComponent> getAllEffectDescriptions(final Player dragon, final int level) {
        List<MutableComponent> descriptions = new ArrayList<>();
        MutableComponent targetDescription = getDescription(dragon, level);
        if (target().right().isPresent()) {
            target().right().get().effects().forEach(effect -> {
                List<MutableComponent> abilityEffectDescriptions = effect.getDescription(dragon, level);
                if(!effect.getDescription(dragon, level).isEmpty()) {
                    descriptions.addAll(abilityEffectDescriptions.stream().map(abilityEffectDescription -> abilityEffectDescription.append(targetDescription)).toList());
                }
            });
        } else if (target().left().isPresent()) {
            if (target().left().get().left().isPresent()) {
                target().left().get().left().get().effects().forEach(effect -> {
                    List<MutableComponent> abilityEffectDescriptions = effect.getDescription(dragon, level);
                    if(!effect.getDescription(dragon, level).isEmpty()) {
                        descriptions.addAll(abilityEffectDescriptions.stream().map(abilityEffectDescription -> abilityEffectDescription.append(targetDescription)).toList());
                    }
                });
            } else if (target().left().get().right().isPresent()) {
                target().left().get().right().get().effects().forEach(effect -> {
                    List<MutableComponent> abilityEffectDescriptions = effect.getDescription(dragon, level);
                    if(!effect.getDescription(dragon, level).isEmpty()) {
                        descriptions.addAll(abilityEffectDescriptions.stream().map(abilityEffectDescription -> abilityEffectDescription.append(targetDescription)).toList());
                    }
                });
            }
        }

        return descriptions;
    }

    void apply(final Projectile projectile, int projectileLevel);
    MutableComponent getDescription(final Player dragon, final int level);
    Either<Either<ProjectileTargeting.BlockTargeting, ProjectileTargeting.EntityTargeting>, ProjectileTargeting.WorldTargeting> target();
    MapCodec<? extends ProjectileTargeting> codec();
}
