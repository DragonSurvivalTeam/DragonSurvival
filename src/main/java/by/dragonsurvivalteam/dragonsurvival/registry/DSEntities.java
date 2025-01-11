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
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DSEntities {
    public static final DeferredRegister<EntityType<?>> DS_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, DragonSurvival.MODID);

    // --- Player --- //

    @Translation(type = Translation.Type.ENTITY, comments = "Dragon")
    public static DeferredHolder<EntityType<?>, EntityType<DragonEntity>> DRAGON = DS_ENTITY_TYPES.register(
            "dummy_dragon",
            () -> new EntityType<>(DragonEntity::new, MobCategory.MISC, true, false, false, false, ImmutableSet.of(), EntityDimensions.fixed(0.9f, 1.9f), 1.0f, 0, 0, FeatureFlagSet.of(FeatureFlags.VANILLA)));

    // --- Fake entities --- //

    @Translation(type = Translation.Type.ENTITY, comments = "Bolas")
    public static DeferredHolder<EntityType<?>, EntityType<Bolas>> BOLAS_ENTITY = DS_ENTITY_TYPES.register(
            "bolas",
            () -> EntityType.Builder.<Bolas>of((entity, level) ->
                            new Bolas(level), MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("bolas"));

    @Translation(type = Translation.Type.ENTITY, comments = "Generic Ball Entity")
    public static DeferredHolder<EntityType<?>, EntityType<GenericBallEntity>> GENERIC_BALL_ENTITY = DS_ENTITY_TYPES.register(
            "generic_ball_entity",
            () -> EntityType.Builder.<GenericBallEntity>of(GenericBallEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("generic_ball_entity"));

    @Translation(type = Translation.Type.ENTITY, comments = "Generic Arrow Entity")
    public static DeferredHolder<EntityType<?>, EntityType<GenericArrowEntity>> GENERIC_ARROW_ENTITY = DS_ENTITY_TYPES.register(
            "generic_arrow_entity",
            () -> EntityType.Builder.<GenericArrowEntity>of(GenericArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("generic_arrow_entity"));

    // --- Entities --- //

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Hound")
    public static DeferredHolder<EntityType<?>, EntityType<HoundEntity>> HUNTER_HOUND = DS_ENTITY_TYPES.register(
            "hunter_hound",
            () -> EntityType.Builder.of(HoundEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_hound"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Griffin")
    public static DeferredHolder<EntityType<?>, EntityType<GriffinEntity>> HUNTER_GRIFFIN = DS_ENTITY_TYPES.register(
            "hunter_griffin",
            () -> EntityType.Builder.of(GriffinEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_griffin"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Spearman")
    public static DeferredHolder<EntityType<?>, EntityType<SpearmanEntity>> HUNTER_SPEARMAN = DS_ENTITY_TYPES.register(
            "hunter_spearman",
            () -> EntityType.Builder.of(SpearmanEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_spearman"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Knight")
    public static DeferredHolder<EntityType<?>, EntityType<KnightEntity>> HUNTER_KNIGHT = DS_ENTITY_TYPES.register(
            "hunter_knight", () -> EntityType.Builder.of(KnightEntity::new, MobCategory.MONSTER)
                    .sized(1.5f, 3f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_knight"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Ambusher")
    public static DeferredHolder<EntityType<?>, EntityType<AmbusherEntity>> HUNTER_AMBUSHER = DS_ENTITY_TYPES.register(
            "hunter_ambusher", () -> EntityType.Builder.of(AmbusherEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_ambusher"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Leader")
    public static DeferredHolder<EntityType<?>, EntityType<LeaderEntity>> HUNTER_LEADER = DS_ENTITY_TYPES.register(
            "hunter_leader",
            () -> EntityType.Builder.of(LeaderEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_leader"));

    @SubscribeEvent
    public static void attributeCreationEvent(final EntityAttributeCreationEvent event) {
        event.put(DRAGON.value(), DragonEntity.createLivingAttributes().build());
        event.put(HUNTER_HOUND.value(), Wolf.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.houndSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.houndDamage).add(Attributes.MAX_HEALTH, ServerConfig.houndHealth).build());
        event.put(HUNTER_SPEARMAN.value(), Vindicator.createAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.spearmanSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.spearmanDamage).add(Attributes.ARMOR, ServerConfig.spearmanArmor).add(Attributes.MAX_HEALTH, ServerConfig.spearmanHealth).build());
        event.put(HUNTER_KNIGHT.value(), KnightEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.knightSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.knightDamage).add(Attributes.ARMOR, ServerConfig.knightArmor).add(Attributes.MAX_HEALTH, ServerConfig.knightHealth).build());
        event.put(HUNTER_AMBUSHER.value(), AmbusherEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.ambusherSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.ambusherDamage).add(Attributes.ARMOR, ServerConfig.ambusherArmor).add(Attributes.MAX_HEALTH, ServerConfig.ambusherHealth).build());
        event.put(HUNTER_GRIFFIN.value(), GriffinEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.griffinSpeed).add(Attributes.FLYING_SPEED, ServerConfig.griffinSpeed).add(Attributes.ATTACK_DAMAGE, ServerConfig.griffinDamage).add(Attributes.MAX_HEALTH, ServerConfig.griffinHealth).build());
        event.put(HUNTER_LEADER.value(), LeaderEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, ServerConfig.leaderSpeed).add(Attributes.MAX_HEALTH, ServerConfig.leaderHealth).build());
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