package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileTargeting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
public class GenericBallEntity extends AbstractHurtingProjectile implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final EntityDataAccessor<String> NAME = SynchedEntityData.defineId(GenericBallEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> TEXTURE_LOCATION = SynchedEntityData.defineId(GenericBallEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> ANIM_LOCATION = SynchedEntityData.defineId(GenericBallEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> GEO_LOCATION = SynchedEntityData.defineId(GenericBallEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> LINGERING = SynchedEntityData.defineId(GenericBallEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Float> DIMENSION_WIDTH = SynchedEntityData.defineId(GenericBallEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DIMENSION_HEIGHT = SynchedEntityData.defineId(GenericBallEntity.class, EntityDataSerializers.FLOAT);

    private ProjectileData.GenericBallResource resources;
    private Optional<EntityPredicate> canHitPredicate;
    private int projectileLevel; // TODO :: name this ability level to make it clear what this represents?
    private List<ProjectileTargeting> tickingEffects;
    private List<ProjectileTargeting> commonHitEffects;
    private List<ProjectileEntityEffect> entityHitEffects;
    private List<ProjectileBlockEffect> blockHitEffects;
    private List<ProjectileTargeting> onDestroyEffects;
    private int maxLingeringTicks;
    private int maxMoveDistance;
    private int maxLifespan;
    private Optional<ParticleOptions> trailParticle;

    protected int lingerTicks;
    private float moveDistance;
    private int lifespan;

    public GenericBallEntity(
            ResourceLocation name,
            ProjectileData.GenericBallResource location,
            Optional<ParticleOptions> trailParticle,
            Level level,
            EntityDimensions dimensions,
            Optional<EntityPredicate> canHitPredicate,
            List<ProjectileTargeting> tickingEffects,
            List<ProjectileTargeting> commonHitEffects,
            List<ProjectileEntityEffect> entityHitEffects,
            List<ProjectileBlockEffect> blockHitEffects,
            List<ProjectileTargeting> onDestroyEffects,
            int projectileLevel,
            int maxLingeringTicks,
            int maxMoveDistance,
            int maxLifespan) {
        super(DSEntities.GENERIC_BALL_ENTITY.get(), level);
        setName(name);
        setResourceLocations(location);
        setDimensionWidth(dimensions.width());
        setDimensionHeight(dimensions.height());
        this.canHitPredicate = canHitPredicate;
        this.trailParticle = trailParticle;
        this.tickingEffects = tickingEffects;
        this.commonHitEffects = commonHitEffects;
        this.entityHitEffects = entityHitEffects;
        this.blockHitEffects = blockHitEffects;
        this.onDestroyEffects = onDestroyEffects;
        this.projectileLevel = projectileLevel;
        this.maxLingeringTicks = maxLingeringTicks;
        this.lingerTicks = maxLingeringTicks;
        this.maxMoveDistance = maxMoveDistance;
        this.maxLifespan = maxLifespan;
        this.lifespan = 0;
        this.moveDistance = 0;
        // TODO: Currently unused, but we could use acceleration in the future. Would just need to sync it.
        this.accelerationPower = 0;
    }

    public GenericBallEntity(
            ResourceLocation name,
            ProjectileData.GenericBallResource location,
            Optional<ParticleOptions> trailParticle,
            Level level,
            EntityDimensions dimensions,
            Optional<EntityPredicate> canHitPredicate,
            List<ProjectileTargeting> tickingEffects,
            List<ProjectileTargeting> commonHitEffects,
            List<ProjectileEntityEffect> entityHitEffects,
            List<ProjectileBlockEffect> blockHitEffects,
            List<ProjectileTargeting> onDestroyEffects,
            int projectileLevel,
            int maxLingeringTicks,
            int maxMoveDistance,
            int maxLifespan,
            Vec3 position) {
        this(name, location, trailParticle, level, dimensions, canHitPredicate, tickingEffects, commonHitEffects, entityHitEffects, blockHitEffects, onDestroyEffects, projectileLevel, maxLingeringTicks, maxMoveDistance, maxLifespan);
        this.setPos(position.x, position.y, position.z);
        this.reapplyPosition();
    }

    public void setFromData(
            ResourceLocation name,
            ProjectileData.GenericBallResource location,
            Optional<ParticleOptions> trailParticle,
            EntityDimensions dimensions,
            Optional<EntityPredicate> canHitPredicate,
            List<ProjectileTargeting> tickingEffects,
            List<ProjectileTargeting> commonHitEffects,
            List<ProjectileEntityEffect> entityHitEffects,
            List<ProjectileBlockEffect> blockHitEffects,
            List<ProjectileTargeting> onDestroyEffects,
            int projectileLevel,
            int maxLingeringTicks,
            int maxMoveDistance,
            int maxLifespan,
            int lifespan,
            float moveDistance,
            int lingerTicks) {
        setName(name);
        setResourceLocations(location);
        setDimensionWidth(dimensions.width());
        setDimensionHeight(dimensions.height());
        this.canHitPredicate = canHitPredicate;
        this.trailParticle = trailParticle;
        this.tickingEffects = tickingEffects;
        this.commonHitEffects = commonHitEffects;
        this.entityHitEffects = entityHitEffects;
        this.blockHitEffects = blockHitEffects;
        this.onDestroyEffects = onDestroyEffects;
        this.projectileLevel = projectileLevel;
        this.maxLingeringTicks = maxLingeringTicks;
        this.maxMoveDistance = maxMoveDistance;
        this.maxLifespan = maxLifespan;
        this.lifespan = lifespan;
        this.moveDistance = moveDistance;
        this.lingerTicks = lingerTicks;
        // TODO: Currently unused, but we could use acceleration in the future. Would just need to sync it.
        this.accelerationPower = 0;
    }

    public GenericBallEntity(final EntityType<GenericBallEntity> type, final Level level) {
        super(type, level);
        this.canHitPredicate = Optional.empty();
        this.trailParticle = Optional.empty();
        this.tickingEffects = List.of();
        this.commonHitEffects = List.of();
        this.entityHitEffects = List.of();
        this.blockHitEffects = List.of();
        this.onDestroyEffects = List.of();
        this.projectileLevel = 0;
        this.maxLingeringTicks = 0;
        this.lingerTicks = 0;
        this.maxMoveDistance = 0;
        this.maxLifespan = 0;
        this.moveDistance = 0;
        this.lifespan = 0;
        // TODO: Currently unused, but we could use acceleration in the future. Would just need to sync it.
        this.accelerationPower = 0;
    }

    private record GenericBallEntityInstance(
            ResourceLocation name,
            ProjectileData.GenericBallResource location,
            Optional<ParticleOptions> trailParticle,
            int projectileLevel,
            int maxLingeringTicks,
            int maxMoveDistance,
            int maxLifespan,
            // Needs to pack moveDistance here as the Codec only allows 16 max entries
            Vec3 dimensionsPlusMoveDistance,
            Optional<EntityPredicate> canHitPredicate,
            List<ProjectileTargeting> tickingEffects,
            List<ProjectileTargeting> commonHitEffects,
            List<ProjectileEntityEffect> entityHitEffects,
            List<ProjectileBlockEffect> blockHitEffects,
            List<ProjectileTargeting> onDestroyEffects,
            int lingerTicks,
            int lifespan)
    {
        public static final Codec<GenericBallEntityInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        ResourceLocation.CODEC.fieldOf("name").forGetter(GenericBallEntityInstance::name),
                        ProjectileData.GenericBallResource.CODEC.fieldOf("location").forGetter(GenericBallEntityInstance::location),
                        ParticleTypes.CODEC.optionalFieldOf("trail_particle").forGetter(GenericBallEntityInstance::trailParticle),
                        Codec.INT.fieldOf("projectile_level").forGetter(GenericBallEntityInstance::projectileLevel),
                        Codec.INT.fieldOf("max_lingering_ticks").forGetter(GenericBallEntityInstance::maxLingeringTicks),
                        Codec.INT.fieldOf("max_move_distance").forGetter(GenericBallEntityInstance::maxMoveDistance),
                        Codec.INT.fieldOf("max_lifespan").forGetter(GenericBallEntityInstance::maxLifespan),
                        Vec3.CODEC.fieldOf("dimensions").forGetter(GenericBallEntityInstance::dimensionsPlusMoveDistance),
                        EntityPredicate.CODEC.optionalFieldOf("can_hit_predicate").forGetter(GenericBallEntityInstance::canHitPredicate),
                        ProjectileTargeting.CODEC.listOf().fieldOf("ticking_effects").forGetter(GenericBallEntityInstance::tickingEffects),
                        ProjectileTargeting.CODEC.listOf().fieldOf("common_hit_effects").forGetter(GenericBallEntityInstance::commonHitEffects),
                        ProjectileEntityEffect.CODEC.listOf().fieldOf("entity_hit_effects").forGetter(GenericBallEntityInstance::entityHitEffects),
                        ProjectileBlockEffect.CODEC.listOf().fieldOf("block_hit_effects").forGetter(GenericBallEntityInstance::blockHitEffects),
                        ProjectileTargeting.CODEC.listOf().fieldOf("on_destroy_effects").forGetter(GenericBallEntityInstance::onDestroyEffects),
                        Codec.INT.fieldOf("linger_ticks").forGetter(GenericBallEntityInstance::lingerTicks),
                        Codec.INT.fieldOf("lifespan").forGetter(GenericBallEntityInstance::lifespan)
                ).apply(instance, GenericBallEntityInstance::new)
        );

        public void load(GenericBallEntity entity) {
            entity.setFromData(
                    ResourceLocation.read(entity.entityData.get(NAME)).getOrThrow(),
                    location,
                    trailParticle,
                    EntityDimensions.scalable((float) dimensionsPlusMoveDistance.x(), (float) dimensionsPlusMoveDistance.y()),
                    canHitPredicate,
                    tickingEffects,
                    commonHitEffects,
                    entityHitEffects,
                    blockHitEffects,
                    onDestroyEffects,
                    projectileLevel,
                    maxLingeringTicks,
                    maxMoveDistance,
                    maxLifespan,
                    lifespan,
                    (float) dimensionsPlusMoveDistance.z(),
                    lingerTicks
            );
        }

        public static GenericBallEntityInstance fromEntity(GenericBallEntity entity) {
            return new GenericBallEntityInstance(
                    ResourceLocation.read(entity.entityData.get(NAME)).getOrThrow(),
                    entity.resources,
                    Optional.ofNullable(entity.getTrailParticle()),
                    entity.projectileLevel,
                    entity.maxLingeringTicks,
                    entity.maxMoveDistance,
                    entity.maxLifespan,
                    new Vec3(entity.getWidth(), entity.getHeight(), entity.moveDistance),
                    entity.canHitPredicate,
                    entity.tickingEffects,
                    entity.commonHitEffects,
                    entity.entityHitEffects,
                    entity.blockHitEffects,
                    entity.onDestroyEffects,
                    entity.lingerTicks,
                    entity.lifespan
            );
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        Tag data = GenericBallEntityInstance.CODEC.encodeStart(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), GenericBallEntityInstance.fromEntity(this)).getOrThrow();
        compound.put("generic_ball_entity_instance", data);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        GenericBallEntityInstance.CODEC.parse(level().registryAccess().createSerializationContext(NbtOps.INSTANCE), compound.get("generic_ball_entity_instance")).getOrThrow().load(this);
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key.equals(DIMENSION_WIDTH) || key.equals(DIMENSION_HEIGHT)) {
            this.refreshDimensions();
        }
    }

    private void setDimensionWidth(float width) {
        this.entityData.set(DIMENSION_WIDTH, width);
    }

    private float getWidth() {
        return this.entityData.get(DIMENSION_WIDTH);
    }

    private void setDimensionHeight(float height) {
        this.entityData.set(DIMENSION_HEIGHT, height);
    }

    private float getHeight() {
        return this.entityData.get(DIMENSION_HEIGHT);
    }

    private EntityDimensions getDimensions() {
        return EntityDimensions.scalable(this.entityData.get(DIMENSION_WIDTH), this.entityData.get(DIMENSION_HEIGHT));
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return getDimensions();
    }

    public ResourceLocation getTextureLocation() {
        return ResourceLocation.read(this.entityData.get(TEXTURE_LOCATION)).getOrThrow();
    }

    public ResourceLocation getAnimLocation() {
        return ResourceLocation.read(this.entityData.get(ANIM_LOCATION)).getOrThrow();
    }

    public ResourceLocation getGeoLocation() {
        return ResourceLocation.read(this.entityData.get(GEO_LOCATION)).getOrThrow();
    }

    public void setName(ResourceLocation name) {
        this.entityData.set(NAME, name.toString());
    }

    private void setResourceLocations(ProjectileData.GenericBallResource location) {
        this.resources = location;
        String texture = location.resource().get(projectileLevel).toString();
        this.entityData.set(TEXTURE_LOCATION, texture);
        this.entityData.set(ANIM_LOCATION, texture);
        this.entityData.set(GEO_LOCATION, texture);
    }

    private boolean isLingering() {
        return this.entityData.get(LINGERING);
    }

    private void setLingering(boolean lingering) {
        this.entityData.set(LINGERING, lingering);
    }

    @Override
    protected @NotNull Component getTypeName() {
        Optional<ResourceLocation> name = ResourceLocation.read(entityData.get(NAME)).result();
        if(name.isEmpty()) {
            return super.getTypeName();
        }

        return Component.translatable(Translation.Type.PROJECTILE.wrap(name.get()));
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return trailParticle.orElse(null);
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity target) {
        boolean canHit = super.canHitEntity(target);
        if(canHitPredicate.isPresent() && level() instanceof ServerLevel serverLevel){
            canHit = canHit && canHitPredicate.get().matches(serverLevel, position(), target);
        }
        return canHit;
    }

    protected void onDestroy() {
        if (!level().isClientSide) {
            for (ProjectileTargeting effect : onDestroyEffects) {
                effect.apply(this, projectileLevel);
            }

            this.discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(NAME, ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "generic_ball").toString());
        pBuilder.define(TEXTURE_LOCATION, ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "generic_ball").toString());
        pBuilder.define(ANIM_LOCATION, ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "generic_ball").toString());
        pBuilder.define(GEO_LOCATION, ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "generic_ball").toString());
        pBuilder.define(LINGERING, false);
        pBuilder.define(DIMENSION_WIDTH, 0.5F);
        pBuilder.define(DIMENSION_HEIGHT, 0.5F);
    }

    @Override
    public @NotNull Vec3 getDeltaMovement() {
        if (isLingering()) {
            return Vec3.ZERO;
        }

        return super.getDeltaMovement();
    }


    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            moveDistance += (float) getDeltaMovement().length();
            lifespan++;

            if (moveDistance > maxMoveDistance || lifespan > maxLifespan) {
                // Call onHitBlock rather than onHit, since calling onHit using the helper function from
                // vanilla will result in HitResult.Miss from 1.20.6 onwards, causing nothing to happen
                this.onHitBlock(new BlockHitResult(this.position(), this.getDirection(), this.blockPosition(), false));
            }

            for (ProjectileTargeting effect : tickingEffects) {
                effect.apply(this, projectileLevel);
            }

            if (isLingering()) {
                lingerTicks--;
                if (lingerTicks <= 0) {
                    this.onDestroy();
                }
            }
        }
    }


    @Override
    protected void onHitEntity(@NotNull EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!level().isClientSide) {
            for (ProjectileEntityEffect effect : entityHitEffects) {
                effect.apply(this, hitResult.getEntity(), projectileLevel);
            }
        }

        onHitCommon();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (!level().isClientSide) {
            for (ProjectileBlockEffect effect : blockHitEffects) {
                effect.apply(this, hitResult.getBlockPos(), projectileLevel);
            }
        }

        onHitCommon();
    }

    public void onHitCommon() {
        if (!level().isClientSide) {
            for (ProjectileTargeting effect : commonHitEffects) {
                effect.apply(this, projectileLevel);
            }

            if(this.maxLingeringTicks <= 0)
                this.discard();
        }

        if (!isLingering()) {
            setLingering(true);
            // These power variables drive the movement of the entity in the parent tick() function, so we need to zero them out as well.
            setDeltaMovement(Vec3.ZERO);
        }
    }

    public PlayState predicate(final AnimationState<GenericBallEntity> state) {

        if (!isLingering() && maxLingeringTicks > 0) {
            state.getController().setAnimation(FLY);
            return PlayState.CONTINUE;
        } else if (lingerTicks < 16 && maxLingeringTicks > 0) {
            state.getController().setAnimation(EXPLOSION);
        } else {
            state.getController().setAnimation(IDLE);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "everything", this::predicate));
    }

    // We don't want these entities to slow down
    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    // Stops fire from completely smothering the animations
    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    private static final RawAnimation EXPLOSION = RawAnimation.begin().thenLoop("explosion");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");
}
