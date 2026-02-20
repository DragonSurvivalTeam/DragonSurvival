package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.render.util.RandomAnimationPicker;
import by.dragonsurvivalteam.dragonsurvival.config.entity.LeaderEntityConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTrades;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.animation.state.AnimationTest;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LeaderEntity extends Villager implements GeoEntity, ConfigurableAttributes {
    @Override
    public double maxHealthConfig() {
        return LeaderEntityConfig.MAX_HEALTH;
    }

    @Override
    public double movementSpeedConfig() {
        return LeaderEntityConfig.MOVEMENT_SPEED;
    }

    @Override
    public double armorConfig() {
        return LeaderEntityConfig.ARMOR;
    }

    @Override
    public double armorToughnessConfig() {
        return LeaderEntityConfig.ARMOR_TOUGHNESS;
    }

    @Override
    public double knockbackResistanceConfig() {
        return LeaderEntityConfig.KNOCKBACK_RESISTANCE;
    }

    private static final EntityDataAccessor<Integer> RESTOCK_TIMER = SynchedEntityData.defineId(LeaderEntity.class, EntityDataSerializers.INT);
    private static final int TOTAL_RESTOCK_TIME = Functions.minutesToTicks(10);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private RawAnimation currentIdleAnim;
    private boolean isIdleAnimSet;

    // Since Villagers do not have 'finalizeSpawn'
    private boolean initializedAttributes;

    public LeaderEntity(EntityType<? extends Villager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("everything", 3, this::fullPredicate));
        controllers.add(new AnimationController<>("head", 3, this::headPredicate));
        controllers.add(new AnimationController<>("arms", 3, this::armsPredicate));
        controllers.add(new AnimationController<>("legs", 3, this::legsPredicate));
    }

    private double getWalkThreshold() {
        return 0.01;
    }

    private double getRunThreshold() {
        return 0.15;
    }

    private boolean isNotIdle() {
        double movement = AnimationUtils.getMovementSpeed(this);
        return swingTime > 0 || movement > getWalkThreshold();
    }

    public PlayState fullPredicate(final AnimationTest<LeaderEntity> state) {
        if (isNotIdle()) {
            isIdleAnimSet = false;
            return PlayState.STOP;
        }

        return state.setAndContinue(getIdleAnim());
    }

    public PlayState headPredicate(final AnimationTest<LeaderEntity> state) {
        return state.setAndContinue(HEAD_BLEND);
    }

    public PlayState armsPredicate(final AnimationTest<LeaderEntity> state) {
        if (swingTime > 0) {
            return state.setAndContinue(ATTACK_BLEND);
        }

        return PlayState.STOP;
    }

    public PlayState legsPredicate(final AnimationTest<LeaderEntity> state) {
        double movement = AnimationUtils.getMovementSpeed(this);

        if (movement > getRunThreshold()) {
            return state.setAndContinue(RUN);
        } else if (movement > getWalkThreshold()) {
            return state.setAndContinue(WALK);
        }

        return PlayState.STOP;
    }

    public RawAnimation getIdleAnim() {
        if (!isIdleAnimSet) {
            currentIdleAnim = IDLE_ANIMS.pickRandomAnimation();
            isIdleAnimSet = true;
        }

        return currentIdleAnim;
    }

    @Override
    protected void defineSynchedData(@NotNull final SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(RESTOCK_TIMER, TOTAL_RESTOCK_TIME);
    }

    private void setRestockTimer(int time) {
        this.entityData.set(RESTOCK_TIMER, time);
    }

    private int getRestockTimer() {
        return this.entityData.get(RESTOCK_TIMER);
    }

    @Override
    public void addAdditionalSaveData(@NotNull ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("RestockTimer", getRestockTimer());
        valueOutput.putBoolean("initialized_attributes", initializedAttributes);
    }

    @Override
    public void readAdditionalSaveData(@NotNull ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        setRestockTimer(valueInput.getIntOr("RestockTimer", 0));
        initializedAttributes = valueInput.getBooleanOr("initialized_attributes", false);
    }

    @Override
    public void tick() {
        if (!initializedAttributes) {
            setAttributes();
            initializedAttributes = true;
        }

        super.tick();

        if (level() instanceof ServerLevel) {
            if (getRestockTimer() > 0) {
                setRestockTimer(getRestockTimer() - 1);
            } else {
                restock();
                setRestockTimer(TOTAL_RESTOCK_TIME);
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // TODO: Custom sounds?
    @Override
    public @NotNull SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    // TODO: Custom sounds?
    @Override
    protected @NotNull SoundEvent getTradeUpdatedSound(boolean pIsYesSound) {
        return pIsYesSound ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
    }

    @Override
    public void playCelebrateSound() {
    }

    @Override
    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> pDynamic) {
        return brainProvider().makeBrain(pDynamic);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override // TODO: Custom sounds?
    protected @NotNull SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    @Override // TODO: Custom sounds?
    protected @NotNull SoundEvent getDeathSound() {
        return SoundEvents.VILLAGER_DEATH;
    }

    @Override
    public void playWorkSound() {
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    protected @NotNull Component getTypeName() {
        return Component.translatable(getType().getDescriptionId());
    }

    @Override
    public boolean wantsToPickUp(@NotNull ServerLevel level, @NotNull ItemStack stack) {
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor pLevel, @NotNull DifficultyInstance pDifficulty, @NotNull EntitySpawnReason pSpawnType, @Nullable SpawnGroupData pSpawnGroupData) {
        setVillagerData(getVillagerData().withProfession(pLevel.registryAccess(), VillagerProfession.NITWIT));
        return super.finalizeSpawn(pLevel, pDifficulty, pSpawnType, pSpawnGroupData);
    }

    @Override
    public void thunderHit(@NotNull ServerLevel level, @NotNull LightningBolt bolt) {
    }

    @Override
    public void gossip(@NotNull ServerLevel level, @NotNull Villager villager, long gameTime) {
    }

    @Override
    public void startSleeping(@NotNull BlockPos blockPos) {
    }

    private static final RandomAnimationPicker IDLE_ANIMS = new RandomAnimationPicker(
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle1"), 69),
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle2"), 20),
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle3"), 10),
            new RandomAnimationPicker.WeightedAnimation(RawAnimation.begin().thenLoop("idle4"), 1)
    );

    // Copied from Villager.java, but with the trades changed to the ones in DSTrades
    @Override
    protected void updateTrades(@NotNull ServerLevel level) {
        VillagerData villagerdata = this.getVillagerData();
        Int2ObjectMap<VillagerTrades.ItemListing[]> int2objectmap;
        int2objectmap = DSTrades.LEADER_TRADES;

        if (!int2objectmap.isEmpty()) {
            VillagerTrades.ItemListing[] avillagertrades$itemlisting = int2objectmap.get(villagerdata.level());
            if (avillagertrades$itemlisting != null) {
                MerchantOffers merchantoffers = this.getOffers();
                this.addOffersFromItemListings(level, merchantoffers, avillagertrades$itemlisting, 2);
            }
        }
    }

    // This prevents the trade window from closing due to this Villager not having a proper profession
    @Override
    protected void customServerAiStep(@NotNull ServerLevel level) {
        Player player = getTradingPlayer();
        super.customServerAiStep(level);
        if (player != null) {
            if (getTradingPlayer() == null) {
                setTradingPlayer(player);
            }
        }
    }

    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("walk");

    private static final RawAnimation RUN = RawAnimation.begin().thenLoop("run");

    private static final RawAnimation ATTACK_BLEND = RawAnimation.begin().thenLoop("blend_attack");

    private static final RawAnimation HEAD_BLEND = RawAnimation.begin().thenLoop("blend_head");
}