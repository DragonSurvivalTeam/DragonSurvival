package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.AmbusherEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.GriffinEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.HoundEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.KnightEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.LeaderEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.SpearmanEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericArrowEntity;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.GenericBallEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public class DSEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, DragonSurvival.MODID);

    // --- Player --- //

    @Translation(type = Translation.Type.ENTITY, comments = "Dragon")
    public static DeferredHolder<EntityType<?>, EntityType<DragonEntity>> DRAGON = REGISTRY.register(
            "dummy_dragon",
            () -> new EntityType<>(DragonEntity::new, MobCategory.MISC, true, false, false, false, ImmutableSet.of(), EntityDimensions.fixed(0.9f, 1.9f), 1.0f, 0, 0, FeatureFlagSet.of(FeatureFlags.VANILLA)));

    // --- Fake entities --- //

    // Properties copied from the ARROW entity that Minecraft uses
    @Translation(type = Translation.Type.ENTITY, comments = "Bolas")
    public static DeferredHolder<EntityType<?>, EntityType<Bolas>> BOLAS_ENTITY = REGISTRY.register(
            "bolas",
            () -> EntityType.Builder.<Bolas>of((entity, level) ->
                            new Bolas(level), MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .eyeHeight(0.13F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("bolas"));

    @Translation(type = Translation.Type.ENTITY, comments = "Generic Ball Entity")
    public static DeferredHolder<EntityType<?>, EntityType<GenericBallEntity>> GENERIC_BALL_ENTITY = REGISTRY.register(
            "generic_ball_entity",
            () -> EntityType.Builder.<GenericBallEntity>of(GenericBallEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("generic_ball_entity"));

    @Translation(type = Translation.Type.ENTITY, comments = "Generic Arrow Entity")
    public static DeferredHolder<EntityType<?>, EntityType<GenericArrowEntity>> GENERIC_ARROW_ENTITY = REGISTRY.register(
            "generic_arrow_entity",
            () -> EntityType.Builder.<GenericArrowEntity>of(GenericArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("generic_arrow_entity"));

    // --- Entities --- //

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Hound")
    public static DeferredHolder<EntityType<?>, EntityType<HoundEntity>> HUNTER_HOUND = REGISTRY.register(
            "hunter_hound",
            () -> EntityType.Builder.of(HoundEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_hound"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Griffin")
    public static DeferredHolder<EntityType<?>, EntityType<GriffinEntity>> HUNTER_GRIFFIN = REGISTRY.register(
            "hunter_griffin",
            () -> EntityType.Builder.of(GriffinEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_griffin"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Spearman")
    public static DeferredHolder<EntityType<?>, EntityType<SpearmanEntity>> HUNTER_SPEARMAN = REGISTRY.register(
            "hunter_spearman",
            () -> EntityType.Builder.of(SpearmanEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_spearman"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Knight")
    public static DeferredHolder<EntityType<?>, EntityType<KnightEntity>> HUNTER_KNIGHT = REGISTRY.register(
            "hunter_knight", () -> EntityType.Builder.of(KnightEntity::new, MobCategory.MONSTER)
                    .sized(1.5f, 3f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_knight"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Ambusher")
    public static DeferredHolder<EntityType<?>, EntityType<AmbusherEntity>> HUNTER_AMBUSHER = REGISTRY.register(
            "hunter_ambusher", () -> EntityType.Builder.of(AmbusherEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_ambusher"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Leader")
    public static DeferredHolder<EntityType<?>, EntityType<LeaderEntity>> HUNTER_LEADER = REGISTRY.register(
            "hunter_leader",
            () -> EntityType.Builder.of(LeaderEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_leader"));

    @SubscribeEvent
    public static void attributeCreationEvent(final EntityAttributeCreationEvent event) {
        event.put(DRAGON.value(), LivingEntity.createLivingAttributes().build());

        // There is no reason to set values here since it will always be the default config values
        // We set the correct values on entity creation through 'finalizeSpawn' (this also means no restart is required)
        event.put(HUNTER_HOUND.value(), hunterAttributes().build());
        event.put(HUNTER_SPEARMAN.value(), hunterAttributes().build());
        event.put(HUNTER_KNIGHT.value(), hunterAttributes().build());
        event.put(HUNTER_AMBUSHER.value(), hunterAttributes().build());
        event.put(HUNTER_GRIFFIN.value(), hunterAttributes().add(Attributes.FLYING_SPEED).build());
        event.put(HUNTER_LEADER.value(), hunterAttributes().build());
    }

    private static AttributeSupplier.Builder hunterAttributes() {
        return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes", "deprecation"}) // ignore
    public static void registerSpawn(final RegisterSpawnPlacementsEvent event) {
        SpawnPlacements.SpawnPredicate predicate = (entity, level, spawnType, position, random) -> level.canSeeSky(position) && level.getBlockState(position.below()).isSolid();

        event.register(HUNTER_SPEARMAN.value(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HUNTER_KNIGHT.value(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HUNTER_AMBUSHER.value(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HUNTER_HOUND.value(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(HUNTER_GRIFFIN.value(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
}