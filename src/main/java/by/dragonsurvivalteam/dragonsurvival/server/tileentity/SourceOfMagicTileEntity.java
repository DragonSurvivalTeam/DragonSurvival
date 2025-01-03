package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTileEntities;
import by.dragonsurvivalteam.dragonsurvival.server.containers.SourceOfMagicContainer;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;

// TODO :: new data attachment that stores effects, input items and valid dragon items
//  said attachment is stored in the item stack data and transferred to the placed block entity
public class SourceOfMagicTileEntity extends BaseBlockTileEntity implements Container, MenuProvider, GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** List of valid items that can be placed in the container */
    public static HashMap<Item, Integer> consumables = new HashMap<>();
    public NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);
    private int ticks;

    static {
        consumables.put(DSItems.ELDER_DRAGON_DUST.value(), Functions.secondsToTicks(ServerConfig.elderDragonDustTime));
        consumables.put(DSItems.ELDER_DRAGON_BONE.value(), Functions.secondsToTicks(ServerConfig.elderDragonBoneTime));
        consumables.put(DSItems.DRAGON_HEART_SHARD.value(), Functions.secondsToTicks(ServerConfig.weakHeartShardTime));
        consumables.put(DSItems.WEAK_DRAGON_HEART.value(), Functions.secondsToTicks(ServerConfig.weakDragonHeartTime));
        consumables.put(DSItems.ELDER_DRAGON_HEART.value(), Functions.secondsToTicks(ServerConfig.elderDragonHeartTime));
    }

    public SourceOfMagicTileEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(DSTileEntities.SOURCE_OF_MAGIC_TILE_ENTITY.get(), pWorldPosition, pBlockState);
    }

    /** Handle the {@link SourceOfMagicBlock#FILLED} state, setting it to true if a {@link SourceOfMagicTileEntity#consumables} is present */
    public static void serverTick(Level level, BlockPos pPos, BlockState pState, SourceOfMagicTileEntity pBlockEntity) {
        if (!pState.getValue(SourceOfMagicBlock.FILLED) && !pBlockEntity.isEmpty()) {
            level.setBlockAndUpdate(pPos, pState.setValue(SourceOfMagicBlock.FILLED, true));
        } else if (pState.getValue(SourceOfMagicBlock.FILLED) && pBlockEntity.isEmpty()) {
            level.setBlockAndUpdate(pPos, pState.setValue(SourceOfMagicBlock.FILLED, false));
        }

        if (!pBlockEntity.isEmpty() && pBlockEntity.ticks % 120 == 0) {
            level.playLocalSound(pPos.getX(), pPos.getY(), pPos.getZ(), pState.getBlock() == DSBlocks.CAVE_SOURCE_OF_MAGIC.value() ? SoundEvents.LAVA_AMBIENT : SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, 0.5f, 1f, true);
        }

        pBlockEntity.ticks += 1;
    }

    @Override
    public boolean isEmpty() {
        return stacks.isEmpty() || getItem(0).isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int i) {
        return stacks.get(i);
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int amount) {
        return ContainerHelper.removeItem(stacks, index, amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(stacks, 0);
    }

    @Override
    public void setItem(int i, @NotNull ItemStack itemStack) {
        if (i >= 0 && i < stacks.size()) {
            stacks.set(i, itemStack);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player playerEntity) {
        return true;
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag pTag, @NotNull HolderLookup.Provider pRegistries) {
        stacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(pTag, stacks, pRegistries);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag pTag, HolderLookup.@NotNull Provider pRegistries) {
        ContainerHelper.saveAllItems(pTag, stacks, pRegistries);
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.empty().append("Source Of Magic");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull Inventory inventory, @NotNull Player player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(worldPosition);
        return new SourceOfMagicContainer(containerId, inventory, buffer);
    }

    @Override
    public void clearContent() {
        stacks.clear();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}