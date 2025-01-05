package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.SourceOfMagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.server.containers.SourceOfMagicContainer;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.stream.Collectors;

public class SourceOfMagicBlockEntity extends BaseBlockBlockEntity implements Container, MenuProvider, GeoBlockEntity {
    @Translation(comments = "Source of Magic")
    private static final String DISPLAY_NAME = Translation.Type.GUI.wrap("container.source_of_magic");

    private static final String SOURCE_OF_MAGIC_DATA = "source_of_magic_data";

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    /** List of valid items that can be placed in the container */
    private @Nullable Map<Item, Integer> consumables;
    private @Nullable Set<ResourceKey<DragonSpecies>> applicableSpecies;

    private NonNullList<ItemStack> inputItem = NonNullList.withSize(1, ItemStack.EMPTY);
    private int ticks;

    public SourceOfMagicBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.SOURCE_OF_MAGIC_TILE_ENTITY.get(), position, state);
    }

    public int getCurrentDuration() {
        return getDuration(getItem(0).getItem());
    }

    public int getDuration(final Item item) {
        if (consumables == null) {
            return 0;
        }

        return consumables.getOrDefault(item, 0);
    }

    public void setConsumables(final List<SourceOfMagicData.Consumable> consumables) {
        Map<Item, Integer> map = new HashMap<>();
        consumables.forEach(consumable -> map.put(consumable.item(), consumable.duration()));
        this.consumables = map;
    }

    public List<SourceOfMagicData.Consumable> getConsumables() {
        if (consumables == null) {
            return List.of();
        }

        return consumables.entrySet().stream().map(entry -> new SourceOfMagicData.Consumable(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public int consumableAmount() {
        if (consumables == null) {
            return 0;
        }

        return consumables.size();
    }

    public boolean isApplicableFor(final DragonStateHandler handler) {
        if (!handler.isDragon()) {
            return false;
        }

        if (applicableSpecies == null || applicableSpecies.isEmpty()) {
            return true;
        }

        return applicableSpecies.contains(handler.speciesKey());
    }

    public void setApplicableSpecies(final List<ResourceKey<DragonSpecies>> applicableSpecies) {
        this.applicableSpecies = Set.copyOf(applicableSpecies);
    }

    /** Handle the {@link SourceOfMagicBlock#FILLED} state, setting it to true if a {@link SourceOfMagicBlockEntity#consumables} is present */
    public static void serverTick(final Level level, final BlockPos position, final BlockState state, final SourceOfMagicBlockEntity blockEntity) {
        if (!state.getValue(SourceOfMagicBlock.FILLED) && !blockEntity.isEmpty()) {
            level.setBlockAndUpdate(position, state.setValue(SourceOfMagicBlock.FILLED, true));
        } else if (state.getValue(SourceOfMagicBlock.FILLED) && blockEntity.isEmpty()) {
            level.setBlockAndUpdate(position, state.setValue(SourceOfMagicBlock.FILLED, false));
        }

        if (!blockEntity.isEmpty() && blockEntity.ticks % 120 == 0) {
            level.playLocalSound(position.getX(), position.getY(), position.getZ(), state.getBlock() == DSBlocks.CAVE_SOURCE_OF_MAGIC.value() ? SoundEvents.LAVA_AMBIENT : SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, 0.5f, 1f, true);
        }

        blockEntity.ticks += 1;
    }

    @Override
    public boolean isEmpty() {
        return inputItem.isEmpty() || getItem(0).isEmpty();
    }

    @Override
    public @NotNull ItemStack getItem(int slot) {
        return inputItem.get(slot);
    }

    @Override
    public @NotNull ItemStack removeItem(int index, int amount) {
        return ContainerHelper.removeItem(inputItem, index, amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inputItem, 0);
    }

    @Override
    public void setItem(int slot, @NotNull final ItemStack stack) {
        if (slot >= 0 && slot < inputItem.size()) {
            inputItem.set(slot, stack);
        }
    }

    @Override
    public boolean stillValid(@NotNull final Player player) {
        return true;
    }

    @Override
    public void loadAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        inputItem = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, inputItem, provider);

        if (tag.contains(SOURCE_OF_MAGIC_DATA)) {
            SourceOfMagicData.CODEC.decode(provider.createSerializationContext(NbtOps.INSTANCE), tag.getCompound(SOURCE_OF_MAGIC_DATA)).ifSuccess(data -> {
                SourceOfMagicData sourceOfMagicData = data.getFirst();
                setConsumables(sourceOfMagicData.consumables());
                setApplicableSpecies(sourceOfMagicData.applicableSpecies());
            });
        }
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        ContainerHelper.saveAllItems(tag, inputItem, provider);
        List<SourceOfMagicData.Consumable> consumables = getConsumables();
        List<ResourceKey<DragonSpecies>> applicableSpecies = new ArrayList<>();

        if (this.applicableSpecies != null) {
            applicableSpecies.addAll(this.applicableSpecies);
        }

        SourceOfMagicData sourceOfMagicData = new SourceOfMagicData(consumables, applicableSpecies);
        SourceOfMagicData.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), sourceOfMagicData).result().ifPresent(compound -> tag.put(SOURCE_OF_MAGIC_DATA, compound));
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(DISPLAY_NAME);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, @NotNull final Inventory inventory, @NotNull final Player player) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeBlockPos(worldPosition);
        return new SourceOfMagicContainer(containerId, inventory, buffer);
    }

    @Override
    public void clearContent() {
        inputItem.clear();
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) { /* Nothing to do */ }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}