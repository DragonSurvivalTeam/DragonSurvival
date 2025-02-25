package by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.ProjectileData;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileTargeting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
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
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GenericBallEntity extends AbstractHurtingProjectile implements GeoEntity, IEntityWithComplexSpawn {
    private static final EntityDataAccessor<Boolean> LINGERING = SynchedEntityData.defineId(GenericBallEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private ProjectileData.GeneralData generalData;
    private ProjectileData.GenericBallData typeData;
    private int projectileLevel;

    private final Lazy<EntityDimensions> dimensions = Lazy.of(() -> EntityDimensions.scalable(
            typeData.behaviourData().width().calculate(projectileLevel),
            typeData.behaviourData().height().calculate(projectileLevel)
    ));

    private float movementDistance;
    private int lingerTicks;
    private int lifespan;
    private int bounces;

    // Not saved, temporary variables
    private Entity lastEntityHit;
    private BlockPos lastBlockHit;
    private boolean hitWasTriggeredByLifespanOrDistance;

    public GenericBallEntity(final ProjectileData.GeneralData generalData, final ProjectileData.GenericBallData typeData, final int projectileLevel, final Vec3 position, final Level level) {
        super(DSEntities.GENERIC_BALL_ENTITY.get(), level);
        this.generalData = generalData;
        this.typeData = typeData;
        this.projectileLevel = projectileLevel;
        accelerationPower = 0;

        refreshDimensions();
        setPos(position.x, position.y, position.z);
        reapplyPosition();
    }

    // Not setting any values here because this should not be called when creating an actual instance of the projectile
    public GenericBallEntity(final EntityType<GenericBallEntity> type, final Level level) {
        super(type, level);
    }

    @Override
    public void writeSpawnData(@NotNull final RegistryFriendlyByteBuf buffer) {
        ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GeneralData.CODEC).encode(buffer, generalData);
        ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GenericBallData.CODEC).encode(buffer, typeData);
        buffer.writeVarInt(projectileLevel);
    }

    @Override
    public void readSpawnData(@NotNull final RegistryFriendlyByteBuf buffer) {
        generalData = ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GeneralData.CODEC).decode(buffer);
        typeData = ByteBufCodecs.fromCodecWithRegistries(ProjectileData.GenericBallData.CODEC).decode(buffer);
        projectileLevel = buffer.readVarInt();
        accelerationPower = 0;

        refreshDimensions();
    }

    @Override
    public void addAdditionalSaveData(@NotNull final CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        RegistryOps<Tag> context = level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        ProjectileData.GeneralData.CODEC.encodeStart(context, generalData).ifSuccess(data -> tag.put(GENERAL_DATA, data));
        ProjectileData.GenericBallData.CODEC.encodeStart(context, typeData).ifSuccess(data -> tag.put(TYPE_DATA, data));

        tag.putInt(PROJECTILE_LEVEL, projectileLevel);
        tag.putFloat(MOVEMENT_DISTANCE, movementDistance);
        tag.putInt(LINGERING_TICKS, lingerTicks);
        tag.putInt(LIFESPAN, lifespan);
        tag.putInt(BOUNCES, bounces);
    }

    @Override
    public void readAdditionalSaveData(@NotNull final CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        RegistryOps<Tag> context = level().registryAccess().createSerializationContext(NbtOps.INSTANCE);

        if (tag.contains(GENERAL_DATA)) {
            ProjectileData.GeneralData.CODEC.parse(context, tag.get(GENERAL_DATA))
                    .resultOrPartial(DragonSurvival.LOGGER::error)
                    .map(data -> generalData = data);
        }

        if (tag.contains(TYPE_DATA)) {
            ProjectileData.GenericBallData.CODEC.parse(context, tag.get(TYPE_DATA))
                    .resultOrPartial(DragonSurvival.LOGGER::error)
                    .map(data -> typeData = data);
        }

        if (generalData == null || typeData == null) {
            // The data structure was changed too much, no need to keep the projectile
            discard();
            return;
        }

        projectileLevel = tag.getInt(PROJECTILE_LEVEL);
        movementDistance = tag.getFloat(MOVEMENT_DISTANCE);
        lingerTicks = tag.getInt(LINGERING_TICKS);
        lifespan = tag.getInt(LIFESPAN);
        bounces = tag.getInt(BOUNCES);
        accelerationPower = 0;

        refreshDimensions();
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull final Pose pose) {
        return dimensions.get();
    }

    private boolean isLingering() {
        return entityData.get(LINGERING);
    }

    @SuppressWarnings("SameParameterValue") // ignore
    private void setLingering(boolean isLingering) {
        entityData.set(LINGERING, isLingering);

        if (isLingering) {
            setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected @NotNull Component getTypeName() {
        return Component.translatable(Translation.Type.PROJECTILE.wrap(generalData.name()));
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return typeData.trailParticle().orElse(null);
    }

    @Override
    protected boolean canHitEntity(@NotNull final Entity target) {
        if (!super.canHitEntity(target)) {
            return false;
        }

        if (level() instanceof ServerLevel serverLevel && generalData.entityHitCondition().isPresent()) {
            return generalData.entityHitCondition().get().test(Condition.projectileContext(serverLevel, this, target));
        }

        return true;
    }

    protected void onDestroy() {
        if (level().isClientSide()) {
            return;
        }

        for (ProjectileTargeting effect : typeData.onDestroyEffects()) {
            effect.apply(this, projectileLevel);
        }

        this.discard();
    }

    @Override
    protected void defineSynchedData(@NotNull final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LINGERING, false);
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

        movementDistance += (float) getDeltaMovement().length();
        lifespan++;

        if (movementDistance > getMaxMovementDistance() || lifespan > getMaxLifespan()) {
            // Call onHitBlock rather than onHit, since calling onHit using the helper function from
            // vanilla will result in HitResult.Miss from 1.20.6 onwards, causing nothing to happen
            hitWasTriggeredByLifespanOrDistance = true;
            this.onHitBlock(new BlockHitResult(this.position(), this.getDirection(), this.blockPosition(), false));
        }

        if (!level().isClientSide()) {
            for (ProjectileTargeting effect : generalData.tickingEffects()) {
                effect.apply(this, projectileLevel);
            }
        }

        if (isLingering()) {
            lingerTicks++;

            if (lingerTicks > getMaxLingeringTicks()) {
                onDestroy();
            }
        }
    }

    @Override
    protected void onHitEntity(@NotNull final EntityHitResult hitResult) {
        if (hitResult.getEntity() == lastEntityHit) {
            return;
        }

        lastEntityHit = hitResult.getEntity();
        super.onHitEntity(hitResult);

        if (!level().isClientSide()) {
            for (ProjectileEntityEffect effect : generalData.entityHitEffects()) {
                effect.apply(this, hitResult.getEntity(), projectileLevel);
            }
        }

        onHitCommon(hitResult.getEntity().getDirection(), true);
    }

    @Override
    protected void onHitBlock(@NotNull final BlockHitResult hitResult) {
        if (hitResult.isInside()) {
            onDestroy();
            return;
        }

        if (hitResult.getBlockPos().equals(lastBlockHit)) {
            return;
        }

        lastBlockHit = hitResult.getBlockPos();
        super.onHitBlock(hitResult);

        if (!level().isClientSide()) {
            for (ProjectileBlockEffect effect : generalData.blockHitEffects()) {
                effect.apply(this, hitResult.getBlockPos(), projectileLevel);
            }
        }

        onHitCommon(hitResult.getDirection(), false);
    }

    private void reflect() {
        Vec3 motion = getDeltaMovement();
        setDeltaMovement(-motion.x(), -motion.y(), -motion.z());
        bounces++;
    }

    private void bounce(final Direction direction) {
        Vec3 motion = getDeltaMovement();
        double x = motion.x();
        double y = motion.y();
        double z = motion.z();

        switch (direction) {
            case NORTH:
            case SOUTH:
                setDeltaMovement(x, y, -z);
                break;
            case EAST:
            case WEST:
                setDeltaMovement(-x, y, z);
                break;
            case UP:
            case DOWN:
                setDeltaMovement(x, -y, z);
                break;
        }

        bounces++;
    }

    public void onHitCommon(final Direction direction, boolean wasEntity) {
        if (!level().isClientSide()) {
            for (ProjectileTargeting effect : generalData.commonHitEffects()) {
                effect.apply(this, projectileLevel);
            }

            if (getMaxLingeringTicks() <= 0 && bounces >= getMaxBounces()) {
                onDestroy();
            }
        }

        if (!isLingering() && (bounces >= getMaxBounces() || hitWasTriggeredByLifespanOrDistance)) {
            setLingering(true);
        } else if (bounces < getMaxBounces()) {
            if (wasEntity) {
                reflect();
            } else {
                bounce(direction);
            }
        }
    }

    public PlayState predicate(final AnimationState<GenericBallEntity> state) {
        if (!isLingering() && getMaxLingeringTicks() > 0) {
            state.getController().setAnimation(FLY);
            return PlayState.CONTINUE;
        } else if (lingerTicks < 16 && getMaxLingeringTicks() > 0) {
            state.getController().setAnimation(EXPLOSION);
        } else {
            state.getController().setAnimation(IDLE);
        }

        return PlayState.CONTINUE;
    }

    public ResourceLocation getTextureResource() {
        return typeData.resources().get(projectileLevel);
    }

    public ResourceLocation getAnimationResource() {
        return typeData.resources().get(projectileLevel);
    }

    public ResourceLocation getModelResource() {
        return typeData.resources().get(projectileLevel);
    }

    private int getMaxBounces() {
        return (int) typeData.behaviourData().maxBounces().calculate(projectileLevel);
    }

    public int getMaxLingeringTicks() {
        return (int) typeData.behaviourData().maxLingeringTicks().calculate(projectileLevel);
    }

    public int getMaxMovementDistance() {
        return (int) typeData.behaviourData().maxMovementDistance().calculate(projectileLevel);
    }

    public int getMaxLifespan() {
        return (int) typeData.behaviourData().maxLifespan().calculate(projectileLevel);
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<>(this, "everything", this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    protected float getInertia() {
        // We don't want these entities to slow down
        return 1;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        // Stops fire from completely smothering the animations
        return true;
    }

    private static final RawAnimation EXPLOSION = RawAnimation.begin().thenLoop("explosion");
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");

    private static final String GENERAL_DATA = "general_data";
    private static final String TYPE_DATA = "type_data";

    private static final String PROJECTILE_LEVEL = "projectile_level";
    private static final String MOVEMENT_DISTANCE = "movement_distance";
    private static final String LINGERING_TICKS = "lingering_ticks";
    private static final String LIFESPAN = "lifespan";
    private static final String BOUNCES = "bounces";
}
