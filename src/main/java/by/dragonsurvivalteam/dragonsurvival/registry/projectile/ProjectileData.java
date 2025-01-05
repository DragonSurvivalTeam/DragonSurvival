package by.dragonsurvivalteam.dragonsurvival.registry.projectile;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileTargeting;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record ProjectileData(
        ResourceLocation name,
        Either<GenericBallData, GenericArrowData> specificProjectileData,
        Optional<EntityPredicate> canHitPredicate,
        List<ProjectileTargeting> tickingEffects,
        List<ProjectileTargeting> commonHitEffects,
        List<ProjectileEntityEffect> entityHitEffects,
        List<ProjectileBlockEffect> blockHitEffects) {

    public record GenericArrowData(LevelBasedResource texture, LevelBasedValue piercingLevel) {
        public static final Codec<GenericArrowData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LevelBasedResource.CODEC.fieldOf("texture").forGetter(GenericArrowData::texture),
                LevelBasedValue.CODEC.optionalFieldOf("piercing_level", LevelBasedValue.constant(0)).forGetter(GenericArrowData::piercingLevel)
        ).apply(instance, GenericArrowData::new));
    }

    public record GenericBallResource(LevelBasedResource resource) {
        public static final Codec<GenericBallResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LevelBasedResource.CODEC.fieldOf("resource").forGetter(GenericBallResource::resource)
        ).apply(instance, GenericBallResource::new));
    }

    public record GenericBallData(
            GenericBallResource ballResources,
            Optional<ParticleOptions> trailParticle,
            // Needed since there is a difference between being hit and destroyed (e.g if we linger)
            List<ProjectileTargeting> onDestroyEffects,
            LevelBasedValue xSize,
            LevelBasedValue ySize,
            LevelBasedValue maxBounces,
            LevelBasedValue maxLingeringTicks,
            LevelBasedValue maxMoveDistance,
            LevelBasedValue maxLifespan) {
        public static final Codec<GenericBallData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GenericBallResource.CODEC.fieldOf("ball_resources").forGetter(GenericBallData::ballResources),
                ParticleTypes.CODEC.optionalFieldOf("trail_particle").forGetter(GenericBallData::trailParticle),
                ProjectileTargeting.CODEC.listOf().optionalFieldOf("on_destroy_effects", List.of()).forGetter(GenericBallData::onDestroyEffects),
                LevelBasedValue.CODEC.fieldOf("x_size").forGetter(GenericBallData::xSize),
                LevelBasedValue.CODEC.fieldOf("y_size").forGetter(GenericBallData::ySize),
                LevelBasedValue.CODEC.optionalFieldOf("max_bounces", LevelBasedValue.constant(0)).forGetter(GenericBallData::maxBounces),
                LevelBasedValue.CODEC.optionalFieldOf("max_lingering_ticks", LevelBasedValue.constant(0)).forGetter(GenericBallData::maxLingeringTicks),
                LevelBasedValue.CODEC.fieldOf("max_move_distance").forGetter(GenericBallData::maxMoveDistance),
                LevelBasedValue.CODEC.fieldOf("max_lifespan").forGetter(GenericBallData::maxLifespan)
        ).apply(instance, GenericBallData::new));
    }

    public static final ResourceKey<Registry<ProjectileData>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("projectile_data"));

    public static final Codec<ProjectileData> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("name").forGetter(ProjectileData::name),
            Codec.either(GenericBallData.CODEC, GenericArrowData.CODEC).fieldOf("specific_projectile_data").forGetter(ProjectileData::specificProjectileData),
            EntityPredicate.CODEC.optionalFieldOf("can_hit_predicate").forGetter(ProjectileData::canHitPredicate),
            ProjectileTargeting.CODEC.listOf().fieldOf("ticking_effects").forGetter(ProjectileData::tickingEffects),
            ProjectileTargeting.CODEC.listOf().fieldOf("common_hit_effects").forGetter(ProjectileData::commonHitEffects),
            ProjectileEntityEffect.CODEC.listOf().fieldOf("entity_hit_effects").forGetter(ProjectileData::entityHitEffects),
            ProjectileBlockEffect.CODEC.listOf().fieldOf("block_hit_effects").forGetter(ProjectileData::blockHitEffects)
    ).apply(instance, ProjectileData::new));

    public static final Codec<Holder<ProjectileData>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ProjectileData>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

}
