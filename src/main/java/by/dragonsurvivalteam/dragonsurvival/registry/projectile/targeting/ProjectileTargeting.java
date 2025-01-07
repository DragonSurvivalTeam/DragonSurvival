package by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileEffect;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
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

    record GeneralData(List<ConditionalEffect> effects, int tickRate, double chance) {
        public static final Codec<GeneralData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ConditionalEffect.CODEC.listOf().fieldOf("effects").forGetter(GeneralData::effects),
                Codec.INT.optionalFieldOf("tick_rate", 1).forGetter(GeneralData::tickRate),
                Codec.DOUBLE.optionalFieldOf("chance", 1.0).forGetter(GeneralData::chance)
        ).apply(instance, GeneralData::new));
    }

    record ConditionalEffect(ProjectileEffect<?> effect, Optional<LootItemCondition> condition) {
        public static final Codec<ConditionalEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ProjectileEffect.GENERIC_CODEC.fieldOf("effect").forGetter(ConditionalEffect::effect),
                LootItemCondition.DIRECT_CODEC.optionalFieldOf("condition").forGetter(ConditionalEffect::condition)
        ).apply(instance, ConditionalEffect::new));

        public boolean apply(final ServerLevel serverLevel, final Projectile projectile, final Object target, final int level) {
            if (this.condition.map(condition -> condition.test(getContext(serverLevel, projectile, target))).orElse(true)) {
                return effect.applyGeneric(projectile, target, level);
            }

            return false;
        }

        private static LootContext getContext(final ServerLevel serverLevel, final Projectile projectile, final Object target) {
            LootContext context;

            if (target instanceof BlockPos position) {
                context = ProjectileEffect.positionContext(serverLevel, projectile, position.getCenter());
            } else if (target instanceof Entity entity) {
                context = ProjectileEffect.entityContext(serverLevel, projectile, entity);
            } else {
                context = ProjectileEffect.positionContext(serverLevel, projectile, projectile.position());
            }

            return context;
        }
    }

    static <T extends ProjectileTargeting> Products.P1<RecordCodecBuilder.Mu<T>, GeneralData> codecStart(final RecordCodecBuilder.Instance<T> instance) {
        return instance.group(GeneralData.CODEC.fieldOf("general_data").forGetter(T::generalData));
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

    default List<MutableComponent> getAllEffectDescriptions(final Player dragon, final int level) {
        List<MutableComponent> descriptions = new ArrayList<>();
        MutableComponent targetDescription = getDescription(dragon, level);
        
        for (ConditionalEffect conditional : generalData().effects()) {
            List<MutableComponent> effectDescription = conditional.effect().getDescription(dragon, level);
            
            if (!effectDescription.isEmpty()) {
                descriptions.addAll(effectDescription.stream().map(abilityEffectDescription -> abilityEffectDescription.append(targetDescription)).toList());
            }
        }

        return descriptions;
    }

    void apply(final Projectile projectile, int projectileLevel);
    MutableComponent getDescription(final Player dragon, final int level);
    MapCodec<? extends ProjectileTargeting> codec();

    GeneralData generalData();
}
