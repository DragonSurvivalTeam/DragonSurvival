package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTileEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicPlaceholder;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicTileEntity;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.SpawningUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

public class SourceOfMagicBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, EntityBlock {
    @Translation(type = Translation.Type.MISC, comments = "You need a 3x3 area to place %s")
    private static final String OCCUPIED = Translation.Type.GUI.wrap("message.occupied");

    public static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.25, 1);
    public static final VoxelShape OUTLINE = Shapes.box(0, 0, 0, 1, 0.5, 1);
    public static final VoxelShape FULL_OUTLINE = Shapes.box(0, 0, 0, 1, 0.99, 1);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty PRIMARY_BLOCK = BooleanProperty.create("primary");
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    private static final BooleanProperty BACK_BLOCK = BooleanProperty.create("back");
    private static final BooleanProperty TOP_BLOCK = BooleanProperty.create("top");

    public SourceOfMagicBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(WATERLOGGED, false).setValue(PRIMARY_BLOCK, true).setValue(BACK_BLOCK, false).setValue(TOP_BLOCK, false).setValue(FILLED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return MapCodec.unit(this);
    }

    private static void breakBlock(Level world, BlockPos pos) {
        world.destroyBlock(pos, !(world.getBlockEntity(pos) instanceof SourceOfMagicPlaceholder));
        world.removeBlockEntity(pos);
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos position, @NotNull RandomSource random) {
        if (state.getBlock() == DSBlocks.CAVE_SOURCE_OF_MAGIC.get()) {
            if (level.getFluidState(position).is(FluidTags.WATER)) {
                double x = position.getX();
                double y = position.getY();
                double z = position.getZ();
                level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + 0.5D, y, z + 0.5D, 0.0D, 0.04D, 0.0D);
                level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + (double) random.nextFloat(), y + (double) random.nextFloat(), z + (double) random.nextFloat(), 0.0D, 0.04D, 0.0D);
            } else if (state.getValue(FILLED)) {
                double x = position.getX();
                double y = position.getY();
                double z = position.getZ();
                level.addAlwaysVisibleParticle(ParticleTypes.LAVA, x + (double) random.nextFloat(), y + (double) random.nextFloat(), z + (double) random.nextFloat(), 0.0D, 0.04D, 0.0D);
            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos clickedPosition = context.getClickedPos();
        Level level = context.getLevel();

        Player player = context.getPlayer();
        Direction direction = player != null ? player.getDirection() : Direction.getRandom(level.getRandom());

        AtomicBoolean isValid = new AtomicBoolean(true);

        // Need to check in the stream due to the usage of 'BlockPos$MutableBlockPos'
        BlockPos.betweenClosedStream(clickedPosition.getX() - 1, clickedPosition.getY(), clickedPosition.getZ() - 1, clickedPosition.getX() + 1, clickedPosition.getY(), clickedPosition.getZ() + 1).forEach(position -> {
            if (isValid.get() && !SpawningUtils.isAirOrFluid(position, level, context)) {
                if (player != null && level.isClientSide()) {
                    player.sendSystemMessage(Component.translatable(OCCUPIED, asItem().getDefaultInstance().getDisplayName()));
                }

                isValid.set(false);
            }
        });

        if (!isValid.get()) {
            return null;
        }

        // Check the backside which has a part which is two blocks high // TODO :: should this also have a message?
        if (/* behind of the clicked position + 1 height */ !SpawningUtils.isAirOrFluid(clickedPosition.relative(direction).above(), level, context)) {
            return null;
        }

        if (/* right corner behind of the clicked position + 1 height */ !SpawningUtils.isAirOrFluid(clickedPosition.relative(direction).above().relative(direction.getClockWise()), level, context)) {
            return null;
        }

        if (/* left corner behind of the clicked position + 1 height */ !SpawningUtils.isAirOrFluid(clickedPosition.relative(direction).above().relative(direction.getCounterClockWise()), level, context)) {
            return null;
        }

        BlockState state = super.getStateForPlacement(context);

        if (state != null) {
            state = state.setValue(FACING, direction.getOpposite());
        }

        return state;
    }

    @Override
    public void setPlacedBy(@NotNull Level level, @NotNull BlockPos position, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack stack) {
        super.setPlacedBy(level, position, state, placer, stack);

        if (placer != null) {
            Direction direction = placer.getDirection();
            setPlaceholder(level, state, position, position.relative(direction.getOpposite()));
            setPlaceholder(level, state.setValue(BACK_BLOCK, true), position, position.relative(direction));

            setPlaceholder(level, state, position, position.relative(direction.getClockWise()));
            setPlaceholder(level, state, position, position.relative(direction.getCounterClockWise()));

            setPlaceholder(level, state.setValue(BACK_BLOCK, true), position, position.relative(direction).relative(direction.getClockWise()));
            setPlaceholder(level, state.setValue(BACK_BLOCK, true), position, position.relative(direction).relative(direction.getCounterClockWise()));

            setPlaceholder(level, state, position, position.relative(direction.getOpposite()).relative(direction.getCounterClockWise()));
            setPlaceholder(level, state, position, position.relative(direction.getOpposite()).relative(direction.getClockWise()));

            setPlaceholder(level, state.setValue(TOP_BLOCK, true), position, position.above().relative(direction));
            setPlaceholder(level, state, position, position.above().relative(direction).relative(direction.getCounterClockWise()));
            setPlaceholder(level, state, position, position.above().relative(direction).relative(direction.getClockWise()));
        }
    }

    @Override
    protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, WATERLOGGED, PRIMARY_BLOCK, BACK_BLOCK, TOP_BLOCK, FILLED);
    }

    private static void setPlaceholder(Level world, BlockState state, BlockPos root, BlockPos newPos) {
        world.setBlockAndUpdate(newPos, state.setValue(PRIMARY_BLOCK, false));
        SourceOfMagicPlaceholder placeholder = (SourceOfMagicPlaceholder) world.getBlockEntity(newPos);
        placeholder.rootPos = root;
    }

    @Override
    public @NotNull BlockState updateShape(BlockState blockState, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos position, @NotNull BlockPos neighborPosition) {
        if (blockState.getValue(WATERLOGGED)) {
            level.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(blockState, direction, neighborState, level, position, neighborPosition);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos position, BlockState newState, boolean isMoving) {
        if (!(newState.getBlock() instanceof SourceOfMagicBlock)) {
            if (state.getValue(PRIMARY_BLOCK)) {
                if (level.getBlockEntity(position) instanceof Container container) {
                    Containers.dropContents(level, position, container);
                    level.updateNeighbourForOutputSignal(position, this);
                }

                super.onRemove(state, level, position, newState, isMoving);

                Direction direction = state.getValue(FACING).getOpposite();

                breakBlock(level, position);

                breakBlock(level, position.relative(direction.getOpposite()));
                breakBlock(level, position.relative(direction));

                breakBlock(level, position.relative(direction.getClockWise()));
                breakBlock(level, position.relative(direction.getCounterClockWise()));

                breakBlock(level, position.relative(direction).relative(direction.getClockWise()));
                breakBlock(level, position.relative(direction).relative(direction.getCounterClockWise()));

                breakBlock(level, position.relative(direction.getOpposite()).relative(direction.getCounterClockWise()));
                breakBlock(level, position.relative(direction.getOpposite()).relative(direction.getClockWise()));

                breakBlock(level, position.above().relative(direction));
                breakBlock(level, position.above().relative(direction).relative(direction.getCounterClockWise()));
                breakBlock(level, position.above().relative(direction).relative(direction.getClockWise()));
            } else {
                BlockEntity tile = level.getBlockEntity(position);
                if (tile instanceof SourceOfMagicPlaceholder placeholder) {
                    BlockPos rootPos = placeholder.rootPos;

                    if (level.getBlockEntity(rootPos) instanceof SourceOfMagicTileEntity) {
                        onRemove(level.getBlockState(rootPos), level, rootPos, Blocks.BUBBLE_COLUMN.defaultBlockState(), isMoving);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos position, @NotNull Player player, @NotNull BlockHitResult result) {
        BlockEntity blockEntity = level.getBlockEntity(position);
        BlockPos pos1 = position;

        if (blockEntity instanceof SourceOfMagicPlaceholder placeholder) {
            pos1 = placeholder.rootPos;
        }

        if (!player.isCrouching()) {
            if (player instanceof ServerPlayer serverPlayer) {
                BlockPos finalPos = pos1;
                BlockEntity blockEntity1 = getBlockEntity(level, pos1);
                serverPlayer.openMenu((MenuProvider) blockEntity1, packetBuffer -> packetBuffer.writeBlockPos(finalPos));
            }
        } else {
            // FIXME :: Magic source handling
            /*if (DragonStateProvider.isDragon(player) && player.getMainHandItem().isEmpty()) {
                if (player.getBlockStateOn().getBlock() == state.getBlock()) {
                    DragonStateHandler handler = DragonStateProvider.getData(player);

                    if (!handler.getMagicData().onMagicSource) {
                        SourceOfMagicTileEntity source = getBlockEntity(level, pos1);

                        if (source != null && !source.isEmpty()) {
                            if (!level.isClientSide()) {
                                handler.getMagicData().magicSourceTimer = 0;
                                handler.getMagicData().onMagicSource = true;
                                PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncMagicSourceStatus.Data(player.getId(), true, 0));
                            }
                        }
                    }
                }
            }*/
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean triggerEvent(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos position, int id, int param) {
        super.triggerEvent(state, level, position, id, param);
        BlockEntity blockentity = level.getBlockEntity(position);
        return blockentity != null && blockentity.triggerEvent(id, param);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return state.getValue(PRIMARY_BLOCK) ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState blockState) {
        return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
    }

    @Override
    @Nullable public MenuProvider getMenuProvider(@NotNull BlockState state, Level pLevel, @NotNull BlockPos position) {
        BlockEntity blockentity = pLevel.getBlockEntity(position);
        return blockentity instanceof MenuProvider ? (MenuProvider) blockentity : null;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos position, @NotNull CollisionContext context) {
        return state.getValue(TOP_BLOCK) || state.getValue(BACK_BLOCK) ? FULL_OUTLINE : OUTLINE;
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos position, @NotNull CollisionContext context) {
        return state.getValue(TOP_BLOCK) ? FULL_OUTLINE : SHAPE;
    }

    @Override
    public void randomTick(@NotNull BlockState state, ServerLevel world, BlockPos pos, @NotNull RandomSource random) {
        BlockPos above = pos.above();

        if (world.getFluidState(pos).is(FluidTags.WATER)) {
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
            world.sendParticles(ParticleTypes.LARGE_SMOKE, (double) above.getX() + 0.5D, (double) above.getY() + 0.25D, (double) above.getZ() + 0.5D, 8, 0.5D, 0.25D, 0.5D, 0.0D);
        }
    }

    @Override
    public void entityInside(@NotNull BlockState state, Level world, @NotNull BlockPos position, @NotNull Entity entity) {
        BlockEntity blockEntity = world.getBlockEntity(position);
        BlockPos sourcePosition = position;

        if (blockEntity instanceof SourceOfMagicPlaceholder placeholder) {
            sourcePosition = placeholder.rootPos;
        }

        SourceOfMagicTileEntity source = getBlockEntity(world, sourcePosition);

        if (source != null) {
            if (shouldHarmPlayer(state, entity)) {
                if (entity instanceof ItemEntity itemE) {
                    ItemStack stack = itemE.getItem();
                    ItemStack tileStack = source.getItem(0);
                    if (SourceOfMagicTileEntity.consumables.containsKey(stack.getItem())) {
                        if (source.isEmpty()) {
                            source.setItem(0, stack);
                            itemE.kill();
                        } else if (ItemStack.isSameItem(tileStack, stack) && tileStack.getCount() < tileStack.getMaxStackSize()) {
                            int left = tileStack.getMaxStackSize() - tileStack.getCount();
                            int toAdd = Math.min(stack.getCount(), left);
                            itemE.getItem().shrink(toAdd);
                            tileStack.setCount(tileStack.getCount() + toAdd);
                        }
                        return;
                    }
                }

                if (ServerConfig.damageWrongSourceOfMagic) {
                    entity.hurt(state.getBlock() == DSBlocks.CAVE_SOURCE_OF_MAGIC.get() ? entity.damageSources().hotFloor() : state.getBlock() == DSBlocks.SEA_SOURCE_OF_MAGIC.get() ? entity.damageSources().drown() : entity.damageSources().cactus(), 1F);
                }
            }
        }
        super.entityInside(state, world, position, entity);
    }

    public static boolean shouldHarmPlayer(BlockState state, Entity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        Block block = state.getBlock();

        // FIXME :: add dragon type tag which specifies which dragon type can interact with it
        //  no or empty tag = all types are valid

        if (block == DSBlocks.CAVE_SOURCE_OF_MAGIC.get() && !DragonUtils.isType(handler, DragonTypes.CAVE)) {
            return true;
        }

        if (block == DSBlocks.SEA_SOURCE_OF_MAGIC.get() && !DragonUtils.isType(handler, DragonTypes.SEA)) {
            return true;
        }

        return block == DSBlocks.FOREST_SOURCE_OF_MAGIC.get() && !DragonUtils.isType(handler, DragonTypes.FOREST);
    }

    public SourceOfMagicTileEntity getBlockEntity(Level world, BlockPos pos) {
        BlockEntity entity = world.getBlockEntity(pos);
        return entity instanceof SourceOfMagicTileEntity ? (SourceOfMagicTileEntity) entity : null;
    }

    @Override
    public boolean placeLiquid(@NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockState state, @NotNull final FluidState fluidState) {
        if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            if (!level.isClientSide()) {
                level.setBlock(position, state.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE), Block.UPDATE_ALL);
                level.scheduleTick(position, fluidState.getType(), fluidState.getType().getTickDelay(level));
            }

            return true;
        } else {
            return false;
        }
    }

    @Override // Entrypoint for bucket interaction
    public @NotNull ItemStack pickupBlock(@Nullable final Player player, @NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockState state) {
        BlockEntity entity = level.getBlockEntity(position);
        BlockPos rootPosition = null;

        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.setBlock(position, state.setValue(BlockStateProperties.WATERLOGGED, false), Block.UPDATE_ALL);
            return Items.WATER_BUCKET.getDefaultInstance();
        }

        if (entity instanceof SourceOfMagicPlaceholder placeholder) {
            rootPosition = placeholder.rootPos;
        } else if (entity instanceof SourceOfMagicTileEntity source) {
            return updateAndTakeLiquid(level, position, state, source);
        }

        if (rootPosition != null && level.getBlockEntity(rootPosition) instanceof SourceOfMagicTileEntity source) {
            return updateAndTakeLiquid(level, rootPosition, level.getBlockState(rootPosition), source);
        }

        return ItemStack.EMPTY;
    }

    /**
     * If the block entity is filled ({@link SourceOfMagicBlock#FILLED}) it will return the appropriate liquid <br>
     * If an item was present in the container (see {@link SourceOfMagicTileEntity#consumables} then said stack may be decremented <br> <br>
     * This happens here because in {@link SourceOfMagicTileEntity#serverTick(Level, BlockPos, BlockState, SourceOfMagicTileEntity)} it will fill back up if an item is present
     */
    private ItemStack updateAndTakeLiquid(final LevelAccessor level, final BlockPos position, final BlockState state, final SourceOfMagicTileEntity source) {
        if (!state.getValue(SourceOfMagicBlock.FILLED)) {
            return ItemStack.EMPTY;
        }

        level.setBlock(position, state.setValue(SourceOfMagicBlock.FILLED, false), Block.UPDATE_ALL);
        Item item = source.getItem(0).getItem();
        Block block = state.getBlock();

        boolean decrementStack = false;

        if (item == DSItems.ELDER_DRAGON_DUST.value()) {
            decrementStack = true;
        } else if (item == DSItems.ELDER_DRAGON_BONE.value()) {
            decrementStack = level.getRandom().nextInt(3) == 0;
        } else if (item == DSItems.DRAGON_HEART_SHARD.value()) {
            decrementStack = level.getRandom().nextInt(5) == 0;
        } else if (item == DSItems.WEAK_DRAGON_HEART.value()) {
            decrementStack = level.getRandom().nextInt(15) == 0;
        } else if (item == DSItems.ELDER_DRAGON_HEART.value()) {
            decrementStack = level.getRandom().nextInt(50) == 0;
        }

        if (decrementStack) {
            source.removeItem(0, 1);
        }

        // TODO :: add custom liquid (poison) for forest dragons? could possibly have various interactions
        if (block == DSBlocks.CAVE_SOURCE_OF_MAGIC.get()) {
            return Items.LAVA_BUCKET.getDefaultInstance();
        } else if (block == DSBlocks.SEA_SOURCE_OF_MAGIC.get() || block == DSBlocks.FOREST_SOURCE_OF_MAGIC.get()) {
            return Items.WATER_BUCKET.getDefaultInstance();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pPos, BlockState pState) {
        if (!pState.getValue(PRIMARY_BLOCK)) {
            return DSTileEntities.SOURCE_OF_MAGIC_PLACEHOLDER.get().create(pPos, pState);
        }

        return DSTileEntities.SOURCE_OF_MAGIC_TILE_ENTITY.get().create(pPos, pState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null : BaseEntityBlock.createTickerHelper(pBlockEntityType, DSTileEntities.SOURCE_OF_MAGIC_TILE_ENTITY.get(), SourceOfMagicTileEntity::serverTick);
    }
}