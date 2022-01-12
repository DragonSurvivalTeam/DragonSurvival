package by.jackraidenph.dragonsurvival.common.entity.creatures;

import by.jackraidenph.dragonsurvival.common.DragonEffects;
import by.jackraidenph.dragonsurvival.common.capability.DragonStateProvider;
import by.jackraidenph.dragonsurvival.common.entity.DSEntities;
import by.jackraidenph.dragonsurvival.config.ConfigHandler;
import by.jackraidenph.dragonsurvival.misc.PrincessTrades;
import by.jackraidenph.dragonsurvival.util.Functions;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.villager.VillagerData;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.villager.VillagerType;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * Horseless princess
 */
public class PrincessEntity extends VillagerEntity {
    private static final List<DyeColor> colors = Arrays.asList(DyeColor.RED, DyeColor.YELLOW, DyeColor.PURPLE, DyeColor.BLUE, DyeColor.BLACK, DyeColor.WHITE);
    public static DataParameter<Integer> color = EntityDataManager.defineId(PrincessEntity.class, DataSerializers.INT);

    public PrincessEntity(EntityType<? extends VillagerEntity> entityType, World world) {
        super(entityType, world);
    }

    public PrincessEntity(EntityType<? extends VillagerEntity> entityType, World world, VillagerType villagerType) {
        super(entityType, world, villagerType);
    }
    
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(color, 0);
    }

    @Nullable
    public ILivingEntityData finalizeSpawn(@Nonnull IServerWorld serverWorld, @Nonnull DifficultyInstance difficultyInstance, SpawnReason reason, @Nullable ILivingEntityData livingEntityData, @Nullable CompoundNBT compoundNBT) {
        setColor(colors.get(this.random.nextInt(6)).getId());
        setVillagerData(getVillagerData().setProfession(DSEntities.PRINCESS_PROFESSION));
        return super.finalizeSpawn(serverWorld, difficultyInstance, reason, livingEntityData, compoundNBT);
    }

    public void readAdditionalSaveData(@Nonnull CompoundNBT compoundNBT) {
        super.readAdditionalSaveData(compoundNBT);
        setColor(compoundNBT.getInt("Color"));
    }

    public void addAdditionalSaveData(@Nonnull CompoundNBT compoundNBT) {
        super.addAdditionalSaveData(compoundNBT);
        compoundNBT.putInt("Color", getColor());
    }

    public int getColor() {
        return this.entityData.get(color);
    }

    public void setColor(int i) {
        this.entityData.set(color, i);
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    protected SoundEvent getHurtSound(@Nonnull DamageSource p_184601_1_) {
        return SoundEvents.GENERIC_HURT;
    }

    @Nonnull
    @Override
    protected SoundEvent getTradeUpdatedSound(boolean p_213721_1_) {
        return null;
    }

    @Nonnull
    @Override
    public SoundEvent getNotifyTradeSound() {
        return null;
    }

    public void playCelebrateSound() {
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    public void playWorkSound() {
    }

    @Nonnull
    @Override
    protected Brain<?> makeBrain(@Nonnull Dynamic<?> p_213364_1_) {
        return brainProvider().makeBrain(p_213364_1_);
    }

    public void refreshBrain(@Nonnull ServerWorld p_213770_1_) {
    }

    public boolean canBreed() {
        return false;
    }

    @Nonnull
    protected ITextComponent getTypeName() {
        return new TranslationTextComponent(this.getType().getDescriptionId());
    }

    public void thunderHit(@Nonnull ServerWorld p_241841_1_, @Nonnull LightningBoltEntity p_241841_2_) {
    }

    protected void pickUpItem(@Nonnull ItemEntity p_175445_1_) {
    }

    protected void updateTrades() {
        VillagerData villagerdata = getVillagerData();
        Int2ObjectMap<VillagerTrades.ITrade[]> int2objectmap = PrincessTrades.colorToTrades.get(getColor());
        if (int2objectmap != null && !int2objectmap.isEmpty()) {
            VillagerTrades.ITrade[] trades = int2objectmap.get(villagerdata.getLevel());
            if (trades != null) {
                MerchantOffers merchantoffers = getOffers();
                addOffersFromItemListings(merchantoffers, trades, 2);
            }
        }
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 0.5D) {
            public boolean canUse() {
                return (!PrincessEntity.this.isTrading() && super.canUse());
            }
        });
        this.goalSelector.addGoal(6, new LookAtGoal(this, LivingEntity.class, 8.0F));
        goalSelector.addGoal(6, new AvoidEntityGoal<>(this, PlayerEntity.class, 16, 1, 1, livingEntity -> {
            return DragonStateProvider.isDragon(livingEntity) && livingEntity.hasEffect(DragonEffects.EVIL_DRAGON);
        }));
        goalSelector.addGoal(7, new PanicGoal(this, 1));
    }

    public void gossip(@Nonnull ServerWorld p_242368_1_, @Nonnull VillagerEntity p_242368_2_, long p_242368_3_) {
    }

    public void startSleeping(@Nonnull BlockPos p_213342_1_) {
    }

    @Override
    public void die(@Nonnull DamageSource damageSource) {
        super.die(damageSource);
        Item flower = Items.AIR;
        DyeColor dyeColor = DyeColor.byId(getColor());
        switch (dyeColor) {
            case BLUE:
                flower = Items.BLUE_ORCHID;
                break;
            case RED:
                flower = Items.RED_TULIP;
                break;
            case BLACK:
                flower = Items.WITHER_ROSE;
                break;
            case YELLOW:
                flower = Items.DANDELION;
                break;
            case PURPLE:
                flower = Items.LILAC;
                break;
            case WHITE:
                flower = Items.LILY_OF_THE_VALLEY;
                break;
        }
        if (!level.isClientSide)
            level.addFreshEntity(new ItemEntity(level, getX(), getY(), getZ(), new ItemStack(flower)));
    }

    @Override
    public boolean removeWhenFarAway(double p_213397_1_) {
        return !this.hasCustomName() && tickCount >= Functions.minutesToTicks(ConfigHandler.COMMON.princessDespawnDelay.get());
    }
}
