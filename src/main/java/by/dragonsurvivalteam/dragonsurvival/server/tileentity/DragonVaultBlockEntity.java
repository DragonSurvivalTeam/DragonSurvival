package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonVaultBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonVaultState;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DragonVaultBlockEntity extends BlockEntity {
    private static final int MAX_REWARDED_PLAYERS = 128;
    private static final int PLAYER_SCAN_INTERVAL = 20;
    private static final int DISPLAY_CYCLE_INTERVAL = 20;
    private static final int UNLOCKING_DELAY = 14;
    private static final int EJECTION_DELAY = 20;
    private static final int FAILED_INSERT_SOUND_DELAY = 15;
    private static final double ACTIVATION_RANGE = 4.0;
    private static final double DEACTIVATION_RANGE = 4.5;

    private final Set<UUID> rewardedPlayers = new LinkedHashSet<>();
    private final Set<UUID> connectedPlayers = new LinkedHashSet<>();
    private final List<ItemStack> itemsToEject = new ArrayList<>();
    private ItemStack displayItem = ItemStack.EMPTY;
    private long nextStateUpdate;
    private long lastFailedInsert = Long.MIN_VALUE;
    private int totalEjections;
    private float previousSpin;
    private float spin;

    public DragonVaultBlockEntity(final BlockPos pos, final BlockState state) {
        super(DSBlockEntities.DRAGON_VAULT.get(), pos, state);
    }

    public static void serverTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final DragonVaultBlockEntity vault
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        DragonVaultState vaultState = state.getValue(DragonVaultBlock.STATE);
        long gameTime = serverLevel.getGameTime();

        if (vaultState == DragonVaultState.ACTIVE && gameTime % DISPLAY_CYCLE_INTERVAL == 0) {
            vault.cycleDisplayItem(serverLevel);
        }

        if (!ServerConfig.forceStateUpdatingOnVaults && gameTime < vault.nextStateUpdate) {
            return;
        }

        switch (vaultState) {
            case INACTIVE, ACTIVE -> {
                vault.updateConnectedPlayers(serverLevel, vaultState == DragonVaultState.ACTIVE ? DEACTIVATION_RANGE : ACTIVATION_RANGE);
                vault.transitionTo(vault.connectedPlayers.isEmpty() ? DragonVaultState.INACTIVE : DragonVaultState.ACTIVE);
                vault.nextStateUpdate = gameTime + PLAYER_SCAN_INTERVAL;
                vault.setChanged();
            }
            case UNLOCKING -> {
                vault.transitionTo(DragonVaultState.EJECTING);
                vault.nextStateUpdate = gameTime + EJECTION_DELAY;
                vault.setChanged();
            }
            case EJECTING -> {
                if (vault.itemsToEject.isEmpty()) {
                    vault.totalEjections = 0;
                    vault.updateConnectedPlayers(serverLevel, DEACTIVATION_RANGE);
                    vault.transitionTo(vault.connectedPlayers.isEmpty() ? DragonVaultState.INACTIVE : DragonVaultState.ACTIVE);
                    vault.nextStateUpdate = gameTime + PLAYER_SCAN_INTERVAL;
                } else {
                    vault.ejectNextItem(serverLevel);
                    vault.nextStateUpdate = gameTime + EJECTION_DELAY;
                }
                vault.setChanged();
            }
        }
    }

    public static void clientTick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final DragonVaultBlockEntity vault
    ) {
        vault.previousSpin = vault.spin;
        vault.spin = Mth.wrapDegrees(vault.spin + 10.0F);

        if (state.getValue(DragonVaultBlock.STATE) == DragonVaultState.INACTIVE) {
            return;
        }

        RandomSource random = level.getRandom();
        ParticleOptions particle = vault.activeParticle();
        if (random.nextFloat() < 0.5F) {
            double x = pos.getX() + Mth.nextDouble(random, 0.1, 0.9);
            double y = pos.getY() + Mth.nextDouble(random, 0.25, 0.75);
            double z = pos.getZ() + Mth.nextDouble(random, 0.1, 0.9);
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
            level.addParticle(particle, x, y, z, 0, 0.01, 0);
        }

        if (level.getGameTime() % PLAYER_SCAN_INTERVAL == 0) {
            vault.emitConnectionParticles(level, state);
        }

        if (random.nextFloat() < 0.02F) {
            level.playLocalSound(
                    pos,
                    SoundEvents.RESPAWN_ANCHOR_AMBIENT,
                    SoundSource.BLOCKS,
                    random.nextFloat() * 0.25F + 0.75F,
                    random.nextFloat() * 0.5F + 0.75F,
                    false
            );
        }
    }

    public void tryInsertKey(final Player player, final ItemStack stack) {
        if (!(level instanceof ServerLevel serverLevel) || !(getBlockState().getBlock() instanceof DragonVaultBlock vaultBlock)) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        if (!BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(vaultBlock.keyItem())) {
            playFailedInsertSound(serverLevel, gameTime, SoundEvents.NOTE_BLOCK_BASS.get());
            return;
        }

        if (rewardedPlayers.contains(player.getUUID())) {
            playFailedInsertSound(serverLevel, gameTime, SoundEvents.DISPENSER_FAIL);
            return;
        }

        List<ItemStack> rewards = resolveLoot(serverLevel, player);
        if (rewards.isEmpty()) {
            return;
        }

        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        addRewardedPlayer(player.getUUID());
        itemsToEject.clear();
        itemsToEject.addAll(rewards);
        totalEjections = itemsToEject.size();
        setDisplayItem(getNextItemToEject());
        nextStateUpdate = gameTime + UNLOCKING_DELAY;
        transitionTo(DragonVaultState.UNLOCKING);
        updateConnectedPlayers(serverLevel, DEACTIVATION_RANGE);
        setChanged();
        sync();
    }

    private List<ItemStack> resolveLoot(final ServerLevel serverLevel, @Nullable final Player player) {
        DragonVaultBlock block = getVaultBlock();
        if (block == null) {
            return List.of();
        }

        LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(block.lootTable());
        LootParams.Builder params = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(worldPosition));
        if (player != null) {
            params.withLuck(player.getLuck());
        }
        return lootTable.getRandomItems(params.create(LootContextParamSets.CHEST));
    }

    private void cycleDisplayItem(final ServerLevel serverLevel) {
        List<ItemStack> possibleItems = resolveLoot(serverLevel, null);
        ItemStack nextDisplayItem = possibleItems.isEmpty()
                ? ItemStack.EMPTY
                : possibleItems.get(serverLevel.getRandom().nextInt(possibleItems.size()));
        setDisplayItem(nextDisplayItem);
    }

    private void ejectNextItem(final ServerLevel serverLevel) {
        ItemStack reward = itemsToEject.remove(itemsToEject.size() - 1);
        ItemEntity item = new ItemEntity(
                serverLevel,
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.2,
                worldPosition.getZ() + 0.5,
                reward
        );
        RandomSource random = serverLevel.getRandom();
        item.setDeltaMovement((random.nextDouble() - 0.5) * 0.1, 0.25, (random.nextDouble() - 0.5) * 0.1);
        serverLevel.addFreshEntity(item);
        serverLevel.sendParticles(ParticleTypes.POOF, item.getX(), item.getY(), item.getZ(), 8, 0.1, 0.1, 0.1, 0.02);

        float progress = totalEjections <= 1
                ? 1.0F
                : 1.0F - Mth.inverseLerp(itemsToEject.size(), 1.0F, totalEjections);
        serverLevel.playSound(null, worldPosition, SoundEvents.DISPENSER_DISPENSE, SoundSource.BLOCKS, 1.0F, 0.8F + 0.4F * progress);
        setDisplayItem(getNextItemToEject());
        sync();
    }

    private void transitionTo(final DragonVaultState nextState) {
        if (level == null) {
            return;
        }

        BlockState currentState = getBlockState();
        DragonVaultState currentVaultState = currentState.getValue(DragonVaultBlock.STATE);
        if (currentVaultState == nextState) {
            return;
        }

        level.setBlock(worldPosition, currentState.setValue(DragonVaultBlock.STATE, nextState), 3);
        if (level instanceof ServerLevel serverLevel) {
            if (nextState == DragonVaultState.ACTIVE) {
                cycleDisplayItem(serverLevel);
                serverLevel.playSound(null, worldPosition, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1.0F, 1.15F);
            } else if (nextState == DragonVaultState.INACTIVE) {
                setDisplayItem(ItemStack.EMPTY);
                serverLevel.playSound(null, worldPosition, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.BLOCKS, 1.0F, 0.8F);
            } else if (nextState == DragonVaultState.UNLOCKING) {
                serverLevel.playSound(null, worldPosition, SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.8F);
            } else if (nextState == DragonVaultState.EJECTING) {
                serverLevel.playSound(null, worldPosition, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0F, 0.8F);
            }
        }
        sync();
    }

    private void updateConnectedPlayers(final ServerLevel serverLevel, final double range) {
        Set<UUID> updated = new LinkedHashSet<>();
        Vec3 center = Vec3.atCenterOf(worldPosition);
        AABB searchBounds = new AABB(worldPosition).inflate(range);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, searchBounds, player -> !player.isSpectator())) {
            if (!rewardedPlayers.contains(player.getUUID()) && player.position().distanceToSqr(center) <= range * range) {
                updated.add(player.getUUID());
            }
        }

        if (!connectedPlayers.equals(updated)) {
            connectedPlayers.clear();
            connectedPlayers.addAll(updated);
            sync();
        }
    }

    private void emitConnectionParticles(final Level level, final BlockState state) {
        Direction facing = state.getValue(DragonVaultBlock.FACING);
        Vec3 keyhole = Vec3.atBottomCenterOf(worldPosition).add(facing.getStepX() * 0.5, 0.75, facing.getStepZ() * 0.5);
        RandomSource random = level.getRandom();

        for (UUID uuid : connectedPlayers) {
            Player player = level.getPlayerByUUID(uuid);
            if (player == null || player.position().distanceToSqr(keyhole) > DEACTIVATION_RANGE * DEACTIVATION_RANGE) {
                continue;
            }

            Vec3 direction = keyhole.vectorTo(player.position().add(0, player.getBbHeight() / 2.0, 0)).normalize().scale(0.08);
            for (int i = 0; i < Mth.nextInt(random, 2, 5); i++) {
                level.addParticle(activeParticle(), keyhole.x, keyhole.y, keyhole.z,
                        direction.x + random.nextGaussian() * 0.01,
                        direction.y + random.nextGaussian() * 0.01,
                        direction.z + random.nextGaussian() * 0.01);
            }
        }
    }

    private ParticleOptions activeParticle() {
        if (!(getBlockState().getBlock() instanceof DragonVaultBlock block)) {
            return ParticleTypes.FLAME;
        }

        String path = block.lootTable().getPath();
        if (path.endsWith("light_vault")) {
            return ParticleTypes.END_ROD;
        }
        if (path.endsWith("dark_vault")) {
            return ParticleTypes.SOUL_FIRE_FLAME;
        }
        return ParticleTypes.CRIT;
    }

    private void playFailedInsertSound(final ServerLevel serverLevel, final long gameTime, final net.minecraft.sounds.SoundEvent sound) {
        if (gameTime >= lastFailedInsert + FAILED_INSERT_SOUND_DELAY) {
            serverLevel.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            lastFailedInsert = gameTime;
        }
    }

    private void addRewardedPlayer(final UUID uuid) {
        rewardedPlayers.add(uuid);
        if (rewardedPlayers.size() > MAX_REWARDED_PLAYERS) {
            Iterator<UUID> iterator = rewardedPlayers.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    private ItemStack getNextItemToEject() {
        return itemsToEject.isEmpty() ? ItemStack.EMPTY : itemsToEject.get(itemsToEject.size() - 1);
    }

    private void setDisplayItem(final ItemStack stack) {
        if (!ItemStack.matches(displayItem, stack)) {
            displayItem = stack.copy();
            setChanged();
            sync();
        }
    }

    private @Nullable DragonVaultBlock getVaultBlock() {
        return getBlockState().getBlock() instanceof DragonVaultBlock block ? block : null;
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 2);
        }
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public float getPreviousSpin() {
        return previousSpin;
    }

    public float getSpin() {
        return spin;
    }

    @Override
    protected void saveAdditional(@NotNull final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("next_state_update", nextStateUpdate);
        tag.putInt("total_ejections", totalEjections);

        ListTag rewarded = new ListTag();
        rewardedPlayers.forEach(uuid -> rewarded.add(NbtUtils.createUUID(uuid)));
        tag.put("rewarded_players", rewarded);

        ListTag pendingItems = new ListTag();
        itemsToEject.forEach(stack -> pendingItems.add(stack.save(new CompoundTag())));
        tag.put("items_to_eject", pendingItems);

        if (!displayItem.isEmpty()) {
            tag.put("display_item", displayItem.save(new CompoundTag()));
        }
    }

    @Override
    public void load(@NotNull final CompoundTag tag) {
        super.load(tag);
        nextStateUpdate = tag.getLong("next_state_update");
        totalEjections = tag.getInt("total_ejections");

        rewardedPlayers.clear();
        ListTag rewarded = tag.getList("rewarded_players", Tag.TAG_INT_ARRAY);
        for (Tag uuidTag : rewarded) {
            rewardedPlayers.add(NbtUtils.loadUUID(uuidTag));
        }

        itemsToEject.clear();
        ListTag pendingItems = tag.getList("items_to_eject", Tag.TAG_COMPOUND);
        for (Tag itemTag : pendingItems) {
            itemsToEject.add(ItemStack.of((CompoundTag) itemTag));
        }

        displayItem = tag.contains("display_item", Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound("display_item"))
                : ItemStack.EMPTY;
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        if (!displayItem.isEmpty()) {
            tag.put("display_item", displayItem.save(new CompoundTag()));
        }

        ListTag connected = new ListTag();
        connectedPlayers.forEach(uuid -> connected.add(NbtUtils.createUUID(uuid)));
        tag.put("connected_players", connected);
        return tag;
    }

    @Override
    public void handleUpdateTag(final CompoundTag tag) {
        displayItem = tag.contains("display_item", Tag.TAG_COMPOUND)
                ? ItemStack.of(tag.getCompound("display_item"))
                : ItemStack.EMPTY;

        connectedPlayers.clear();
        ListTag connected = tag.getList("connected_players", Tag.TAG_INT_ARRAY);
        for (Tag uuidTag : connected) {
            connectedPlayers.add(NbtUtils.loadUUID(uuidTag));
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
