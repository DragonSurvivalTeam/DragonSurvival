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
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(modid = DragonSurvival.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DSEntities {
    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, DragonSurvival.MODID);

    // --- Player --- //

    @Translation(type = Translation.Type.ENTITY, comments = "Dragon")
    public static RegistryObject<EntityType<DragonEntity>> DRAGON = REGISTRY.register(
            "dummy_dragon",
            () -> EntityType.Builder.of(DragonEntity::new, MobCategory.MISC)
                    .noSummon()
                    .sized(0.9F, 1.9F)
                    .clientTrackingRange(0)
                    .updateInterval(0)
                    .build("dummy_dragon"));

    // --- Fake entities --- //

    // Properties copied from the ARROW entity that Minecraft uses
    @Translation(type = Translation.Type.ENTITY, comments = "Bolas")
    public static RegistryObject<EntityType<Bolas>> BOLAS_ENTITY = REGISTRY.register(
            "bolas",
            () -> EntityType.Builder.<Bolas>of((entity, level) ->
                            new Bolas(level), MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("bolas"));

    @Translation(type = Translation.Type.ENTITY, comments = "Generic Ball Entity")
    public static RegistryObject<EntityType<GenericBallEntity>> GENERIC_BALL_ENTITY = REGISTRY.register(
            "generic_ball_entity",
            () -> EntityType.Builder.<GenericBallEntity>of(GenericBallEntity::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("generic_ball_entity"));

    @Translation(type = Translation.Type.ENTITY, comments = "Generic Arrow Entity")
    public static RegistryObject<EntityType<GenericArrowEntity>> GENERIC_ARROW_ENTITY = REGISTRY.register(
            "generic_arrow_entity",
            () -> EntityType.Builder.<GenericArrowEntity>of(GenericArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build("generic_arrow_entity"));

    // --- Entities --- //

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Hound")
    public static RegistryObject<EntityType<HoundEntity>> HUNTER_HOUND = REGISTRY.register(
            "hunter_hound",
            () -> EntityType.Builder.of(HoundEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_hound"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Griffin")
    public static RegistryObject<EntityType<GriffinEntity>> HUNTER_GRIFFIN = REGISTRY.register(
            "hunter_griffin",
            () -> EntityType.Builder.of(GriffinEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 0.85F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_griffin"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Spearman")
    public static RegistryObject<EntityType<SpearmanEntity>> HUNTER_SPEARMAN = REGISTRY.register(
            "hunter_spearman",
            () -> EntityType.Builder.of(SpearmanEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_spearman"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Knight")
    public static RegistryObject<EntityType<KnightEntity>> HUNTER_KNIGHT = REGISTRY.register(
            "hunter_knight", () -> EntityType.Builder.of(KnightEntity::new, MobCategory.MONSTER)
                    .sized(1.5f, 3f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_knight"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Ambusher")
    public static RegistryObject<EntityType<AmbusherEntity>> HUNTER_AMBUSHER = REGISTRY.register(
            "hunter_ambusher", () -> EntityType.Builder.of(AmbusherEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_ambusher"));

    @Translation(type = Translation.Type.ENTITY, comments = "Hunter Leader")
    public static RegistryObject<EntityType<LeaderEntity>> HUNTER_LEADER = REGISTRY.register(
            "hunter_leader",
            () -> EntityType.Builder.of(LeaderEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("hunter_leader"));

    @SubscribeEvent
    public static void attributeCreationEvent(final EntityAttributeCreationEvent event) {
        event.put(DRAGON.get(), LivingEntity.createLivingAttributes().build());

        // There is no reason to set values here since it will always be the default config values
        // We set the correct values on entity creation through 'finalizeSpawn' (this also means no restart is required)
        event.put(HUNTER_HOUND.get(), hunterAttributes().build());
        event.put(HUNTER_SPEARMAN.get(), hunterAttributes().build());
        event.put(HUNTER_KNIGHT.get(), hunterAttributes().build());
        event.put(HUNTER_AMBUSHER.get(), hunterAttributes().build());
        event.put(HUNTER_GRIFFIN.get(), hunterAttributes().add(Attributes.FLYING_SPEED).build());
        event.put(HUNTER_LEADER.get(), hunterAttributes().build());
    }

    private static AttributeSupplier.Builder hunterAttributes() {
        return Mob.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes", "deprecation"}) // ignore
    public static void registerSpawn(final SpawnPlacementRegisterEvent event) {
        SpawnPlacements.SpawnPredicate predicate = (entity, level, spawnType, position, random) -> level.canSeeSky(position) && level.getBlockState(position.below()).isSolid();

        event.register(HUNTER_SPEARMAN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(HUNTER_KNIGHT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(HUNTER_AMBUSHER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(HUNTER_HOUND.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
        event.register(HUNTER_GRIFFIN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, predicate, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
