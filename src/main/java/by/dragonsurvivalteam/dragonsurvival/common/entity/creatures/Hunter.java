package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.HurtByTargetGoalExtended;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class Hunter extends PathfinderMob implements GeoEntity, ConfigurableAttributes {
    private static final EntityDataAccessor<Boolean> IS_AGGRO = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public Hunter(EntityType<? extends PathfinderMob> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    @SuppressWarnings("deprecation") // ignore deprecated
    public @Nullable SpawnGroupData finalizeSpawn(@NotNull final ServerLevelAccessor level, @NotNull final DifficultyInstance difficulty, @NotNull final MobSpawnType spawnType, @Nullable final SpawnGroupData spawnGroupData) {
        setAttributes();

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void tick() {
        updateSwingTime();
        super.tick();
    }

    protected void registerGoals() {
        super.registerGoals();

        // The Hunter.class in the constructor refers to the mobs that are ignored when the mob is hurt by them (we don't want hunters attacking each other!)
        this.targetSelector.addGoal(1, new HurtByTargetGoalExtended(this, Hunter.class).setHeeders(Hunter.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, living -> living.hasEffect(DSEffects.HUNTER_OMEN)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 0, false, false, living -> living.getType().is(DSEntityTypeTags.HUNTER_TARGETS)));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.6));
    }

    @Override
    protected void defineSynchedData(@NotNull final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_AGGRO, false);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        this.setAggro(target != null);
    }

    public void setAggro(boolean aggro) {
        this.entityData.set(IS_AGGRO, aggro);
    }

    public boolean isAggro() {
        return this.entityData.get(IS_AGGRO);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}