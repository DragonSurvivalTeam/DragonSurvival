package by.dragonsurvivalteam.dragonsurvival.registry.projectile;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileTargeting;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record ProjectileData(GeneralData generalData, Either<GenericBallData, GenericArrowData> typeData) {
    public static final ResourceKey<Registry<ProjectileData>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_data"));

    public static final Codec<ProjectileData> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GeneralData.CODEC.fieldOf("general_data").forGetter(ProjectileData::generalData),
            Codec.either(GenericBallData.CODEC, GenericArrowData.CODEC).fieldOf("type_data").forGetter(ProjectileData::typeData)
    ).apply(instance, ProjectileData::new));

    public static final Codec<Holder<ProjectileData>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ProjectileData>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

    // TODO :: add homing field
    //  and when doing so maybe store a count on the targets for how many projectiles already target it
    //  projectiles then may target entities with lower values
    public record GeneralData(
            ResourceLocation name,
            Optional<LootItemCondition> entityHitCondition,
            List<ProjectileTargeting> tickingEffects,
            List<ProjectileTargeting> commonHitEffects,
            List<ProjectileEntityEffect> entityHitEffects,
            List<ProjectileBlockEffect> blockHitEffects
    ) {
        public static final Codec<GeneralData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("name").forGetter(GeneralData::name),
                LootItemCondition.DIRECT_CODEC.optionalFieldOf("entity_hit_condition").forGetter(GeneralData::entityHitCondition),
                ProjectileTargeting.CODEC.listOf().fieldOf("ticking_effects").forGetter(GeneralData::tickingEffects),
                ProjectileTargeting.CODEC.listOf().fieldOf("common_hit_effects").forGetter(GeneralData::commonHitEffects),
                ProjectileEntityEffect.CODEC.listOf().fieldOf("entity_hit_effects").forGetter(GeneralData::entityHitEffects),
                ProjectileBlockEffect.CODEC.listOf().fieldOf("block_hit_effects").forGetter(GeneralData::blockHitEffects)
        ).apply(instance, GeneralData::new));
    }

    public record GenericArrowData(LevelBasedResource texture, LevelBasedValue piercingLevel) {
        public static final Codec<GenericArrowData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LevelBasedResource.CODEC.fieldOf("texture").forGetter(GenericArrowData::texture),
                LevelBasedValue.CODEC.optionalFieldOf("piercing_level", LevelBasedValue.constant(0)).forGetter(GenericArrowData::piercingLevel)
        ).apply(instance, GenericArrowData::new));
    }

    public record GenericBallData(LevelBasedResource resources, Optional<ParticleOptions> trailParticle, List<ProjectileTargeting> onDestroyEffects, BehaviourData behaviourData) {
        public static final Codec<GenericBallData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LevelBasedResource.CODEC.fieldOf("resources").forGetter(GenericBallData::resources),
                ParticleTypes.CODEC.optionalFieldOf("trail_particle").forGetter(GenericBallData::trailParticle),
                ProjectileTargeting.CODEC.listOf().optionalFieldOf("on_destroy_effects", List.of()).forGetter(GenericBallData::onDestroyEffects),
                BehaviourData.CODEC.fieldOf("behaviour_data").forGetter(GenericBallData::behaviourData)
        ).apply(instance, GenericBallData::new));
    }

    public record BehaviourData(
            LevelBasedValue width,
            LevelBasedValue height,
            LevelBasedValue maxBounces,
            LevelBasedValue maxLingeringTicks,
            LevelBasedValue maxMovementDistance,
            LevelBasedValue maxLifespan
    ) {
        public static final Codec<BehaviourData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LevelBasedValue.CODEC.fieldOf("width").forGetter(BehaviourData::width),
                LevelBasedValue.CODEC.fieldOf("height").forGetter(BehaviourData::height),
                LevelBasedValue.CODEC.optionalFieldOf("max_bounces", LevelBasedValue.constant(0)).forGetter(BehaviourData::maxBounces),
                LevelBasedValue.CODEC.optionalFieldOf("max_lingering_ticks", LevelBasedValue.constant(0)).forGetter(BehaviourData::maxLingeringTicks),
                LevelBasedValue.CODEC.fieldOf("max_movement_distance").forGetter(BehaviourData::maxMovementDistance),
                LevelBasedValue.CODEC.fieldOf("max_lifespan").forGetter(BehaviourData::maxLifespan)
        ).apply(instance, BehaviourData::new));
    }
}
