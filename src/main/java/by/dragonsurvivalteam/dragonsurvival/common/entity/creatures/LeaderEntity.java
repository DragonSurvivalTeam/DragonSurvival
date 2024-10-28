package by.dragonsurvivalteam.dragonsurvival.common.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.render.util.RandomAnimationPicker;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTrades;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class LeaderEntity extends Villager implements DragonHunter, GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final EntityDataAccessor<Integer> RESTOCK_TIMER = SynchedEntityData.defineId(LeaderEntity.class, EntityDataSerializers.INT);
    private static final int TOTAL_RESTOCK_TIME = Functions.minutesToTicks(10);

    private RawAnimation currentIdleAnim;
    private boolean isIdleAnimSet = false;

    public LeaderEntity(EntityType<? extends Villager> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "everything", 3, this::fullPredicate));
        controllers.add(new AnimationController<>(this, "head", 3, this::headPredicate));
        controllers.add(new AnimationController<>(this, "arms", 3, this::armsPredicate));
        controllers.add(new AnimationController<>(this, "legs", 3, this::legsPredicate));
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

    public PlayState fullPredicate(final AnimationState<LeaderEntity> state) {
        if (isNotIdle()) {
            isIdleAnimSet = false;
            return PlayState.STOP;
        }

        return state.setAndContinue(getIdleAnim());
    }

    public PlayState headPredicate(final AnimationState<LeaderEntity> state) {
        return state.setAndContinue(HEAD_BLEND);
    }

    public PlayState armsPredicate(final AnimationState<LeaderEntity> state) {
        if (swingTime > 0) {
            return state.setAndContinue(ATTACK_BLEND);
        }

        return PlayState.STOP;
    }

    public PlayState legsPredicate(final AnimationState<LeaderEntity> state) {
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
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(RESTOCK_TIMER, TOTAL_RESTOCK_TIME);
    }

    private void setRestockTimer(int time) {
        this.entityData.set(RESTOCK_TIMER, time);
    }

    private int getRestockTimer() {
        return this.entityData.get(RESTOCK_TIMER);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compoundNBT) {
        super.addAdditionalSaveData(compoundNBT);
        compoundNBT.putInt("RestockTimer", getRestockTimer());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compoundNBT) {
        super.readAdditionalSaveData(compoundNBT);
        setRestockTimer(compoundNBT.getInt("RestockTimer"));
    }

    @Override
    public void tick() {
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
    @Nullable protected SoundEvent getAmbientSound() {
        return null;
    }

    // TODO: Custom sounds?
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.VILLAGER_HURT;
    }

    // TODO: Custom sounds?
    @Override
    protected SoundEvent getDeathSound() {
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
    public boolean wantsToPickUp(@NotNull ItemStack pStack) {
        return false;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor pLevel, @NotNull DifficultyInstance pDifficulty, @NotNull MobSpawnType pSpawnType, @Nullable SpawnGroupData pSpawnGroupData) {
        setVillagerData(getVillagerData().setProfession(VillagerProfession.NITWIT));
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
    protected void updateTrades() {
        VillagerData villagerdata = this.getVillagerData();
        Int2ObjectMap<VillagerTrades.ItemListing[]> int2objectmap;
        int2objectmap = DSTrades.LEADER_TRADES;

        if (!int2objectmap.isEmpty()) {
            VillagerTrades.ItemListing[] avillagertrades$itemlisting = int2objectmap.get(villagerdata.getLevel());
            if (avillagertrades$itemlisting != null) {
                MerchantOffers merchantoffers = this.getOffers();
                this.addOffersFromItemListings(merchantoffers, avillagertrades$itemlisting, 2);
            }
        }
    }

    // This prevents the trade window from closing due to this Villager not having a proper profession
    @Override
    protected void customServerAiStep() {
        Player player = getTradingPlayer();
        super.customServerAiStep();
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